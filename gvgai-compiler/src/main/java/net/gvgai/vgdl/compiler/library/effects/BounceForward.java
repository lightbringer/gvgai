package net.gvgai.vgdl.compiler.library.effects;

import java.util.function.Consumer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;

public class BounceForward implements Effect, Opcodes {
    private final Type myType;

    public BounceForward( Type... myType ) {
        this.myType = myType[0];
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
        VGDLCompiler.generateConsoleMessage( mg, "Bounce forward" );
        mg.loadThis();
        mg.visitFieldInsn( GETFIELD, myType.getInternalName(), "forward", "Ljava/util/function/Consumer;" );
        mg.loadThis();
        final Method m2 = Method.getMethod( "void accept(Object)" );
        mg.invokeInterface( Type.getType( Consumer.class ), m2 );

    }

}
