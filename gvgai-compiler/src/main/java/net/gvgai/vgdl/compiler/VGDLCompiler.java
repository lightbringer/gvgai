package net.gvgai.vgdl.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
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
import net.gvgai.vgdl.compiler.generated.vgdlParser.Sprite_setContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.TerminationContext;
import net.gvgai.vgdl.compiler.generated.vgdlParser.Termination_setContext;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.compiler.library.GameClass;
import net.gvgai.vgdl.compiler.library.Termination;
import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.game.VGDLGame;
import net.gvgai.vgdl.sprites.VGDLSprite;
import net.gvgai.vgdl.sprites.producer.SpawnPoint;

public class VGDLCompiler extends vgdlBaseListener implements Opcodes {
    public final static String BASE_PACKAGE = "net/gvgai/game";

    private final static String END_OF_SCREEN = "EOS";

    public static String formatClassName( String name ) {
        if (Character.isUpperCase( name.codePointAt( 0 ) )) {
            return name;
        }
        else {
            return name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
        }
    }

    public static void generateGetGameMap( GeneratorAdapter mg ) {
        mg.loadThis();
        mg.getField( Type.getType( VGDLSprite.class ), "map", Type.getType( GameMap.class ) );
    }

    public static void generateGetGameStat( GeneratorAdapter mg ) {
        mg.loadThis();
        mg.getField( Type.getType( VGDLSprite.class ), "state", Type.getType( GameState.class ) );
    }

    public static void generateLogMessage( String loggerName, GeneratorAdapter mg, String string ) {
        generateLogMessage( loggerName, mg, string, Level.INFO );
    }

    public static void generateLogMessage( String loggerName, GeneratorAdapter mg, String string, Level level ) {
        mg.push( loggerName );
        mg.invokeStatic( Type.getType( Logger.class ), Method.getMethod( Logger.class.getName() + " getLogger(String)" ) );
        mg.getStatic( Type.getType( Level.class ), level.getName(), Type.getType( Level.class ) );
        mg.push( string );
        mg.invokeVirtual( Type.getType( Logger.class ), Method.getMethod( "void log(" + Level.class.getName() + ", String)" ) );
    }

    private static void generateCollideMethod( Type actorType, GeneratorAdapter m, int argIndex, Map<Type[], Object[]> map, int[] locals ) {
        final Map<Type, Map<Type[], Object[]>> reducedMap = new HashMap<>();
        for (final Map.Entry<Type[], Object[]> e : map.entrySet()) {
            final Type[] keys = e.getKey();
            if (keys == null) {
                continue;
            }
            final Type c = keys[0];
            Map<Type[], Object[]> subList = reducedMap.get( c );
            if (subList == null) {
                subList = new HashMap<>();
                reducedMap.put( c, subList );
            }
            if (keys.length == 1) {
                subList.put( null, e.getValue() );
            }
            else {
                subList.put( Arrays.copyOfRange( keys, 1, keys.length ), e.getValue() );
            }

        }

        for (final Map.Entry<Type, Map<Type[], Object[]>> e : reducedMap.entrySet()) {
            final Label loopIncrement = m.newLabel();
            final Label loopTest = m.newLabel();
            final Label loopBlock = m.newLabel();
            final Label trueBlock = m.newLabel();
            final Label caseDone = m.newLabel();

            //Loop over the elements argIndex ... others.length to see if there's an object of the required class
            //We can assume that the class ids are in ascending order
            final int iLocal = m.newLocal( Type.INT_TYPE );
            m.push( argIndex );
            m.storeLocal( iLocal, Type.INT_TYPE );
            m.goTo( loopTest );
            m.mark( loopBlock );
            m.loadArg( 0 );
            m.loadLocal( iLocal );
            m.arrayLoad( Type.getType( VGDLSprite.class ) );
            m.loadThis();
            m.ifCmp( Type.getType( VGDLSprite.class ), IFEQ, loopIncrement );
            m.loadArg( 0 );
            m.loadLocal( iLocal, Type.INT_TYPE );
            m.arrayLoad( Type.getType( VGDLSprite.class ) );
            generateLogMessage( actorType.getClassName(), m, "Checking if argument is of type " + e.getKey(), Level.FINER );
            m.instanceOf( e.getKey() );
            m.push( true );
            m.ifCmp( Type.BOOLEAN_TYPE, IFEQ, trueBlock );
            m.goTo( loopIncrement );
            m.mark( trueBlock );

            //Dive deeper into the rabbit hole ...
            final int[] newLocals = Arrays.copyOf( locals, locals.length + 1 );
            newLocals[newLocals.length - 1] = iLocal;

            generateCollideMethod( actorType, m, argIndex + 1, e.getValue(), newLocals );

            // .. if we come back here, nothing down there has triggered a return
            final Map.Entry<Type[], Object[]> leafStatement = e.getValue().entrySet().stream().filter( en -> en.getKey() == null ).findAny().orElse( null );

            if (leafStatement != null) {
                final Type[] argTypes = (Type[]) leafStatement.getValue()[0];
                generateLogMessage( actorType.getClassName(), m, actorType + "collides with " + Arrays.toString( argTypes ), Level.FINER );

                String signature = "void collide(";
                final Type[] signatureTypes = (Type[]) leafStatement.getValue()[0];
                for (int i = 0; i < signatureTypes.length; i++) {
                    signature += signatureTypes[i].getClassName();
                    if (i < signatureTypes.length - 1) {
                        signature += ", ";
                    }
                }
                signature += ")";

                m.loadThis();
                for (int i = 0; i < argTypes.length; i++) {
                    m.loadArg( 0 );
                    m.loadLocal( newLocals[i] );
                    m.arrayLoad( Type.getType( VGDLSprite.class ) );
                    m.visitTypeInsn( CHECKCAST, argTypes[i].getInternalName() );
                }

                final Method typedCollide = Method.getMethod( signature );
                m.invokeVirtual( actorType, typedCollide );
//                m.returnValue();
                m.goTo( caseDone );

            }

            m.mark( loopIncrement );
            m.iinc( iLocal, 1 );
            m.mark( loopTest );
            m.loadLocal( iLocal );
            m.loadArg( 0 );
            m.arrayLength();
            m.ifICmp( IFLT, loopBlock );
            m.mark( caseDone );
        }

        /* At this point none of the if-instanceof blocks triggered a return, so the call will be delegated to the parent's collide method */

    }

    private static Field getField( Class<?> clazz, String name ) {
        try {
            return clazz.getDeclaredField( name );
        }
        catch (final NoSuchFieldException e) {
            if (clazz.getSuperclass() != Object.class) {
                return getField( clazz.getSuperclass(), name );
            }
            else {
                return null;
            }
        }
    }

    /**
     *
     */

    private VGDLClassLoader classLoader;

    private final Stack<GeneratorAdapter> methods;

    private final SortedMap<Integer, String[]> levelMapping;

    final boolean writeClassesToDisk;

    private int local;

    private int nextClassId;

    private final Set<VGDLRuntime.Feature> requiredFeatures;

    private final Type gameType;

    private int zLevel;

    private int nextLambda;

    private final String packageName;

    VGDLCompiler( String gameName ) {
        this( gameName, false );
    }

    /**
     * @param gameName name of the game class
     */
    VGDLCompiler( String gameName, boolean writeClassesToDisk ) {
        packageName = BASE_PACKAGE + '/' + gameName.toLowerCase();
        gameType = Type.getType( "L" + packageName + "/" + formatClassName( gameName ) + ";" );
        this.writeClassesToDisk = writeClassesToDisk;

        levelMapping = new TreeMap<>();
        methods = new Stack<>();
        requiredFeatures = new HashSet<>();

    }

    @Override
    public void enterGame( GameContext ctx ) {
        zLevel = 0;
        classLoader = new VGDLClassLoader( Thread.currentThread().getContextClassLoader() );
        Thread.currentThread().setContextClassLoader( classLoader );
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

        final String nameA = ctx.known_sprite_name( 0 ).getText();
        final GeneratedType actorType = classLoader.getGeneratedTypes().get( getTypeForSimpleName( formatClassName( nameA ) ) );
        GeneratorAdapter mg;

        if (nameA.equals( END_OF_SCREEN )) {
            throw new IllegalStateException( END_OF_SCREEN
                            + " must not be the first actor. If you want to collide something with the world boundary, set that as the first actor" );
        }

        final String nameB = ctx.known_sprite_name( 1 ).getText();
        //Call OnOutOfBounds instead of a collision method
        if (nameB.equals( END_OF_SCREEN )) {
            if (ctx.known_sprite_name().size() != 2) {
                throw new IllegalStateException( END_OF_SCREEN + " supplied and overflowing list of actors" );
            }

            System.out.println( "Setting interaction " + actorType.type + " -> <End of Screen>" );
            mg = actorType.onBoundary;
            if (mg == null) {
                System.out.println( "Creating new method" );
                final Method m = Method.getMethod( "void OnOutOfBounds ()" );
                mg = new GeneratorAdapter( ACC_PUBLIC, m, null, null, actorType.cw );
                //Call super
                mg.loadThis();
                mg.visitMethodInsn( INVOKESPECIAL, actorType.parentType.getInternalName(), m.getName(), m.getDescriptor(), false );
                actorType.onBoundary = mg;
            }
            else {
                System.out.println( "Extending existing method" );
            }
            final Effect e = getEffectForName( formatClassName( ctx.action.getText() ), actorType.type, null, ctx.option() );
            e.generate( this, requiredFeatures, mg );
        }
        else {
            //Build the collision methods
            final List<GeneratedType> otherTypes = new ArrayList();
            for (int i = 1; i < ctx.known_sprite_name().size(); i++) {
                final GeneratedType otherType = classLoader.getGeneratedTypes()
                                .get( getTypeForSimpleName( formatClassName( ctx.known_sprite_name( i ).getText() ) ) );
                otherTypes.add( otherType );
            }
            otherTypes.sort( ( c1, c2 ) -> Integer.compare( c2.classId, c1.classId ) );

            System.out.println( "Setting interaction " + actorType.type + " -> " + otherTypes.stream().map( gt -> gt.type ).collect( Collectors.toList() ) );

            String signature = "void collide (";
            for (int i = 0; i < otherTypes.size(); i++) {
                signature += otherTypes.get( i ).type.getClassName();
                if (i < otherTypes.size() - 1) {
                    signature += ", ";
                }
            }

            signature += ")";

            Map.Entry<Type[], GeneratorAdapter> en = actorType.definedInteractions.get( signature );

            if (en != null) {
                System.out.println( "Extending existing method" );
                mg = en.getValue();
            }
            else {
                System.out.println( "Creating new method" );
                final Method m = Method.getMethod( signature );
                mg = new GeneratorAdapter( ACC_PROTECTED, m, null, null, actorType.cw );
                en = new DefaultMapEntry( otherTypes.stream().map( ga -> ga.type ).toArray( Type[]::new ), mg );
                actorType.definedInteractions.put( signature, en );
            }

            final Effect e = getEffectForName( formatClassName( ctx.action.getText() ), actorType.type,
                            otherTypes.stream().map( gt -> gt.type ).toArray( Type[]::new ), ctx.option() );
            e.generate( this, requiredFeatures, mg );

            //Don't end the method here
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
        final Type spriteType = Type.getType( "L" + packageName + "/" + spriteTypeName + ";" );
        final GeneratedType g = new GeneratedType( spriteType, spcw );
        classLoader.getGeneratedTypes().put( spriteType, g );
        final Type parentType = getTypeForSimpleName( formatClassName( ctx.parentClass ) );
        g.parentType = parentType;
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

        //XXX make this more robust
        for (final OptionContext o : ctx.option()) {
            switch (o.option_key().getText().toLowerCase()) {
                case "img":
                case "color":
                    img += o.option_key().getText() + "=" + o.option_value().getText() + " ";

                    break;
                case "stype":
                    g.options.put( "spriteType", o.option_value().getText() );
                    break;
                case "orientation":
                    g.options.put( "orientation", o.option_value().getText() );
                    break;
                default:
                    generateSetField( mg, spriteType, o.option_key().getText(), o.option_value().getText() );
                    break;

            }
        }
        img += "zLevel=" + zLevel++;
        //Now add the sprite class meta information
        final AnnotationVisitor an = spcw.visitAnnotation( Type.getDescriptor( SpriteInfo.class ), true );
        //We combine the graphics options into a string that may be read out by the runtime.
        //the sprite class logic should be oblivious to this
        an.visit( "resourceInfo", img );

        String orientation = g.options.get( "orientation" );
        if (orientation != null) {
            orientation = orientation.toUpperCase();

            mg.loadThis();
            switch (orientation) {
                default:
                    System.err.println( "Orientation " + orientation + " on class " + g.type + " unknown. Defaulting to UP" );
                case "UP":
                    mg.getStatic( Type.getType( VGDLSprite.class ), "UpDirection", Type.getType( Supplier.class ) );
                    break;
                case "DOWN":
                    mg.getStatic( Type.getType( VGDLSprite.class ), "DownDirection", Type.getType( Supplier.class ) );
                    break;
                case "LEFT":
                    mg.getStatic( Type.getType( VGDLSprite.class ), "LeftDirection", Type.getType( Supplier.class ) );
                    break;
                case "RIGHT":
                    mg.getStatic( Type.getType( VGDLSprite.class ), "RightDirection", Type.getType( Supplier.class ) );
                    break;
            }
            System.out.println( g.type + " has orientation " + orientation );
            final Method getMethod = Method.getMethod( "Object get( )" );
            mg.invokeInterface( Type.getType( Supplier.class ), getMethod );
            final Method setDirectionMethod = Method.getMethod( "void setDirection(Object )" );
            mg.invokeVirtual( spriteType, setDirectionMethod );
        }
        g.constructor = mg;
        //Don't end the constructor here. Effects might want to alter it

        generateCopyMethod( spcw, spriteType, parentType );
        final int classId = generateClassIdMethod( spcw );

        g.classId = classId;

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

        //Also close the constructors for the sprite classes
        for (final GeneratedType g : classLoader.getGeneratedTypes().values()) {
            if (g.type == gameType) {
                continue;
            }
            final GeneratorAdapter constructor = g.constructor;
            constructor.returnValue();
            constructor.endMethod();

            //That's all folks
            g.cw.visitEnd();
        }
    }

    @Override
    public void exitInteraction_set( Interaction_setContext ctx ) {
        //now implement the abstract lookup method
        final Method overLoadMethod = Method.getMethod( "void collide (" + VGDLSprite.class.getName() + "[])" );
        for (final Map.Entry<Type, GeneratedType> e : classLoader.getGeneratedTypes().entrySet()) {
            if (e.getKey().equals( gameType )) {
                continue;
            }
            final GeneratedType g = e.getValue();
            final ClassWriter cw = g.cw;
            final Type actorType = e.getKey();
            final Type parentType = e.getValue().parentType;
            final GeneratorAdapter m = new GeneratorAdapter( ACC_PUBLIC, overLoadMethod, null, null, cw );
            generateLogMessage( actorType.getClassName(), m, "Collide in class " + actorType.getClassName() + " has been called", Level.FINER );

            /* We defined outselves a helper map that maps the collision types to the GeneratorAdaptor and the a copy
             * of the collision type arrays. We use the latter in the recursive reduction function of the ifinstace blocks (see generateInteractions)
             */

            final Map<Type[], Object[]> interactions = g.definedInteractions.entrySet().stream()
                            .collect( Collectors.toMap( f -> Arrays.copyOf( f.getValue().getKey(), f.getValue().getKey().length ),
                                            f -> new Object[] { f.getValue().getKey(), f.getValue().getValue() } ) );

            //Special case for onBoundary
            if (g.onBoundary != null) {
                requiredFeatures.add( Feature.OBEY_END_OF_BOUNDARIES );
                System.out.println( actorType + " has OnBoundaryMethod" );
                g.onBoundary.returnValue();
                g.onBoundary.endMethod();
            }

            //The empty case
            if (interactions.isEmpty()) {
//              generateConsoleMessage( m, "No interactions defined for " + actorType.getClassName() + ". Delegating to super" );
                System.out.println( "No interactions for " + actorType.getClassName() + " Delegating to " + parentType.getClassName() );
                m.loadThis();
                m.loadArg( 0 );
                m.visitMethodInsn( INVOKESPECIAL, parentType.getInternalName(), overLoadMethod.getName(), overLoadMethod.getDescriptor(), false );
                m.returnValue();
                m.endMethod();
                continue;
            }

            generateCollideMethod( actorType, m, 0, interactions, new int[0] );

            m.loadThis();
            m.loadArg( 0 );
            m.visitMethodInsn( INVOKESPECIAL, parentType.getInternalName(), overLoadMethod.getName(), overLoadMethod.getDescriptor(), false );
            m.returnValue();
            m.endMethod();

            //While we're at it. We need to close the collision method and onBoundary method
            //It's only at this point that we know that all effects have been generated
            interactions.values().stream().map( o -> o[1] ).forEach( o -> {
                final GeneratorAdapter ga = (GeneratorAdapter) o;
                ga.returnValue();
                ga.endMethod();
            } );

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
    public void exitSprite_set( Sprite_setContext ctx ) {
        for (final GeneratedType g : classLoader.getGeneratedTypes().values()) {
            if (g.options.get( "spriteType" ) != null) {
                handleSpriteTypeOption( g, g.options.get( "spriteType" ) );
            }
        }
    }

    @Override
    public void exitTermination_set( Termination_setContext ctx ) {
        final GeneratorAdapter currentMethod = methods.pop();
        final int ret = currentMethod.newLocal( Type.BOOLEAN_TYPE );
        currentMethod.storeLocal( ret );

        //Call the game state's setter and store the outcome there
        currentMethod.loadThis();
        final Method getGameState = Method.getMethod( "net.gvgai.vgdl.game.GameState getGameState()" );
        currentMethod.invokeInterface( Type.getType( VGDLGame.class ), getGameState );
        currentMethod.loadLocal( ret );
        final Method setGameOver = Method.getMethod( "void setGameOver(boolean)" );
        currentMethod.invokeInterface( Type.getType( GameState.class ), setGameOver );

        //Return the outcome
        currentMethod.loadLocal( ret );
        currentMethod.returnValue();
        currentMethod.endMethod();
    }

    public Set<Class> getClasses() {
        final Set<Class> clazzes = new HashSet<>();
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

    public VGDLClassLoader getClassLoader() {
        return classLoader;
    }

    public String getGamePackageName() {
        return packageName;
    }

    public Type getNonGeneratedParentType( Object spriteType ) {
        GeneratedType g = classLoader.getGeneratedTypes().get( spriteType );
        Type parentType = null;
        while (g != null) {
            parentType = g.parentType;
            g = classLoader.getGeneratedTypes().get( g.parentType );
        }
        return parentType;
    }

    public Type getTypeForSimpleName( String name ) {
        final Type ret = classLoader.getGeneratedTypes().keySet().stream()
                        .filter( t -> t.getClassName().substring( t.getClassName().lastIndexOf( '.' ) + 1 ).equals( name ) ).findAny().orElse( null );
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

    public int nextLambda() {
        return nextLambda++;
    }

    public void writeClassesToDisk( File rootDir ) {
        classLoader.getGeneratedTypes().forEach( ( t, gt ) -> {
            try {
                final String fileName = rootDir + "/" + t.getClassName().replace( '.', '/' ) + ".class";
                final File f = new File( fileName );
                f.mkdirs();
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                final FileOutputStream fOut = new FileOutputStream( f );
                final byte[] code = gt.cw.toByteArray();
                fOut.write( code );
                fOut.close();
            }
            catch (final IOException e) {
                throw new RuntimeException( e );
            }
        } );
    }

    private int generateClassIdMethod( ClassWriter spcw ) {
        final Method m = Method.getMethod( "int getClassId ()" );
        final GeneratorAdapter mg = new GeneratorAdapter( ACC_PUBLIC, m, null, null, spcw );

        final int classId = nextClassId++;
        mg.push( classId );
        mg.returnValue();
        mg.endMethod();

        return classId;
    }

    private void generateCopyMethod( ClassWriter spcw, Type spriteType, Type parentType ) {
        final Method m = Method.getMethod( VGDLSprite.class.getName() + " copy ()" );
        final GeneratorAdapter mg = new GeneratorAdapter( ACC_PUBLIC, m, null, null, spcw );
        final int ret = mg.newLocal( spriteType );

        mg.newInstance( spriteType );
        mg.storeLocal( ret );
        mg.loadLocal( ret );
        final Method constr = Method.getMethod( "void <init> ()" );
        mg.invokeConstructor( spriteType, constr );

        mg.loadThis();
        mg.loadLocal( ret );

        final Method setup = Method.getMethod( "void setup (" + VGDLSprite.class.getName() + ")" );
        mg.invokeVirtual( parentType, setup );

        mg.loadLocal( ret );
        mg.returnValue();
        mg.endMethod();
    }

    private void generateSetField( GeneratorAdapter mg, Type spriteType, String key, String value ) {
        final Type parentType = getNonGeneratedParentType( spriteType );

        try {
            final Class<? extends VGDLSprite> clazz = (Class<? extends VGDLSprite>) Class.forName( parentType.getClassName(), false, classLoader );

            final Field f = getField( clazz, key );
            if (f == null) {
                System.err.println( "Warning: ignoring unknown option \"" + key + "=" + value + "\" on sprite class " + spriteType );
                return;
            }
            final Class fieldClazz = f.getType();
            final Type fieldType = Type.getType( fieldClazz );

            if (fieldClazz == String.class) {
                mg.loadThis();
                mg.push( value );
                mg.putField( spriteType, key, fieldType );
            }
            else if (fieldClazz.isEnum()) {
                final Object[] enums = fieldClazz.getEnumConstants();
                final java.lang.reflect.Method getName = Enum.class.getDeclaredMethod( "name" );
                boolean found = false;
                for (final Object o : enums) {
                    final String name = (String) getName.invoke( o );
                    if (name.equals( value )) {
                        mg.loadThis();
                        mg.getStatic( fieldType, value, fieldType );
                        mg.putField( spriteType, key, fieldType );
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.err.println( "Warning: option \"" + key + " is of type " + fieldClazz + ". None of the possible values matches " + value );
                }
            }
            else if (fieldClazz == float.class) {
                try {
                    final float floatValue = Float.parseFloat( value );
                    mg.loadThis();
                    mg.push( floatValue );
                    mg.putField( spriteType, key, Type.FLOAT_TYPE );
                }
                catch (final NumberFormatException e) {
                    System.err.println( "Warning: option \"" + key + " is of type float." + value + " is not a valid numeric value" );
                }
            }
            else if (fieldClazz == double.class) {
                try {
                    final double doubleValue = Double.parseDouble( value );
                    mg.loadThis();
                    mg.push( doubleValue );
                    mg.putField( spriteType, key, Type.DOUBLE_TYPE );
                }
                catch (final NumberFormatException e) {
                    System.err.println( "Warning: option \"" + key + " is of type double." + value + " is not a valid numeric value" );
                }
            }
            else {
                System.err.println( "Warning: option \"" + key + " is of unsupported type " + f.getType() );
            }
        }
        catch (final ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException( e );
        }

        System.out.println( "non-generated parent: " + parentType );

    };

    private Effect getEffectForName( String text, Type actorType, Type[] otherTypes, List<OptionContext> list ) {
        try {
            final Type t = getTypeForSimpleName( text );
            final Class<? extends Effect> effectClass = (Class<? extends Effect>) Class.forName( t.getClassName() );

            final Constructor<? extends Effect> c = effectClass.getConstructor( Type.class, Type[].class, String[].class );
            String[] options;
            if (list != null && !list.isEmpty()) {
                options = new String[list.size() * 2];
                int i = 0;
                for (final OptionContext o : list) {
                    options[i] = o.option_key().getText();
                    options[i + 1] = o.option_value().getText();
                    i += 2;
                }
            }
            else {
                options = null;
            }
            return c.newInstance( actorType, otherTypes, options );
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException |

                        InvocationTargetException e)

        {
            throw new RuntimeException( e );
        }

    }

    private void handleSpriteTypeOption( GeneratedType g, String option_value ) {
        final Type parent = getNonGeneratedParentType( g.type );

        try {
            final Class parentClazz = Class.forName( parent.getClassName() );
            if (SpawnPoint.class.isAssignableFrom( parentClazz )) {
                final Type spriteType = getTypeForSimpleName( formatClassName( option_value ) );
                if (spriteType == null) {
                    System.err.println( "Unrecognized sprite type option on class " + parent + " for type " + g.type + ". Class " + option_value + " unknown" );
                    return;
                }
                final ClassWriter cw = g.cw;
                final Method m = Method.getMethod( VGDLSprite.class.getName() + " createNewSprite ()" );
                final GeneratorAdapter mg = new GeneratorAdapter( ACC_PUBLIC, m, null, null, cw );
                final Method spriteTypeConstructor = Method.getMethod( "void <init> ( )" );
                mg.newInstance( spriteType );
                final int ret = mg.newLocal( spriteType );
                mg.storeLocal( ret );
                mg.loadLocal( ret );
                mg.invokeConstructor( spriteType, spriteTypeConstructor );
                mg.loadLocal( ret );
                mg.returnValue();
                mg.endMethod();

            }
            else {
                System.err.println( "Unrecognized option stype on class " + parent + " for type " + g.type );
            }
        }
        catch (final ClassNotFoundException e) {
            throw new RuntimeException( e );
        }
    }

}