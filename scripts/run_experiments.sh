#!/bin/bash

RESULT_DIR=/scratch/ssamot/results
echo $RESULT_DIR
mkdir $RESULT_DIR
chmod -R 755 $RESULT_DIR

for i in 0 1 # games
do
	for j in {0..3} # repetitions
	do
	   echo "Starting training for game $i, instance $j"
	   OMP_NUM_THREADS=1 nohup java -jar gvgai.jar ./GVGAI-PythonClient/PyClient.py experiment_evo_agent.EvoAgentEpsilonGreedyLinear $i 100 >$RESULT_DIR/$i_$j.out 2>$RESULT_DIR/$i_$j.err &
	   chmod -R 755 $RESULT_DIR
	   sleep 0.5
	done
done