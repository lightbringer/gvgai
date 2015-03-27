package core;

import core.competition.CompetitionParameters;
import core.game.Game;
import core.game.StateObservation;
import core.game.StateView;
import core.player.AbstractPlayer;
import core.player.LearningPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.StatSummary;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 06/11/13
 * Time: 11:24
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class LearningMachine
{
    public static final boolean VERBOSE = false;

    /**
     * Reads and launches a game for a human to be played. Graphics always on.
     * @param game_file game description file.
     * @param level_file file with the level to be played.
     */
    public static double playOneGame(String game_file, String level_file, String actionFile, int randomSeed)
    {
        String agentName = "controllers.human.Agent";
        boolean visuals = true;
        return runOneGame(game_file, level_file, visuals, agentName, actionFile, 0, randomSeed);
    }

    /**
     * Reads and launches a game for a bot to be played. Graphics can be on or off.
     * @param game_file game description file.
     * @param level_file file with the level to be played.
     * @param visuals true to show the graphics, false otherwise. Training games have never graphics set to ON.
     * @param agentName name (inc. package) where the controller is otherwise.
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param trainingPlays Number of training plays before an evaluation (thus the player will play trainingPlays+1 games)
     * @param randomSeed sampleRandom seed for the sampleRandom generator.
     */
    public static double runOneGame(String game_file, String level_file, boolean visuals,
                                    String agentName, String actionFile, int trainingPlays, int randomSeed)
    {
        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();

        System.out.println(" ** Playing game " + game_file + ", level " + level_file + " **");

        //Create the player.
        LearningPlayer player = LearningMachine.createPlayer(agentName);

        //1. Play the training games.
        for(int trainingGame = 0; trainingGame < trainingPlays; trainingGame++)
        {
            playOnce(player, actionFile, game_file, level_file, false, randomSeed, true);
        }

        //2. Play the training games.
        double finalScore = playOnce(player, actionFile, game_file, level_file, visuals, randomSeed, false);

        return finalScore;
    }

    private static double playOnce(LearningPlayer player, String actionFile, String game_file, String level_file,
                                   boolean visuals, int randomSeed, boolean isTraining)
    {
        //Create the game.
        Game toPlay = new VGDLParser().parseGame(game_file);
        toPlay.buildLevel(level_file);

        //Init the player for the game.
        LearningMachine.initPlayer(player, actionFile, toPlay.getView(), randomSeed, isTraining);

        if(player == null)
        {
            //Something went wrong in the constructor, controller disqualified
            toPlay.disqualify();

            //Get the score for the result.
            return toPlay.handleResult();
        }

        //Then, play the game.
        double score = 0.0;
        if(visuals)
            score = toPlay.playGame(player, randomSeed);
        else
            score = toPlay.runGame(player, randomSeed);

        //Finally, when the game is over, we need to tear the player down.
        LearningMachine.tearPlayerDown(player, toPlay);

        return score;
    }

    /**
     * Creates a player given its name. This method starts the process that runs this client.
     * @param playerName name of the agent to create.
     * @return the player, created but NOT initialized, ready to start playing the game.
     */
    private static LearningPlayer createPlayer(String playerName)
    {
        boolean python = playerName.contains(".py");
        boolean java = playerName.contains(".java");

        Process client;

        //TODO: For the moment we use something fixed.
        try{
            String cmd = null;
            if(python)
                cmd = "/usr/bin/python " + playerName;
            else if(java)
            {
                //Compile
                String path = playerName.substring(0, playerName.lastIndexOf("/"));
                compileJava(path);

                //Execute (prepare the line)
                String agent = playerName.substring(playerName.lastIndexOf("/")+1, playerName.lastIndexOf("."));
                cmd = "/usr/bin/java -cp " + path + " -Xms512m -Xmx2048m " +  agent;
            }

            client = Runtime.getRuntime().exec(cmd);
            if(VERBOSE) System.out.println(cmd);

        }catch(IOException e)
        {
            System.out.println("IO Exception creating the client process: " + e);
            e.printStackTrace();
            return null;
        }catch (Exception e){
            System.out.println("Exception creating the client process: " + e);
            e.printStackTrace();
            return null;
        }

        return new LearningPlayer(client);
    }

    private static void compileJava(String path) throws Exception
    {
        String cmd = "find " + path + " | grep \\.java";
        Process p = Runtime.getRuntime().exec(cmd);
        if(VERBOSE) System.out.println(cmd);

        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

        while ((line = in.readLine()) != null) {

            if(line.contains(".java"))
            {
                cmd = "/usr/bin/javac -cp " + path + " " + line;
                //System.out.println(cmd);
                Process proc = Runtime.getRuntime().exec(cmd);
                if(VERBOSE) System.out.println(cmd);
                proc.waitFor(); //We need to wait for the compilation process to finish before moving on.
            }
        }
    }


    private static void printLines(String name, InputStream ins) {
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins));
        try {
            while ((line = in.readLine()) != null) {
                System.out.println(name + " " + line);
            }
        }catch (Exception e) {}
    }

    /**
     * Inits the player for a given game.
     * @param player Player to init.
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param sv Initial state of the game to be played by the agent.
     * @param randomSeed Seed for the sampleRandom generator of the game to be played.
     * @return the player, created and initialized, ready to start playing the game.
     */
    private static LearningPlayer initPlayer(LearningPlayer player, String actionFile, StateView sv,
                                             int randomSeed, boolean isTraining)
    {

        try{
            //Determine the time due for the controller initialization.
            ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
            ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

            //Initialize the controller.
            player.init(sv, ect, isTraining);

            //Check if we returned on time, and act in consequence.
            long timeTaken = ect.elapsedMillis();
            if(ect.exceededMaxTime())
            {
                long exceeded =  - ect.remainingTimeMillis();
                System.out.println("Controller initialization time out (" + exceeded + ").");

                return null;
            }
            else
            {
                //System.out.println("Controller initialization time: " + timeTaken + " ms.");
            }

            //If we have a player, set it up for action recording.
            if(player != null)
                player.setup(actionFile, randomSeed);

        }catch (Exception e)
        {
            //This probably happens because controller took too much time to be created.
            e.printStackTrace();
            System.exit(1);
        }

        return player;
    }

    /**
     * Tears the player down. This initiates the saving of actions to file.
     * It should be called when the game played is over.
     * @param player player to be closed.
     */
    private static void tearPlayerDown(LearningPlayer player, Game toPlay)
    {
        //Determine the time due for the controller initialization.
        ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
        ect.setMaxTimeMillis(CompetitionParameters.FINALIZATION_TIME);

        player.finishGame(toPlay.getView(), ect);

        player.teardown();
        //player.close();
    }
}

