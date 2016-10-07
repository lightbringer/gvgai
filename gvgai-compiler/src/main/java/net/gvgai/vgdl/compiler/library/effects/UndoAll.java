package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.game.VGDLException;

public class UndoAll implements Effect {
    public UndoAll( Type myType, Type otherType ) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
        VGDLCompiler.generateConsoleMessage( mg, "UndoAll" );
        mg.throwException( Type.getType( VGDLException.class ), "UndoAll" );

    }

}
