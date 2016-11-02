package net.gvgai.vgdl.controllers.singleplayer.sampleMCTS;

import java.util.Random;

import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.input.Action;
import net.gvgai.vgdl.input.Controller;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent implements Controller {

    public static final int ROLLOUT_DEPTH = 10;
    public static final double K = Math.sqrt( 2 );

    private final long timeBudgetMs;

    /**
     * Random generator for the agent.
     */
    private final SingleMCTSPlayer mctsPlayer;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent( long timeBudgetMs ) {

        //Create the player.
        mctsPlayer = new SingleMCTSPlayer( new Random() );
        this.timeBudgetMs = timeBudgetMs;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    @Override
    public Action act( GameState stateObs, double delta ) {

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init( stateObs );

        //Determine the action using MCTS...
        final long time = System.currentTimeMillis();
        final int action = mctsPlayer.run( timeBudgetMs );
        System.out.println( "Time: " + (System.currentTimeMillis() - time) );
        System.out.println( SingleTreeNode.totalIters );

        //... and return it.
        return Action.values()[action];
    }

    /**
     * Function called when the game is over. This method must finish before CompetitionParameters.TEAR_DOWN_TIME,
     *  or the agent will be DISQUALIFIED
     * @param stateObservation the game state at the end of the game
     * @param elapsedCpuTimer timer when this method is meant to finish.
     */
    public void result( GameState stateObservation, long elapsedCpuTimer ) {
//        System.out.println("MCTS avg iters: " + SingleMCTSPlayer.iters / SingleMCTSPlayer.num);
        //Include your code here to know how it all ended.
        //System.out.println("Game over? " + stateObservation.isGameOver());
    }

}
