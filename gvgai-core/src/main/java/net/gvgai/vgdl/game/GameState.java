package net.gvgai.vgdl.game;

import java.util.stream.Stream;

public interface GameState<T> extends Copyable<T> {

    MovingAvatar getAvatar();

    double getScore();

    boolean isGameOver();

    boolean isReady();

    void postFrame();

    void preFrame();

    void resetFrame();

    void setAvatar( MovingAvatar a );

    void setGameOver( boolean b );

    void setScore( double d );

    Stream<VGDLSprite> values();
}
