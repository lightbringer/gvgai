package net.gvgai.vgdl.game;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class GameState3D implements GameState<GameState3D> {
    private GameMap3D map;
    private MovingAvatar avatar;
    private double score;
    private boolean gameOver;

    private final Set<VGDLSprite> deletedSprites;

    public GameState3D() {
        deletedSprites = new HashSet<>();

    }

    @Override
    public GameState3D copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MovingAvatar getAvatar() {
        return avatar;
    }

    public Set<VGDLSprite> getDeletedSprites() {
        return deletedSprites;
    }

    public GameMap3D getLevel() {
        return map;
    }

    @Override
    public double getScore() {
        return score;
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
    public void postFrame() {
        values().forEach( s -> s.postFrame() );
    }

    @Override
    public void preFrame() {
        deletedSprites.clear();
        values().forEach( s -> s.preFrame() );
    }

    @Override
    public void resetFrame() {
        //NOP
    }

    @Override
    public void setAvatar( MovingAvatar a ) {
        avatar = a;

    }

    @Override
    public void setGameOver( boolean b ) {
        gameOver = b;
    }

    public void setLevel( GameMap3D level ) {
        map = level;

    }

    @Override
    public void setScore( double d ) {
        score = d;

    }

    @Override
    public Stream<VGDLSprite> values() {
        return map.values();
    }

}
