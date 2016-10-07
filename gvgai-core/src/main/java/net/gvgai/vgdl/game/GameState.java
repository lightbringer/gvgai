package net.gvgai.vgdl.game;

import java.util.Collection;

public interface GameState {

    boolean forward( VGDLSprite s );

    MovingAvatar getAvatar();

    int getSpriteCount( Class<? extends VGDLSprite> clazz );

    Collection<VGDLSprite> getSpritesAt( Object pos );

    boolean isReady();

    boolean move( VGDLSprite s, Object direction );

    /**
     * Moves a sprite one unit south (resp. down)
     * @param s
     * @return
     */
    boolean moveDown( VGDLSprite s );

    /**
     * Moves a sprite one unit west (resp. left)
     * @param s
     * @return
     */
    boolean moveLeft( VGDLSprite s );

    /**
     * Moves a sprite one unit east (resp. right)
     * @param s
     * @return
     */
    boolean moveRight( VGDLSprite s );

    /**
     * Moves a sprite one unit north (resp. up)
     * @param s
     * @return
     */
    boolean moveUp( VGDLSprite s );

    void postFrame();

    void preFrame();

    void resetFrame();

    void reverse( VGDLSprite s );

    void setAvatar( MovingAvatar a );
}
