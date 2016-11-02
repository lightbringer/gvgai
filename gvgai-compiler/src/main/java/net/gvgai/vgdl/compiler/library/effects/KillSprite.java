package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.game.VGDLSprite;

public class KillSprite implements Effect, Opcodes {
    private final Type myType;

    public KillSprite( Type myType, Type otherType ) {
        this.myType = myType;
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
//        VGDLCompiler.generateConsoleMessage( mg, "Killed" );
        mg.loadThis();
        final Method m1 = Method.getMethod( "void kill()" );
        mg.invokeVirtual( Type.getType( VGDLSprite.class ), m1 );
    }

}
