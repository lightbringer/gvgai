package net.gvgai.vgdl;

import java.io.InputStream;

import net.gvgai.vgdl.game.VGDLGame;

public interface VGDLRuntime {
    public enum Feature {
        PRE_BUFFER_FRAME, DISCRETE_GAME, OBEY_END_OF_BOUNDARIES
    }

    void loadGame( Class<? extends VGDLGame> gameClass );

    void loadLevel( InputStream s );

    void run();
}
