package net.gvgai.vgdl.game;

import java.util.stream.Stream;

import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.VGDLSprite;

public interface GameState<T> extends Copyable<T> {

    MovingAvatar getAvatar();

    GameMap getLevel();

    double getScore();

    boolean isGameOver();

    boolean isReady();

    void postFrame();

    void preFrame();

    void resetFrame();

    void setAvatar( MovingAvatar a );

    void setGameOver( boolean b );

    void setScore( double d );

    void update( double seconds );

    Stream<VGDLSprite> values();
}
