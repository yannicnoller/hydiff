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
time_bound=3600 #60min
step_size_eval=30
time_symexe_first=600 # 10min

wkdir="/home/ladmin/projects"

declare -a subjects=(
"mnist2_1"
)

declare -a classpaths=(
"./bin-instr/" # "mnist2_1"
)

declare -a drivers=(
"FuzzDiffDriver" # mnist2_1"
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
  nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci ${drivers[i]} @@ > ../symexe-out-$j/server-log.txt &
  server_pid=$!
  sleep 5 # Wait a little bit to ensure that server is started

  # Start modified AFL
  AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 /home/ladmin/projects/hydiff/afl/afl-fuzz-import -i in_dir -o ../hydiff-out-$j -c regression -S afl-spf -t 999999999 /home/ladmin/projects/hydiff/kelinci-regression/fuzzerside/interface @@ > ../symexe-out-$j/afl-log.txt
  server_pid=$!

  kill $server_pid

  # Wait a little bit to make sure that processes are killed
  sleep 10

  cd ../

done

# Evaluate run
python3 /home/ladmin/projects/hydiff/regression-experiments/evaluate_regression_hydiff_afl_spf.py "$wkdir/${subjects[i]}/hydiff-out-" $number_of_runs $time_bound $step_size_eval

# SPF only
for j in `seq 1 $number_of_runs`
do

  run_counter=$(( $run_counter + 1 ))
  echo "[$run_counter/$total_number_experiments] Evaluate symexe run in HyDiff for ${subjects[i]}, round $j .."

  # Assess.
  cd ./fuzzing

  # Start Kelinci server
  nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci ${drivers[i]} @@ > ../symexe-out-$j/server-log.txt &
  server_pid=$!
  sleep 5 # Wait a little bit to ensure that server is started

  # Start modified AFL
  AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 /home/ladmin/projects/hydiff/afl/afl-fuzz-import-spf -i in_dir -o ../hydiff-out-$j -c regression -S spf-replay -t 999999999 /home/ladmin/projects/hydiff/kelinci-regression/fuzzerside/interface @@ > ../symexe-out-$j/afl-log.txt

  kill $server_pid

  # Wait a little bit to make sure that processes are killed
  sleep 10

  cd ../

done

# Evaluate run
python3 /home/ladmin/projects/hydiff/regression-experiments/evaluate_regression_hydiff_spf.py "$wkdir/${subjects[i]}/hydiff-out-" $number_of_runs $time_bound $step_size_eval

done
