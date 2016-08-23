package net.gvgai.vgdl.game;

import java.util.function.Function;

import net.gvgai.vgdl.AutoWire;

public abstract class VGDLSprite {
//    @AutoWire
//    private Function<Object, VGDLSprite> getPosition;

    @AutoWire
    private Function<Object, VGDLSprite> getDirection;

    public <T extends VGDLSprite> void collide( T other ) {
        System.out.println( "no interaction defined for " + getClass() + "+" + other.getClass() );
    }

    public Object getDirection() {
        return getDirection.apply( this );
    }

//    public Object getPosition() {
//        return getPosition.apply( this );
//    }

    public void update( float seconds ) {

    }
}
