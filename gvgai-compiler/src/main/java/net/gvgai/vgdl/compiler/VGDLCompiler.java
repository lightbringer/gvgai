package net.gvgai.vgdl.compiler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

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
import net.gvgai.vgdl.compiler.library.GameClass;
import net.gvgai.vgdl.compiler.library.Termination;
import net.gvgai.vgdl.game.VGDLSprite;
import net.gvgai.vgdl.game.Wall;

public class VGDLCompiler extends vgdlBaseListener implements Opcodes {

    private class VGDLClassLoader extends ClassLoader {
        Class<?> defineClass( String name, ClassWriter cw ) {
            final byte[] code = cw.toByteArray();
            if (writeClassesToDisk) {
                try {
                    final FileOutputStream out = new FileOutputStream( name + ".class" );
                    out.write( code );
                    out.close();
                }
                catch (final Exception e) {

                }
            }
            return defineClass( name, code, 0, code.length );

        }

        Set<Class> getFQNs( String simpleName ) throws IOException {
            final ImmutableSet<ClassInfo> allClasses = ClassPath.from( this ).getAllClasses();
            final Set<Class> ret = new HashSet<Class>();
            for (final ClassInfo c : allClasses) {
                if (c.getSimpleName().equals( simpleName )) {
                    ret.add( c.load() );
                }
            }

            try {
                ret.add( Class.forName( simpleName, false, this ) );
            }
            catch (final ClassNotFoundException e) {
                //Ignore
            }

            return ret;
        }

    }

    private final static String PACKAGE = "net/gvgai/game";

    private final Map<Type, Set<Type>> definedInteractions;

    private final Map<Type, Integer> classIds;
    /**
     *
     */

    private VGDLClassLoader classLoader;

//    private final String gameName;

    private final Map<Type, ClassWriter> classes;
    private final Stack<GeneratorAdapter> methods;

    private final SortedMap<Integer, String[]> levelMapping;

    private final boolean writeClassesToDisk;

    private int local;

    private final Set<VGDLRuntime.Feature> requiredFeatures;

    private int nextClassId;

    private final Type gameType;

    VGDLCompiler( String gameName ) {
        this( gameName, false );
    }

    /**
     * @param gameName name of the game class
     */
    VGDLCompiler( String gameName, boolean writeClassesToDisk ) {
//        this.gameName = gameName;
        gameType = Type.getType( "L" + PACKAGE + "/" + gameName + ";" );
        System.out.println( gameType.getInternalName() );
        this.writeClassesToDisk = writeClassesToDisk;
        classes = new HashMap<Type, ClassWriter>();

        levelMapping = new TreeMap<Integer, String[]>();
        methods = new Stack<GeneratorAdapter>();
        requiredFeatures = new HashSet<VGDLRuntime.Feature>();
        definedInteractions = new HashMap<Type, Set<Type>>();
        classIds = new HashMap<Type, Integer>();
    }

    @Override
    public void enterGame( GameContext ctx ) {
        classes.clear();
        classLoader = new VGDLClassLoader();
        levelMapping.clear();
        requiredFeatures.clear();
        methods.clear();
        definedInteractions.clear();
        classIds.clear();
        nextClassId = 0;
    }

    @Override
    public void enterGame_class( Game_classContext ctx ) {
        super.enterGame_class( ctx );

        final String simpleName = ctx.name.getText();

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

        classes.put( gameType, cw );
    }

    @Override
    public void enterInteraction( InteractionContext ctx ) {
        final List<String> actors = ctx.known_sprite_name().stream().map( ktx -> ktx.Identifier().getText() ).collect( Collectors.toList() );
        final ClassWriter cw = classes.get( getTypeForSimpleName( actors.get( 0 ) ) );
        assert cw != null;
        assert actors.size() >= 2;

        System.out.println( "Setting interaction " + actors );
        final Type actorType = getTypeForSimpleName( actors.get( 0 ) );
        final Type otherType = getTypeForSimpleName( actors.get( 1 ) );
        definedInteractions.get( actorType ).add( otherType );
        final Type t = getTypeForSimpleName( actors.get( 1 ) );

        final Method m = Method.getMethod( "void collide (" + t.getClassName() + ")" );
        final GeneratorAdapter mg = new GeneratorAdapter( ACC_PROTECTED, m, null, null, cw );
        mg.getStatic( Type.getType( System.class ), "out", Type.getType( PrintStream.class ) );
        mg.push( "Collision " + actors.get( 0 ) + " and " + actors.get( 1 ) );
        mg.invokeVirtual( Type.getType( PrintStream.class ), Method.getMethod( "void println (String)" ) );
        mg.returnValue();
        mg.endMethod();

    }

    @Override
    public void enterInteraction_set( Interaction_setContext ctx ) {
        for (final Type n : classes.keySet()) {
            if (!n.equals( gameType )) {
                definedInteractions.put( n, new HashSet<Type>() );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void enterLevel_mapping( Level_mappingContext ctx ) {

        final GeneratorAdapter currentMehod = methods.peek();

        final String[] classes = new String[ctx.known_sprite_name().size()];
        int i = 0;
        for (final Known_sprite_nameContext c : ctx.known_sprite_name()) {
            classes[i] = getTypeForSimpleName( c.Identifier().getText() ).getClassName();
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
                        classes.get( gameType ) );
        methods.push( currentMehod );
        levelMapping.clear();
    }

    @Override
    public void enterSprite( SpriteContext ctx ) {

//        final Class parentClass = getClassForSimpleName( ctx.parentClass );
        final ClassWriter spcw = new ClassWriter( ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES );
        assert !classes.containsKey( ctx.name.getText() );
        System.out.println( ctx.name.getText() );

        //If it's a known class, parentClass will be != null, if it's one of the classes we're currently generating, it's null. Then use ctx.parentClass
        //if it's neither, the next call will throw an exception
        final Type spriteType = Type.getType( "L" + PACKAGE + "/" + ctx.name.getText() + ";" );
        final Type parentType = getTypeForSimpleName( ctx.parentClass );
        System.out.println( "Parent of " + ctx.name.getText() + " is " + parentType );
        spcw.visit( V1_8, ACC_PUBLIC, spriteType.getInternalName(), null, parentType.getInternalName(), null );

        // creates a GeneratorAdapter for the (implicit) constructor
        final Method m = Method.getMethod( "void <init> ()" );
        final GeneratorAdapter mg = new GeneratorAdapter( ACC_PUBLIC, m, null, null, spcw );
        mg.loadThis();

        mg.visitMethodInsn( INVOKESPECIAL, parentType.getInternalName(), "<init>", "()V", false );
//        mg.invokeConstructor( Type.getType( parentClass ), m );

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

        //The class id is needed at runtime
        final GeneratorAdapter idGetter = new GeneratorAdapter( ACC_PUBLIC, Method.getMethod( "int getClassId( )" ), null, null, spcw );
        final int id = nextClassId++;
        if (classIds.containsKey( id )) {
            throw new IllegalStateException( "FIXME duplicate classid" );
        }
        classIds.put( spriteType, id );
        idGetter.push( id );
        idGetter.returnValue();
        idGetter.endMethod();

        //Now add the sprite class meta information
        final AnnotationVisitor an = spcw.visitAnnotation( Type.getDescriptor( SpriteInfo.class ), true );
        //We combine the graphics options into a string that may be read out by the runtime.
        //the sprite class logic should be oblivious to this
        an.visit( "resourceInfo", img );

        mg.returnValue();
        mg.endMethod();

        spcw.visitEnd();
        classes.put( spriteType, spcw );

    }

    @Override
    public void enterTermination( TerminationContext ctx ) {
        final GeneratorAdapter currentMethod = methods.peek();
        currentMethod.loadLocal( local );

        try {
            final Class<? extends Termination> clazz = (Class<? extends Termination>) Class
                            .forName( getTypeForSimpleName( ctx.termination_class.getText() ).getClassName() );
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
            t.generate( this, gameType, requiredFeatures, classes.get( gameType ), currentMethod );
        }
        catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | ClassNotFoundException e) {
            throw new RuntimeException( e );
        }

        currentMethod.visitInsn( IOR );
    }

    @Override
    public void enterTermination_set( Termination_setContext ctx ) {
        final GeneratorAdapter currentMehod = new GeneratorAdapter( ACC_PUBLIC, Method.getMethod( "boolean isGameOver()" ), null, null,
                        classes.get( gameType ) );
        methods.push( currentMehod );
        local = currentMehod.newLocal( Type.BOOLEAN_TYPE );
        currentMehod.push( false );
        currentMehod.storeLocal( local, Type.BOOLEAN_TYPE );
    }

    @Override
    public void exitGame( GameContext ctx ) {
        final ClassWriter cw = classes.get( gameType );
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
        for (final Map.Entry<Type, ClassWriter> e : classes.entrySet()) {
            if (e.getKey().equals( gameType )) {
                continue;
            }
            final ClassWriter cw = e.getValue();
            final Type actorType = e.getKey();
            final GeneratorAdapter m = new GeneratorAdapter( ACC_PUBLIC, overLoadMethod, null, null, cw );

            final Set<Type> interactions = definedInteractions.get( actorType );
            if (interactions.isEmpty()) {
                //TODO call super
                m.returnValue();
                continue;
            }

            m.loadArg( 0 );
            m.invokeVirtual( Type.getType( VGDLSprite.class ), Method.getMethod( "int getClassId( )" ) );
            final List<Integer> keysList = new LinkedList<Integer>();
            for (final Type otherType : interactions) {
                if (otherType.equals( Type.getType( Wall.class ) )) {
                    keysList.add( Wall.VGDL_WALL_ID );
                }
                else {
                    keysList.add( classIds.get( otherType ) );
                }
            }
            Collections.sort( keysList );
            final int[] keys = new int[keysList.size()];
            //Keys have to be ascending, we link them back to the classes in the switch generator
            for (int i = 0; i < keys.length; i++) {
                keys[i] = keysList.get( i );
            }

            final TableSwitchGenerator generator = new TableSwitchGenerator() {

                @Override
                public void generateCase( int key, Label end ) {
                    m.loadThis();
                    m.loadArg( 0 );
                    final Type otherType = key != Wall.VGDL_WALL_ID
                                    ? classIds.entrySet().stream().filter( e -> e.getValue().equals( key ) ).findAny().get().getKey()
                                    : Type.getType( Wall.class );
                    final Method typedCollide = Method.getMethod( "void collide (" + otherType.getClassName() + ")" );
                    m.visitTypeInsn( CHECKCAST, otherType.getInternalName() );
                    m.invokeVirtual( actorType, typedCollide );
                    m.returnValue();
                }

                @Override
                public void generateDefault() {
                    //TODO call super method
                    m.returnValue();
                }
            };
            m.tableSwitch( keys, generator, true );
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
        for (final Entry<Type, ClassWriter> e : classes.entrySet()) {
            clazzes.add( classLoader.defineClass( e.getKey().getClassName(), e.getValue() ) );
        }
        return clazzes;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Type getTypeForSimpleName( String name ) {
        if (name.equals( "wall" )) {
            return Type.getType( Wall.class );
        }

        final Type ret = classes.keySet().stream().filter( t -> t.getClassName().endsWith( name ) ).findAny().orElse( null );
        if (ret != null) {
            return ret;
        }

        try {
            final Set<Class> fqns = classLoader.getFQNs( name );

            if (fqns.size() != 1) {
                throw new IllegalStateException( "class \"" + name + " is " + (fqns.isEmpty() ? "unknown" : "ambiguous") );
            }

            return Type.getType( fqns.iterator().next() );
        }
        catch (final IOException e) {
            throw new RuntimeException( e );
        }

    }

}