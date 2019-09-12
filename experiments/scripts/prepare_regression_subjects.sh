#!/bin/bash
trap "exit" INT

declare -a subjects=(
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
"commons-cli_v1-2_noformat"
"commons-cli_v2-3_noformat"
"commons-cli_v3-4_noformat"
"commons-cli_v4-5_noformat"
"commons-cli_v5-6_noformat"
)

declare -a classpaths=(
"." # "tcas_v1"
"." # "tcas_v2"
"." # "tcas_v3"
"." # "tcas_v4"
"." # "tcas_v5"
"." # "tcas_v6"
"." # "tcas_v7"
"." # "tcas_v8"
"." # "tcas_v9"
"." # "tcas_v10"
"." # "math_10"
"." # "math_46"
"." # "math_60"
".:../lib/*" # "time_1"
".:../lib/*" # "commons-cli_v1-2_noformat"
".:../lib/*" # "commons-cli_v2-3_noformat"
".:../lib/*" # "commons-cli_v3-4_noformat"
".:../lib/*" # "commons-cli_v4-5_noformat"
".:../lib/*" # "commons-cli_v5-6_noformat"
)

declare -a fuzz_dist_targets=(
"tcas.TCAS_V1.Non_Crossing_Biased_Climb()Z:62" # "tcas_v1"
"tcas.TCAS_V2.Inhibit_Biased_Climb()I:52" # "tcas_v2"
"tcas.TCAS_V3.alt_sep_test()I:98" # "tcas_v3"
"tcas.TCAS_V4.Non_Crossing_Biased_Climb()Z:64" # "tcas_v4"
"tcas.TCAS_V5.alt_sep_test()I:96" # "tcas_v5"
"tcas.TCAS_V6.Own_Below_Threat()Z:88" # "tcas_v6"
"tcas.TCAS_V7.initialize()V:42" # "tcas_v7"
"tcas.TCAS_V8.initialize()V:44" # "tcas_v8"
"tcas.TCAS_V9.Non_Crossing_Biased_Climb()Z:60" # "tcas_v9"
"tcas.TCAS_V10.Own_Below_Threat()Z:85,tcas.TCAS_V10.Own_Above_Threat()Z:90" # "tcas_v10"
"test.org.apache.commons.math3.analysis.differentiation.DSCompiler_v1.atan2([DI[DI[DI)V:1433" # "math_10"
"test.org.apache.commons.math.complex.Complex1.divide(Ltest/org/apache/commons/math/complex/Complex1;)Ltest/org/apache/commons/math/complex/Complex1;:259" # "math_46"
"test.org.apache.commons.math.util.ContinuedFraction1.evaluate(DDI)D:148,test.org.apache.commons.math.util.ContinuedFraction1.evaluate(DDI)D:177,test.org.apache.commons.math.util.ContinuedFraction1.evaluate(DDI)D:185,test.org.apache.commons.math.special.Gamma1.regularizedGammaP(DDDI)D:169,test.org.apache.commons.math.special.Gamma1.regularizedGammaP(DDDI)D:178,test.org.apache.commons.math.special.Gamma1.regularizedGammaP(DDDI)D:190,test.org.apache.commons.math.special.Gamma1.regularizedGammaQ(DDDI)D:247" # "math_60"
"org.joda.time.MutableDateTime1.add(Lorg/joda/time/DurationFieldType;I)V:639,org.joda.time.MutableDateTime1.addYears(I)V:662,org.joda.time.MutableDateTime1.addMonths(I)V:708,org.joda.time.MutableDateTime1.addWeeks(I)V:731,org.joda.time.MutableDateTime1.addDays(I)V:774,org.joda.time.MutableDateTime1.addHours(I)V:797,org.joda.time.MutableDateTime1.addMinutes(I)V:830,org.joda.time.MutableDateTime1.addSeconds(I)V:863,org.joda.time.MutableDateTime1.addMillis(I)V:898,org.joda.time.Partial1.<init>([Lorg/joda/time/DateTimeFieldType;[ILorg/joda/time/Chronology;)V:219,org.joda.time.Partial1.<init>([Lorg/joda/time/DateTimeFieldType;[ILorg/joda/time/Chronology;)V:224,org.joda.time.Partial1.<init>([Lorg/joda/time/DateTimeFieldType;[ILorg/joda/time/Chronology;)V:240,org.joda.time.Partial1.with(Lorg/joda/time/DateTimeFieldType;I)Lorg/joda/time/Partial1;:449,org.joda.time.field.UnsupportedDurationField1.compareTo(Lorg/joda/time/DurationField;)I:228" # "time_1"
"v2_org.apache.commons.cli.CommandLine.setOpt(Lv2_org/apache/commons/cli/Option;)V:273,v2_org.apache.commons.cli.GnuParser.processArgs(Lv2_org/apache/commons/cli/Option;Ljava/util/ListIterator;)V:188,v2_org.apache.commons.cli.Option.validateOption(Ljava/lang/String;)V:160,v2_org.apache.commons.cli.Option.isValidOpt(C)Z:185,v2_org.apache.commons.cli.Option.isValidChar(C)Z:195,v2_org.apache.commons.cli.Option.setOptionalArg(Z)V:317,v2_org.apache.commons.cli.Option.hasOptionalArg()Z:327,v2_org.apache.commons.cli.Option.clone()Ljava/lang/Object;:539,v2_org.apache.commons.cli.OptionBuilder.hasOptionalArg()Lv2_org/apache/commons/cli/OptionBuilder;:232,v2_org.apache.commons.cli.OptionBuilder.create(Ljava/lang/String;)Lv2_org/apache/commons/cli/Option;:293,v2_org.apache.commons.cli.OptionBuilder.create()Lv2_org/apache/commons/cli/Option;:307,v2_org.apache.commons.cli.PosixParser.parse(Lv2_org/apache/commons/cli/Options;[Ljava/lang/String;Z)Lv2_org/apache/commons/cli/CommandLine;:184,v2_org.apache.commons.cli.PosixParser.processArgs(Lv2_org/apache/commons/cli/Option;Ljava/util/ListIterator;)V:286" # "commons-cli_v1-2_noformat"
"v3_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:187,v3_org.apache.commons.cli.CommandLine.addOption(Lv3_org/apache/commons/cli/Option;)V:302,v3_org.apache.commons.cli.GnuParser.flatten(Lv3_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:108,v3_org.apache.commons.cli.Option.isValidChar(C)Z:198,v3_org.apache.commons.cli.Option.setOptionalArg(Z)V:320,v3_org.apache.commons.cli.OptionBuilder.reset()V:111,v3_org.apache.commons.cli.OptionBuilder.hasOptionalArg()Lv3_org/apache/commons/cli/OptionBuilder;:252,v3_org.apache.commons.cli.OptionBuilder.create(Ljava/lang/String;)Lv3_org/apache/commons/cli/Option;:361,v3_org.apache.commons.cli.Options.addOptionGroup(Lv3_org/apache/commons/cli/OptionGroup;)Lv3_org/apache/commons/cli/Options;:117,v3_org.apache.commons.cli.Options.addOptionGroup(Lv3_org/apache/commons/cli/OptionGroup;)Lv3_org/apache/commons/cli/Options;:125,v3_org.apache.commons.cli.Options.getOptions()Ljava/util/Collection;:201,v3_org.apache.commons.cli.PosixParser.flatten(Lv3_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:141,v3_org.apache.commons.cli.Parser.processOption(Ljava/lang/String;Ljava/util/ListIterator;)V:270" # "commons-cli_v2-3_noformat"
"v4_org.apache.commons.cli.Parser.parse(Lv4_org/apache/commons/cli/Options;[Ljava/lang/String;Ljava/util/Properties;Z)Lv4_org/apache/commons/cli/CommandLine;:189,v4_org.apache.commons.cli.Parser.processProperties(Ljava/util/Properties;)V:254,v4_org.apache.commons.cli.OptionGroup.toString()Ljava/lang/String;:176,v4_org.apache.commons.cli.OptionBuilder.create()Lv4_org/apache/commons/cli/Option;:335,v4_org.apache.commons.cli.Option.clearValues()V:384,v4_org.apache.commons.cli.Option.getKey()Ljava/lang/String;:194,v4_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:193,v4_org.apache.commons.cli.CommandLine.addOption(Lv4_org/apache/commons/cli/Option;)V:295" # "commons-cli_v3-4_noformat"
"v5_org.apache.commons.cli.CommandLine.hasOption(Ljava/lang/String;)Z:67,v5_org.apache.commons.cli.CommandLine.getOptionObject(Ljava/lang/String;)Ljava/lang/Object;:94,v5_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:148,v5_org.apache.commons.cli.Option.addValue(Ljava/lang/String;)V:408,v5_org.apache.commons.cli.Option.processValue(Ljava/lang/String;)V:443,v5_org.apache.commons.cli.Option.add(Ljava/lang/String;)V:478,v5_org.apache.commons.cli.Option.getValue()Ljava/lang/String;:495,v5_org.apache.commons.cli.Option.getValue(I)Ljava/lang/String;:513,v5_org.apache.commons.cli.Option.getValues()[Ljava/lang/String;:542,v5_org.apache.commons.cli.OptionBuilder.reset()V:71,v5_org.apache.commons.cli.Options.addOption(Lv5_org/apache/commons/cli/Option;)Lv5_org/apache/commons/cli/Options;:157,v5_org.apache.commons.cli.Parser.processProperties(Ljava/util/Properties;)V:248,v5_org.apache.commons.cli.PatternOptionBuilder.isValueCode(C)Z:142" # "commons-cli_v4-5_noformat"
"v6_org.apache.commons.cli.Util.stripLeadingHyphens(Ljava/lang/String;)Ljava/lang/String;:37,v6_org.apache.commons.cli.TypeHandler.createNumber(Ljava/lang/String;)Ljava/lang/Number;:164,v6_org.apache.commons.cli.PosixParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:163,v6_org.apache.commons.cli.PosixParser.burstToken(Ljava/lang/String;Z)V:306,v6_org.apache.commons.cli.Parser.setOptions(Lv6_org/apache/commons/cli/Options;)V:46,v6_org.apache.commons.cli.Parser.getOptions()Lv6_org/apache/commons/cli/Options;:51,v6_org.apache.commons.cli.Parser.getRequiredOptions()Ljava/util/List;:55,v6_org.apache.commons.cli.Parser.checkRequiredOptions()V:312,v6_org.apache.commons.cli.Options.helpOptions()Ljava/util/List;:191,v6_org.apache.commons.cli.Option.<init>(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V:140,v6_org.apache.commons.cli.Option.getValues()[Ljava/lang/String;:543,v6_org.apache.commons.cli.Option.toString()Ljava/lang/String;:576,v6_org.apache.commons.cli.Option.toString()Ljava/lang/String;:578,v6_org.apache.commons.cli.Option.clone()Ljava/lang/Object;:649,v6_org.apache.commons.cli.Option.clearValues()V:665,v6_org.apache.commons.cli.Option.addValue(Ljava/lang/String;)Z:675,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:77,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:84,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:95,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:114,v6_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:158" # "commons-cli_v5-6_noformat"
)

declare -a instr_skip_classes=(
"empty" # "tcas_v1"
"empty" # "tcas_v2"
"empty" # "tcas_v3"
"empty" # "tcas_v4"
"empty" # "tcas_v5"
"empty" # "tcas_v6"
"empty" # "tcas_v7"
"empty" # "tcas_v8"
"empty" # "tcas_v9"
"empty" # "tcas_v10"
"empty" # "math_10"
"empty" # "math_46"
"empty" # "math_60"
"empty" # "time_1"
"empty" # "commons-cli_v1-2_noformat"
"empty" # "commons-cli_v2-3_noformat"
"empty" # "commons-cli_v3-4_noformat"
"empty" # "commons-cli_v4-5_noformat"
"empty" # "commons-cli_v5-6_noformat"
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
if [[ ${#subjects[@]} != ${#instr_skip_classes[@]} ]]
then
echo "[Error in script] the array sizes of subjects and instr_skip_classes do not match!. Abort!"
exit 1
fi

run_counter=0
total_number_subjects=${#subjects[@]}

workingDir=$(pwd)
cd ../subjects

for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  run_counter=$(( $run_counter + 1 ))
  echo "   [$run_counter/$total_number_subjects] Build ${subjects[i]}.."

  cd ${subjects[i]}/fuzzing
  rm -f build.log

  # Generate fuzzing bytecode
  rm -rf bin
  mkdir bin
  cd src
  javac -cp ${classpaths[i]}:../../../../../tool/fuzzing/kelinci-differential/instrumentor/build/libs/kelinci.jar *.java -d ../bin >> ../build.log
  cd ..

  # Instrument fuzzing bytecode
  rm -rf bin-instr
  java -cp ${classpaths[i]}:../../../../tool/fuzzing/kelinci-differential/instrumentor/build/libs/kelinci.jar edu.cmu.sv.kelinci.instrumentor.Instrumentor -mode REGRESSION -i bin -o bin-instr -skipmain -export-cfgdir cfg -dist-target ${fuzz_dist_targets[i]} -skipclass ${instr_skip_classes[i]} >> build.log

  cd ../symexe
  rm -f build.log

  # Generate symexe bytecode
  rm -rf bin
  mkdir bin
  cd src
  javac -g -cp ${classpaths[i]}:../../../../../tool/symbolicexecution/jpf-symbc-differential/build/* *.java -d ../bin >> ../build.log
  cd ../../../

done

cd $workingDir
echo "   Done."
