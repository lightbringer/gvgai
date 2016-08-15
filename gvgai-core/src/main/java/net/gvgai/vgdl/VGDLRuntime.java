package net.gvgai.vgdl;

import java.io.InputStream;

import net.gvgai.vgdl.game.VGDLGame;

public interface VGDLRuntime {
    public enum Feature {
        GET_SPRITE_COUNT, DISCRETE_GAME
    }

    void loadGame( Class<? extends VGDLGame> gameClass );

    void loadLevel( InputStream s );

    void run();
}
