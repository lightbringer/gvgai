package net.gvgai.vgdl.game;

import java.util.Collection;
import java.util.stream.Stream;

public interface GameState {

    GameState advanceFrame();

    boolean forward( VGDLSprite s );

    Stream<VGDLSprite> getAllSprites();

    MovingAvatar getAvatar();

    double getScore();

    int getSpriteCount( Class<? extends VGDLSprite> clazz );

    Collection<VGDLSprite> getSpritesAt( Object pos );

    boolean isReady();

    boolean move( VGDLSprite s, Object direction );

    void postFrame();

    void preFrame();

    void remove( VGDLSprite s );

    void resetFrame();

    void setAvatar( MovingAvatar a );

    void setScore( double d );

}
