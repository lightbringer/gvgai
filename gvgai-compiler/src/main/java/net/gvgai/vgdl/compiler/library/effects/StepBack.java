package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.sprites.Passive;

public class StepBack extends BaseEffect {

    public StepBack( Type myType, Type[] otherType, String... s ) {
        super( myType, otherType, s );

    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, requiredFeatures, mg );
        VGDLCompiler.generateLogMessage( myType.getClassName(), mg, "Step back!" );
        mg.loadThis();
        mg.loadThis();
        final Method getDir = Method.getMethod( "Object reverseDirection( )" );
        mg.invokeVirtual( Type.getType( Passive.class ), getDir );
        final Method move = Method.getMethod( "void move(Object)" );
        mg.invokeVirtual( Type.getType( Passive.class ), move );
    }

}
