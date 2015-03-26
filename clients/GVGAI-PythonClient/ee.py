import numpy as np
from sknn import sknn, IncrementalMinMaxScaler
from snes import SNES


np.set_printoptions(precision=4)
np.set_printoptions(suppress=True)







popsize = 100


class EvoEpisodic():
    def __init__(self,  n_actions, coevolution = False, rounds = 1):

        self.n_actions = n_actions
        layers =  [
                    #("RectifiedLinear", 200),
                   # ("RectifiedLinear", 20),
                    ("Linear", )
                   ]
        self.sknns = [sknn(layers, input_scaler=IncrementalMinMaxScaler()) for _ in range(rounds)]
        self.all_initialised = 0


        self.asked_position = []

        self.coevolution = coevolution
        if(coevolution):
            self.challenger = [sknn(layers, input_scaler=IncrementalMinMaxScaler()) for _ in range(rounds)]
            self.challenger_iterations = 1



        self.states = []



    def predict(self, senses_all,  round = 0, challenger = False,   print_probs = True,detect_outliers = False, dead_actions = [], min_random = 1, enable_probs = True):


        self.states.append((senses_all, challenger))
        round = 0
        if(challenger):
            #exit()
            return 1, [0]


        if(challenger and self.coevolution):
            cn = self.challenger[round]
            assert(False)
        else:
            cn = self.sknns[round]
        senses_all = np.array([senses_all])
        #print len(self.mlps)
        if(self.all_initialised == 0 ):

            if(cn.ds is None):
                cn.linit(senses_all,np.array([[0.0]*self.n_actions]))
                if(self.coevolution):
                    self.challenger[round].linit(senses_all,np.array([[0.0]*self.n_actions]))

            self.all_initialised = 1
            for mlp in self.sknns:
                    if(mlp.ds is None):
                        self.all_initialised = 0
                        break

        if(self.all_initialised == 1):


            snes, ask, tell = self.__initNES()
            self.sness = snes
            self.asked =  ask
            self.tell = tell
            self.asked_position =  0
            self.generation =  1

            #print "initalised", exit()
            if(self.coevolution):
                self.__genomeToMlp(self.challenger,  0)
            self.all_initialised = 2




        desired_action = cn.action_selector.softmax(senses_all, dead_actions)

        return desired_action, [0]


    def __genomeToMlp(self, sknns, position):
        last_size = 0
        #print self.asked_position[population]
        asked = self.asked[position]
        #print "position", position
        for sknn in sknns:
            for layer in sknn.mlp.layers:
                weights = layer.get_weights()
                cma_weights = asked[last_size:last_size+ weights.size]
                last_size += weights.size

                biases = layer.get_biases()
                bias_weights = asked[last_size:last_size+ biases.size]
                last_size += biases.size

                layer.set_weights(cma_weights.reshape(weights.shape))
                layer.set_biases(bias_weights.reshape(biases.shape))


    def fit(self, reward):


        if( self.all_initialised == 2):
            ## update the rewards (and possibly train)
                self.tell.append(reward)
                if(len(self.tell) == len(self.asked) == popsize):

                    #print "Updating ES..."
                    if(self.coevolution):
                        if(self.generation%self.challenger_iterations == 0 ):
                            #print "Changing challenger"
                            next_challenger = np.array(self.tell).argmax()
                            self.__genomeToMlp(self.challenger,  next_challenger)
                    self.sness.tell(self.asked, self.tell)
                    self.clearOldGeneration()






                else:
                    self.asked_position+=1
                    self.__genomeToMlp(self.sknns,  self.asked_position)


    def clearOldGeneration(self):
        if(self.all_initialised == 2):
            self.asked = self.sness.ask()
            self.asked_position = 0
            self.tell = []
            self.generation+=1
            self.__genomeToMlp(self.sknns,  self.asked_position)


    def __initNES(self):




        total_weights = 0

        for mlp in self.sknns:
            for layer in mlp.mlp.layers:
                total_weights += layer.get_weights().size
                total_weights += layer.get_biases().size

        snes = SNES(np.zeros(total_weights), popsize=popsize)
        asked = snes.ask()


        tell = []

        return snes, asked, tell










def test():
    episodes = 100000

    s_0 = [0,0,0.1]
    s_1= [0,0.0,2]
    ac = EvoEpisodic(n_actions=2)
    avg_rwrd = 0
    for i in xrange(1,episodes):
        action_0,_ = ac.predict(s_0,print_probs=False)
        action_1,_ = ac.predict(s_1,print_probs=False)

        print action_0, action_1, "myact"
        if(action_0 == 1 and action_1 ==1):
            score = 1
        else:
            score = -1

        ac.fit([score])
        #ac.predict_mean([0,1,1,1])
        #ac.predict_mean([1,1,1,1])
        avg_rwrd+=score
        print avg_rwrd/float(i)
        print "====================", i

    print "Actors:" ,  ac.critics_Q
    print "Critics:", ac.actors

def test2():

    import os
    episodes = 100000

    ac = EvoEpisodic(3, coevolution= True)

    #for i in range(1000)
    #    ac.fit([[[0,1,1,1],0]],1000)
    #    ac.fit([[[1,1,1,1],0]],1000)


    for i in xrange(0,episodes):
        print "++++++++++++++++++++++++++++++++++++++++++++="
        #print random()
        s_0= [1,1,1,1]
        s_1 = [1,1,1,1]
        action_0,_ = ac.predict(s_0,challenger=False, print_probs=True)
        action_1,_ = ac.predict(s_1,challenger=True, print_probs=True)
        #action_2 = ac.predict([1,1,1,2])

        #print action_0, action_1, "myact"
        a_0 = action_0
        a_1 = action_1

        # if(action_0 == 2):
        #     a_0 = np.random.randint(0,2)
        #
        #
        # elif(action_1 == 2):
        #     a_1 = np.random.randint(0,2)


        reward = 1



        if(a_0 != a_1 ):
            areward = [reward, -reward]

        else:
            areward = [-reward, reward]


        if(a_0 == 2):
            areward[0] = -reward

        if(a_1 == 2):
            areward[1] = -reward



        ac.fit(areward[0])


        print "===================="

    print "Actors:" ,  ac.critics_Q
    print "Critics:", ac.actors
    exit()

def test3():
    avg_rwrd = 0
    episodes = 100000

    state_0 = [1]*20+[0]
    state_1= [1]*20+[1]
    ac = EvoEpisodic(2)
    from random import sample
    for i in xrange(1,episodes):
        state = sample([state_0,state_1],1)[0]
        #print state
        #print state
        action_0, _ = ac.predict(state,print_probs=False)


        if(action_0 == state[-1]):
            score = 1;
        else:
            score = -1;

        avg_rwrd+=score
        print avg_rwrd/float(i)
        ac.fit(score)
        #ac.predict_mean([0,1,1,1])
        #ac.predict_mean([1,1,1,1])
        #print "====================", i

    print "Actors:" ,  ac.critics_Q
    print "Critics:", ac.actors



def test4():
    avg_rwrd = 0
    episodes = 100000000

    state_0 = [1]*20+[0]
    state_1= [1]*20+[1]
    ac = EvoEpisodic(2)
    from random import sample
    for i in xrange(1,episodes):
        #state = sample([state_0,state_1],1)[0]
        state = [1,2,3,4]
        state.append(np.random.random())
        #print state
        #print state
        action_0, _ = ac.predict(state,print_probs=False)

        score = -1
        if(state[-1] > 0.5 and action_0 == 1):
            score = 1
        if(state[-1] < 0.5 and action_0 == 0):
            score = 1




        avg_rwrd+=score
        print avg_rwrd/float(i)
        ac.fit(score)
        #ac.predict_mean([0,1,1,1])
        #ac.predict_mean([1,1,1,1])
        #print "====================", i

    print "Actors:" ,  ac.critics_Q
    print "Critics:", ac.actors



def test5():
    avg_rwrd = []
    episodes = 100000000

    state_0 = [1]*20+[0]
    state_1= [1]*20+[1]
    ac = EvoEpisodic(2)
    from random import sample
    for i in xrange(0,episodes):
        #state = sample([state_0,state_1],1)[0]
        state = [1,2,3,4, -1]
        scores_all = []
        my_roll = np.random.random()
        hidden_roll = np.random.random()
        for k in range(0,popsize):

                    #print state
                #print state
            state[-1]= my_roll
            action_0, _ = ac.predict(state,print_probs=False)



            score = -1
            if(state[-1] > hidden_roll and action_0 == 1):
                    score = 1
            if(state[-1] < hidden_roll and action_0 == 1):
                    score = -1

            if(action_0 == 0 ):
                    score = 0


            #scores_all.append(score)
            #print state, hidden_roll
            ac.fit(score)



            avg_rwrd.append(score)
        #print avg_rwrd/float(i)
        print my_roll, action_0, score,  np.array(avg_rwrd).mean(), i

        if(i%10000  == 0):
            avg_rwrd = []
        #s = np.array(scores_all).mean()
        # print s
        #ac.fit(s)
        #ac.predict_mean([0,1,1,1])
        #ac.predict_mean([1,1,1,1])
        #print "====================", i

    print "Actors:" ,  ac.critics_Q
    print "Critics:", ac.actors


if __name__=="__main__":
    test3()


