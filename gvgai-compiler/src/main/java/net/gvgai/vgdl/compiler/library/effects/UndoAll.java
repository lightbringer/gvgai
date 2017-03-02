package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class UndoAll extends BaseEffect {

    public UndoAll( VGDLCompiler context, Type my, Type[] others, String... parameters ) {
        super( context, my, others, parameters );
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        if (!requiredFeatures.contains( Feature.PRE_BUFFER_FRAME )) {
            requiredFeatures.add( Feature.PRE_BUFFER_FRAME );
        }
        super.generate( vgdlCompiler, requiredFeatures, mg );
//        VGDLCompiler.generateConsoleMessage( mg, "UndoAll" );
        final Method reset = Method.getMethod( "void resetAll()" );
        mg.loadThis();
        mg.invokeVirtual( Type.getType( VGDLSprite.class ), reset );

    }

}
