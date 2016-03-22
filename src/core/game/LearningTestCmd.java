//package core.game;
//
//import core.LearningMachine;
//
//import java.util.Random;
//
///**
// * Created with IntelliJ IDEA.
// * User: Diego
// * Date: 04/10/13
// * Time: 16:29
// * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
// */
//public class LearningTestCmd
//{
//
//    public static void main(String[] args)
//    {
//        //Available controllers:
//        //String pythonController = "/Users/dperez/Work/git/gvgai/clients/GVGAI-PythonClient/EvoClient.py";
//        String pythonController = "/Users/dperez/Work/git/gvgai/clients/GVGAI-PythonClient/PyClient.py";
//        String javaController = "/Users/dperez/Work/git/gvgai/clients/GVGAI-JavaClient/src/JavaClient.java";
//
//        //Available games:
//        String gamesPath = "examples/gridphysics/";
//
//        //CIG 2014 Training Set Games
//        String games[] = new String[]{"aliens", "boulderdash", "butterflies", "chase", "frogs",
//                                      "missilecommand", "portals", "sokoban", "survivezombies", "zelda",
//                                      "camelRace", "digdug", "firestorms", "infection", "firecaster",
//                                      "overload", "pacman", "seaquest", "whackamole", "eggomania",
//                                      "bait", "boloadventures", "brainman", "chipschallenge",  "modality",
//                                      "painter", "realportals", "realsokoban", "thecitadel", "zenpuzzle"};
//
//
//        //Other settings
//        boolean visuals = false;
//        String recordActionsFile = null; //where to record the actions executed. null if not to save.
//        int seed = new Random().nextInt();
//
//        if(args.length != 4)
//        {
//            System.out.println("ERROR. Usage: java -jar gvgai.jar <pathToPyClient.py> <pyAgentFile> <gameidx> <trials>");
//            return;
//        }
//
//        //LearningMachine.PYTHON_BIN = "/scratch/ssamot/anaconda/bin/python";
//        //LearningMachine.JAVA_BIN = "/scratch/ssamot/jdk1.8.0_40/bin/java";
//        pythonController = args[0];
//        String pyAgentFile = pythonController + " " + args[1];
//        int gameIdx = Integer.parseInt(args[2]);
//        int trainingPlays = Integer.parseInt(args[3]);
//
//        //Game and level to play
//        int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
//        String game = gamesPath + games[gameIdx] + ".txt";
//        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";
//
//        // 1. This starts a game, in a level, played by a human.
//        //ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);
//
//        // 2. This plays a game in a level by the controller (through the "Learning Machine").
//        //LearningMachine.runOneGame(game, level1, visuals, javaController, recordActionsFile, trainingPlays, seed);
//        LearningMachine.runOneGame(game, level1, visuals, pyAgentFile, recordActionsFile, trainingPlays, seed);
//
//        // 3. This replays a game from an action file previously recorded
//        //String readActionsFile = "seminar/SeaQuest.txt";  //This example is for
//        //ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);
//
//    }
//}
