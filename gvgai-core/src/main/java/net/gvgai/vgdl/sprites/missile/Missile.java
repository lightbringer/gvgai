package net.gvgai.vgdl.sprites.missile;

import net.gvgai.vgdl.sprites.Passive;
import net.gvgai.vgdl.sprites.VGDLSprite;

public abstract class Missile extends Passive {
    protected double speed;
    private double blockDistance;
    private double preFrameBlockDistance;

    private boolean frameAction;

    @Override
    public void preFrame() {
        super.preFrame();

        preFrameBlockDistance = blockDistance;

        blockDistance += speed;

        if (blockDistance >= 1.0) {
            frameAction = true;

            blockDistance = 0;
        }

    }

    @Override
    public void reset() {
        super.reset();

        blockDistance = preFrameBlockDistance;
        frameAction = false;
    }

    @Override
    public void update( double seconds ) {
        super.update( seconds );

        if (frameAction) {
            move( getDirection() );

        }
    }

    @Override
    protected void setup( VGDLSprite s ) {
        super.setup( s );

        final Missile m = (Missile) s;
        m.speed = speed;
        m.blockDistance = blockDistance;
    }

}
