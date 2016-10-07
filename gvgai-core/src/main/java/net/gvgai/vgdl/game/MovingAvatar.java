package net.gvgai.vgdl.game;

import net.gvgai.vgdl.input.Action;

public abstract class MovingAvatar extends Passive {

    public void act( Action a ) throws VGDLException {
        switch (a) {
            case ACTION_DOWN:
                moveDown();
                break;
            case ACTION_ESCAPE:
                break;
            case ACTION_LEFT:
                moveLeft();
                break;
            case ACTION_NIL:
                break;
            case ACTION_RIGHT:
                moveRight();
                break;
            case ACTION_UP:
                moveUp();
                break;
            case ACTION_USE:
                break;
            default:
                break;

        }

    }

}
