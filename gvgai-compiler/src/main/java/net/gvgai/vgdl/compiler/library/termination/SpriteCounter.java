package net.gvgai.vgdl.compiler.library.termination;

import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Termination;
import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class SpriteCounter implements Termination, Opcodes {
    private String stype;
    private int limit;
    private String win;

    @Override
    public void generate( VGDLCompiler context, Type gameType, Set<Feature> requiredFeatures, ClassWriter cw, GeneratorAdapter ga ) {
        final int lambda = context.nextLambda();
//        VGDLCompiler.generateLogMessage( gameType.getClassName(), ga,
//                        "Counting sprites of type " + context.getGamePackageName() + "/" + VGDLCompiler.formatClassName( stype ) );
        ga.loadThis();
        ga.visitMethodInsn( INVOKEVIRTUAL, gameType.getInternalName(), "getGameState", "()L" + Type.getType( GameState.class ).getInternalName() + ";", false );
        ga.visitMethodInsn( INVOKEINTERFACE, Type.getType( GameState.class ).getInternalName(), "values", "()Ljava/util/stream/Stream;", true );
        ga.visitInvokeDynamicInsn( "test", "()Ljava/util/function/Predicate;",
                        new Handle( Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;" ),
                        new Object[] { Type.getType( "(Ljava/lang/Object;)Z" ),
                                        new Handle( Opcodes.H_INVOKESTATIC, gameType.getInternalName(), "lambda$" + lambda,
                                                        "(L" + Type.getType( VGDLSprite.class ).getInternalName() + ";)Z" ),
                                        Type.getType( "(L" + Type.getType( VGDLSprite.class ).getInternalName() + ";)Z" ) } );
        ga.visitMethodInsn( INVOKEINTERFACE, "java/util/stream/Stream", "filter", "(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", true );
        ga.visitMethodInsn( INVOKEINTERFACE, "java/util/stream/Stream", "count", "()J", true );

        ga.cast( Type.LONG_TYPE, Type.INT_TYPE );
        ga.push( limit );
        final Label t = ga.newLabel();
        final Label e = ga.newLabel();
        ga.visitJumpInsn( IF_ICMPEQ, t );
        ga.push( false );
        ga.goTo( e );
        ga.mark( t );
//        //TODO this simply sets the score, not increment it
//        ga.loadThis();
//        ga.push( 1.0 );
//        ga.invokeVirtual( gameType, Method.getMethod( "void setScore(double)" ) );
        ga.push( true );
        ga.mark( e );

        final MethodVisitor lambdaWriter = cw.visitMethod( ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "lambda$" + lambda,
                        "(L" + Type.getType( VGDLSprite.class ).getInternalName() + ";)Z", null, null );
        lambdaWriter.visitCode();
        lambdaWriter.visitVarInsn( ALOAD, 0 );
        lambdaWriter.visitTypeInsn( INSTANCEOF, context.getGamePackageName() + "/" + VGDLCompiler.formatClassName( stype ) );
        lambdaWriter.visitInsn( IRETURN );
        lambdaWriter.visitEnd();
        lambdaWriter.visitMaxs( 1, 0 );
        //TODO scores
    }

}
