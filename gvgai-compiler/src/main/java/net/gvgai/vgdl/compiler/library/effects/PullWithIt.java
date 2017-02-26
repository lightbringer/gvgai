package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;

public class PullWithIt extends BaseEffect {

    public PullWithIt( Type my, Type[] other, String[] parameters ) {
        super( my, other, parameters );
        // TODO Auto-generated constructor stub
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, requiredFeatures, mg );

        //XXX
        VGDLCompiler.generateConsoleMessage( mg, "PullWithIt called" );
    }

}
