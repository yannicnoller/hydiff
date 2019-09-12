#!/bin/bash
trap "exit" INT

echo "Building HyDiff sources and experimental subjects.."

echo "[1/6] build afl.."
cd tool/fuzzing/afl-differential
rm -f build.log
make clean > build.log
make > build.log
cd ../../../

echo "[2/6] build kelinci .."
cd tool/fuzzing/kelinci-differential/
cd fuzzerside/
rm -f build.log
make clean > build.log
make > build.log
cd ../instrumentor/
rm -f build.log
gradle clean > build.log
gradle build --refresh-dependencies > build.log
cd ../../../../

echo "[3/6] setting up site.properties .."
workingDir=$(pwd)
sitePropFile="$HOME/.jpf/site.properties"
if [ -e $sitePropFile ]
then
  rm $sitePropFile
fi
touch $sitePropFile
echo "jpf-core = $workingDir/tool/symbolicexecution/jpf-core" >> $sitePropFile
echo "jpf-symbc = $workingDir/tool/symbolicexecution/jpf-symbc-differential" >> $sitePropFile
echo "badger = $workingDir/tool/symbolicexecution/badger-differential" >> $sitePropFile
echo "extensions=\${jpf-core},\${jpf-symbc},\${badger}" >> $sitePropFile

echo "[4/6] build jpf-core .."
cd tool/symbolicexecution/jpf-core
rm -f build.log
ant clean > build.log
ant > build.log
cd ../../../

echo "[5/6] build jpf-symbc-differential .."
cd tool/symbolicexecution/jpf-symbc-differential
rm -f build.log
ant clean > build.log
ant > build.log
cd ../../../

echo "[6/6] build badger-differential .."
cd tool/symbolicexecution/badger-differential/
rm -f build.log
ant clean > build.log
ant > build.log
cd ../../../

echo "[7/11] clean experiments .."
cd experiments/scripts
./clean_experiments.sh

echo "[8/11] prepare example subject .."
./prepare_example.sh

echo "[9/11] prepare regression subjects .."
./prepare_regression_subjects.sh

echo "[10/11] prepare sidechannel subjects .."
./prepare_sidechannel_subjects.sh

echo "[11/11] prepare dnn subjects .."
./prepare_dnn_subjects.sh

echo "Done."
