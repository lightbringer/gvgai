package net.gvgai.vgdl.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import net.gvgai.vgdl.input.Action;

public class GameState2D implements GameState<GameState2D>, GameMap<GameState2D, int[], Action> {
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

    private double score;

    private boolean resetFrame;

    private final int height;

    private final int width;

    private List<VGDLSprite>[][] sprites;
    private List<VGDLSprite>[][] preFrameSprites;

    private boolean gameOver;

    public GameState2D( int width, int height ) {
        this.height = height;
        this.width = width;
        sprites = new List[height][width];
        for (final List[] row : sprites) {
            Arrays.setAll( row, i -> new ArrayList<>() );
        }
    }

    private GameState2D( GameState2D other ) {
        height = other.height;
        width = other.width;
        sprites = new List[height][width];

        score = other.score;
        gameOver = other.gameOver;
        resetFrame = other.resetFrame;
        sprites = Arrays.stream( other.sprites ).map( el -> el.clone() ).toArray( $ -> other.sprites.clone() );
        Arrays.stream( sprites ).map( a -> a ).forEach( a -> Arrays.setAll( a, i -> new ArrayList( a[i] ) ) );
        final Stream<List<VGDLSprite>[]> rows = Stream.of( sprites );
        final Stream<List<VGDLSprite>> places = rows.flatMap( r -> Stream.of( r ) );
        places.forEach( l -> l.replaceAll( s -> s.copy() ) );

        values().forEach( s -> {
            setupDelegates( s );
            if (s instanceof MovingAvatar) {
                avatar = (MovingAvatar) s;
            }
        } );

    }

    @Override
    public GameState2D copy() {
        return new GameState2D( this );
    }

    @Override
    public Collection<VGDLSprite> get( int[] p ) {
        final List<VGDLSprite> s = sprites[p[1]][p[0]];
        if (s == null) {
            return Collections.emptySet();
        }
        else {
            return s;
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
        return get( p ).isEmpty();
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
        int[] p = (int[]) s.getPosition();
        assert p != null;
        remove( p, s );
        p = Arrays.copyOf( p, 2 );
        move( direction, p );
        if (set( p, s )) {
            final List<VGDLSprite> src = (List<VGDLSprite>) get( p );
            final int size = src.size();
            final List<VGDLSprite> copy = new ArrayList<>( size );
            copy.addAll( Collections.nCopies( size, null ) );

            Collections.copy( copy, src );
            copy.forEach( o -> {
                if (s != o) {
                    s.collide( o );
                    o.collide( s );
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
            values().forEach( s -> s.reset() );
        }

    }

    @Override
    public void preFrame() {
        preFrameSprites = Arrays.stream( sprites ).map( el -> el.clone() ).toArray( $ -> sprites.clone() );
    }

    @Override
    public void remove( int[] p, VGDLSprite s ) {
        assert p != null;
        sprites[p[1]][p[0]].remove( s );

    }

    @Override
    public Void resetFrame() {
        resetFrame = true;
        return null;
    }

    @Override
    public boolean set( int[] p, VGDLSprite s ) {
        assert p != null;

        setupDelegates( s );

        s.setPosition( p );
        final List<VGDLSprite> l = sprites[p[1]][p[0]];
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
        final Stream<List<VGDLSprite>[]> rows = Stream.of( sprites );
        final Stream<List<VGDLSprite>> places = rows.flatMap( r -> Stream.of( r ) );
        final Stream<VGDLSprite> sprites = places.flatMap( Collection::stream );
        return sprites;
    }

    private void move( Action a, int[] p ) {
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

    private boolean moveDelegate( VGDLSprite s, Object direction ) {
        return move( s, (Action) direction );
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
