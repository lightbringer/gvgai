package net.gvgai.vgdl.compiler.library.effects;

import java.util.function.Consumer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;

public class KillSprite implements Effect, Opcodes {
    private final Type myType;

    public KillSprite( Type myType, Type otherType ) {
        this.myType = myType;
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
        VGDLCompiler.generateConsoleMessage( mg, "Killed" );
        mg.loadThis();
        mg.visitFieldInsn( GETFIELD, myType.getInternalName(), "kill", "Ljava/util/function/Consumer;" );
        mg.loadThis();
        final Method m1 = Method.getMethod( "void accept(Object)" );
        mg.invokeInterface( Type.getType( Consumer.class ), m1 );
    }

}
