package net.gvgai.vgdl.game;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Passive extends Immovable {
    protected Function<Object, Object> reverse;
    protected BiConsumer<VGDLSprite, Object> move;
    protected BiConsumer<VGDLSprite, Object> moveFrameStart;

    public void move( Object direction ) {
        move.accept( this, direction );

    }

    @Override
    public void preFrame() {
        super.preFrame();

        moveFrameStart = move;
    }

    @Override
    public void reset() {
        super.reset();

        move = moveFrameStart;
    }
}
