package net.gvgai.vgdl.sprites.missile;

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
    public void update( double seconds ) {
        super.update( seconds );

        blockDistance += speed;

        if (blockDistance >= 1.0) {
            blockDistance = 0;
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
