package net.gvgai.vgdl.controllers.singleplayer.sampleMCTS;

import java.util.Random;

import net.gvgai.vgdl.game.GameState;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:13
 */
public class SingleMCTSPlayer {
    public static int iters = 0, num = 0;

    /**
     * Root of the tree.
     */
    public SingleTreeNode m_root;

    /**
     * Random generator.
     */
    public Random m_rnd;

    /**
     * Creates the MCTS player with a sampleRandom generator object.
     * @param a_rnd sampleRandom generator object.
     */
    public SingleMCTSPlayer( Random a_rnd ) {
        m_rnd = a_rnd;
        m_root = new SingleTreeNode( a_rnd );
    }

    /**
     * Inits the tree with the new observation state in the root.
     * @param game
     * @param a_gameState current state of the game.
     */
    public void init( GameState a_gameState ) {
        //Set the game observation to a newly root node.
        m_root = new SingleTreeNode( m_rnd );
        m_root.state = a_gameState;

    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public int run( long elapsedTimer ) {
        //Do the search within the available time.
        m_root.mctsSearch( elapsedTimer );

        iters += SingleTreeNode.totalIters;
        num++;

        //Determine the best action to take and return it.
        final int action = m_root.mostVisitedAction();
        //int action = m_root.bestAction();
        return action;
    }

}
