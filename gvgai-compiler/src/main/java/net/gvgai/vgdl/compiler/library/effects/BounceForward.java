package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;

public class BounceForward extends BaseEffect {

    public BounceForward( VGDLCompiler context, Type my, Type[] others, String... parameters ) {
        super( context, my, others, parameters );

    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, requiredFeatures, mg );
        vgdlCompiler.generateLogMessage( myType.getClassName(), mg, "Bounce forward" );

        //Move object 0 (this) into the direction that 1 (0) is facing
        mg.loadThis();
        mg.loadArg( 0 );
        final Method getDir = Method.getMethod( "Object getDirection( )" );
        mg.invokeVirtual( otherTypes[0], getDir );
        final Method move = Method.getMethod( "void move(Object)" );
        mg.invokeVirtual( myType, move );
    }

}
