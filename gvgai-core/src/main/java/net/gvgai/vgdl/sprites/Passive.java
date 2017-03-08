package net.gvgai.vgdl.sprites;

import java.util.function.Function;

import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.tools.AutoWire;

public abstract class Passive extends Immovable {
    @AutoWire
    public static Function<Object, Object> reverse;

    public void move( GameMap map, Object direction ) {
        setDirection( direction );
        map.move( this, direction );

    }

    public Object reverseDirection() {
        return reverse.apply( getDirection() );
    }

}
