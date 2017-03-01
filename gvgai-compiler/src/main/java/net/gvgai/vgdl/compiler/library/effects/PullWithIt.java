package net.gvgai.vgdl.compiler.library.effects;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.GeneratedType;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.sprites.Passive;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class PullWithIt extends BaseEffect {
    private static final String PULL_WITH_IT = "pullWithIt";

    public PullWithIt( Type my, Type[] other, String[] parameters ) {
        super( my, other, parameters );
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter collisionMethodAdapter ) {
        super.generate( vgdlCompiler, requiredFeatures, collisionMethodAdapter );

        //XXX
        VGDLCompiler.generateLogMessage( myType.getClassName(), collisionMethodAdapter, "PullWithIt called" );

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
        final Method moveMethod = Method.getMethod( "void move(Object )" );

        final GeneratedType otherGeneratedType = vgdlCompiler.getClassLoader().getGeneratedTypes().get( otherTypes[0] );
        if (otherGeneratedType.options.get( PULL_WITH_IT ) == null) {
            System.out.println( "Injecting " + otherTypes[0] + " with \"PullWithIt\" listener" );
            final ClassWriter cw = otherGeneratedType.cw;

            //Add a set with things to pull
            cw.visitField( ACC_PRIVATE, PULL_WITH_IT, "L" + Type.getType( Set.class ).getInternalName() + ";", null, null );
            otherGeneratedType.options.put( PULL_WITH_IT, "true" );

            //Add the initializer to the constructor
            final GeneratorAdapter otherConstructor = otherGeneratedType.constructor;
            otherConstructor.loadThis();
            otherConstructor.newInstance( Type.getType( HashSet.class ) );
            otherConstructor.dup();
            otherConstructor.invokeConstructor( Type.getType( HashSet.class ), Method.getMethod( "void <init> ()" ) );
            otherConstructor.putField( otherTypes[0], PULL_WITH_IT, Type.getType( Set.class ) );

            //add add/remove methods
            final GeneratorAdapter newAddMethod = new GeneratorAdapter( ACC_PUBLIC, addMethod, null, null, cw );
//            VGDLCompiler.generateConsoleMessage( newAddMethod, "pullWithItAdded" );
            newAddMethod.loadThis();
            newAddMethod.getField( otherTypes[0], PULL_WITH_IT, Type.getType( Set.class ) );
            newAddMethod.loadArg( 0 );
            newAddMethod.invokeInterface( Type.getType( Set.class ), Method.getMethod( "boolean add(Object)" ) );
            newAddMethod.returnValue();
            newAddMethod.endMethod();

            final GeneratorAdapter newRemoveMethod = new GeneratorAdapter( ACC_PUBLIC, removeMethod, null, null, cw );
//            VGDLCompiler.generateConsoleMessage( newRemoveMethod, "pullWithItRemoved" );
            newRemoveMethod.loadThis();
            newRemoveMethod.getField( otherTypes[0], PULL_WITH_IT, Type.getType( Set.class ) );
            newRemoveMethod.loadArg( 0 );
            newRemoveMethod.invokeInterface( Type.getType( Set.class ), Method.getMethod( "boolean remove(Object)" ) );
            newRemoveMethod.returnValue();
            newRemoveMethod.endMethod();

            //Override the move method
            final GeneratorAdapter mv = new GeneratorAdapter( ACC_PUBLIC, moveMethod, null, null, cw );
            mv.visitCode();
//            VGDLCompiler.generateConsoleMessage( mv, "pulling moved called" );
            final Label l0 = new Label();
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESPECIAL, Type.getInternalName( Passive.class ), "move", "(Ljava/lang/Object;)V", false );
            final Label l1 = new Label();
            mv.visitLabel( l1 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitFieldInsn( GETFIELD, otherTypes[0].getInternalName(), PULL_WITH_IT, "L" + Type.getInternalName( Set.class ) + ";" );
            mv.visitMethodInsn( INVOKEINTERFACE, Type.getInternalName( Set.class ), "iterator", "()L" + Type.getInternalName( Iterator.class ) + ";", true );
            mv.visitVarInsn( ASTORE, 3 );
            final Label l2 = new Label();
            mv.visitJumpInsn( GOTO, l2 );
            final Label l3 = new Label();
            mv.visitLabel( l3 );
            mv.visitFrame( F_NEW, 4, new Object[] { "A", "java/lang/Object", TOP, Type.getInternalName( Iterator.class ) }, 0, new Object[] {} );
            mv.visitVarInsn( ALOAD, 3 );
            mv.visitMethodInsn( INVOKEINTERFACE, Type.getInternalName( Iterator.class ), "next", "()Ljava/lang/Object;", true );
            mv.visitTypeInsn( CHECKCAST, Type.getInternalName( Passive.class ) );
            mv.visitVarInsn( ASTORE, 2 );
            final Label l4 = new Label();
            mv.visitLabel( l4 );
            mv.visitVarInsn( ALOAD, 2 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKEVIRTUAL, Type.getInternalName( Passive.class ), "move", "(Ljava/lang/Object;)V", false );
            mv.visitLabel( l2 );
            mv.visitFrame( F_NEW, 0, null, 0, null );
            mv.visitVarInsn( ALOAD, 3 );
            mv.visitMethodInsn( INVOKEINTERFACE, Type.getInternalName( Iterator.class ), "hasNext", "()Z", true );
            mv.visitJumpInsn( IFNE, l3 );
            final Label l5 = new Label();
            mv.visitLabel( l5 );
            mv.visitInsn( RETURN );
            final Label l6 = new Label();
            mv.visitLabel( l6 );
            mv.visitLocalVariable( "this", "LA;", null, l0, l6, 0 );
            mv.visitLocalVariable( "o", "Ljava/lang/Object;", null, l0, l6, 1 );
            mv.visitLocalVariable( "p", "L" + Type.getInternalName( Passive.class ) + ";", null, l4, l2, 2 );
            mv.visitMaxs( 2, 4 );
            mv.visitEnd();

            //TODO implement what happens when puller is removed

        }

//        VGDLCompiler.generateConsoleMessage( mg, "Will add " + myType + " to be pulled by " + otherTypes[0] );
        final GeneratedType myGeneratedType = vgdlCompiler.getClassLoader().getGeneratedTypes().get( myType );
        myGeneratedType.cw.visitField( ACC_PRIVATE, "puller", "L" + otherTypes[0].getInternalName() + ";", null, null );
        collisionMethodAdapter.loadThis();
        collisionMethodAdapter.loadArg( 0 );

        collisionMethodAdapter.putField( myType, "puller", otherTypes[0] );
        collisionMethodAdapter.loadArg( 0 );
        collisionMethodAdapter.loadThis();
        collisionMethodAdapter.invokeVirtual( otherTypes[0], addMethod );

        //TODO unify move methods
        //Override the move method of myType to reset the puller/pullee connection if 'this' is moved
        final GeneratorAdapter moveMethodAdapter = new GeneratorAdapter( ACC_PUBLIC, moveMethod, null, null, myGeneratedType.cw );
        final Label noPuller = new Label();

        moveMethodAdapter.loadThis();
        moveMethodAdapter.getField( myType, "puller", otherTypes[0] );
        moveMethodAdapter.visitJumpInsn( IFNULL, noPuller );

        moveMethodAdapter.loadThis();
        moveMethodAdapter.getField( myType, "puller", otherTypes[0] );
        moveMethodAdapter.loadThis();
        moveMethodAdapter.invokeVirtual( otherTypes[0], removeMethod );

        moveMethodAdapter.loadThis();
        moveMethodAdapter.visitInsn( ACONST_NULL );
        moveMethodAdapter.putField( myType, "puller", otherTypes[0] );
        moveMethodAdapter.mark( noPuller );

        //Call super
        moveMethodAdapter.loadThis();
        moveMethodAdapter.loadArg( 0 );
        moveMethodAdapter.visitMethodInsn( INVOKESPECIAL, myGeneratedType.parentType.getInternalName(), "move", "(Ljava/lang/Object;)V", false );

        moveMethodAdapter.returnValue();
        moveMethodAdapter.endMethod();
    }

}
