import pylab
import scipy
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.pyplot import errorbar
from scipy.stats.mstats import kruskalwallis as anova
from scipy.stats import shapiro as normality_test
from scipy.stats import mannwhitneyu as nonparam
from scipy.stats import ttest_ind as ttest
import csv
import os

games = ["aliens", "boulderdash", "butterflies", "chase", "frogs",
         "missilecommand", "portals", "sokoban", "survivezombies", "zelda"]


def errorfill(x, y, yerr, color=None, alpha_fill=0.3, ax=None):
    ax = ax if ax is not None else plt.gca()
    if color is None:
        color = ax._get_lines.color_cycle.next()
    if np.isscalar(yerr) or len(yerr) == len(y):
        ymin = [a - b for a,b in zip(y, yerr)]
        ymax = [sum(pair) for pair in zip(y, yerr)]
    elif len(yerr) == 2:
        ymin, ymax = yerr

    ax.plot(x, y, color=color)
    #print ymin
    #print y
    #print ymax
    ax.fill_between(x, ymax, ymin, color=color, alpha=alpha_fill)


def plot(data, numLines, ylabel, filename, show_plot, ylims=None):

    #Create a figure
    fig = pylab.figure()

    #Add a subplot (Grid of plots 1x1, adding plot 1)
    ax = fig.add_subplot(111)

    #All data we need is in avarage*AtTrial. Calculate averages for each repeat, stddev and stderr
    averages = []
    std_dev = []
    std_err = []
    for i in range(numLines):
        averages.append(np.average(data[i]))
        stdev = np.std(data[i])
        std_dev.append(stdev)
        std_err.append(stdev / np.sqrt(numLines))


    errorfill(range(numLines), averages, std_err)

    plt.legend("Running average of victories",
               shadow=True, fancybox=True)

    #Titles and labels
    #plt.title('Heuristic estimation: 8 routes')
    plt.xlabel("Trial", fontsize=16)
    plt.ylabel(ylabel, fontsize=16)

    if ylims != None:
        plt.ylim(ylims)


    #Save to file.
    fig.savefig(filename)

    # And show it:
    if show_plot:
        plt.show()



def plot_game(input_dir, output_dir, game_number, show_plot = False):
    repeats = 50
    numLines = 1000
    AVOID_FIRST = 10 #AVOID_FIRST number of trials before computing accum. averages.

    averageWinAtTrial = [[] for i in range(1000)]
    averageScoresAtTrial = [[] for i in range(1000)]
    averageTimesAtTrial = [[] for i in range(1000)]

    #1. This first loop calculates the data out of the raw output.
    for i in range(repeats):
        filename = input_dir + str(game_number) + "-" + str(i) + ".out"
        #print filename
        repeatWins = []
        repeatScores = []
        repeatTimespents = []

        f = open(filename, 'rb')
        csvReader = csv.reader(f, delimiter=' ')
        numLines = np.min([numLines, sum(1 for row in csvReader) - 1])
        f.seek(0)

        for row in csvReader:
            if row[0] != '' and len(row)>2:

                nTrial = int(row[0])

                gameRes = row[3][-2:-1]
                repeatWins.append(int(gameRes))

                gameScore = row[4].split(":")[1][0:-1]
                repeatScores.append(float(gameScore))

                gameTimeSpent = row[5].split(":")[1][0:]
                repeatTimespents.append(int(gameTimeSpent))

                if len(repeatWins) > AVOID_FIRST:

                    acumAvgWins = np.average(repeatWins)
                    acumAvgScore = np.average(repeatScores)
                    acumAvgTimes = np.average(repeatTimespents)

                    averageWinAtTrial[nTrial-AVOID_FIRST].append(acumAvgWins)
                    averageScoresAtTrial[nTrial-AVOID_FIRST].append(acumAvgScore)
                    averageTimesAtTrial[nTrial-AVOID_FIRST].append(acumAvgTimes)


    plot(averageWinAtTrial, numLines, "Average number of victories", output_dir+str(games[game_number])+"_wins.pdf", show_plot, [-0.2,1.2])
    plot(averageScoresAtTrial, numLines, "Average score", output_dir+str(games[game_number])+"_scores.pdf", show_plot)
    plot(averageTimesAtTrial, numLines, "Average time spent", output_dir+str(games[game_number])+"_times.pdf", show_plot, [0,1100])


if __name__=="__main__":
    for i in range(10):
        plot_game("results/GreedyLinear/", "pics/GreedyLinear/", i)