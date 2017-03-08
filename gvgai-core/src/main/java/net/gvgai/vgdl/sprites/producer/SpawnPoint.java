package net.gvgai.vgdl.sprites.producer;

import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.sprites.VGDLSprite;
import net.gvgai.vgdl.tools.AutoWire;

public abstract class SpawnPoint extends VGDLSprite {

    @AutoWire
    protected double prob;

    @AutoWire
    protected float cooldown;

    private float tick;

    public abstract VGDLSprite createNewSprite();

    @Override
    public void update( GameState state, double seconds ) {
        super.update( state, seconds );
        tick += seconds;

        if (tick >= cooldown && Math.random() < prob) {
            final VGDLSprite s = createNewSprite();
            final GameMap map = state.getLevel();
            final Object newpos = map.add( getPosition(), s.getDirection() );
            map.set( newpos, s );
            tick = 0f;
        }

    }

}
