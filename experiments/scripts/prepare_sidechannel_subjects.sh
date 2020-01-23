## prepare_sidechannel_subjects.sh
#####################################
# chmod +x prepare_sidechannel_subjects.sh
# ./prepare_sidechannel_subjects.sh
#

trap "exit" INT

declare -a subjects=(
"blazer_login_unsafe"
"blazer_login_safe"
"themis_jetty_unsafe"
"themis_jetty_safe"
"stac_ibasys_unsafe"
"rsa_modpow_1717"
"rsa_modpow_834443"
"rsa_modpow_1964903306"
)

declare -a classpaths=(
"." # "blazer_login_unsafe"
"." # "blazer_login_safe"
"." # "themis_jetty_unsafe"
"." # "themis_jetty_safe"
"." # "stac_ibasys_unsafe"
"." # "rsa_modpow_1717"
"." # "rsa_modpow_834443"
"." # "rsa_modpow_1964903306"
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
