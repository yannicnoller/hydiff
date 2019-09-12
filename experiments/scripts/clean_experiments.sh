#!/bin/bash
trap "exit" INT

declare -a subjects=(
"example"
"foo"
"tcas_v1"
"tcas_v2"
"tcas_v3"
"tcas_v4"
"tcas_v5"
"tcas_v6"
"tcas_v7"
"tcas_v8"
"tcas_v9"
"tcas_v10"
"math_10"
"math_46"
"math_60"
"time_1"
"commons-cli_v1-2"
"commons-cli_v2-3"
"commons-cli_v3-4"
"commons-cli_v4-5"
"commons-cli_v5-6"
"commons-cli_v1-2_noformat"
"commons-cli_v2-3_noformat"
"commons-cli_v3-4_noformat"
"commons-cli_v4-5_noformat"
"commons-cli_v5-6_noformat"
"blazer_login_unsafe"
"blazer_login_safe"
"themis_jetty_unsafe"
"themis_jetty_safe"
"stac_ibasys_unsafe"
"rsa_modpow_1717"
"rsa_modpow_834443"
"rsa_modpow_1964903306"
"mnist2_1"
"mnist2_2"
"mnist2_5"
"mnist2_10"
"mnist2_20"
"mnist2_50"
"mnist2_100"
)

run_counter=0
total_number_subjects=${#subjects[@]}

for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  run_counter=$(( $run_counter + 1 ))
  echo "   [$run_counter/$total_number_subjects] Clean ${subjects[i]}.."

  rm -rf ../subjects/${subjects[i]}/fuzzer-out*
  rm -rf ../subjects/${subjects[i]}/symexe-out*
  rm -rf ../subjects/${subjects[i]}/hydiff-out*
  rm -rf ../subjects/${subjects[i]}/fuzzing/bin
  rm -rf ../subjects/${subjects[i]}/fuzzing/bin-instr
  rm -rf ../subjects/${subjects[i]}/symexe/bin
done

echo "   Done."
