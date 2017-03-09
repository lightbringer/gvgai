package net.gvgai.vgdl.compiler.library.effects;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.GeneratedType;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.sprites.Passive;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class PullWithIt extends BaseEffect {
    private static final String PULL_WITH_IT = "pullWithIt";
    private static final String PULL_WITH_IT_ADDED = "pullWithItAdded";
    private static final Logger LOGGER = Logger.getLogger( PullWithIt.class.getName() );
    private static final String PULLER = "puller";

    private static void createAddMethod( VGDLCompiler vgdlCompiler, Type type, Method addMethod, ClassWriter cw ) {
//        if (sprites == null) {
//            sprites = new VGDLSprite[] { s };
//        }
//        for (int i = 0; i < sprites.length; i++) {
//            if (sprites[i] == null) {
//                sprites[i] = s;
//                return;
//            }
//        }
//        sprites = Arrays.copyOf( sprites, sprites.length + 1 );
//        sprites[sprites.length - 1] = s;
        final GeneratorAdapter newAddMethod = new GeneratorAdapter( ACC_PUBLIC, addMethod, null, null, cw );
        vgdlCompiler.generateLogMessage( type.getClassName(), newAddMethod, "pullWithItAdded", Level.FINEST );

        newAddMethod.loadThis();
        newAddMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        final Label insertLabel = newAddMethod.newLabel();
        newAddMethod.ifNonNull( insertLabel );
        newAddMethod.loadThis();
        newAddMethod.push( 1 );
        newAddMethod.newArray( Type.getType( VGDLSprite.class ) );
        newAddMethod.dup();
        newAddMethod.push( 0 );
        newAddMethod.loadArg( 0 );
        newAddMethod.arrayStore( Type.getType( VGDLSprite.class ) );
        newAddMethod.putField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newAddMethod.returnValue();

        newAddMethod.mark( insertLabel );
        final int counter = newAddMethod.newLocal( Type.INT_TYPE );
        newAddMethod.push( 0 );
        newAddMethod.storeLocal( counter );
        final Label testBlock = newAddMethod.newLabel();
        newAddMethod.goTo( testBlock );
        final Label block = newAddMethod.newLabel();
        newAddMethod.mark( block );

        newAddMethod.loadThis();
        newAddMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newAddMethod.loadLocal( counter );
        newAddMethod.arrayLoad( Type.getType( VGDLSprite.class ) );
        final Label incrementBlock = newAddMethod.newLabel();
        final Label nullBlock = newAddMethod.newLabel();
        newAddMethod.dup();
        newAddMethod.ifNonNull( nullBlock );
        newAddMethod.loadThis();
        newAddMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newAddMethod.loadLocal( counter );
        newAddMethod.loadArg( 0 );
        newAddMethod.arrayStore( Type.getType( VGDLSprite.class ) );
        newAddMethod.returnValue();

        newAddMethod.mark( nullBlock );
        newAddMethod.pop();
        newAddMethod.mark( incrementBlock );
        newAddMethod.iinc( counter, 1 );
        newAddMethod.mark( testBlock );
        newAddMethod.loadLocal( counter );
        newAddMethod.loadThis();
        newAddMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newAddMethod.arrayLength();
        newAddMethod.ifCmp( Type.INT_TYPE, GeneratorAdapter.LT, block );

        newAddMethod.loadThis();
        newAddMethod.dup();
        newAddMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newAddMethod.dup();
        newAddMethod.arrayLength();
        newAddMethod.push( 1 );
        newAddMethod.math( GeneratorAdapter.ADD, Type.INT_TYPE );
        newAddMethod.invokeStatic( Type.getType( Arrays.class ), Method.getMethod( "Object[] copyOf(Object[], int)" ) );
        newAddMethod.checkCast( Type.getType( VGDLSprite[].class ) );
        newAddMethod.putField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );

        newAddMethod.loadThis();
        newAddMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newAddMethod.loadLocal( counter );
        newAddMethod.loadArg( 0 );
        newAddMethod.arrayStore( Type.getType( VGDLSprite.class ) );
        newAddMethod.returnValue();
        newAddMethod.endMethod();
    }

    private static void createRemoveMethod( VGDLCompiler vgdlCompiler, Type type, Method removeMethod, ClassWriter cw ) {
//        for (int i = 0; i < sprites.length; i++) {
//            if (sprites[i] == s) {
//                sprites[i] = null;
//                return;
//            }
//        }
        final GeneratorAdapter newRemoveMethod = new GeneratorAdapter( ACC_PUBLIC, removeMethod, null, null, cw );
        vgdlCompiler.generateLogMessage( type.getClassName(), newRemoveMethod, "pullWithItRemoved", Level.FINEST );
        final int counter = newRemoveMethod.newLocal( Type.INT_TYPE );
        newRemoveMethod.push( 0 );
        newRemoveMethod.storeLocal( counter );

        final Label testBlock = newRemoveMethod.newLabel();
        newRemoveMethod.goTo( testBlock );

        final Label block = newRemoveMethod.newLabel();
        newRemoveMethod.mark( block );
        newRemoveMethod.loadThis();
        newRemoveMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newRemoveMethod.loadLocal( counter );
        newRemoveMethod.arrayLoad( Type.getType( VGDLSprite.class ) );
        newRemoveMethod.loadArg( 0 );
        final Label spriteFound = newRemoveMethod.newLabel();
        newRemoveMethod.ifCmp( Type.getType( VGDLSprite.class ), GeneratorAdapter.EQ, spriteFound );
        final Label incrementBlock = newRemoveMethod.newLabel();
        newRemoveMethod.goTo( incrementBlock );

        newRemoveMethod.mark( spriteFound );
        newRemoveMethod.loadThis();
        newRemoveMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newRemoveMethod.loadLocal( counter );
        newRemoveMethod.push( (String) null );
        newRemoveMethod.arrayStore( Type.getType( VGDLSprite.class ) );
        newRemoveMethod.returnValue();

        newRemoveMethod.mark( incrementBlock );
        newRemoveMethod.iinc( counter, 1 );

        newRemoveMethod.mark( testBlock );
        newRemoveMethod.loadLocal( counter );
        newRemoveMethod.loadThis();
        newRemoveMethod.getField( type, PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
        newRemoveMethod.arrayLength();
        newRemoveMethod.ifCmp( Type.INT_TYPE, GeneratorAdapter.LT, block );

        vgdlCompiler.generateLogMessage( type.getClassName(), newRemoveMethod, "removePullWIthIt found no match to remove", Level.WARNING );

        newRemoveMethod.returnValue();
        newRemoveMethod.endMethod();

    }

    public PullWithIt( VGDLCompiler context, Type my, Type[] others, String... parameters ) {
        super( context, my, others, parameters );
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter collisionMethodAdapter ) {
        super.generate( vgdlCompiler, requiredFeatures, collisionMethodAdapter );

        //XXX
        vgdlCompiler.generateLogMessage( myType.getClassName(), collisionMethodAdapter, "PullWithIt called" );

        final Type parentType = vgdlCompiler.getNonGeneratedParentType( myType );
        try {
            if (!Passive.class.isAssignableFrom( Class.forName( parentType.getClassName() ) )) {
                throw new IllegalStateException( myType + " is a non-movable class." );
            }
        }
        catch (final ClassNotFoundException e) {
            throw new RuntimeException( e );
        }
        final Type otherParentType = vgdlCompiler.getNonGeneratedParentType( otherTypes[0] );
        try {
            if (!Passive.class.isAssignableFrom( Class.forName( otherParentType.getClassName() ) )) {
                throw new IllegalStateException( myType + " is specified to be pulled with a non-movable class " + otherTypes[0] );
            }
        }
        catch (final ClassNotFoundException e) {
            throw new RuntimeException( e );
        }

        final Method addMethod = Method.getMethod( "void addToPull(" + VGDLSprite.class.getName() + ")" );
        final Method removeMethod = Method.getMethod( "void removeFromPull(" + VGDLSprite.class.getName() + ")" );
        final Method moveMethod = Method.getMethod( "void move(" + GameMap.class.getName() + ", Object )" );

        final GeneratedType otherGeneratedType = vgdlCompiler.getClassLoader().getGeneratedTypes().get( otherTypes[0] );
        if (otherGeneratedType.options.get( PULL_WITH_IT ) == null) {
            LOGGER.fine( "Injecting " + otherTypes[0] + " with \"PullWithIt\" listener" );
            final ClassWriter cw = otherGeneratedType.cw;

            //Add a set with things to pull
            cw.visitField( ACC_PRIVATE, PULL_WITH_IT, "[L" + Type.getType( VGDLSprite.class ).getInternalName() + ";", null, null );
            otherGeneratedType.options.put( PULL_WITH_IT, "true" );

            //add add/remove methods
            createAddMethod( vgdlCompiler, otherTypes[0], addMethod, cw );
            createRemoveMethod( vgdlCompiler, otherTypes[0], removeMethod, cw );

            //Override the move method
            final GeneratorAdapter mv = vgdlCompiler.getMethod( otherGeneratedType, VGDLCompiler.MOVE );
            vgdlCompiler.generateLogMessage( otherTypes[0].getClassName(), mv, "moveWithPull called", Level.FINEST );
            mv.loadThis();
            mv.getField( otherTypes[0], PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
            final Label skipLabel = mv.newLabel();
            mv.ifNull( skipLabel );
            final int counter = mv.newLocal( Type.INT_TYPE );
            mv.push( 0 );
            mv.storeLocal( counter );
            final Label testBlock = mv.newLabel();
            mv.goTo( testBlock );

            final Label block = mv.newLabel();
            mv.mark( block );

            final Label incrementBlock = mv.newLabel();
            mv.loadThis();
            mv.getField( otherTypes[0], PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
            mv.loadLocal( counter );
            mv.arrayLoad( Type.getType( VGDLSprite.class ) );
            mv.dup();
            final Label nullBlock = mv.newLabel();
            mv.ifNull( nullBlock );
            vgdlCompiler.generateLogMessage( otherTypes[0].getClassName(), mv, "Pulling another object", Level.FINEST );
            mv.checkCast( Type.getType( Passive.class ) );
            mv.loadArgs();

            //First throw away the current pullee. It (or its copy) will be eventually added in the same slot, but we don't want to pull it again
            mv.loadThis();
            mv.getField( otherTypes[0], PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
            mv.loadLocal( counter );
            mv.push( (Type) null );
            mv.arrayStore( Type.getType( VGDLSprite.class ) );

            mv.invokeVirtual( Type.getType( Passive.class ), moveMethod );
            mv.goTo( incrementBlock );
            mv.mark( nullBlock );
            mv.pop();
            mv.mark( incrementBlock );
            mv.iinc( counter, 1 );
            mv.mark( testBlock );
            mv.loadLocal( counter );
            mv.loadThis();
            mv.getField( otherTypes[0], PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
            mv.arrayLength();
            mv.ifCmp( Type.INT_TYPE, GeneratorAdapter.LT, block );
            mv.mark( skipLabel );
            //The puller's setup method has to copy all the references in the set
            final GeneratorAdapter setupMethod = vgdlCompiler.getMethod( otherGeneratedType, VGDLCompiler.SETUP );
            setupMethod.loadThis();
            setupMethod.getField( otherTypes[0], PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
            final Label spritesNullLabel = setupMethod.newLabel();
            setupMethod.ifNull( spritesNullLabel );

            setupMethod.loadArg( 0 );
            setupMethod.checkCast( otherTypes[0] );
            setupMethod.loadThis();
            setupMethod.getField( otherTypes[0], PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );
            setupMethod.dup();
            setupMethod.arrayLength();
            setupMethod.invokeStatic( Type.getType( Arrays.class ), Method.getMethod( "Object[] copyOf(Object[], int)" ) );
            setupMethod.checkCast( Type.getType( VGDLSprite[].class ) );

            setupMethod.putField( otherTypes[0], PULL_WITH_IT, Type.getType( VGDLSprite[].class ) );

            setupMethod.mark( spritesNullLabel );
        }

        vgdlCompiler.generateLogMessage( myType.getClassName(), collisionMethodAdapter, "Will add " + myType + " to be pulled by " + otherTypes[0],
                        Level.FINE );
        final GeneratedType myGeneratedType = vgdlCompiler.getClassLoader().getGeneratedTypes().get( myType );
        myGeneratedType.cw.visitField( ACC_PRIVATE, PULLER, "L" + Type.getType( Passive.class ).getInternalName() + ";", null, null );
        myGeneratedType.cw.visitField( ACC_PRIVATE, PULL_WITH_IT_ADDED, "Z", null, null );

        collisionMethodAdapter.loadThis();
        collisionMethodAdapter.push( true );
        collisionMethodAdapter.putField( myType, PULL_WITH_IT_ADDED, Type.BOOLEAN_TYPE );

        collisionMethodAdapter.loadThis();
        collisionMethodAdapter.loadArg( 1 );

        collisionMethodAdapter.putField( myType, PULLER, Type.getType( Passive.class ) );
        collisionMethodAdapter.loadArg( 1 );
        collisionMethodAdapter.loadThis();
        collisionMethodAdapter.invokeVirtual( otherTypes[0], addMethod );

        //Override the move method of myType to reset the puller/pullee connection if 'this' is moved
        //But only if the "super.move" didn't just trigger a new connection
        final GeneratorAdapter moveMethodAdapter = vgdlCompiler.getMethod( myGeneratedType, VGDLCompiler.MOVE );
        final Label noPuller = new Label();

        moveMethodAdapter.loadThis();
        moveMethodAdapter.getField( myType, PULL_WITH_IT_ADDED, Type.BOOLEAN_TYPE );
        moveMethodAdapter.visitJumpInsn( IFNE, noPuller );

        moveMethodAdapter.loadThis();
        moveMethodAdapter.getField( myType, PULLER, Type.getType( Passive.class ) );
        moveMethodAdapter.visitJumpInsn( IFNULL, noPuller );

        moveMethodAdapter.loadThis();
        moveMethodAdapter.getField( myType, PULLER, Type.getType( Passive.class ) );
        moveMethodAdapter.checkCast( otherTypes[0] );
        moveMethodAdapter.loadThis();
        moveMethodAdapter.invokeVirtual( otherTypes[0], removeMethod );

        moveMethodAdapter.loadThis();
        moveMethodAdapter.visitInsn( ACONST_NULL );
        moveMethodAdapter.putField( myType, "puller", Type.getType( Passive.class ) );
        moveMethodAdapter.mark( noPuller );

        moveMethodAdapter.loadThis();
        moveMethodAdapter.push( false );
        moveMethodAdapter.putField( myType, PULL_WITH_IT_ADDED, Type.BOOLEAN_TYPE );

        //Now instruct the setup method to copy the puller
        final GeneratorAdapter setupMethod = vgdlCompiler.getMethod( myGeneratedType, VGDLCompiler.SETUP );
        setupMethod.loadArg( 0 );
        setupMethod.checkCast( myType );
        final int targetObject = setupMethod.newLocal( myType );
        setupMethod.storeLocal( targetObject );
        setupMethod.loadThis();
        setupMethod.getField( myType, PULLER, Type.getType( Passive.class ) );
        final Label skipLabel = setupMethod.newLabel();
        setupMethod.ifNull( skipLabel );
        setupMethod.loadThis();
        setupMethod.loadArg( 1 );
        setupMethod.invokeInterface( Type.getType( GameMap.class ), Method.getMethod( Stream.class.getName() + " values()" ) );
        setupMethod.loadThis();
        final int lambda = vgdlCompiler.nextLambda();
        setupMethod.visitInvokeDynamicInsn( "test", "(" + myType.getDescriptor() + ")Ljava/util/function/Predicate;",
                        new Handle( Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;" ),
                        new Object[] { Type.getType( "(Ljava/lang/Object;)Z" ),
                                        new Handle( Opcodes.H_INVOKESPECIAL, myType.getInternalName(), "lambda$" + lambda,
                                                        "(Lnet/gvgai/vgdl/sprites/VGDLSprite;)Z" ),
                                        Type.getType( "(Lnet/gvgai/vgdl/sprites/VGDLSprite;)Z" ) } );
        setupMethod.invokeInterface( Type.getType( Stream.class ), Method.getMethod( Stream.class.getName() + " filter(" + Predicate.class.getName() + ")" ) );
        setupMethod.invokeInterface( Type.getType( Stream.class ), Method.getMethod( Optional.class.getName() + " findAny()" ) );
        setupMethod.invokeVirtual( Type.getType( Optional.class ), Method.getMethod( "Object get()" ) );
        setupMethod.checkCast( otherTypes[0] );
        setupMethod.putField( myType, PULLER, Type.getType( Passive.class ) );
        setupMethod.mark( skipLabel );

        final GeneratorAdapter lambdaMethod = new GeneratorAdapter( ACC_PRIVATE + ACC_SYNTHETIC,
                        Method.getMethod( "boolean lambda$" + lambda + "(" + VGDLSprite.class.getName() + ")" ), null, null, myGeneratedType.cw );
        lambdaMethod.loadArg( 0 );
        lambdaMethod.invokeVirtual( Type.getType( VGDLSprite.class ), Method.getMethod( "int getId()" ) );
        lambdaMethod.loadThis();
        lambdaMethod.getField( myType, PULLER, Type.getType( Passive.class ) );
        lambdaMethod.invokeVirtual( Type.getType( VGDLSprite.class ), Method.getMethod( "int getId()" ) );
        final Label trueLabel = lambdaMethod.newLabel();
        lambdaMethod.ifICmp( GeneratorAdapter.EQ, trueLabel );
        lambdaMethod.push( false );
        lambdaMethod.returnValue();
        lambdaMethod.mark( trueLabel );
        lambdaMethod.push( true );
        lambdaMethod.returnValue();
        lambdaMethod.endMethod();

    }

}
