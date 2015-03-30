from EvoAgent import EvoAgent

class EvoAgentEpsilonGreedyLinear(EvoAgent):
    def __init__(self, logger):
        super(EvoAgentEpsilonGreedyLinear, self).__init__(logger)

        self.popsize = 10
        self.action_selection = 0 # 0 is e-greedy, 1 is softmax
        self.layers =   [
                    ("Linear", )
        ]

        self.learning_rate = 1.

class EvoAgentSoftmaxLinear(EvoAgent):
    def __init__(self, logger):
        super(EvoAgentSoftmaxLinear, self).__init__(logger)

        self.popsize = 10
        self.action_selection = 1 # 0 is e-greedy, 1 is softmax
        self.layers =   [
                    ("Linear", )
        ]

        self.learning_rate = 1.


class EvoAgentEpsilonGreedyNN(EvoAgent):
    def __init__(self, logger):
        super(EvoAgentEpsilonGreedyNN, self).__init__(logger)

        self.popsize = 10
        self.action_selection = 0 # 0 is e-greedy, 1 is softmax
        self.layers =   [
                    ("RectifiedLinear", 20),
                    ("Linear", )
        ]

        self.learning_rate = 1.

class EvoAgentEpsilonSoftmaxNN(EvoAgent):
    def __init__(self, logger):
        super(EvoAgentEpsilonSoftmaxNN, self).__init__(logger)

        self.popsize = 10
        self.action_selection = 1 # 0 is e-greedy, 1 is softmax
        self.layers =   [
                    ("RectifiedLinear", 20),
                    ("Linear", )
        ]

        self.learning_rate = 1.