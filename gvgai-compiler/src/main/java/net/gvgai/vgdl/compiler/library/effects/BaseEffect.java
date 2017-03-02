package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.game.GameState;

public abstract class BaseEffect implements Effect, Opcodes {
    protected final Type myType;
    protected final Type[] otherTypes;

    private double scoreChange;

    protected BaseEffect( VGDLCompiler context, Type my, Type[] others, String... parameters ) {
        myType = my;
        otherTypes = others;

        if (parameters != null) {
            for (int i = 0; i < parameters.length; i += 2) {
                switch (parameters[i]) {
                    case "scoreChange":
                        scoreChange = Double.parseDouble( parameters[i + 1] );
                        break;
                    default:
//                        Logger.getLogger( getClass().getName() ).warning( "Ignoring unknown option " + parameters[i]  + "=" + parameters[i + 1] );

                        break;
                }
            }
        }
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        if (scoreChange != 0) {
            VGDLCompiler.generateGetGameStat( mg );
            mg.dup();
            final Method m = Method.getMethod( "double getScore()" );
            mg.invokeInterface( Type.getType( GameState.class ), m );
            mg.push( scoreChange );
            mg.math( GeneratorAdapter.ADD, Type.DOUBLE_TYPE );
            final Method m2 = Method.getMethod( "void setScore(double)" );
            mg.invokeInterface( Type.getType( GameState.class ), m2 );
        }
    }

    @Override
    public Type getMyType() {
        return myType;
    }

    @Override
    public Type[] getOtherTypes() {
        return otherTypes;
    }

    @Override
    public void postGeneration( VGDLCompiler vgdlCompiler, GeneratorAdapter method, Set<Feature> requiredFeatures, Effect ef ) {
        //NOP

    }

    @Override
    public void preGeneration( VGDLCompiler vgdlCompiler, GeneratorAdapter method, Set<Feature> requiredFeatures, Effect ef ) {
        //NOP

    }

}
