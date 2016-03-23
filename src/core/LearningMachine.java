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
public class LearningMachine {
    public static final boolean VERBOSE = false;


    /**
     * Reads and launches a game for a human to be played. Graphics always on.
     *
     * @param game_file  game description file.
     * @param level_file file with the level to be played.
     */
    public static double playOneGame(String game_file, String level_file, String actionFile, int randomSeed, boolean isTraining) throws IOException {
        String agentName = "controllers.human.Agent";
        boolean visuals = true;
        return runOneGame(game_file, level_file, visuals, agentName, actionFile, randomSeed, isTraining);
    }

    /**
     * Reads and launches a game for a bot to be played. Graphics can be on or off.
     *
     * @param game_file  game description file.
     * @param level_file file with the level to be played.
     * @param visuals    true to show the graphics, false otherwise. Training games have never graphics set to ON.
     * @param agentName  name (inc. package) where the controller is otherwise.
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param randomSeed sampleRandom seed for the sampleRandom generator.
     */
    public static double runOneGame(String game_file, String level_file, boolean visuals,
                                    String agentName, String actionFile, int randomSeed, boolean isTraining) throws IOException {
        int trainingPlays = 0;

        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();

        System.out.println(" ** Playing game " + game_file + ", level " + level_file + " **");

        //Create the player.
        LearningPlayer player = LearningMachine.createPlayer(agentName);

//
        //2. Play the training games.
        System.out.print(trainingPlays + " ");
        double finalScore = playOnce(player, actionFile, game_file, level_file, visuals, randomSeed, isTraining);

        return finalScore;
    }

    private static double playOnce(LearningPlayer player, String actionFile, String game_file, String level_file,
                                   boolean visuals, int randomSeed, boolean isTraining) throws IOException {
        //Create the game.
        Game toPlay = new VGDLParser().parseGame(game_file);
        toPlay.buildLevel(level_file);

        //Init the player for the game.
        LearningMachine.initPlayer(player, actionFile, toPlay.getView(), randomSeed, isTraining);

        if (player == null) {
            //Something went wrong in the constructor, controller disqualified
            toPlay.disqualify();

            //Get the score for the result.
            return toPlay.handleResult();
        }

        //Then, play the game.
        double score = 0.0;
        if (visuals)
            score = toPlay.playGame(player, randomSeed);
        else
            score = toPlay.runGame(player, randomSeed);

        //Finally, when the game is over, we need to tear the player down.
        LearningMachine.tearPlayerDown(player, toPlay);

        return score;
    }

    /**
     * Creates a player given its name. This method starts the process that runs this client.
     *
     * @param cmd name of the agent to create.
     * @return the player, created but NOT initialized, ready to start playing the game.
     */
    private static LearningPlayer createPlayer(String cmd) throws IOException {


        Process client;


        client = Runtime.getRuntime().exec(cmd);


        return new LearningPlayer(client);
    }


//
//    private static void printLines(String name, InputStream ins) {
//        String line = null;
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(ins));
//
//            while ((line = in.readLine()) != null) {
//                System.out.println(name + " " + line);
//            }
//
//    }

    /**
     * Inits the player for a given game.
     *
     * @param player     Player to init.
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param sv         Initial state of the game to be played by the agent.
     * @param randomSeed Seed for the sampleRandom generator of the game to be played.
     * @return the player, created and initialized, ready to start playing the game.
     */
    private static LearningPlayer initPlayer(LearningPlayer player, String actionFile, StateView sv,
                                             int randomSeed, boolean isTraining) {


        //Determine the time due for the controller initialization.
        ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
        ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

        //Initialize the controller.
        player.init(sv, ect, isTraining);

        //Check if we returned on time, and act in consequence.
        long timeTaken = ect.elapsedMillis();
        if (ect.exceededMaxTime()) {
            long exceeded = -ect.remainingTimeMillis();
            System.out.println("Controller initialization time out (" + exceeded + ").");

            return null;
        } else {
            //System.out.println("Controller initialization time: " + timeTaken + " ms.");
        }

        //If we have a player, set it up for action recording.
        if (player != null)
            player.setup(actionFile, randomSeed);


        return player;
    }

    /**
     * Tears the player down. This initiates the saving of actions to file.
     * It should be called when the game played is over.
     *
     * @param player player to be closed.
     */
    private static void tearPlayerDown(LearningPlayer player, Game toPlay) throws IOException {
        //Determine the time due for the controller initialization.
        ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
        ect.setMaxTimeMillis(CompetitionParameters.FINALIZATION_TIME);

        player.finishGame(toPlay.getView(), ect);

        player.teardown();
        //player.close();
    }
}

