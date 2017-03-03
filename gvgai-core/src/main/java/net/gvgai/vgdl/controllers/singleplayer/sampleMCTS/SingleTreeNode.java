package net.gvgai.vgdl.controllers.singleplayer.sampleMCTS;

import java.util.Random;

import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.input.Action;
import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.tools.Utils;

public class SingleTreeNode {
    private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE = 10000000.0;
    public static double epsilon = 1e-6;
    public static double egreedyEpsilon = 0.05;
    public static Random m_rnd;
    protected static double[] bounds = new double[] { Double.MAX_VALUE, -Double.MAX_VALUE };
    public static int totalIters = 0;
    public GameState<GameState> state;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    private int m_depth;

    public SingleTreeNode( GameState state, SingleTreeNode parent, Random rnd ) {
        this.state = state;
        this.parent = parent;
        m_rnd = rnd;
        children = new SingleTreeNode[Action.values().length];
        totValue = 0.0;
        if (parent != null) {
            m_depth = parent.m_depth + 1;
        }
        else {
            m_depth = 0;
        }
    }

    public SingleTreeNode( Random rnd ) {
        this( null, null, rnd );
    }

    public void backUp( SingleTreeNode node, double result ) {
        SingleTreeNode n = node;
        while (n != null) {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }

    public int bestAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i = 0; i < children.length; i++) {

            if (children[i] != null) {
                double childValue = children[i].totValue / (children[i].nVisits + epsilon);
                childValue = Utils.noise( childValue, epsilon, m_rnd.nextDouble() ); //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1) {
            System.out.println( "Unexpected selection!" );
            selected = 0;
        }

        return selected;
    }

    public SingleTreeNode egreedy() {

        SingleTreeNode selected = null;

        if (m_rnd.nextDouble() < egreedyEpsilon) {
            //Choose randomly
            final int selectedIdx = m_rnd.nextInt( children.length );
            selected = children[selectedIdx];

        }
        else {
            //pick the best Q.
            double bestValue = -Double.MAX_VALUE;
            for (final SingleTreeNode child : children) {
                double hvVal = child.totValue;
                hvVal = Utils.noise( hvVal, epsilon, m_rnd.nextDouble() ); //break ties randomly
                // small sampleRandom numbers: break ties in unexpanded nodes
                if (hvVal > bestValue) {
                    selected = child;
                    bestValue = hvVal;
                }
            }

        }

        if (selected == null) {
            throw new RuntimeException( "Warning! returning null: " + children.length );
        }

        return selected;
    }

    public SingleTreeNode expand() {

        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < children.length; i++) {
            final double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        final GameState nextState = state.copy();
        nextState.preFrame();
        nextState.update( 1 );
        final MovingAvatar a = nextState.getAvatar();
        if (a != null) {
            nextState.getAvatar().act( Action.values()[bestAction] );
        }
        nextState.postFrame();

        final SingleTreeNode tn = new SingleTreeNode( nextState, this, m_rnd );
        children[bestAction] = tn;
        return tn;

    }

    public boolean finishRollout( GameState rollerState, int depth ) {
        if (depth >= Agent.ROLLOUT_DEPTH) {
            return true;
        }

        if (state.isGameOver()) {
            return true;
        }

        return false;
    }

    public void mctsSearch( long elapsedTimer ) {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer;
        int numIters = 0;

        final int remainingLimit = 5;
        while (remaining > 2 * avgTimeTaken && remaining > remainingLimit) {
            final long time = System.currentTimeMillis();
            final SingleTreeNode selected = treePolicy();
            final double delta = selected.rollOut();
            backUp( selected, delta );
            final long timeTaken = System.currentTimeMillis() - time;

            numIters++;
            acumTimeTaken += timeTaken;

            avgTimeTaken = acumTimeTaken / numIters;
            remaining -= timeTaken;
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
        }
        //System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
        totalIters = numIters;

        //ArcadeMachine.performance.add(numIters);
    }

    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i = 0; i < children.length; i++) {

            if (children[i] != null) {
                if (first == -1) {
                    first = children[i].nVisits;
                }
                else if (first != children[i].nVisits) {
                    allEqual = false;
                }

                double childValue = children[i].nVisits;
                childValue = Utils.noise( childValue, epsilon, m_rnd.nextDouble() ); //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1) {
            System.out.println( "Unexpected selection!" );
            selected = 0;
        }
        else if (allEqual) {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }

    public boolean notFullyExpanded() {
        for (final SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }

    public double rollOut() {
        final GameState rollerState = state.copy();
        int thisDepth = m_depth;

        while (!finishRollout( rollerState, thisDepth )) {

            final int action = m_rnd.nextInt( Action.values().length );
            rollerState.preFrame();
            rollerState.update( 1 );
            final MovingAvatar a = rollerState.getAvatar();
            if (a != null) {
                rollerState.getAvatar().act( Action.values()[action] );
            }
            rollerState.postFrame();
            thisDepth++;
        }

        final double delta = value( rollerState );

        if (delta < bounds[0]) {
            bounds[0] = delta;
        }

        if (delta > bounds[1]) {
            bounds[1] = delta;
        }

        return delta;
    }

    public SingleTreeNode treePolicy() {

        SingleTreeNode cur = this;
        while (!state.isGameOver() && cur.m_depth < Agent.ROLLOUT_DEPTH) {
            if (cur.notFullyExpanded()) {
                return cur.expand();

            }
            else {
                final SingleTreeNode next = cur.uct();
                //SingleTreeNode next = cur.egreedy();
                cur = next;
            }
        }

        return cur;
    }

    public SingleTreeNode uct() {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (final SingleTreeNode child : children) {
            final double hvVal = child.totValue;
            double childValue = hvVal / (child.nVisits + epsilon);

            childValue = Utils.normalise( childValue, bounds[0], bounds[1] );

            double uctValue = childValue + Agent.K * Math.sqrt( Math.log( nVisits + 1 ) / (child.nVisits + epsilon) );

            // small sampleRandom numbers: break ties in unexpanded nodes
            uctValue = Utils.noise( uctValue, epsilon, m_rnd.nextDouble() ); //break ties randomly

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }

        if (selected == null) {
            throw new RuntimeException( "Warning! returning null: " + bestValue + " : " + children.length );
        }

        return selected;
    }

    public double value( GameState a_gameState ) {

        final boolean gameOver = a_gameState.isGameOver();
//        final Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getScore();

        //FIXME
        final boolean win = rawScore == 3.0;
        if (gameOver && !win) {
            rawScore += HUGE_NEGATIVE;
        }

        if (gameOver && win) {
            rawScore += HUGE_POSITIVE;
        }

        return rawScore;
    }

}
