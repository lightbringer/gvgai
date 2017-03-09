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
import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.VGDLSprite;

/**
 * This class implements a discrete cartesian space as a game map. It also supports write-back mechanisms
 * that allow adaptive cloning and copy-on-write using cascading instances of this class. The static member
 * FLATTEN_DEPTH controls at which depth instances of this class should svere ties with its father objects
 *
 * @author Tobias Mahlmann
 *
 */
public class GameState2D implements GameState<GameState2D>, GameMap<GameState2D, int[], Action> {
    private static int FLATTEN_DEPTH = 1000;

    public static Action reverse( Object s ) {
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

    private static MovingAvatar findAvatar( List[] a, int index ) {
        return (MovingAvatar) a[index].stream().filter( i -> i instanceof MovingAvatar ).findFirst().orElse( null );
    }

    private MovingAvatar avatar;
    private int avatarIndex;

    private int preFrameAvatarIndex;

    private MovingAvatar preFrameAvatar;
    private double score;

    private double preFrameScore;

    private boolean resetFrame;

    private final int height;
    private final int width;

    private List<VGDLSprite>[] sprites;

    private List<VGDLSprite>[] preFrameSprites;

    private boolean gameOver;

    private final GameState2D parent;

    private int depth;

    private boolean inFrame;
    private boolean preFrameGameOver;
    private boolean preBufferFrame;
    private boolean obeyBoundaries;

    private int tick;

    public GameState2D( int width, int height ) {
        this.height = height;
        this.width = width;
        sprites = new List[height * width];
        if (preBufferFrame) {
            preFrameSprites = new List[height * width];
        }
        Arrays.setAll( sprites, i -> new ArrayList<>() );

        parent = null;
        depth = 0;
    }

    private GameState2D( GameState2D other ) {
        if (other.inFrame) {
            throw new IllegalStateException( "don't advance in-frame. Inconsistencies ahead!" );
        }
        height = other.height;
        width = other.width;
        sprites = new List[height * width];
        avatarIndex = other.avatarIndex;

        if (other.depth >= FLATTEN_DEPTH) {
            IntStream.range( 0, sprites.length ).mapToObj( i -> new ArrayList( other.get( i ) ) ).collect( Collectors.toList() ).toArray( sprites );
            Stream.of( sprites ).forEach( l -> l.replaceAll( s -> s.copy( this ) ) );
            depth = 0;
            parent = null;
        }
        else {
            depth = other.depth + 1;
            parent = other;
            if (avatarIndex >= 0) {
                makeCurrent( avatarIndex );
            }
        }

        preBufferFrame = other.preBufferFrame;
        if (preBufferFrame) {
            preFrameSprites = new List[height * width];
        }
        score = other.score;
        gameOver = other.gameOver;
        resetFrame = other.resetFrame;
        if (avatarIndex >= 0) {
            avatar = findAvatar( sprites, avatarIndex );
        }
        obeyBoundaries = other.obeyBoundaries;
        tick = other.tick;
    }

    @Override
    public int[] add( int[] position, Action direction ) {
        final int[] newPos = Arrays.copyOf( position, 2 );
        switch (direction) {
            case ACTION_DOWN:
                newPos[1]--;
                break;
            case ACTION_LEFT:
                newPos[0]--;
                break;
            case ACTION_RIGHT:
                newPos[0]++;
                break;
            case ACTION_UP:
                newPos[1]++;
                break;
            default:
                throw new IllegalStateException( "action must a spatial one" );
        }
        return newPos;

    }

    public GameState2D copy() {
        return new GameState2D( this );
    }

    @Override
    public Action down() {
        return Action.ACTION_DOWN;
    }

    @Override
    public List<VGDLSprite> get( int[] p, boolean forWriting ) {
        final int index = p[1] * width + p[0];
        final List<VGDLSprite> s = sprites[index];
        if (s == null) {
            if (parent == null) {
                if (!forWriting) {
                    return Collections.unmodifiableList( Collections.emptyList() );
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
                return Collections.unmodifiableList( s );
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
    public GameMap getLevel() {
        return this;
    }

    public GameState2D getParent() {
        return parent;
    }

    @Override
    public double getScore() {
        return score;
    }

    public int getTick() {
        return tick;
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
    public boolean isInBounds( int[] p ) {
        final int x = p[0];
        final int y = p[1];

        return x >= 0 && y >= 0 && x < width && y < height;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public Action left() {
        return Action.ACTION_LEFT;
    }

    @Override
    public boolean move( VGDLSprite s, Action direction ) {

        final int[] p = (int[]) s.getPosition();
        assert p != null;

        final int[] pCopy = Arrays.copyOf( p, 2 );
        movePosition( direction, pCopy );

        final VGDLSprite sCopy;
        if (isCurrent( p, s )) {
            sCopy = s;
        }
        else {
            sCopy = s.copy( this );

        }

        remove( p, s );

        if (s instanceof MovingAvatar) {
            avatar = (MovingAvatar) sCopy;
            avatarIndex = pCopy[1] * width + pCopy[0];
        }

        if (set( pCopy, sCopy )) {
            final Collection<VGDLSprite> src = get( pCopy, false );

            final VGDLSprite[] copy = new VGDLSprite[src.size()];
            src.toArray( copy );

            for (final VGDLSprite o : copy) {
                o.collide( this, copy );
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void postFrame() {
        assert inFrame;

        if (resetFrame) {
            assert preBufferFrame;

            resetFrame = false;
            score = preFrameScore;
            gameOver = preFrameGameOver;
            final List[] tmp = sprites;
            sprites = preFrameSprites;
            preFrameSprites = tmp;
            avatarIndex = preFrameAvatarIndex;
            avatar = preFrameAvatar;
            assert avatar != null;
            Arrays.asList( sprites ).stream().filter( l -> l != null ).flatMap( Collection::stream ).forEach( s -> s.reset() );
        }
        values().forEach( s -> s.postFrame() );

        inFrame = false;
    }

    @Override
    public void preFrame() {
        assert !inFrame;
        if (preBufferFrame) {
            preFrameAvatarIndex = avatarIndex;
            preFrameScore = score;
            preFrameAvatar = avatar;
            preFrameGameOver = gameOver;
            Arrays.setAll( preFrameSprites, i -> sprites[i] != null ? new ArrayList<>( sprites[i] ) : null );
            assert preFrameSprites != null;
        }

        values().forEach( s -> s.preFrame() );

        inFrame = true;
    }

    @Override
    public void remove( int[] p, VGDLSprite s ) {
        assert p != null;
        final boolean ret = get( p, true ).removeIf( sp -> sp.getId() == s.getId() );
        assert ret;
        if (s == avatar) {
            avatar = null;
            avatarIndex = -1;
        }

    }

    @Override
    public void resetFrame() {
        resetFrame = true;
    }

    @Override
    public Action right() {
        return Action.ACTION_RIGHT;
    }

    @Override
    public boolean set( int[] p, VGDLSprite s ) {

        assert p != null;

        if (obeyBoundaries && !isInBounds( p )) {
            s.OnOutOfBounds( this );
            return false;
        }
        else {
            if (s instanceof MovingAvatar) {
                avatarIndex = p[1] * width + p[0];
            }

            s.setPosition( p );
            final List<VGDLSprite> l = get( p, true );
            if (l.isEmpty()) {
                l.add( s );
                return false;
            }
            else {
                //Make sure the list is ordered by classIds
                final int size = l.size();
                final int myId = s.getClassId();
                for (int i = 0; i < size - 1; i++) {
                    if (l.get( i + 1 ).getClassId() > myId) {
                        l.add( i, s );
                        return true;
                    }
                }
                l.add( s );
                return true;
            }
        }
    }

    @Override
    public void setAvatar( MovingAvatar a ) {
        avatar = a;
    }

    public void setBufferFrame( boolean b ) {
        preBufferFrame = b;
        if (b && preFrameSprites == null) {
            preFrameSprites = new List[width * height];
        }
        else if (!b && preFrameSprites != null) {
            preFrameSprites = null;
        }
    }

    @Override
    public void setGameOver( boolean b ) {
        gameOver = b;
    }

    public void setObeyBoundaries( boolean b ) {
        obeyBoundaries = b;

    }

    @Override
    public void setScore( double d ) {
        score = d;
    }

    @Override
    public Action up() {
        return Action.ACTION_UP;
    }

    @Override
    public void update( double seconds ) {
        tick++;
        values().collect( Collectors.toList() ).forEach( s -> s.update( this, seconds ) );
    }

    @Override
    public Stream<VGDLSprite> values() {
        return IntStream.range( 0, sprites.length ).mapToObj( i -> (sprites[i] != null ? sprites[i] : get( i )) ).flatMap( Collection::stream );
    }

    @Override
    public int[] wrap( int[] p ) {
        if (p[0] < 0) {
            p[0] = width - 1;
        }
        else if (p[0] >= width - 1) {
            p[0] = 0;
        }
        if (p[1] < 0) {
            p[1] = height - 1;
        }
        else if (p[1] >= height - 1) {
            p[1] = 0;
        }

        return p;
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
        sprites[index].replaceAll( sp -> sp.copy( this ) );

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
    }

}
