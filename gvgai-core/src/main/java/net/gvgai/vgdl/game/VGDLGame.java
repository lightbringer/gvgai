package net.gvgai.vgdl.game;

import net.gvgai.vgdl.VGDLRuntime.Feature;

public interface VGDLGame {
    GameState getGameState();

    Class<? extends VGDLSprite>[] getMappedSpriteClass( char c );

    Feature[] getRequiredFeatures();

    double getScore();

    boolean isGameOver();

    void postFrame();

    void preFrame();

    void resetFrame();

    void setGameState( GameState state );

    void setScore( double s );

    void update( double seconds );
}
