package core.competition;

import core.ArcadeMachine;
import core.LearningMachine;

import java.io.IOException;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class AgentExecutorLearning {

    public static void main(String[] args) throws IOException {

        String map = args[0];
        String level = args[1];
        String playerClassString = args[2];
        String action_file = args[3];
        Boolean isTraining = Boolean.valueOf(args[4]);
        System.out.println("Map: " + map);
        System.out.println("level: " + level);
        System.out.println("Player Execution String: " + playerClassString);
        System.out.println("Agent Action file: " + action_file);
        System.out.println("IsTraining?: " + isTraining);

        int seed = new Random().nextInt();

        double gameScore = LearningMachine.runOneGame(map, level, false, playerClassString, action_file, seed, isTraining);
        //System.out.println(gameScore);
    }
}
