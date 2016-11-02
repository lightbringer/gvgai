package net.gvgai.vgdl.input;

import net.gvgai.vgdl.game.GameState;

public interface Controller {

    Action act( GameState gameState, double seconds );

}
