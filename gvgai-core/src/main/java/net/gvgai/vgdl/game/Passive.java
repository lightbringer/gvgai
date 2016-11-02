package net.gvgai.vgdl.game;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Passive extends Immovable {
    Function<Object, Object> reverse;
    BiConsumer<VGDLSprite, Object> move;

    public void move( Object direction ) {
        setDirection( direction );
        move.accept( this, direction );

    }

    public Object reverseDirection() {
        return reverse.apply( getDirection() );
    }

    @Override
    protected void setup( VGDLSprite s ) {
        super.setup( s );

        final Passive p = (Passive) s;
        p.reverse = reverse;
        p.move = move;
    }

}
