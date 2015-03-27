from Ontology import *
import numpy as np

LARGE = 10000000
MAX_DEFAULT_RES = 4

def features(game, avatar):
    all_features = []

    all_features.append(game.score)
    all_features.append(game.gameTick)

    if game.gameWinner == 'NO_WINNER':
        all_features.append(-1)
    elif game.gameWinner == 'PLAYER_LOSES':
        all_features.append(0)
    elif game.gameWinner == 'PLAYER_WINS':
        all_features.append(1)

    if game.gameOver:
        all_features.append(1)
    else:
        all_features.append(0)

    numRes = len(avatar.resources)
    extraRes = MAX_DEFAULT_RES - numRes
    for r in avatar.resources:
        all_features.append(avatar.resources[r])

    for er in range(extraRes):
        all_features.append(0)

    for id in game.grid:
        spriteBitMap = game.grid[id]
        minDistance = LARGE
        numRows = spriteBitMap.shape[0]
        numColumns = spriteBitMap.shape[1]

        for r in range(numRows):
            for c in range(numColumns):

                if spriteBitMap[r][c] == 1:
                    spritePos = [r*game.blockSize, c*game.blockSize]
                    dist = distance(spritePos, avatar.position)

                    if dist < minDistance:
                        minDistance = dist

        if minDistance == LARGE:
            minDistance = -1

        all_features.append(minDistance)

    all_features.append(avatar.speed)

    return all_features


def distance(toPos, fromPos):
    sumSq = pow(toPos[0]-fromPos[0], 2) + pow(toPos[1]-fromPos[1], 2)
    return np.sqrt(sumSq)

