from __future__ import print_function
import numpy as np
import random
import sys
import os
from ee import EvoEpisodic

class GVGame:

    def __init__(self):
        self.score = 0
        self.gameTick = -1
        self.gameWinner = 0
        self.gameOver = False
        self.worldDim = [0,0]
        self.blockSize = 0
        self.grid = {}
        self.remMillis = 0

    def printToFile(self, gameIdx):
        f = open("pyclient-test-game-" + str(gameIdx) + ".txt", 'w')
        f.write("Score " + str(self.score) + ", GameTick: " + str(self.gameTick) + ", Winner: " + str(self.gameWinner)
        + ", GameOver: " + str(self.gameOver)+ ", WorldDimW: " + str(self.worldDim[0])+ ", WorldDimW: " + str(self.worldDim[1])
        + ", BlockSize: " + str(self.blockSize) + ", RemMillis: " + str(self.remMillis) + os.linesep)

        for id in self.grid:
            spriteBitMap = self.grid[id]

            f.write("Sprite Type: " + str(id) + " bit array: " + os.linesep)
            for r in spriteBitMap:
                for c in r:
                    f.write(str(c))
                f.write(os.linesep)

        f.close()


class GVGAvatar:

    def __init__(self):
        self.actionList = []
        self.position = [0,0]
        self.speed = 0
        self.lastAction = 0
        self.resources = {}

    def printToFile(self, gameIdx):
        f = open("pyclient-test-avatar-" + str(gameIdx) + ".txt", 'w')
        f.write("Position X: " +  str(self.position[0]) + ", Position Y: " +  str(self.position[1]) +
        ", Speed: " + str(self.speed) + ", Last Action: " +  str(self.lastAction) + os.linesep + "Actions: {")

        for a in self.actionList:
            f.write(str(a) + ",")
        f.write("}" + os.linesep)

        f.write("Resources: {")
        for r in self.resources:
            f.write("(" + str(r) + "," + str(self.resources[r]) + ")")
        f.write("}" + os.linesep)

        f.close()


class CommState:
    START, INIT, INIT_END, ACT, ACT_END, ENDED, ENDED_END = range(7)

class PyClient:

    def __init__(self):
        self.commState = CommState.START
        self.game = GVGame()
        self.avatar = GVGAvatar()
        self.numGames = 0

    def writeToPipe(self, line):
        sys.stdout.write(line + os.linesep)
        sys.stdout.flush()


    def listen(self):
        """
        This process is continuously listening for commands from the GVGAI engine.
        """
        messageIdx = 0
        line = "start"
        while (line != ""):

            #Read the line
            line = sys.stdin.readline()
            self.commState = self.processCommLine(line)
            self.processLine(line)

            #if self.commState == CommState.INIT: #In this state, we are still receiving init information
            #if self.commState == CommState.INIT_END: #In this state, we are still receiving init information
            #if self.commState == CommState.ACT: #In this state, we are receiving state information for ACTING
            #if self.commState == CommState.ACT_END: #In this state, we have finished receiving information for ACTING.
            #if self.commState == CommState.ENDED: #In this state, the game has ended, and we are receiving the last state.
            #if self.commState == CommState.ENDED_END: #In this state, the game has ended, and we have received all final state info.

            if self.commState == CommState.INIT_END:
                #We can work on some initialization stuff here.


                self.ee = EvoEpisodic(len(self.avatar.actionList))

                self.writeToPipe("INIT_DONE.")

            if self.commState == CommState.ACT_END:
                #This is the place to think and return what action to take.
                ##rndAction = random.choice(self.avatar.actionList)
                senses_all = []
                dead_actions = []

                desired_action = self.ee.predict(senses_all, dead_actions)
                self.writeToPipe(desired_action)

            if self.commState == CommState.ENDED_END:
                #We can study what happened in the game here.

                #For debug, print here game and avatar info:
                #self.game.printToFile(self.numGames)
                #self.avatar.printToFile(self.numGames)

                #Also, we need to reset game and avatar back
                self.game = GVGame()
                self.avatar = GVGAvatar()
                score = 0
                
                self.ee.fit(score)

                self.writeToPipe("GAME_DONE.")

            messageIdx += 1




    def processCommLine(self, line):

        if "INIT-END" in line:
            splitLine = line.split(" ")
            self.game.remMillis = int(splitLine[1])
            return CommState.INIT_END

        if "ACT-END" in line:
            splitLine = line.split(" ")
            self.game.remMillis = int(splitLine[1])
            return CommState.ACT_END

        if "ENDGAME-END" in line:
            splitLine = line.split(" ")
            self.game.remMillis = int(splitLine[1])
            return CommState.ENDED_END


        if "INIT" in line:
            self.numGames+=1
            splitLine = line.split(" ")
            self.isTraining = (splitLine[1] == 'true')
            return CommState.INIT

        if "ACT" in line:
            return CommState.ACT

        if "ENDGAME" in line:
            return CommState.ENDED

        return self.commState


    def processLine(self, line):

        """
        This function process a line received.
        """

        splitLine = line.split("#")
        lineType = splitLine[0]


        if lineType == "Game":
            self.game.score = float(splitLine[1])
            self.game.gameTick = int(splitLine[2])
            self.game.gameWinner = splitLine[3]
            self.game.gameOver = (splitLine[4] == 'true')

            if len(splitLine) > 6: # It will only be >6 in the init case.
                self.game.worldDim = [int(splitLine[5]), int(splitLine[6])]
                self.game.blockSize = int(splitLine[7])


        elif lineType == "Actions":
            actions = splitLine[1].split(",")
            for act in actions: self.avatar.actionList.append(act)

        elif lineType == "Avatar":
            self.avatar.position = [float(splitLine[1]), float(splitLine[2])]
            self.avatar.speed = float(splitLine[3])
            self.avatar.lastAction = splitLine[4]

            if len(splitLine[5]) > 0:
                #We have resources:
                resources = splitLine[5].split(";")
                for r in resources:

                    key = int(r.split(",")[0])
                    val = int(r.split(",")[1])

                    self.avatar.resources[key] = val

        elif lineType[0] == 's': #Observation Grid
            spriteID = int(lineType[1:])
            bitData = splitLine[1].split(",")

            nRows = len(bitData)
            nColumns = len(bitData[0])

            spriteBitMap = np.ndarray(shape=(nRows, nColumns), dtype=int)

            for r in range(0,nRows):
                row = bitData[r]
                for c in range (0, nColumns):
                    spriteBitMap[r][c] = int(row[c])

            self.game.grid[spriteID] = spriteBitMap

        #print(self.game)

        return False


if __name__=="__main__":
    pyClient = PyClient()
    pyClient.listen()