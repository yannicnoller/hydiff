## run_complete_evaluation.sh
# CAUTION
# Run this script within its folder. Otherwise the paths might be wrong!
#####################################
# chmod +x run_complete_evaluation.sh
# ./run_complete_evaluation.sh
#

trap "exit" INT

#####################

echo "Run complete evaluation..."

number_of_runs=30
time_bound=600 #10min
step_size_eval=30

declare -a subjects=(
"tcas_v1"
)

declare -a classpaths=(
"./bin-instr/" # "tcas_v1"
)

declare -a fuzz_dist_targets=(
"tcas.TCAS_V1.Non_Crossing_Biased_Climb()Z:62" # "tcas_v1"
)

declare -a num_dist_targets=(
"1" # "tcas_v1"
)

# Check array sizes
if [[ ${#subjects[@]} != ${#classpaths[@]} ]]
then
echo "[Error in script] the array sizes of subjects and classpaths do not match!. Abort!"
exit 1
fi
if [[ ${#subjects[@]} != ${#fuzz_dist_targets[@]} ]]
then
echo "[Error in script] the array sizes of subjects and fuzz_dist_targets do not match!. Abort!"
exit 1
fi
if [[ ${#subjects[@]} != ${#num_dist_targets[@]} ]]
then
echo "[Error in script] the array sizes of subjects and num_dist_targets do not match!. Abort!"
exit 1
fi

run_counter=0
total_number_subjects=${#subjects[@]}
total_number_experiments=$(( $total_number_subjects * $(($number_of_runs)))) #3 because of fuzzing+symexe+hybrid

if [ "$(uname)" == "Darwin" ]; then
  echo "set DYLD_LIBRARY_PATH to ../../../jpf-symbc-regression/lib"
  export DYLD_LIBRARY_PATH=../../../jpf-symbc-regression/lib
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  echo "set LD_LIBRARY_PATH to ../../../jpf-symbc-regression/lib"
  export LD_LIBRARY_PATH=../../../jpf-symbc-regression/lib
else
  echo "OS not supported!"
  exit 1
fi

## WORKING DIR
cd "/home/ladmin/projects/tcas_v1"

for j in `seq 1 $number_of_runs`
do

  run_counter=$(( $run_counter + 1 ))
  echo "[$run_counter/$total_number_experiments] Evaluate symexe run in HyDiff for ${subjects[i]}, round $j .."

    # Assess.
    cd ./fuzzing

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci -dist-target ${fuzz_dist_targets[i]} FuzzDriver @@ > ../symexe-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 /home/ladmin/projects/hydiff/afl/afl-fuzz-import -i in_dir -o ../hydiff-out-$j -c regression -S afl-spf -t 999999999 -r ${num_dist_targets[i]} /home/ladmin/projects/hydiff/kelinci-regression/fuzzerside/interface @@ > ../symexe-out-$j/afl-log.txt

    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10

    cd ../

  done

  cd ../

  # Evaluate run
  python3 /home/ladmin/projects/hydiff/regression-experiments/evaluate_regression_hydiff_afl_spf.py ${subjects[i]}/hydiff-out- $number_of_runs $time_bound $step_size_eval