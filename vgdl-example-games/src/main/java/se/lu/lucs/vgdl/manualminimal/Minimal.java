package se.lu.lucs.vgdl.manualminimal;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.game.BasicGame;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class Minimal extends BasicGame {

    @Override
    public Class<? extends VGDLSprite>[] getMappedSpriteClass( char c ) {
        switch (c) {
            case 'w':
                return new Class[] { Wall.class };
            case 'a':
                return new Class[] { Avatar.class };
            case 'g':
                return new Class[] { Goal.class };
            case 'l':
                return new Class[] { Obstacle.class };
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public Feature[] getRequiredFeatures() {
        return new Feature[0];
    }

    @Override
    public boolean isGameOver() {
        return getGameState().values().filter( s -> s instanceof Avatar ).count() == 0;
    }

}
