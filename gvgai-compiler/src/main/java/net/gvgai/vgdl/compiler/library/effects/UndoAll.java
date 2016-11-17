package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.game.VGDLSprite;

public class UndoAll extends BaseEffect {
    public UndoAll( Type myType, Type otherType, String... s ) {
        super( myType, otherType, s );
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, mg );
//        VGDLCompiler.generateConsoleMessage( mg, "UndoAll" );
        final Method reset = Method.getMethod( "void resetAll()" );
        mg.loadThis();
        mg.invokeVirtual( Type.getType( VGDLSprite.class ), reset );

    }

}
