## run_dnn_evaluation.sh
# CAUTION
# Run this script within its folder. Otherwise the paths might be wrong!
#####################################
# chmod +x run_dnn_evaluation.sh
# ./run_dnn_evaluation.sh
#

trap "exit" INT

#####################

number_of_runs=30
time_bound=3600 #60min
step_size_eval=30
time_symexe_first=600 # 10min

declare -a subjects=(
"mnist2_1"
"mnist2_2"
"mnist2_5"
"mnist2_10"
"mnist2_20"
"mnist2_50"
"mnist2_100"
)

declare -a classpaths=(
"./bin-instr/" # "mnist2_1"
"./bin-instr/" # "mnist2_2"
"./bin-instr/" # "mnist2_5"
"./bin-instr/" # "mnist2_10"
"./bin-instr/" # "mnist2_20"
"./bin-instr/" # "mnist2_50"
"./bin-instr/" # "mnist2_100"
)

declare -a drivers=(
"FuzzDiffDriver" # mnist2_1"
"FuzzDiffDriver" # mnist2_2"
"FuzzDiffDriver" # mnist2_5"
"FuzzDiffDriver" # mnist2_10"
"FuzzDiffDriver" # mnist2_20"
"FuzzDiffDriver" # mnist2_50"
"FuzzDiffDriver" # mnist2_100"
)

# Check time bounds.
if [[ $time_bound -lt $time_symexe_first ]]
then
  echo "[Error in script] time_bound must be larger than time_symexe_first. Abort!"
  exit 1
fi
time_remainig_fuzz=$(($time_bound - $time_symexe_first))

# Check array sizes
if [[ ${#subjects[@]} != ${#classpaths[@]} ]]
then
  echo "[Error in script] the array sizes of subjects and classpaths do not match!. Abort!"
  exit 1
fi
if [[ ${#subjects[@]} != ${#drivers[@]} ]]
then
  echo "[Error in script] the array sizes of subjects and drivers do not match!. Abort!"
  exit 1
fi

run_counter=0
total_number_subjects=${#subjects[@]}
total_number_experiments=$(( $total_number_subjects * $(($number_of_runs * 3)))) #3 because of fuzzing+symexe+hybrid

if [ "$(uname)" == "Darwin" ]; then
  echo "set DYLD_LIBRARY_PATH to ../../../../tool/symbolicexecution/jpf-symbc-differential/lib"
  export DYLD_LIBRARY_PATH=../../../../tool/symbolicexecution/jpf-symbc-differential/lib
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  echo "set LD_LIBRARY_PATH to ../../../../tool/symbolicexecution/jpf-symbc-differential/lib"
  export LD_LIBRARY_PATH=../../../../tool/symbolicexecution/jpf-symbc-differential/lib
else
  echo "OS not supported!"
  exit 1
fi

echo
echo "Run complete evaluation..."

cd ../subjects

# Run just fuzzing
for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  cd ./${subjects[i]}/fuzzing/
  for j in `seq 1 $number_of_runs`
  do
    run_counter=$(( $run_counter + 1 ))
    echo "[$run_counter/$total_number_experiments] Run fuzzing analysis for ${subjects[i]}, round $j .."

    mkdir ../fuzzer-out-$j/

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci ${drivers[i]} @@ > ../fuzzer-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 nohup ../../../../tool/fuzzing/afl-differential/afl-fuzz -i in_dir -o ../fuzzer-out-$j -c regression -S afl -t 999999999 ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface @@ > ../fuzzer-out-$j/afl-log.txt &
    afl_pid=$!

    # Wait for timebound
    sleep $time_bound

    # Stop AFL and Kelinci server
    kill $afl_pid
    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10
  done
  cd ../../

  # Evaluate run
  python3 ../scripts/evaluate_regression_fuzz.py ${subjects[i]}/fuzzer-out- $number_of_runs $time_bound $step_size_eval

done

# Run just symexe
for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  cd ./${subjects[i]}/

  for j in `seq 1 $number_of_runs`
  do

    run_counter=$(( $run_counter + 1 ))
    echo "[$run_counter/$total_number_experiments] Run symexe analysis for ${subjects[i]}, round $j .."

    mkdir ./symexe-out-$j/

    cd ./symexe/

    # Start SPF
    nohup java -Xmx6144m -cp "../../../../tool/symbolicexecution/badger-differential/build/*:../../../../tool/symbolicexecution/badger-differential/lib/*:../../../../tool/symbolicexecution/jpf-symbc-differential/build/*:../../../../tool/symbolicexecution/jpf-symbc-differential/lib/*:../../../../tool/symbolicexecution/jpf-core/build/*" edu.cmu.sv.badger.app.BadgerRunner config_symexe $j > ../symexe-out-$j/spf-log.txt &
    spf_pid=$!

    # Wait for timebound
    sleep $time_bound

    # Stop SPF
    kill $spf_pid

    # Assess.
    cd ../fuzzing

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci ${drivers[i]} @@ > ../symexe-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 ../../../../tool/fuzzing/afl-differential/afl-fuzz-import -i in_dir -o ../symexe-out-$j -c regression -S afl -t 999999999 ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface @@ > ../symexe-out-$j/afl-log.txt

    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10

    cd ../
  done

  cd ../

  # Evaluate run
  python3 ../scripts/evaluate_regression_symexe.py ${subjects[i]}/symexe-out- $number_of_runs $time_bound $step_size_eval

done

# Run just hybrid analysis
for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  cd ./${subjects[i]}/

  for j in `seq 1 $number_of_runs`
  do
    run_counter=$(( $run_counter + 1 ))
    echo "[$run_counter/$total_number_experiments] Run hybrid analysis for ${subjects[i]}, round $j .."

    mkdir ./hydiff-out-$j/

    cd ./symexe/

    # Start SPF
    nohup java -Xmx6144m -cp "../../../../tool/symbolicexecution/badger-differential/build/*:../../../../tool/symbolicexecution/badger-differential/lib/*:../../../../tool/symbolicexecution/jpf-symbc-differential/build/*:../../../../tool/symbolicexecution/jpf-symbc-differential/lib/*:../../../../tool/symbolicexecution/jpf-core/build/*" edu.cmu.sv.badger.app.BadgerRunner config_hybrid $j > ../hydiff-out-$j/spf-log.txt &
    spf_pid=$!

    cd ../fuzzing/

    sleep $time_symexe_first # sleep for the defined time limit to let spf generate some initial files for the fuzzer

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci ${drivers[i]} @@ > ../hydiff-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 nohup ../../../../tool/fuzzing/afl-differential/afl-fuzz  -i in_dir -o ../hydiff-out-$j -c regression -S afl -t 999999999 ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface @@ > ../hydiff-out-$j/afl-log.txt &
    afl_pid=$!

    # Wait for timebound
    sleep $time_remainig_fuzz

    # Stop SPF, AFL and Kelinci server
    kill $spf_pid
    kill $afl_pid
    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10

    ####
    # Assess both runs, i.e. combine them in one instance afl-spf.
    cd ../fuzzing

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci ${drivers[i]} @@ > ../hydiff-out-$j/server-afl+spf-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 ../../../../tool/fuzzing/afl-differential/afl-fuzz-import -i in_dir -o ../hydiff-out-$j -c regression -S afl-spf -t 999999999 ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface @@ > ../hydiff-out-$j/afl+spf-log.txt
    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10


    ####
    # Assess only spf run to determine later the times for the first odiff.

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci ${drivers[i]} @@ > ../hydiff-out-$j/server-spf-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 ../../../../tool/fuzzing/afl-differential/afl-fuzz-import-spf -i in_dir -o ../hydiff-out-$j -c regression -S spf-replay -t 999999999 ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface @@ > ../hydiff-out-$j/spf-replay-log.txt

    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10

    cd ../
  done

  cd ../

  # Evaluate run
  python3 ../scripts/evaluate_regression_hydiff.py ${subjects[i]}/hydiff-out- $number_of_runs $time_bound $step_size_eval $time_symexe_first

done
