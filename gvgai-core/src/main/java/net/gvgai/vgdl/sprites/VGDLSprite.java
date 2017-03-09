package net.gvgai.vgdl.sprites;

import java.util.function.Supplier;

import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.tools.AutoWire;

public abstract class VGDLSprite {

    @AutoWire
    public static Supplier<Object> RightDirection;
    @AutoWire
    public static Supplier<Object> LeftDirection;
    @AutoWire
    public static Supplier<Object> UpDirection;
    @AutoWire
    public static Supplier<Object> DownDirection;

    //Direction and position need to be immutable
    private Object position;

    private Object preFramePosition;

    private Object direction;
    private Object preFrameDirection;

    private int id;

    protected VGDLSprite() {
        id = hashCode();
        direction = UpDirection.get();
    }

    /**
     * Collides this sprite with others. Others are expected to be ordered ascending by their class id
     * @param state TODO
     * @param map TODO
     * @param others
     */
    public void collide( GameState state, VGDLSprite... others ) {
        //NOP
    }

    public abstract VGDLSprite copy( GameMap m );

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

    public void kill( GameState state ) {
        state.getLevel().remove( position, this );
    }

    public void OnOutOfBounds( GameState state ) {
//        //Set the sprite back to its original position without triggering a new collision
//        map.set( position, this );

//        throw new IllegalStateException( "FIXME: no default policy for out of bound sprites" );
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

    public void setDirection( Object d ) {
        assert d != null;
        direction = d;
    }

    public void setPosition( Object p ) {
        assert p != null;
        position = p;
    }

    public void update( GameState state, double seconds ) {
        //NOP
    }

    protected void setup( VGDLSprite s, GameMap map ) {
        s.id = id;
        s.direction = direction;
        s.position = position;
    }
}
