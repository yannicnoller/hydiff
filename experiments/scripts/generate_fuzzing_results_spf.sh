# Run steps to generate path_cost.csv for SymExe run
cd ../fuzzing/
nohup java -cp bin-instr edu.cmu.sv.kelinci.Kelinci MoreSanity_LoopAndBranch_FuzzDriver @@ &
server_pid=$!
sleep 5 # Wait a little bit to ensure that server is started
cd ../symexe/
for filename in ../symexe-out/queue/*; do
nohup ../../../kelinci-regression/fuzzerside/interface_log_cost $filename ../symexe-out/path_cost.csv >> ../symexe-out/interface-log.txt
done
kill $server_pid
