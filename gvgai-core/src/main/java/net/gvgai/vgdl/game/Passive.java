package net.gvgai.vgdl.game;

import java.util.function.Function;

import net.gvgai.vgdl.tools.AutoWire;

public abstract class Passive extends Immovable {
    @AutoWire
    Function<Object, Object> reverse;

    public void move( Object direction ) {
        setDirection( direction );
        map.move( this, direction );

    }

    public Object reverseDirection() {
        return reverse.apply( getDirection() );
    }

    @Override
    protected void setup( VGDLSprite s ) {
        super.setup( s );

        final Passive p = (Passive) s;
        p.reverse = reverse;
    }

}
