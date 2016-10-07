package net.gvgai.vgdl.game;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

public class DiscreteGameState implements GameState {

    public enum Direction {
        NORTH, EAST, WEST, SOUTH
    };

    //We keep to maps to have fast access for Sprite->Position and Position->Sprite
    private RecordingMap<VGDLSprite> level;

    private RecordingMap<VGDLSprite> frameStart;

    private MovingAvatar avatar;

    public DiscreteGameState() {

    }

    @Override
    public boolean forward( VGDLSprite s ) {
        final Direction p = (Direction) s.getDirection();
        switch (p) {
            case EAST:
                return moveRight( s );
            case NORTH:
                return moveUp( s );
            case SOUTH:
                return moveDown( s );
            case WEST:
                return moveLeft( s );
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public MovingAvatar getAvatar() {
        return avatar;
    }

    public RecordingMap getLevel() {
        return level;
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
        final Direction direction = (Direction) o;

        switch (direction) {
            case EAST:
                return moveRight( s );
            case NORTH:
                return moveUp( s );
            case SOUTH:
                return moveDown( s );
            case WEST:
                return moveLeft( s );
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public boolean moveDown( VGDLSprite s ) {
        advanceFrame();

        Pair<Integer, Integer> p = (Pair<Integer, Integer>) s.getPosition();

        level.remove( p.getLeft(), p.getRight(), s );
        s.setDirection( Direction.SOUTH );
        p = Pair.of( p.getLeft(), p.getRight() + 1 );
        s.setPosition( p );

        return insertAt( p.getLeft(), p.getRight(), s );
    }

    @Override
    public boolean moveLeft( VGDLSprite s ) {
        advanceFrame();
        Pair<Integer, Integer> p = (Pair<Integer, Integer>) s.getPosition();

        level.remove( p.getLeft(), p.getRight(), s );
        s.setDirection( Direction.WEST );
        p = Pair.of( p.getLeft() - 1, p.getRight() );
        s.setPosition( p );
        return insertAt( p.getLeft(), p.getRight(), s );

    }

    @Override
    public boolean moveRight( VGDLSprite s ) {
        advanceFrame();
        Pair<Integer, Integer> p = (Pair<Integer, Integer>) s.getPosition();

        level.remove( p.getLeft(), p.getRight(), s );
        s.setDirection( Direction.EAST );
        p = Pair.of( p.getLeft() + 1, p.getRight() );
        s.setPosition( p );
        return insertAt( p.getLeft(), p.getRight(), s );
    }

    @Override
    public boolean moveUp( VGDLSprite s ) {
        advanceFrame();
        Pair<Integer, Integer> p = (Pair<Integer, Integer>) s.getPosition();

        level.remove( p.getLeft(), p.getRight(), s );
        s.setDirection( Direction.NORTH );
        p = Pair.of( p.getLeft(), p.getRight() - 1 );
        s.setPosition( p );
        return insertAt( p.getLeft(), p.getRight(), s );

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
        final Pair<Integer, Integer> p = (Pair<Integer, Integer>) s.getPosition();
        level.remove( p.getLeft(), p.getRight(), s );

    }

    @Override
    public void resetFrame() {
        level = frameStart;
        level.values().forEach( s -> s.reset() );
    }

    @Override
    public void reverse( VGDLSprite s ) {
        advanceFrame();
        final Direction d = (Direction) s.getDirection();
        switch (d) {
            case EAST:
                s.setDirection( Direction.WEST );
                break;
            case NORTH:
                s.setDirection( Direction.SOUTH );
                break;
            case SOUTH:
                s.setDirection( Direction.NORTH );
                break;
            case WEST:
                s.setDirection( Direction.EAST );
                break;
            default:
                throw new RuntimeException();

        }

    }

    @Override
    public void setAvatar( MovingAvatar avatar ) {
        this.avatar = avatar;
    }

    public void setLevel( RecordingMap level ) {
        this.level = level;

    }

    private void advanceFrame() {
        level = level.advanceFrame();

    }

    private boolean insertAt( int x, int y, VGDLSprite s ) {
        final boolean collision = !level.isEmpty( x, y );
        level.set( x, y, s );
        return collision;
    }

}
