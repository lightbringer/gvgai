package net.gvgai.vgdl.game;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiscreteGameState implements GameState {
    private class SpritePosition {
        private int x;
        private int y;
        private int index;
    }

    //We keep to maps to have fast access for Sprite->Position and Position->Sprite
    private VGDLSprite[][][] level;
    private final Map<VGDLSprite, SpritePosition> positions;

    private MovingAvatar avatar;

    public DiscreteGameState() {
        positions = new LinkedHashMap<VGDLSprite, SpritePosition>();
    }

    @Override
    public MovingAvatar getAvatar() {
        return avatar;
    }

    @Override
    public Object getPosition( VGDLSprite s ) {
        return positions.get( s );
    }

    @Override
    public int getSpriteCount( Class<? extends VGDLSprite> clazz ) {
        final int counter = (int) positions.keySet().stream().filter( s -> clazz.isAssignableFrom( s.getClass() ) ).count();
//        System.out.println( "Count(" + clazz + ") = " + counter );
        return counter;
    }

    @Override
    public VGDLSprite[] getSpritesAt( Object pos ) {
        final SpritePosition p = (SpritePosition) pos;
        return level[p.x][p.y];
    }

    @Override
    public boolean isReady() {
        return level != null;
    }

    @Override
    public boolean moveDown( VGDLSprite s ) {
        final SpritePosition p = positions.get( s );
        level[p.y][p.x][p.index] = null;

        p.y--;

        return insertAt( p.x, p.y, s );
    }

    @Override
    public boolean moveLeft( VGDLSprite s ) {
        final SpritePosition p = positions.get( s );
        level[p.y][p.x][p.index] = null;

        p.x--;
        return insertAt( p.x, p.y, s );
    }

    @Override
    public boolean moveRight( VGDLSprite s ) {
        final SpritePosition p = positions.get( s );
        level[p.y][p.x][p.index] = null;

        p.x++;
        return insertAt( p.x, p.y, s );
    }

    @Override
    public boolean moveUp( VGDLSprite s ) {
        final SpritePosition p = positions.get( s );
        level[p.y][p.x][p.index] = null;

        p.y++;
        return insertAt( p.x, p.y, s );
    }

    @Override
    public void setAvatar( MovingAvatar avatar ) {
        this.avatar = avatar;
    }

    public void setLevel( VGDLSprite[][][] level ) {
        this.level = level;

        positions.clear();
        for (int y = 0; y < level.length; y++) {
            for (int x = 0; x < level.length; x++) {
                if (level[y][x] != null) {
                    for (int i = 0; i < level[y][x].length; i++) {
                        final SpritePosition p = new SpritePosition();
                        p.index = i;
                        p.x = x;
                        p.y = y;
                        positions.put( level[y][x][i], p );
                    }
                }
            }
        }
    }

    private boolean insertAt( int x, int y, VGDLSprite s ) {
        VGDLSprite[] newPos = level[y][x];
        if (newPos == null) {
            level[y][x] = new VGDLSprite[] { s };
            return false;
        }
        else {
            boolean collision = false;
            boolean inserted = false;
            for (int i = 0; i < newPos.length; i++) {
                if (newPos[i] == null) {
                    if (!inserted) {
                        inserted = true;
                        newPos[i] = s;
                    }
                }
                else {
                    collision = true;
                }

            }
            if (!inserted) {
                newPos = Arrays.copyOf( newPos, newPos.length + 1 );
                newPos[newPos.length - 1] = s;
                level[y][x] = newPos;
            }
            return collision;
        }
    }
}
