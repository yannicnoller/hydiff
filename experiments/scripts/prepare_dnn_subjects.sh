## prepare_dnn_subjects.sh
#####################################
# chmod +x prepare_dnn_subjects.sh
# ./prepare_dnn_subjects.sh
#

trap "exit" INT

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
"." # "mnist2_1"
"." # "mnist2_2"
"." # "mnist2_5"
"." # "mnist2_10"
"." # "mnist2_20"
"." # "mnist2_50"
"." # "mnist2_100"
)

# Check array sizes
if [[ ${#subjects[@]} != ${#classpaths[@]} ]]
then
echo "[Error in script] the array sizes of subjects and classpaths do not match!. Abort!"
exit 1
fi

run_counter=0
total_number_subjects=${#subjects[@]}
echo

cd ../subjects

for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  run_counter=$(( $run_counter + 1 ))
  echo "[$run_counter/$total_number_subjects] Prepare ${subjects[i]}.."

  cd ./${subjects[i]}/fuzzing

  # Generate fuzzing bytecode
  rm -rf bin
  mkdir bin
  cd src
  javac -cp ${classpaths[i]}:../../../../../tool/fuzzing/kelinci-differential/instrumentor/build/libs/kelinci.jar *.java -d ../bin
  cd ..

  # Instrument fuzzing bytecode
  rm -rf bin-instr
  java -cp ${classpaths[i]}:../../../../tool/fuzzing/kelinci-differential/instrumentor/build/libs/kelinci.jar edu.cmu.sv.kelinci.instrumentor.Instrumentor -mode REGRESSION -i bin -o bin-instr -skipmain

  cd ../symexe

  # Generate symexe bytecode
  rm -rf bin
  mkdir bin
  cd src
  javac -g -cp ${classpaths[i]}:../../../../../tool/symbolicexecution/jpf-symbc-differential/build/* *.java -d ../bin
  cd ../../../
  echo

done

echo ""
