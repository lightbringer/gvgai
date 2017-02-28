package net.gvgai.vgdl.sprites;

import net.gvgai.vgdl.input.Action;

public abstract class MovingAvatar extends Passive {

    public void act( Action a ) {
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
        move( reverseDirection() );
    }
}