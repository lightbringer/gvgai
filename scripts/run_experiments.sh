#!/bin/bash

RESULT_DIR=/scratch/ssamot/results
echo $RESULT_DIR
mkdir $RESULT_DIR
chmod -R 755 $RESULT_DIR

for i in 0 9 # games
do
	for j in {0..50} # repetitions
	do
	   echo "Starting training for game $i, instance $j"
	   OMP_NUM_THREADS=1 nohup java -jar gvgai.jar ./GVGAI-PythonClient/PyClient.py experiment_evo_agent.EvoAgentEpsilonGreedyLinear $i 1000 >$RESULT_DIR/$i_$j.out 2>$RESULT_DIR/$i_$j.err &
	   chmod -R 755 $RESULT_DIR
	   sleep 0.5
	done
done
