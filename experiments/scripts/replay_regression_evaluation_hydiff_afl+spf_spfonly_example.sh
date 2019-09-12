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

wkdir="/Users/yannic/Downloads/hydiff-experiment-results-updated"

declare -a subjects=(
"example"
)

declare -a classpaths=(
"./bin-instr/" # "example"
)

declare -a drivers=(
"FuzzDriver" # "example"
)

declare -a fuzz_dist_targets=(
"Example2.calculate(II)I:761,Example2.calculate(II)I:769" # "example"
)

declare -a num_dist_targets=(
"2" # "example"
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
total_number_experiments=$(( $total_number_subjects * $(($number_of_runs * 2)))) #3 because of fuzzing+symexe+hybrid

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


for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do

## WORKING DIR
cd "$wkdir/${subjects[i]}"

# AFL+SPF
for j in `seq 1 $number_of_runs`
do

  run_counter=$(( $run_counter + 1 ))
  echo "[$run_counter/$total_number_experiments] Merge AFL+SymExe of Hydiff run for ${subjects[i]}, round $j .."

  # Assess.
  cd ./fuzzing

  # Start Kelinci server
  nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci -dist-target ${fuzz_dist_targets[i]} FuzzDriver @@ > ../symexe-out-$j/server-log.txt &
  server_pid=$!
  sleep 5 # Wait a little bit to ensure that server is started

  # Start modified AFL
  AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 /Users/yannic/repositories/regfuzzsym/afl/afl-fuzz-import -i in_dir -o ../hydiff-out-$j -c regression -S afl-spf -t 999999999 -r ${num_dist_targets[i]} /Users/yannic/repositories/regfuzzsym/kelinci-regression/fuzzerside/interface @@ > ../symexe-out-$j/afl-log.txt

  kill $server_pid

  # Wait a little bit to make sure that processes are killed
  sleep 10

  cd ../

done

# Evaluate run
python3 /Users/yannic/repositories/regfuzzsym/regression-experiments/evaluate_regression_hydiff_afl_spf.py "$wkdir/${subjects[i]}/hydiff-out-" $number_of_runs $time_bound $step_size_eval

# SPF only
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
  AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 /Users/yannic/repositories/regfuzzsym/afl/afl-fuzz-import-spf -i in_dir -o ../hydiff-out-$j -c regression -S spf-replay -t 999999999 -r ${num_dist_targets[i]} /Users/yannic/repositories/regfuzzsym/kelinci-regression/fuzzerside/interface @@ > ../symexe-out-$j/afl-log.txt

  kill $server_pid

  # Wait a little bit to make sure that processes are killed
  sleep 10

  cd ../

done

# Evaluate run
python3 /Users/yannic/repositories/regfuzzsym/regression-experiments/evaluate_regression_hydiff_spf.py "$wkdir/${subjects[i]}/hydiff-out-" $number_of_runs $time_bound $step_size_eval

done
