package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class KillSprite extends BaseEffect {

    public KillSprite( Type myType, Type[] otherType, String... s ) {
        super( myType, otherType, s );
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, requiredFeatures, mg );
//        VGDLCompiler.generateConsoleMessage( mg, "Killed" );
        mg.loadThis();
        final Method m1 = Method.getMethod( "void kill()" );
        mg.invokeVirtual( Type.getType( VGDLSprite.class ), m1 );
    }

}
