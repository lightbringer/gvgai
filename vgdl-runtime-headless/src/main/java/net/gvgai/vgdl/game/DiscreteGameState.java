package net.gvgai.vgdl.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import net.gvgai.vgdl.input.Action;

public class DiscreteGameState implements GameState {

    private static Object reverse( Object a ) {
        final Action s = (Action) a;
        switch (s) {
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

    private RecordingMap<VGDLSprite> level;

    private RecordingMap<VGDLSprite> frameStart;

    private MovingAvatar avatar;

    private double score;

    public DiscreteGameState() {

    }

    private DiscreteGameState( DiscreteGameState other ) {
        level = other.level.advanceFrame();
        frameStart = level;
        avatar = other.avatar;
        score = other.score;
        makeCurrent( avatar );
    }

    @Override
    public GameState advanceFrame() {
        return new DiscreteGameState( this );
    }

    @Override
    public boolean forward( VGDLSprite s ) {
        final Action p = (Action) s.getDirection();
        return move( s, p );
    }

    @Override
    public Stream<VGDLSprite> getAllSprites() {
        return level.values();
    }

    @Override
    public MovingAvatar getAvatar() {
        if (avatar != null) {
            makeCurrent( avatar );
        }
        return avatar;
    }

    public RecordingMap getLevel() {
        return level;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public int getSpriteCount( Class<? extends VGDLSprite> clazz ) {
        final int counter = (int) level.values().filter( s -> clazz.isAssignableFrom( s.getClass() ) ).count();
        return counter;
    }

    @Override
    public Collection<VGDLSprite> getSpritesAt( Object pos ) {
        final Pair<Integer, Integer> p = (Pair) pos;
        return level.get( p.getLeft(), p.getRight() );
    }

    @Override
    public boolean isReady() {
        return level != null;
    }

    @Override
    public boolean move( VGDLSprite s, Object o ) {
        advanceFrameInternal();
        makeCurrent( s );
        final Action direction = (Action) o;

        Pair<Integer, Integer> p = (Pair<Integer, Integer>) s.getPosition();
        level.remove( p.getLeft(), p.getRight(), s );
        s.setDirection( direction );
        switch (direction) {
            case ACTION_RIGHT:
                p = Pair.of( p.getLeft() + 1, p.getRight() );
                break;
            case ACTION_UP:
                p = Pair.of( p.getLeft(), p.getRight() - 1 );
                break;
            case ACTION_DOWN:
                p = Pair.of( p.getLeft(), p.getRight() + 1 );
                break;
            case ACTION_LEFT:
                p = Pair.of( p.getLeft() - 1, p.getRight() );
                break;
            default:
                throw new RuntimeException();
        }
        s.setPosition( p );
        if (insertAt( p.getLeft(), p.getRight(), s )) {
            collide( s );
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void postFrame() {
        level.flatten();
        level.values().forEach( s -> s.postFrame() );
    }

    @Override
    public void preFrame() {
        frameStart = level;
        level.values().forEach( s -> s.preFrame() );
    }

    @Override
    public void remove( VGDLSprite s ) {
        if (s instanceof Passive) {
            final Passive p = (Passive) s;
            p.move = null;
            p.kill = null;
        }
        final Pair<Integer, Integer> p = (Pair<Integer, Integer>) s.getPosition();
        level.remove( p.getLeft(), p.getRight(), s );

    }

    @Override
    public void resetFrame() {
        level = frameStart;
        level.values().forEach( s -> s.reset() );
    }

    @Override
    public void setAvatar( MovingAvatar avatar ) {
        this.avatar = avatar;
    }

    public void setLevel( RecordingMap<VGDLSprite> level ) {
        this.level = level;
        level.values().forEach( s -> makeCurrent( s ) );
    }

    @Override
    public void setScore( double score ) {
        this.score = score;
    }

    private void advanceFrameInternal() {
        level = level.advanceFrame();

    }

    private <T extends VGDLSprite> void collide( VGDLSprite s ) {
        final Set<VGDLSprite> collided = new HashSet<>( getSpritesAt( s.getPosition() ).stream().filter( t -> t != s ).collect( Collectors.toSet() ) );
        for (final VGDLSprite other : collided) {
            System.out.println( "Calling collide with " + other + " on " + s );
            s.collide( other );
            other.collide( s );

        }
    }

    private boolean insertAt( int x, int y, VGDLSprite s ) {
        final boolean collision = !level.isEmpty( x, y );
        level.set( x, y, s );
        return collision;
    }

    private void makeCurrent( VGDLSprite s ) {
        if (s instanceof Passive) {
            final Passive p = (Passive) s;
            p.move = this::move;
            p.reverse = DiscreteGameState::reverse;
        }
        s.kill = this::remove;
    }

}
