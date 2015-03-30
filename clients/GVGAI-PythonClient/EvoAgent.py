from ee import EvoEpisodic
from FeatureExtraction import *




def ExceptionHandler(f):
    def wrapper(*args):
        try:
            result = f(*args)
            return result
        except Exception, e:
            with open("error.log", "a") as error_log:
                error_log.write(str(e))

    return wrapper

class EvoAgent(object):

     @ExceptionHandler
     def __init__(self, logger):

        """
        The agent is created when playing in a new game starts.
        :return: None.
        """
        #raise Exception("Crap")
        self.ee = None
        self.logger = logger

        self.popsize = 10
        self.action_selection = 1 # 0 is e-greedy, 1 is softmax
        self.layers =   [
                    ("RectifiedLinear", 20),
                    ("Linear", )
        ]

        self.learning_rate = 1.

     @ExceptionHandler
     def init(self, game, avatar, remMillis):

        """
        This function is called when each game is started.
        :param game Information about the game.
        :param avatar Information about the avatar.
        :param remMillis Number of milliseconds when this function is due to finish.
        :return: None.
        """
        if self.ee == None:
            self.ee = EvoEpisodic(len(avatar.actionList), self.layers, self.popsize, self.action_selection, self.learning_rate)

        senses_all = features(game, avatar)
        dead_actions = []
        #This is just to compile and save time for the game cycles
        self.ee.predict(senses_all, dead_actions)

     @ExceptionHandler
     def act(self, game, avatar, remMillis):

        """
        This function is called at every game cycle of a given game.
        :param game Information about the game.
        :param avatar Information about the avatar.
        :param remMillis Number of milliseconds when this function is due to finish.
        :return: The ACTION (one from the ones defined in avatar.actionList) to apply in the game this cycle.
        """
        senses_all = features(game, avatar)
        dead_actions = []

        desired_action, a = self.ee.predict(senses_all, dead_actions)
        return avatar.actionList[desired_action]

     @ExceptionHandler
     def end(self, game, avatar, remMillis):

        """
        This function is called when a game has finished.
        :param game Information about the game.
        :param avatar Information about the avatar.
        :param remMillis Number of milliseconds when this function is due to finish.
        :return: None.
        """
        score = game.score
        if game.gameOver and game.gameWinner == 'PLAYER_WINS':
            score = score + 10000

        self.ee.fit(score)
        self.logger.info("Finished training... FIT: " + str(score))


