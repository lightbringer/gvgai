from Ontology import *
from ee import EvoEpisodic
from FeatureExtraction import *

class EvoAgent:

     def __init__(self, logger):

        """
        The agent is created when playing in a new game starts.
        :return: None.
        """
        self.ee = None
        self.logger = logger


     def init(self, game, avatar, remMillis):

        """
        This function is called when each game is started.
        :param game Information about the game.
        :param avatar Information about the avatar.
        :param remMillis Number of milliseconds when this function is due to finish.
        :return: None.
        """
        if self.ee == None:
            self.ee = EvoEpisodic(len(avatar.actionList))

        senses_all = features(game, avatar)
        dead_actions = []
        #This is just to compile and save time for the game cycles
        self.ee.predict(senses_all, dead_actions)


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


