package net.gvgai.vgdl.sprites;

import java.util.function.Function;

import net.gvgai.vgdl.tools.AutoWire;

public abstract class Passive extends Immovable {
    @AutoWire
    private Function<Object, Object> reverse;

    public void move( Object direction ) {
        setDirection( direction );
        map.move( this, direction );

    }

    public Object reverseDirection() {
        return reverse.apply( getDirection() );
    }

    public void setReverse( Function<Object, Object> reverse ) {
        this.reverse = reverse;
    }

    @Override
    protected void setup( VGDLSprite s ) {
        super.setup( s );

        final Passive p = (Passive) s;
        p.setReverse( reverse );
    }

}
