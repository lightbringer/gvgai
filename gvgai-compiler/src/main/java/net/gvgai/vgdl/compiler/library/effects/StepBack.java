package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.sprites.Passive;

public class StepBack extends BaseEffect {

    public StepBack( VGDLCompiler context, Type my, Type[] others, String... parameters ) {
        super( context, my, others, parameters );

    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, requiredFeatures, mg );
        vgdlCompiler.generateLogMessage( myType.getClassName(), mg, "Step back!" );
        mg.loadThis();
        mg.loadArg( 0 );
        mg.loadThis();
        final Method getDir = Method.getMethod( "Object reverseDirection( )" );
        mg.invokeVirtual( Type.getType( Passive.class ), getDir );
        final Method move = Method.getMethod( "void move(" + GameMap.class.getName() + ", Object)" );
        mg.invokeVirtual( Type.getType( Passive.class ), move );
    }

}
