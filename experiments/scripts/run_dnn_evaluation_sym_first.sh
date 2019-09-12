## run_complete_evaluation.sh
# CAUTION
# Run this script within its folder. Otherwise the paths might be wrong!
#####################################
# chmod +x run_complete_evaluation.sh
# ./run_complete_evaluation.sh
#

trap "exit" INT

# Ask user.
# 58 subjects, 5 times, 30min
#read -p "Do you really want to run the complete evaluation? It will take around **6 days**? " -n 1 -r
#echo
#if [[ ! $REPLY =~ ^[Yy]$ ]]
#then
#  echo "ABORT."
#  exit 1
#fi

#####################

echo "Run complete evaluation..."

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
  export DYLD_LIBRARY_PATH=../../../jpf-symbc-regression/lib
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  export LD_LIBRARY_PATH=../../../jpf-symbc-regression/lib
else
  echo "OS not supported!"
  exit 1
fi

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
    nohup java -Xmx6144m -cp "../../../badger-regression/badger/build/*:../../../badger-regression/badger/lib/*:../../../jpf-symbc-regression/build/*:../../../jpf-symbc-regression/lib/*:../../../jpf-core/build/*" edu.cmu.sv.badger.app.BadgerRunner config_hybrid $j > ../hydiff-out-$j/spf-log.txt &
    spf_pid=$!

    cd ../fuzzing/

    sleep $time_symexe_first # sleep 10min to let spf generate some initial files for the fuzzer

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci ${drivers[i]} @@ > ../hydiff-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 nohup ../../../afl/afl-fuzz -i in_dir -o ../hydiff-out-$j -c regression -S afl -t 999999999 ../../../kelinci-regression/fuzzerside/interface @@ > ../hydiff-out-$j/afl-log.txt &
    afl_pid=$!

    cd ../

    # Wait for timebound
    sleep $time_remainig_fuzz

    # Stop SPF, AFL and Kelinci server
    kill $spf_pid
    kill $afl_pid
    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10
  done

  cd ../

  # Evaluate run
  python3 evaluate_regression_fuzz.py ${subjects[i]}/hydiff-out- $number_of_runs $time_bound $step_size_eval

done
