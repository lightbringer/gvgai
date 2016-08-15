package net.gvgai.vgdl.game;

import java.util.function.Consumer;

import net.gvgai.vgdl.AutoWire;

public abstract class Movable extends VGDLSprite {
    @AutoWire
    private static Consumer<VGDLSprite> moveUp;
    @AutoWire
    private static Consumer<VGDLSprite> moveDown;
    @AutoWire
    private static Consumer<VGDLSprite> moveLeft;
    @AutoWire
    private static Consumer<VGDLSprite> moveRight;

    public void moveDown() {
        moveDown.accept( this );
    }

    public void moveLeft() {
        moveLeft.accept( this );
    }

    public void moveRight() {
        moveRight.accept( this );
    }

    public void moveUp() {
        moveUp.accept( this );
    }
}
