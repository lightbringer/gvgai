package net.gvgai.vgdl.game;

import net.gvgai.vgdl.tools.AutoWire;

public abstract class VGDLSprite implements Copyable<VGDLSprite> {

    //Direction and position need to be immutable
    private Object position;
    private Object preFramePosition;
    private Object direction;
    private Object preFrameDirection;

    @AutoWire
    protected GameState state;
    @AutoWire
    protected GameMap map;

    int id;

    protected VGDLSprite() {
        id = hashCode();
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
        map.remove( position, this );
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
        state.resetFrame();
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
        s.id = id;
        s.direction = direction;
        s.position = position;
    }
}
