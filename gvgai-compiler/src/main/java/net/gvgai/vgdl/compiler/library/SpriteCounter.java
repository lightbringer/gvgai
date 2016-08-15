package net.gvgai.vgdl.compiler.library;

import java.util.Set;
import java.util.function.Function;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.AutoWire;
import net.gvgai.vgdl.VGDLRuntime.Feature;

public class SpriteCounter implements Termination, Opcodes {
    private String stype;
    private int limit;
    private String win;

    @Override
    public void generate( String gameName, Set<Feature> requiredFeatures, ClassWriter cw, GeneratorAdapter ga ) {
        if (!requiredFeatures.contains( Feature.GET_SPRITE_COUNT )) {
            final FieldVisitor fw = cw.visitField( ACC_PUBLIC, "countSprites", "Ljava/util/function/Function;",
                            "Ljava/util/function/Function<Class<? extends Lnet/gvgai/vgdl/VGDLSprite, Integer>;", null );
            fw.visitAnnotation( Type.getDescriptor( AutoWire.class ), true );
            requiredFeatures.add( Feature.GET_SPRITE_COUNT );
        }

        ga.loadThis();
        ga.visitFieldInsn( GETFIELD, gameName, "countSprites", "Ljava/util/function/Function;" );
        ga.invokeStatic( Type.getType( Thread.class ), Method.getMethod( "Thread currentThread()" ) );
        ga.invokeVirtual( Type.getType( Thread.class ), Method.getMethod( "ClassLoader getContextClassLoader()" ) );
        ga.push( stype );
        ga.invokeVirtual( Type.getType( ClassLoader.class ), Method.getMethod( "Class loadClass(String)" ) );
        final Method m = Method.getMethod( "Object apply(Object)" );
        ga.invokeInterface( Type.getType( Function.class ), m );
        ga.unbox( Type.INT_TYPE );
        ga.push( limit );
        final Label t = ga.newLabel();
        final Label e = ga.newLabel();
        ga.visitJumpInsn( IF_ICMPEQ, t );
        ga.push( false );
        ga.goTo( e );
        ga.mark( t );
        ga.push( true );
        ga.mark( e );

        //TODO scores
    }

    private boolean test( int a, int b ) {
        return a == b;
    }

}
