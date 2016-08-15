package net.gvgai.vgdl.game;

import net.gvgai.vgdl.VGDLRuntime.Feature;

public interface VGDLGame {
    Class<? extends VGDLSprite>[] getMappedSpriteClass( char c );

    Feature[] getRequiredFeatures();

    boolean isGameOver();

    void update( double seconds );
}
