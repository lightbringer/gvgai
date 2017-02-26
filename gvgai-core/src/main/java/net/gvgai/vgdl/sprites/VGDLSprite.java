package net.gvgai.vgdl.sprites;

import net.gvgai.vgdl.game.Copyable;
import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.game.GameState;
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

    private int id;

    protected VGDLSprite() {
        id = hashCode();
    }

    /**
     * Collides this sprite with others. Others are expected to be ordered ascending by their class id
     * @param others
     */
    public void collide( VGDLSprite... others ) {
        //NOP
    }

    @Override
    public abstract VGDLSprite copy();

    /**
     * Class ids are only unique locally, i.e. not across games
     * @return
     */
    public abstract int getClassId();

    public Object getDirection() {
        return direction;
    }

    public int getId() {
        return id;
    }

    public Object getPosition() {
        return position;
    }

    public void kill() {
        map.remove( position, this );
    }

    public boolean OnOutOfBounds() {
        return false;
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

    public void setMap( GameMap map ) {
        this.map = map;
    }

    public void setPosition( Object p ) {
        assert p != null;
        position = p;
    }

    public void setState( GameState state ) {
        this.state = state;
    }

    public void update( double seconds ) {
        //NOP
    }

    protected void setup( VGDLSprite s ) {
        s.id = id;
        s.direction = direction;
        s.position = position;
    }
}
