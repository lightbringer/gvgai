package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.game.Passive;

public class StepBack implements Effect, Opcodes {
    private final Type myType;

    public StepBack( Type myType, Type otherType ) {
        super();
        this.myType = myType;
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
//        VGDLCompiler.generateConsoleMessage( mg, "Step back!" );
        mg.loadThis();
        mg.loadThis();
        final Method getDir = Method.getMethod( "Object reverseDirection( )" );
        mg.invokeVirtual( Type.getType( Passive.class ), getDir );
        final Method move = Method.getMethod( "void move(Object)" );
        mg.invokeVirtual( Type.getType( Passive.class ), move );
    }

}
