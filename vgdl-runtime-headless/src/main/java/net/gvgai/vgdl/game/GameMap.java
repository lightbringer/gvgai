package net.gvgai.vgdl.game;

import java.util.Collection;
import java.util.stream.Stream;

public interface GameMap<T, P, D> extends Copyable<T> {

    Collection<VGDLSprite> get( P p );

    int getHeight();

    int getSpriteCount( Class<? extends VGDLSprite> clazz );

    int getWidth();

    boolean isEmpty( P p );

    boolean move( VGDLSprite s, D direction );

    void remove( P p, VGDLSprite s );

    boolean set( P p, VGDLSprite s );

    Stream<VGDLSprite> values();
}
