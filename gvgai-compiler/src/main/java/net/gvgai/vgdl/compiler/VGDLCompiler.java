package net.gvgai.vgdl.compiler;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.generated.vgdlBaseListener;
import net.gvgai.vgdl.compiler.generated.vgdlParser.GameContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.Game_classContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.InteractionContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.Interaction_setContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.Known_sprite_nameContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.Level_mappingContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.Level_mappingsContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.OptionContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.SpriteContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.TerminationContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.Termination_setContext;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.compiler.library.GameClass;
import net.gvgai.vgdl.compiler.library.Termination;
import net.gvgai.vgdl.game.VGDLSprite;

public class VGDLCompiler extends vgdlBaseListener implements Opcodes {
    private final static String PACKAGE = "net/gvgai/game";

    public static String formatClassName( String name ) {
        if (Character.isUpperCase( name.codePointAt( 0 ) )) {
            return name;
        }
        else {
            return name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
        }
    }

    public static void generateConsoleMessage( GeneratorAdapter mg, String string ) {
        mg.getStatic( Type.getType( System.class ), "out", Type.getType( PrintStream.class ) );
        mg.push( string );
        mg.invokeVirtual( Type.getType( PrintStream.class ), Method.getMethod( "void println (String)" ) );
    }

    /**
     *
     */

    private VGDLClassLoader classLoader;

    private final Stack<GeneratorAdapter> methods;

    private final SortedMap<Integer, String[]> levelMapping;

    final boolean writeClassesToDisk;

    private int local;

    private final Set<VGDLRuntime.Feature> requiredFeatures;

    private final Type gameType;

    private int zLevel;

    VGDLCompiler( String gameName ) {
        this( gameName, false );
    }

    /**
     * @param gameName name of the game class
     */
    VGDLCompiler( String gameName, boolean writeClassesToDisk ) {
        gameType = Type.getType( "L" + PACKAGE + "/" + formatClassName( gameName ) + ";" );
        this.writeClassesToDisk = writeClassesToDisk;

        levelMapping = new TreeMap<Integer, String[]>();
        methods = new Stack<GeneratorAdapter>();
        requiredFeatures = new HashSet<VGDLRuntime.Feature>();

    }

    @Override
    public void enterGame( GameContext ctx ) {
        zLevel = 0;
        classLoader = new VGDLClassLoader();
        levelMapping.clear();
        requiredFeatures.clear();
        methods.clear();

    }

    @Override
    public void enterGame_class( Game_classContext ctx ) {
        super.enterGame_class( ctx );

        final String simpleName = formatClassName( ctx.name.getText() );
        final Type parent = getTypeForSimpleName( simpleName );

        final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES );
        cw.visit( V1_8, ACC_PUBLIC, gameType.getInternalName(), null, parent.getInternalName(), null );

//        try {
//            for (final OptionContext o : ctx.option()) {
//                final Field f = parentClass.getDeclaredField( o.option_key().getText() );
//
//                final int access = (f.getModifiers() & Member.PUBLIC) > 1 ? Opcodes.ACC_PUBLIC : ACC_PROTECTED;
//                if (f.getType() == int.class) {
//                    final int i = Integer.parseInt( o.option_value().getText() );
//                    cw.visitField( access, f.getName(), Type.getDescriptor( f.getType() ), null, i );
//                }
//                else if (f.getType() == String.class) {
//                    cw.visitField( access, f.getName(), Type.getDescriptor( f.getType() ), null, o.option_value().getText() );
//                }
//                else {
//                    throw new IllegalStateException( "unhandled field type" );
//                }
//
//            }
//        }
//        catch (final NoSuchFieldException | SecurityException e) {
//            throw new IllegalStateException( e );
//        }

        // creates a GeneratorAdapter for the (implicit) constructor
        final Method m = Method.getMethod( "void <init> ()" );
        final GeneratorAdapter currentMehod = new GeneratorAdapter( ACC_PUBLIC, m, null, null, cw );
        methods.push( currentMehod );
        currentMehod.loadThis();
        currentMehod.invokeConstructor( parent, m );
        for (final OptionContext o : ctx.option()) {
            GameClass.handleGameClassOption( requiredFeatures, currentMehod, parent, o );
        }

        currentMehod.returnValue();
        currentMehod.endMethod();
        methods.pop();

        classLoader.getGeneratedTypes().put( gameType, new GeneratedType( gameType, cw ) );
    }

    @Override
    public void enterInteraction( InteractionContext ctx ) {
        final List<Type> actors = ctx.known_sprite_name().stream().map( ktx -> getTypeForSimpleName( formatClassName( ktx.Identifier().getText() ) ) )
                        .collect( Collectors.toList() );
        final Type actorType = actors.get( 0 );
        final GeneratedType g = classLoader.getGeneratedTypes().get( actorType );
        final ClassWriter cw = g.cw;
        assert cw != null;
        assert actors.size() >= 2;

        for (int i = 1; i < actors.size(); i++) {
            final Type otherType = actors.get( i );
            System.out.println( "Setting interaction " + actorType + " -> " + otherType );

            g.definedInteractions.add( otherType );

            final Method m = Method.getMethod( "void collide (" + otherType.getClassName() + ")" );
            final GeneratorAdapter mg = new GeneratorAdapter( ACC_PROTECTED, m, null, null, cw );
            generateConsoleMessage( mg, "Collision " + actorType.getClassName() + " and " + otherType.getClassName() );
            final Effect e = getEffectForName( formatClassName( ctx.action.getText() ), actorType, actors.get( i ), ctx.option() );
            e.generate( this, mg );
            mg.returnValue();
            mg.endMethod();
        }

    }

    @Override
    public void enterLevel_mapping( Level_mappingContext ctx ) {

        final String[] classes = new String[ctx.known_sprite_name().size()];
        int i = 0;
        for (final Known_sprite_nameContext c : ctx.known_sprite_name()) {
            classes[i] = getTypeForSimpleName( formatClassName( c.Identifier().getText() ) ).getClassName();
            i++;
        }

        if (levelMapping.containsKey( Integer.valueOf( ctx.symbol.getText().charAt( 0 ) ) )) {
            throw new IllegalStateException( "level mapping " + ctx.symbol.getText().charAt( 0 ) + " already defined" );
        }
        levelMapping.put( Integer.valueOf( ctx.symbol.getText().charAt( 0 ) ), classes );
    }

    @Override
    public void enterLevel_mappings( Level_mappingsContext ctx ) {
        final GeneratorAdapter currentMehod = new GeneratorAdapter( ACC_PUBLIC, Method.getMethod( "Class[] getMappedSpriteClass (char)" ), null, null,
                        classLoader.getGeneratedTypes().get( gameType ).cw );
        methods.push( currentMehod );
        levelMapping.clear();
    }

    @Override
    public void enterSprite( SpriteContext ctx ) {
        final ClassWriter spcw = new ClassWriter( ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES );

        final String spriteTypeName = formatClassName( ctx.name.getText() );
        final Type spriteType = Type.getType( "L" + PACKAGE + "/" + spriteTypeName + ";" );
        final Type parentType = getTypeForSimpleName( formatClassName( ctx.parentClass ) );
        System.out.println( "Parent of " + spriteType + " is " + parentType );

        spcw.visit( V1_8, ACC_PUBLIC, spriteType.getInternalName(), null, parentType.getInternalName(), null );

        // creates a GeneratorAdapter for the (implicit) constructor
        final Method m = Method.getMethod( "void <init> ()" );
        final GeneratorAdapter mg = new GeneratorAdapter( ACC_PUBLIC, m, null, null, spcw );
        mg.loadThis();

        mg.visitMethodInsn( INVOKESPECIAL, parentType.getInternalName(), "<init>", "()V", false );

        //Handle options
        //For now: filter out graphics stuff and try to query the rest via reflection
        String img = "";

        for (final OptionContext o : ctx.option()) {
//            try {
            switch (o.option_key().getText().toLowerCase()) {
                case "img":
                case "color":
                    img += o.option_key().getText() + "=" + o.option_value().getText() + " ";

                    break;
                default:
//                        final Field f = parentClass.getDeclaredField( o.option_key().getText() );
//                        mg.loadThis();
//                        if (f.getType() == String.class) {
//                            mg.push( o.option_value().getText() );
//                        }
//                        else {
//                            throw new IllegalStateException( "unhandled field type" );
//                        }
                    System.err.println( "Warning: ignoring unknown option \"" + o.option_key().getText() + "=" + o.option_value().getText()
                                    + "\" on sprite class " + ctx.name.getText() );
                    break;

            }
//            }
//            catch (NoSuchFieldError | NoSuchFieldException | SecurityException e) {
//                System.err.println( "Warning: ignoring unknown option \"" + o.option_key().getText() + "=" + o.option_value().getText() + "\" on sprite class "
//                                + ctx.name.getText() );
//            }
        }
        img += "zLevel=" + zLevel++;
        //Now add the sprite class meta information
        final AnnotationVisitor an = spcw.visitAnnotation( Type.getDescriptor( SpriteInfo.class ), true );
        //We combine the graphics options into a string that may be read out by the runtime.
        //the sprite class logic should be oblivious to this
        an.visit( "resourceInfo", img );

        mg.returnValue();
        mg.endMethod();

        spcw.visitEnd();
        final GeneratedType g = new GeneratedType( spriteType, spcw );
        g.parentType = parentType;
        classLoader.getGeneratedTypes().put( spriteType, g );

    }

    @Override
    public void enterTermination( TerminationContext ctx ) {
        final GeneratorAdapter currentMethod = methods.peek();
        currentMethod.loadLocal( local );

        try {
            final Class<? extends Termination> clazz = (Class<? extends Termination>) Class
                            .forName( getTypeForSimpleName( formatClassName( ctx.termination_class.getText() ) ).getClassName() );
            final Termination t = clazz.newInstance();
            for (final OptionContext o : ctx.option()) {
                final Field f = clazz.getDeclaredField( o.option_key().getText() );
                f.setAccessible( true );
                if (f.getType() == int.class) {
                    final int i = Integer.parseInt( o.option_value().getText() );
                    f.set( t, i );
                }
                else if (f.getType() == String.class) {
                    f.set( t, o.option_value().getText() );
                }
                else {
                    throw new IllegalStateException( "unhandled field type" );
                }
            }
            t.generate( this, gameType, requiredFeatures, classLoader.getGeneratedTypes().get( gameType ).cw, currentMethod );
        }
        catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | ClassNotFoundException e) {
            throw new RuntimeException( e );
        }

        currentMethod.visitInsn( IOR );
    }

    @Override
    public void enterTermination_set( Termination_setContext ctx ) {
        final GeneratorAdapter currentMehod = new GeneratorAdapter( ACC_PUBLIC, Method.getMethod( "boolean isGameOver()" ), null, null,
                        classLoader.getGeneratedTypes().get( gameType ).cw );
        methods.push( currentMehod );
        local = currentMehod.newLocal( Type.BOOLEAN_TYPE );
        currentMehod.push( false );
        currentMehod.storeLocal( local, Type.BOOLEAN_TYPE );
    }

    @Override
    public void exitGame( GameContext ctx ) {
        final ClassWriter cw = classLoader.getGeneratedTypes().get( gameType ).cw;
        cw.visitField( ACC_STATIC | ACC_FINAL | ACC_PRIVATE, "REQUIRED_FEATURES", "[Lnet/gvgai/vgdl/VGDLRuntime$Feature;", null, null );
        final Type at = Type.getType( Array.class );
        final Method staticinit = Method.getMethod( "void <clinit>()" );
        final GeneratorAdapter staticblock = new GeneratorAdapter( ACC_STATIC, staticinit, null, null, cw );
        staticblock.push( requiredFeatures.size() );
        final int array = staticblock.newLocal( at );
        staticblock.newArray( Type.getType( Feature.class ) );
        staticblock.storeLocal( array );
        int i = 0;
        for (final Feature f : requiredFeatures) {
            staticblock.loadLocal( array );
            staticblock.push( i );
            staticblock.getStatic( Type.getType( Feature.class ), f.name(), Type.getType( Feature.class ) );
            staticblock.arrayStore( at );
            i++;
        }

        staticblock.loadLocal( array );
        staticblock.visitFieldInsn( PUTSTATIC, gameType.getInternalName(), "REQUIRED_FEATURES", "[Lnet/gvgai/vgdl/VGDLRuntime$Feature;" );

        staticblock.returnValue();
        staticblock.endMethod();

        final Method getter = Method.getMethod( "net.gvgai.vgdl.VGDLRuntime$Feature[] getRequiredFeatures ()" );
        final GeneratorAdapter getterA = new GeneratorAdapter( ACC_PUBLIC, getter, null, null, cw );
        getterA.visitFieldInsn( GETSTATIC, gameType.getInternalName(), "REQUIRED_FEATURES", "[Lnet/gvgai/vgdl/VGDLRuntime$Feature;" );
        getterA.returnValue();
        getterA.endMethod();

        cw.visitEnd();

    }

    @Override
    public void exitInteraction_set( Interaction_setContext ctx ) {
        //now implement the abstract lookup method
        final Method overLoadMethod = Method.getMethod( "void collide (" + VGDLSprite.class.getName() + ")" );
        for (final Map.Entry<Type, GeneratedType> e : classLoader.getGeneratedTypes().entrySet()) {
            if (e.getKey().equals( gameType )) {
                continue;
            }
            final GeneratedType g = e.getValue();
            final ClassWriter cw = g.cw;
            final Type actorType = e.getKey();
            final Type parentType = e.getValue().parentType;
            final GeneratorAdapter m = new GeneratorAdapter( ACC_PUBLIC, overLoadMethod, null, null, cw );
            generateConsoleMessage( m, "Collide in class " + actorType.getClassName() + " has been called" );

            final Set<Type> interactions = g.definedInteractions;
            if (interactions.isEmpty()) {
                System.out.println( "No interactions for " + actorType.getClassName() + " Delegating to " + parentType.getClassName() );
                m.loadThis();
                m.loadArg( 0 );
                m.visitMethodInsn( INVOKESPECIAL, parentType.getInternalName(), overLoadMethod.getName(), overLoadMethod.getDescriptor(), false );
                m.returnValue();
                m.endMethod();
                continue;
            }

            //The following block constructs a large if-instanceof-else block. The final else is a delegation to super
            for (final Type otherType : interactions) {
                final Label falseLabel = m.newLabel();
                m.loadArg( 0 );
                m.instanceOf( otherType );
                m.push( false );
                m.ifCmp( Type.BOOLEAN_TYPE, IFEQ, falseLabel ); //Skip the method call if sprite is not instance of of whatever
                m.loadThis();
                m.loadArg( 0 );
                final Method typedCollide = Method.getMethod( "void collide (" + otherType.getClassName() + ")" );
                m.visitTypeInsn( CHECKCAST, otherType.getInternalName() );
                m.invokeVirtual( actorType, typedCollide );
                m.returnValue();
                m.mark( falseLabel );
            }
            generateConsoleMessage( m, "No interactions defined for " + actorType.getClassName() + ". Delegating to super" );
            m.loadThis();
            m.loadArg( 0 );
            m.visitMethodInsn( INVOKESPECIAL, parentType.getInternalName(), overLoadMethod.getName(), overLoadMethod.getDescriptor(), false );
            m.returnValue();
            m.endMethod();

        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void exitLevel_mappings( Level_mappingsContext ctx ) {
        /*
         * This will produce code like:
         * Class[] getMappedSpriteClass (char)
         *  switch(char)
         *      <for each level mapping i>
         *      case i:
         *          return new Class[]{ <sprite classes for i};
         *      default:
         *          throw new IllegalStateException(..);
         *
         */
        final GeneratorAdapter currentMehod = methods.pop();
        currentMehod.loadArg( 0 );
        final int[] keys = new int[levelMapping.size()];
        final Object[] temp = levelMapping.entrySet().toArray();
        for (int i = 0; i < temp.length; i++) {
            keys[i] = ((Map.Entry<Integer, String[]>) temp[i]).getKey();
        }
        final TableSwitchGenerator generator = new TableSwitchGenerator() {

            @Override
            public void generateCase( int key, Label end ) {
                final Type at = Type.getType( Array.class );
                @SuppressWarnings( "rawtypes" )
                final String[] clazzes = levelMapping.get( key );
                final int length = clazzes.length;

                currentMehod.push( length );
                currentMehod.newArray( Type.getType( Class.class ) );
                final int array = currentMehod.newLocal( at );
                currentMehod.storeLocal( array, at );

                for (int i = 0; i < length; i++) {
                    currentMehod.loadLocal( array );
                    currentMehod.push( i );

                    //Thread.currentThread().getContextClassLoader()
                    //Don't call the ClassLoader.getSystemClassLoader, this would require the code to be written to the CLASSPATH and the app restarted
                    //If the generated class is invoked outside the compiler application, this will eqaul to the system classloader anyway
                    currentMehod.invokeStatic( Type.getType( Thread.class ), Method.getMethod( "Thread currentThread()" ) );
                    currentMehod.invokeVirtual( Type.getType( Thread.class ), Method.getMethod( "ClassLoader getContextClassLoader()" ) );

                    currentMehod.push( clazzes[i] );
                    currentMehod.invokeVirtual( Type.getType( ClassLoader.class ), Method.getMethod( "Class loadClass(String)" ) );

                    currentMehod.arrayStore( at );
                }
                ;
                currentMehod.loadLocal( array );
                currentMehod.returnValue();

            }

            @Override
            public void generateDefault() {
                // Initializing the exception object with a string message
                final Type ext = Type.getType( IllegalStateException.class );
                final Type sbt = Type.getType( StringBuilder.class );
                currentMehod.newInstance( ext );
                final int ex = currentMehod.newLocal( ext );
                currentMehod.storeLocal( ex, ext );
                currentMehod.loadLocal( ex );

                final Method sbCon = Method.getMethod( "void <init> (String)" );
                currentMehod.newInstance( sbt );
                final int sb = currentMehod.newLocal( sbt );
                currentMehod.storeLocal( sb, sbt );
                currentMehod.loadLocal( sb );
                currentMehod.push( "No such character mapping " );
                currentMehod.invokeConstructor( sbt, sbCon );
                currentMehod.loadLocal( sb );
                currentMehod.loadArg( 0 );
                final Method append = Method.getMethod( "StringBuilder append(char)" );
                currentMehod.invokeVirtual( sbt, append );
                final Method toString = Method.getMethod( "String toString()" );
                currentMehod.invokeVirtual( sbt, toString );

                final Method excCons = Method.getMethod( "void <init> (String)" );
                currentMehod.invokeConstructor( ext, excCons );
                currentMehod.loadLocal( ex );
                currentMehod.throwException();
            }
        };
        currentMehod.tableSwitch( keys, generator, true );
        currentMehod.endMethod();
    }

    @Override
    public void exitTermination_set( Termination_setContext ctx ) {
        final GeneratorAdapter currentMethod = methods.pop();
        currentMethod.returnValue();
        currentMethod.endMethod();
    }

    public Set<Class> getClasses() {
        final Set<Class> clazzes = new HashSet<Class>();
        for (final Entry<Type, GeneratedType> e : classLoader.getGeneratedTypes().entrySet()) {
            try {
                clazzes.add( classLoader.findClass( e.getKey().getClassName() ) );
            }
            catch (final ClassNotFoundException e1) {
                throw new RuntimeException( e1 );
            }
        }
        return clazzes;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Type getTypeForSimpleName( String name ) {
//        if (name.toLowerCase().equals( "wall" )) {
//            return Type.getType( Wall.class );
//        }

        final Type ret = classLoader.getGeneratedTypes().keySet().stream().filter( t -> t.getClassName().endsWith( name ) ).findAny().orElse( null );
        if (ret != null) {
            return ret;
        }

        try {
            final Set<Class> fqns = classLoader.getFQNs( name );

            if (fqns.size() != 1) {
                throw new IllegalStateException( "class \"" + name + "\" is " + (fqns.isEmpty() ? "unknown" : "ambiguous") );
            }

            return Type.getType( fqns.iterator().next() );
        }
        catch (final IOException e) {
            throw new RuntimeException( e );
        }

    }

    private Effect getEffectForName( String text, Type actorType, Type otherType, List<OptionContext> list ) {
        try {
            final Type t = getTypeForSimpleName( text );
            final Class<? extends Effect> effectClass = (Class<? extends Effect>) Class.forName( t.getClassName() );

            final Constructor<? extends Effect> c = effectClass.getConstructor( Type.class, Type.class );
            //TODO options
            return c.newInstance( actorType, otherType );
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException |

        InvocationTargetException e)

        {
            throw new RuntimeException( e );
        }

    }

}