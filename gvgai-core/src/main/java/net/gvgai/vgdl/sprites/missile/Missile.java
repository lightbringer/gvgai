package net.gvgai.vgdl.sprites.missile;

import net.gvgai.vgdl.input.Action;
import net.gvgai.vgdl.sprites.Passive;

public abstract class Missile extends Passive {
    public enum Orientation {
        LEFT, RIGHT, UP, DOWN
    }

    protected Orientation orientation;
    private Orientation preFrameOrientation;
    protected double speed;
    private double blockDistance;
    private double preFrameBlockDistance;

    private Action frameAction;

    @Override
    public void preFrame() {
        super.preFrame();

        preFrameBlockDistance = blockDistance;
        preFrameOrientation = orientation;

        blockDistance += speed;
        if (blockDistance >= 1.0) {

            switch (orientation) {
                case DOWN:
                    frameAction = Action.ACTION_DOWN;
                    break;
                case LEFT:
                    frameAction = Action.ACTION_LEFT;
                    break;
                case RIGHT:
                    frameAction = Action.ACTION_RIGHT;
                    break;
                case UP:
                    frameAction = Action.ACTION_UP;
                    break;

            }

            blockDistance = 0;
        }

    }

    @Override
    public void reset() {
        super.reset();

        blockDistance = preFrameBlockDistance;
        orientation = preFrameOrientation;
        frameAction = null;
    }

    @Override
    public void update( double seconds ) {
        super.update( seconds );

        if (frameAction != null) {
            move( frameAction );
            frameAction = null;
        }
    }

}
