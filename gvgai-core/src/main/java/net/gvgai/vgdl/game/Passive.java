package net.gvgai.vgdl.game;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.gvgai.vgdl.AutoWire;

public abstract class Passive extends Immovable {
    @AutoWire
    protected Consumer<VGDLSprite> moveUp;
    @AutoWire
    protected Consumer<VGDLSprite> moveDown;
    @AutoWire
    protected Consumer<VGDLSprite> moveLeft;
    @AutoWire
    protected Consumer<VGDLSprite> moveRight;
    @AutoWire
    protected Consumer<VGDLSprite> reverse;
    @AutoWire
    protected Consumer<VGDLSprite> forward;
    @AutoWire
    protected BiConsumer<VGDLSprite, Object> move;

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
