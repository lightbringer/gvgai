package net.gvgai.vgdl.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.gvgai.vgdl.input.Action;

/**
 * This class implements a discrete cartesian space as a game map. It also supports write-back mechanisms
 * that allow adaptive cloning and copy-on-write using cascading instances of this class. The static member
 * FLATTEN_DEPTH controls at which depth instances of this class should svere ties with its father objects
 *
 * @author Tobias Mahlmann
 *
 */
public class GameState2D implements GameState<GameState2D>, GameMap<GameState2D, int[], Action> {
    private static int FLATTEN_DEPTH = 20;

    private static Action reverse( Object s ) {
        final Action a = (Action) s;
        switch (a) {
            case ACTION_LEFT:
                return Action.ACTION_RIGHT;
            case ACTION_UP:
                return Action.ACTION_DOWN;
            case ACTION_DOWN:
                return Action.ACTION_UP;
            case ACTION_RIGHT:
                return Action.ACTION_LEFT;
            default:
                throw new RuntimeException();

        }

    }

    private MovingAvatar avatar;
    private int avatarIndex;

    private int preFrameAvatarIndex;

    private double score;

    private boolean resetFrame;

    private final int height;

    private final int width;
    private List<VGDLSprite>[] sprites;

    private List<VGDLSprite>[] preFrameSprites;

    private boolean gameOver;

    private final GameState2D parent;

    private int depth;

    public GameState2D( int width, int height ) {
        this.height = height;
        this.width = width;
        sprites = new List[height * width];
        Arrays.setAll( sprites, i -> new ArrayList<>() );

        parent = null;
        depth = 0;
    }

    private GameState2D( GameState2D other ) {
        height = other.height;
        width = other.width;
        sprites = new List[height * width];
        avatarIndex = other.avatarIndex;

        if (other.depth >= FLATTEN_DEPTH) {
            IntStream.range( 0, sprites.length ).mapToObj( i -> new ArrayList( other.get( i ) ) ).collect( Collectors.toList() ).toArray( sprites );
            Stream.of( sprites ).forEach( l -> l.replaceAll( s -> s.copy() ) );
            values().forEach( s -> setupDelegates( s ) );
            depth = 0;
            parent = null;
        }
        else {
            depth = other.depth + 1;
            parent = other;
            makeCurrent( avatarIndex );
        }

        score = other.score;
        gameOver = other.gameOver;
        resetFrame = other.resetFrame;

        avatar = findavatar( avatarIndex );
    }

    @Override
    public GameState2D copy() {
        return new GameState2D( this );
    }

    @Override
    public Collection<VGDLSprite> get( int[] p, boolean forWriting ) {
        final int index = p[1] * width + p[0];
        final List<VGDLSprite> s = sprites[index];
        if (s == null) {
            if (parent == null) {
                if (!forWriting) {
                    return Collections.unmodifiableCollection( Collections.emptySet() );
                }
                else {
                    throw new IllegalStateException( "root map has no collection to write in here" );
                }
            }
            else {
                if (!forWriting) {
                    return parent.get( p, false );
                }
                else {
                    makeCurrent( index );
                    return sprites[index];
                }
            }
        }
        else {
            if (forWriting) {
                return s;
            }
            else {
                return Collections.unmodifiableCollection( s );
            }
        }
    }

    @Override
    public MovingAvatar getAvatar() {
        return avatar;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public int getSpriteCount( Class<? extends VGDLSprite> clazz ) {
        return (int) values().filter( s -> clazz.isAssignableFrom( s.getClass() ) ).count();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public boolean isEmpty( int[] p ) {
        return get( p, false ).isEmpty();
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public boolean move( VGDLSprite s, Action direction ) {
        final int[] p = (int[]) s.getPosition();
        assert p != null;
        remove( p, s );

        final int[] pCopy = Arrays.copyOf( p, 2 );
        movePosition( direction, pCopy );

        final VGDLSprite sCopy;
        if (isCurrent( p, s )) {
            sCopy = s;
        }
        else {
            sCopy = s.copy();
            if (s instanceof MovingAvatar) {
                avatar = (MovingAvatar) sCopy;
                avatarIndex = pCopy[1] * width + pCopy[0];
            }
        }

        if (set( pCopy, sCopy )) {
            final Collection<VGDLSprite> src = get( pCopy, false );

            final List<VGDLSprite> copy = new ArrayList<>( src );
            copy.forEach( o -> {
                if (sCopy != o) {
                    sCopy.collide( o );
                    o.collide( sCopy );
                }
            } );
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void postFrame() {
        if (resetFrame) {
            resetFrame = false;
            sprites = preFrameSprites;
            avatarIndex = preFrameAvatarIndex;
            avatar = findavatar( avatarIndex );
            assert avatar != null;
            Arrays.asList( sprites ).stream().flatMap( Collection::stream ).forEach( s -> s.reset() );
        }
        Stream.of( sprites ).filter( l -> l != null ).flatMap( Collection::stream ).forEach( s -> s.postFrame() );
    }

    @Override
    public void preFrame() {
        preFrameAvatarIndex = avatarIndex;
        preFrameSprites = Arrays.stream( sprites ).toArray( $ -> sprites.clone() );
        Arrays.asList( sprites ).replaceAll( l -> l != null ? new ArrayList<>( l ) : null );
        Stream.of( sprites ).filter( l -> l != null ).flatMap( Collection::stream ).forEach( s -> s.preFrame() );
    }

    @Override
    public void remove( int[] p, VGDLSprite s ) {
        assert p != null;
        final boolean ret = get( p, true ).removeIf( sp -> sp.id == s.id );
        assert ret;

    }

    @Override
    public Void resetFrame() {
        resetFrame = true;
        return null;
    }

    @Override
    public boolean set( int[] p, VGDLSprite s ) {
        assert p != null;
        if (s instanceof MovingAvatar) {
            avatarIndex = p[1] * width + p[0];
        }
        setupDelegates( s );

        s.setPosition( p );
        final Collection<VGDLSprite> l = get( p, true );
        final boolean ret = !l.isEmpty();
        l.add( s );
        return ret;
    }

    @Override
    public void setAvatar( MovingAvatar a ) {
        avatar = a;
    }

    @Override
    public void setGameOver( boolean b ) {
        gameOver = b;

    }

    @Override
    public void setScore( double d ) {
        score = d;

    }

    @Override
    public Stream<VGDLSprite> values() {
        return IntStream.range( 0, sprites.length ).mapToObj( i -> (sprites[i] != null ? sprites[i] : get( i )) ).flatMap( Collection::stream );
    }

    private MovingAvatar findavatar( int index ) {
        return (MovingAvatar) sprites[avatarIndex].stream().filter( i -> i instanceof MovingAvatar ).findFirst().get();
    }

    private Collection<VGDLSprite> get( int index ) {
        if (sprites[index] != null) {
            return sprites[index];
        }
        else {
            return parent.get( index );
        }
    }

    private boolean isCurrent( int[] p, VGDLSprite s ) {
        final int index = p[1] * width + p[0];
        return sprites[index] != null && sprites[index].contains( s );
    }

    private void makeCurrent( int index ) {
        assert sprites[index] == null;

        sprites[index] = new ArrayList( parent.get( index ) );
        sprites[index].replaceAll( sp -> sp.copy() );
        sprites[index].forEach( sp -> setupDelegates( sp ) );

    }

    private boolean moveDelegate( VGDLSprite s, Object direction ) {
        return move( s, (Action) direction );
    }

    private void movePosition( Action a, int[] p ) {
        switch (a) {
            case ACTION_LEFT:
                p[0]--;
                break;
            case ACTION_UP:
                p[1]--;
                break;
            case ACTION_DOWN:
                p[1]++;
                break;
            case ACTION_RIGHT:
                p[0]++;
                break;
            default:
                throw new RuntimeException();
        }
        p[0] = Math.max( p[0], 0 );
        p[1] = Math.max( p[1], 0 );
        p[0] = Math.min( p[0], width - 1 );
        p[1] = Math.min( p[1], height - 1 );
    }

    private void removeDelegate( VGDLSprite s ) {
        remove( (int[]) s.getPosition(), s );
    }

    private void setupDelegates( VGDLSprite s ) {
        if (s instanceof Passive) {
            final Passive passive = (Passive) s;
            passive.move = this::moveDelegate;
            passive.reverse = GameState2D::reverse;
        }
        s.resetAll = this::resetFrame;
        s.kill = this::removeDelegate;
    }

}
