package net.gvgai.vgdl.game;

import java.util.function.Consumer;

public abstract class VGDLSprite {

    protected Object direction;
    protected Object frameStartDirection;

    protected Object position;
    protected Object frameStartPosition;

    protected Consumer<VGDLSprite> kill;

    protected VGDLSprite() {

    }

    public <T extends VGDLSprite> void collide( T other ) {
        System.out.println( "no interaction defined for " + getClass() + "+" + other.getClass() );
    }

    public Object getDirection() {
        return direction;
    }

    public Object getPosition() {
        return position;
    }

    public void postFrame() {

    }

    public void preFrame() {
        frameStartDirection = direction;
        frameStartPosition = position;
    }

    public void reset() {
        direction = frameStartDirection;
        position = frameStartPosition;

    }

    public void setDirection( Object direction ) {
        this.direction = direction;
    }

    public void setPosition( Object pos ) {
        position = pos;

    }

    public void update( float seconds ) {

    }
}
