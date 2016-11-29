package net.gvgai.vgdl.game;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A spatial data-structure that holds VGDLSprites. The interface is agnostic in which relation
 * the sprites are stored, e.g. cartesian 2D/3D space, graphs etc.
 *
 * @author Tobias Mahlmann
 *
 * @param <T> The implementing class itself. It is only required to be passed into the {@link Copyable} interface
 *            to allow classes to both implement the {@link GameState} and this interface (and hence avoiding the
 *            duplicate definition of {@link Copyable#copy()}.
 * @param <P> The class of the location object that positions are related to. This is typically an immutable data vector
 *            such as int[] for 2D or Vector3f for three spaces. No assumption about this is made in the VGDL Runtime or sprite classes
 *            and stored as {@link Object}.
 * @param <D> A class defining spatial-differences, i.e. directions, this is typicall a direction (e.g. NORTH, SOUTH, EAST, etc.) or
 *            a vector. As with positions, no assumptions about directions is made in the runtime.
 */
public interface GameMap<T, P, D> extends Copyable<T> {

    /**
     * Convenience method for get
     * @param p
     * @return
     */
    default Collection<VGDLSprite> get( P p ) {
        return get( p, false );
    }

    /**
     * Returns a collection of sprites at the location determined by p.
     * The returned collection is backed up the by the actual game state, i.e. altering
     * it will modify the location on the game map. The caller must provide a flag if she
     * plans to alter to location in order to allow write-back capable implementations to provide
     * a writeable location. To avoid unwanted side-effects, the implementing class should mark
     * returned collections as unmodifiable when writable is set to false
     *
     * @param p the location object
     * @param writable flag if returned value may be modified (see description above)
     * @return a collection of sprites that are located at p
     */
    Collection<VGDLSprite> get( P p, boolean writable );

    /**
     * @deprecated This currently only used for the DebugRendering and probably will be removed in the future
     * @return the height of the map
     */
    @Deprecated
    int getHeight();

    /**
     * @deprecated This currently only used for the DebugRendering and probably will be removed in the future
     * @return the width of the map
     */
    @Deprecated
    int getWidth();

    /**
     * Tests if a location is empty
     * @param p the location
     * @return true if no sprite is at that location
     */
    boolean isEmpty( P p );

    /**
     * Moves a sprite along a direction. The return value only denotes
     * if the sprite collided with another along its path. Based on the collision effect,
     * the sprite might have reached its designated position or not.
     *
     * @param s the sprite
     * @param direction the direction
     * @return true if a collision occurred.
     */
    boolean move( VGDLSprite s, D direction );

    /**
     * Removes the sprite <i>s</i> at position <i>p</i> from the map
     * @param p the position
     * @param s the sprite
     */
    void remove( P p, VGDLSprite s );

    /**
     * Places the sprite <i>s</i> at position <i>p</i>. Returns true if other sprites
     * were already at this location. This triggers collision effects as well.
     * @param p the position
     * @param s the sprite
     * @return true if a collision occured
     */
    boolean set( P p, VGDLSprite s );

    /**
     * A stream of all the sprites on the map. The stream is not backed by the map, i.e.
     * filtering, removing or adding sprites will have no effect on the map itself. Modifying
     * members of the returned sprites however will. It is recommended that the caller doesn't
     * modify the position and direction members.
     * @return a stream of all sprites on the map
     */
    Stream<VGDLSprite> values();
}
