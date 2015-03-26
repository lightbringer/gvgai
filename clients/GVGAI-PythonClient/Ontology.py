import os

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

