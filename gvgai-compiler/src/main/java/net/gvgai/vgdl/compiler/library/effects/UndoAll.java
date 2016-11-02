package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.game.VGDLSprite;

public class UndoAll implements Effect {
    public UndoAll( Type myType, Type otherType ) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
//        VGDLCompiler.generateConsoleMessage( mg, "UndoAll" );
        final Method reset = Method.getMethod( "void resetAll()" );
        mg.loadThis();
        mg.invokeVirtual( Type.getType( VGDLSprite.class ), reset );

    }

}
