package net.gvgai.vgdl.game;

/**
 * Has the same function as the {@link Cloneable} interface with the difference
 * that {@link #copy()} is statically-typed to avoid unnecessary casting from {@link Object}.
 * @author Tobias Mahlmann
 *
 * @param <T> the implementing class
 */
public interface Copyable<T> {
    T copy();
}
