package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;

public class StepBack implements Effect, Opcodes {
    private final Type myType;

    public StepBack( Type myType, Type otherType ) {
        super();
        this.myType = myType;
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
        VGDLCompiler.generateConsoleMessage( mg, "Step back!" );
        mg.loadThis();
        mg.loadThis();
        mg.visitFieldInsn( GETFIELD, myType.getInternalName(), "reverse", "Ljava/util/function/Function;" );
        mg.loadThis();
        mg.visitFieldInsn( GETFIELD, myType.getInternalName(), "direction", "Ljava/lang/Object;" );
        mg.visitMethodInsn( INVOKEINTERFACE, "java/util/function/Function", "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", true );
        mg.visitMethodInsn( INVOKEVIRTUAL, myType.getInternalName(), "move", "(Ljava/lang/Object;)V", false );
    }

}
