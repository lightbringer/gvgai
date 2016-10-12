package net.gvgai.vgdl.game;

import net.gvgai.vgdl.input.Action;

public abstract class MovingAvatar extends Passive {

    public void act( Action a ) throws VGDLException {
        switch (a) {
            case ACTION_UP:
            case ACTION_LEFT:
            case ACTION_RIGHT:
            case ACTION_DOWN:
                move( a );
                break;
            case ACTION_ESCAPE:
            case ACTION_NIL:
            case ACTION_USE:
            default:
                break;

        }

    }

    public void stepBack() {
        move( reverse.apply( direction ) );
    }
}
