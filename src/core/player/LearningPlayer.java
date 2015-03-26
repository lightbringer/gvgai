package core.player;

import core.VGDLRegistry;
import core.game.StateObservation;
import core.game.StateView;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 13:42
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 * http://stackoverflow.com/questions/4112470/java-how-to-both-read-and-write-to-from-process-thru-pipe-stdin-stdout
 */
public class LearningPlayer extends AbstractPlayer{

    /**
     * Last action executed by this agent.
     */
    private Types.ACTIONS lasAction = null;

    /**
     * Reader of the player. Will read actions from the client.
     */
    public static BufferedReader input;

    /**
     * Writer of the player. Used to pass the client the state view information.
     */
    public static BufferedWriter output;

    /**
     * Line separator for messages.
     */
    private String lineSep = System.getProperty("line.separator");

    /**
     * Client process
     */
    private Process client;

    /**
     * Print debug info
     */
    private boolean verbose;

    /**
     * Public constructor of the player.
     * @param client process that runs the agent.
     */
    public LearningPlayer(Process client)
    {
        isLearner = true;
        verbose = true;

        try {

            this.client = client;
            initBuffers();

        }catch(Exception e)
        {
            System.out.println("Exception creating the client process: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Creates the buffers for pipe communication.
     */
    private void initBuffers()
    {
        try {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

        }catch(Exception e)
        {
            System.out.println("Exception creating the client process: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Picks an action. This function is called at the beginning of the game for
     * initialization.
     * @param stateView View of the current state.
     * @param elapsedTimer Timer when the initialization is due to finish.
     * @param isTraining true if this game is played as training.
     */
    public void init(StateView stateView, ElapsedCpuTimer elapsedTimer, boolean isTraining)
    {
        isLearner = true;
        verbose = true;

        stateView.assignPlayer(this);

        //Sending messages.
        commSend ("INIT " + isTraining);

        commSend(stateView.getGameInfo(false));
        commSend(stateView.getActionsInfo());
        commSend(stateView.getAvatarInfo());

        int nSprites = VGDLRegistry.GetInstance().numSpriteTypes();
        for(int i = 0; i < nSprites; ++i)
        {
            commSend(stateView.getBitGrid(i));
        }

        commSend ("INIT-END " + elapsedTimer.remainingTimeMillis());

        String response = commRecv(elapsedTimer);
        if(verbose) System.out.println("Received: " + response);
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player. The action returned must be contained in the
     * actions accessible from stateObs.getAvailableActions(), or action NIL
     * will be applied.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        initBuffers();

        //Pipes here.
        StateView sv = (StateView)stateObs;

        //Sending messages.
        commSend ("ACT");

        commSend(sv.getGameInfo(true));
        // commSend(sv.getActionsInfo()); //NOT NEEDED (they don't change).
        commSend(sv.getAvatarInfo());

        int nSprites = VGDLRegistry.GetInstance().numSpriteTypes();
        for(int i = 0; i < nSprites; ++i)
        {
            commSend(sv.getBitGrid(i));
        }

        commSend ("ACT-END " + elapsedTimer.remainingTimeMillis());

        String response = commRecv(elapsedTimer);
        if(verbose)  System.out.println("Received ACTION: " + response);

        Types.ACTIONS action = Types.ACTIONS.fromString(response);
        return action;
    }


    public void finishGame(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        initBuffers();

        //Pipes here.
        StateView sv = (StateView)stateObs;

        //Sending messages.
        commSend ("ENDGAME");

        commSend(sv.getGameInfo(true));
        // commSend(sv.getActionsInfo()); //NOT NEEDED (they don't change).
        commSend(sv.getAvatarInfo());

        int nSprites = VGDLRegistry.GetInstance().numSpriteTypes();
        for(int i = 0; i < nSprites; ++i)
        {
            commSend(sv.getBitGrid(i));
        }

        commSend ("ENDGAME-END " + elapsedTimer.remainingTimeMillis());

        String response = commRecv(elapsedTimer);
        verbose = true;
        if(verbose) System.out.println("Received: " + response);
    }

    /**
     * Sends a message through the pipe.
     * @param msg message to send.
     */
    public void commSend(String msg) {
        String ret;

        if(verbose)
            System.out.println(msg);

        try {
            output.write( msg + lineSep );
            output.flush();
        }
        catch (Exception err) {
            System.out.println("Exception sending message: " + err);
            err.printStackTrace();
        }
    }

    /**
     * Waits for a response during T milliseconds.
     * @param elapsedTimer Timer when the initialization is due to finish.
     * @return the response got from the client, or null if no response was received after due time.
     */
    public static String commRecv(ElapsedCpuTimer elapsedTimer) {
        String ret = null;

        try {

            while(elapsedTimer.remainingTimeMillis() > 0)
            {
                if(input.ready()) {

                    ret = input.readLine();
                    if (ret != null && ret.trim().length() > 0) {
                        return ret.trim();
                    }
                }
            }
        }
        catch (Exception err) {
            System.out.println("Exception receiving message: " + err);
            err.printStackTrace();
        }

        if(elapsedTimer.remainingTimeMillis() <= 0)
            System.out.println("TIME OUT " + elapsedTimer.remainingTimeMillis());

        return null;
    }

    public final void close()
    {
        try
        {
            input.close();
            output.close();

        }catch(IOException e)
        {
            System.out.println("IO Exception closing the buffers: " + e);
            e.printStackTrace();
        }catch(Exception e)
        {
            System.out.println("Exception closing the buffers: " + e);
            e.printStackTrace();
        }
    }

}
