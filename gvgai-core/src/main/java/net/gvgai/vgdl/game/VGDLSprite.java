package net.gvgai.vgdl.game;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class VGDLSprite implements Copyable<VGDLSprite> {

    //Direction and position need to be immutable
    private Object position;
    private Object preFramePosition;
    private Object direction;
    private Object preFrameDirection;

    Consumer<VGDLSprite> kill;
    Supplier<Void> resetAll;

    protected VGDLSprite() {

    }

    public <T extends VGDLSprite> void collide( T other ) {
        //NOP
    }

    @Override
    public abstract VGDLSprite copy();

    public Object getDirection() {
        return direction;
    }

    public Object getPosition() {
        return position;
    }

    public void kill() {
        kill.accept( this );
    }

    public void postFrame() {
        //NOP
    }

    public void preFrame() {
        preFrameDirection = direction;
        preFramePosition = position;
    }

    public void reset() {
        direction = preFrameDirection;
        position = preFramePosition;

//        assert position != null;
    }

    public void resetAll() {
        resetAll.get();
    }

    public void setDirection( Object d ) {
        assert d != null;
        direction = d;
    }

    public void setPosition( Object p ) {
        assert p != null;
        position = p;
    }

    public void update( float seconds ) {
        //NOP
    }

    protected void setup( VGDLSprite s ) {
        s.direction = direction;
        s.position = position;
        s.kill = kill;
        s.direction = direction;
    }
}
