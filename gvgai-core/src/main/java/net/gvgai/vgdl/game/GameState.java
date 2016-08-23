package net.gvgai.vgdl.game;

public interface GameState {

    boolean forward( VGDLSprite s );

    MovingAvatar getAvatar();

    Object getDirection( VGDLSprite s );

    Object getPosition( VGDLSprite s );

    int getSpriteCount( Class<? extends VGDLSprite> clazz );

    VGDLSprite[] getSpritesAt( Object pos );

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

    void reverse( VGDLSprite s );

    void setAvatar( MovingAvatar a );
}
