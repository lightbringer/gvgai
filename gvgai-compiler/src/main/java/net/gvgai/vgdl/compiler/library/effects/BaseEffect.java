package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;
import net.gvgai.vgdl.game.GameState;

public abstract class BaseEffect implements Effect, Opcodes {
    protected final Type myType;
    protected final Type otherType;

    private double scoreChange;

    public BaseEffect( Type my, Type other, String... parameters ) {
        myType = my;
        otherType = other;

        if (parameters != null) {
            for (int i = 0; i < parameters.length; i += 2) {
                switch (parameters[i]) {
                    case "scoreChange":
                        scoreChange = Double.parseDouble( parameters[i + 1] );
                        break;
                    default:
                        System.err.println( "Warning: ignoring unknown option \"" + parameters[i] + "=" + parameters[i + 1] + "\"" );
                        break;
                }
            }
        }
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
        if (scoreChange != 0) {
            VGDLCompiler.getGameState( mg );
            mg.dup();
            final Method m = Method.getMethod( "double getScore()" );
            mg.invokeInterface( Type.getType( GameState.class ), m );
            mg.push( scoreChange );
            mg.math( GeneratorAdapter.ADD, Type.DOUBLE_TYPE );
            final Method m2 = Method.getMethod( "void setScore(double)" );
            mg.invokeInterface( Type.getType( GameState.class ), m2 );
        }
    }

}
