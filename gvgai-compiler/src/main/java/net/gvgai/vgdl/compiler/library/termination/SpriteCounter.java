package net.gvgai.vgdl.compiler.library.termination;

import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Termination;

public class SpriteCounter implements Termination, Opcodes {
    private String stype;
    private int limit;
    private String win;

    @Override
    public void generate( VGDLCompiler context, Type gameType, Set<Feature> requiredFeatures, ClassWriter cw, GeneratorAdapter ga ) {
        if (!requiredFeatures.contains( Feature.GET_SPRITE_COUNT )) {
            final FieldVisitor fw = cw.visitField( ACC_PUBLIC, "countSprites", "Ljava/util/function/Function;",
                            "Ljava/util/function/Function<Class<? extends Lnet/gvgai/vgdl/VGDLSprite, Integer>;", null );
//            fw.visitAnnotation( Type.getDescriptor( AutoWire.class ), true );
            requiredFeatures.add( Feature.GET_SPRITE_COUNT );
        }

        ga.loadThis();
        ga.visitMethodInsn( INVOKEVIRTUAL, gameType.getInternalName(), "getGameState", "()Lnet/gvgai/vgdl/game/GameState;", false );
        ga.visitMethodInsn( INVOKEINTERFACE, "net/gvgai/vgdl/game/GameState", "values", "()Ljava/util/stream/Stream;", true );
        ga.visitInvokeDynamicInsn( "test", "()Ljava/util/function/Predicate;",
                        new Handle( Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;" ),
                        new Object[] { Type.getType( "(Ljava/lang/Object;)Z" ),
                                        new Handle( Opcodes.H_INVOKESTATIC, gameType.getInternalName(), "lambda$0", "(Lnet/gvgai/vgdl/game/VGDLSprite;)Z" ),
                                        Type.getType( "(Lnet/gvgai/vgdl/game/VGDLSprite;)Z" ) } );
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
        //TODO this simply sets the score, not increment it
        ga.loadThis();
        ga.push( 1.0 );
        ga.invokeVirtual( gameType, Method.getMethod( "void setScore(double)" ) );
        ga.push( true );
        ga.mark( e );

        final MethodVisitor lambdaWriter = cw.visitMethod( ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "lambda$" + context.nextLambda(),
                        "(Lnet/gvgai/vgdl/game/VGDLSprite;)Z", null, null );
        lambdaWriter.visitCode();
        lambdaWriter.visitVarInsn( ALOAD, 0 );
        lambdaWriter.visitTypeInsn( INSTANCEOF, VGDLCompiler.PACKAGE + "/" + VGDLCompiler.formatClassName( stype ) );
        lambdaWriter.visitInsn( IRETURN );
        lambdaWriter.visitEnd();
        lambdaWriter.visitMaxs( 1, 0 );
        //TODO scores
    }

}
