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

wkdir="/home/ladmin/projects"

declare -a subjects=(
"math_10"
"math_46"
"math_60"
"time_1"
)

declare -a classpaths=(
"./bin-instr/" # "math_10"
"./bin-instr/" # "math_46"
"./bin-instr/" # "math_60"
"./bin-instr/" # "time_1"
)

declare -a fuzz_dist_targets=(
"test.org.apache.commons.math3.analysis.differentiation.DSCompiler_v1.atan2([DI[DI[DI)V:1433" # "math_10"
"test.org.apache.commons.math.complex.Complex1.divide(Ltest/org/apache/commons/math/complex/Complex1;)Ltest/org/apache/commons/math/complex/Complex1;:259" # "math_46"
"test.org.apache.commons.math.util.ContinuedFraction1.evaluate(DDI)D:148,test.org.apache.commons.math.util.ContinuedFraction1.evaluate(DDI)D:177,test.org.apache.commons.math.util.ContinuedFraction1.evaluate(DDI)D:185,test.org.apache.commons.math.special.Gamma1.regularizedGammaP(DDDI)D:169,test.org.apache.commons.math.special.Gamma1.regularizedGammaP(DDDI)D:178,test.org.apache.commons.math.special.Gamma1.regularizedGammaP(DDDI)D:190,test.org.apache.commons.math.special.Gamma1.regularizedGammaQ(DDDI)D:247" # "math_60"
"org.joda.time.MutableDateTime1.add(Lorg/joda/time/DurationFieldType;I)V:639,org.joda.time.MutableDateTime1.addYears(I)V:662,org.joda.time.MutableDateTime1.addMonths(I)V:708,org.joda.time.MutableDateTime1.addWeeks(I)V:731,org.joda.time.MutableDateTime1.addDays(I)V:774,org.joda.time.MutableDateTime1.addHours(I)V:797,org.joda.time.MutableDateTime1.addMinutes(I)V:830,org.joda.time.MutableDateTime1.addSeconds(I)V:863,org.joda.time.MutableDateTime1.addMillis(I)V:898,org.joda.time.Partial1.<init>([Lorg/joda/time/DateTimeFieldType;[ILorg/joda/time/Chronology;)V:219,org.joda.time.Partial1.<init>([Lorg/joda/time/DateTimeFieldType;[ILorg/joda/time/Chronology;)V:224,org.joda.time.Partial1.<init>([Lorg/joda/time/DateTimeFieldType;[ILorg/joda/time/Chronology;)V:240,org.joda.time.Partial1.with(Lorg/joda/time/DateTimeFieldType;I)Lorg/joda/time/Partial1;:449,org.joda.time.field.UnsupportedDurationField1.compareTo(Lorg/joda/time/DurationField;)I:228" # "time_1"
)

declare -a num_dist_targets=(
"1" # "math_10"
"1" # "math_46"
"7" # "math_60"
"14" # "time_1"
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
  AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 /home/ladmin/projects/hydiff/afl/afl-fuzz-import -i in_dir -o ../hydiff-out-$j -c regression -S afl-spf -t 999999999 -r ${num_dist_targets[i]} /home/ladmin/projects/hydiff/kelinci-regression/fuzzerside/interface @@ > ../symexe-out-$j/afl-log.txt

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
  nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci -dist-target ${fuzz_dist_targets[i]} FuzzDriver @@ > ../symexe-out-$j/server-log.txt &
  server_pid=$!
  sleep 5 # Wait a little bit to ensure that server is started

  # Start modified AFL
  AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 /home/ladmin/projects/hydiff/afl/afl-fuzz-import-spf -i in_dir -o ../hydiff-out-$j -c regression -S spf-replay -t 999999999 -r ${num_dist_targets[i]} /home/ladmin/projects/hydiff/kelinci-regression/fuzzerside/interface @@ > ../symexe-out-$j/afl-log.txt

  kill $server_pid

  # Wait a little bit to make sure that processes are killed
  sleep 10

  cd ../

done

# Evaluate run
python3 /home/ladmin/projects/hydiff/regression-experiments/evaluate_regression_hydiff_spf.py "$wkdir/${subjects[i]}/hydiff-out-" $number_of_runs $time_bound $step_size_eval

done
