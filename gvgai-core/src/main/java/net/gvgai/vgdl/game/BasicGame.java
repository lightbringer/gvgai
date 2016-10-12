package net.gvgai.vgdl.game;

public abstract class BasicGame implements VGDLGame {
    protected String key_handler;
    protected int square_size;
    protected int no_players;

    private GameState gameState;

    protected BasicGame() {
        System.out.println( "Spawing basic game" );

    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public abstract Class<? extends VGDLSprite>[] getMappedSpriteClass( char c );

    @Override
    public double getScore() {
        return gameState.getScore();
    }

    @Override
    public void postFrame() {
        gameState.postFrame();

    }

    @Override
    public void preFrame() {
        gameState.preFrame();

    }

    @Override
    public void resetFrame() {
        gameState.resetFrame();

    }

    @Override
    public void setGameState( GameState gameState ) {
        this.gameState = gameState;
    }

    @Override
    public void setScore( double d ) {
        gameState.setScore( d );
    }

    @Override
    public String toString() {
        return "BasicGame [key_handler=" + key_handler + ", square_size=" + square_size + ", no_players=" + no_players + "]";
    }

    @Override
    public void update( double seconds ) {
//        System.out.println( seconds );

    }

}
