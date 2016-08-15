package net.gvgai.vgdl.game;

import java.util.function.Consumer;

import net.gvgai.vgdl.AutoWire;

public abstract class BasicGame implements VGDLGame {
    protected String key_handler;
    protected int square_size;
    protected int no_players;

    @AutoWire
    private Consumer<Integer> lose;

    @AutoWire
    private Consumer<Integer> win;

    protected BasicGame() {
        System.out.println( "Spawing basic game" );

    }

    @Override
    public abstract Class<? extends VGDLSprite>[] getMappedSpriteClass( char c );

    @Override
    public String toString() {
        return "BasicGame [key_handler=" + key_handler + ", square_size=" + square_size + ", no_players=" + no_players + "]";
    }

    @Override
    public void update( double seconds ) {
//        System.out.println( seconds );

    }

}
