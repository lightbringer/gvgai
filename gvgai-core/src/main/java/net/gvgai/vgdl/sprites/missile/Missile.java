package net.gvgai.vgdl.sprites.missile;

import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.sprites.Passive;
import net.gvgai.vgdl.sprites.VGDLSprite;

public abstract class Missile extends Passive {
    protected double speed;
    private double blockDistance;
    private double preFrameBlockDistance;
    private double preFrameSpeed;

    @Override
    public void preFrame() {
        super.preFrame();

        preFrameBlockDistance = blockDistance;
        preFrameSpeed = speed;

    }

    @Override
    public void reset() {
        super.reset();

        blockDistance = preFrameBlockDistance;
        speed = preFrameSpeed;
    }

    @Override
    public void update( GameState state, double seconds ) {
        super.update( state, seconds );

        blockDistance += speed;

        if (blockDistance >= 1.0) {
            blockDistance = 0;
            move( state.getLevel(), getDirection() );
        }
    }

    @Override
    protected void setup( VGDLSprite s, GameMap map ) {
        super.setup( s, map );

        final Missile m = (Missile) s;
        m.speed = speed;
        m.blockDistance = blockDistance;
    }

}
