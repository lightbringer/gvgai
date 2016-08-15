package net.gvgai.vgdl.game;

public abstract class VGDLSprite {
    public <T extends VGDLSprite> void collide( T other ) {
        System.out.println( "no interaction defined for " + getClass() + "+" + other.getClass() );
    }

    public abstract int getClassId();

    public void update( float seconds ) {

    }
}
