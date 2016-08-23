package net.gvgai.vgdl.compiler.library.effects;

import java.util.function.BiConsumer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.game.VGDLSprite;

public class BounceForward implements Effect, Opcodes {
    private final Type myType;

    public BounceForward( Type myType, Type otherType ) {
        System.out.println( "Bounce " + myType );
        this.myType = myType;
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
        VGDLCompiler.generateConsoleMessage( mg, "Bounce forward" );
//        mg.loadThis();
//        mg.visitFieldInsn( GETFIELD, myType.getInternalName(), "forward", "Ljava/util/function/Consumer;" );
//        mg.loadThis();
//        final Method m2 = Method.getMethod( "void accept(Object)" );
//        mg.invokeInterface( Type.getType( Consumer.class ), m2 );

        mg.loadThis();
        mg.visitFieldInsn( GETFIELD, myType.getInternalName(), "move", "Ljava/util/function/BiConsumer;" );
        mg.loadThis();
        mg.loadArg( 0 );
        final Method getDir = Method.getMethod( "Object getDirection( )" );
        mg.invokeVirtual( Type.getType( VGDLSprite.class ), getDir );
        final Method m2 = Method.getMethod( "void accept(Object, Object)" );
        mg.invokeInterface( Type.getType( BiConsumer.class ), m2 );
    }

}
