from Ontology import *

def game_features(game):
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

    return all_features

def avatar_features(avatar):
    all_features = []

    all_features.append(avatar.speed)
    for r in avatar.resources:
        all_features.append(avatar.resources[r])

    return all_features