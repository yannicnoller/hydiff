## run_regression_evaluation.sh
# CAUTION
# Run this script within its folder. Otherwise the paths might be wrong!
#####################################
# chmod +x run_regression_evaluation.sh
# ./run_regression_evaluation.sh
#

trap "exit" INT

#####################

number_of_runs=30
time_bound=600 #10min
step_size_eval=30

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
)

declare -a classpaths=(
"./bin-instr/" # "tcas_v1"
"./bin-instr/" # "tcas_v2"
"./bin-instr/" # "tcas_v3"
"./bin-instr/" # "tcas_v4"
"./bin-instr/" # "tcas_v5"
"./bin-instr/" # "tcas_v6"
"./bin-instr/" # "tcas_v7"
"./bin-instr/" # "tcas_v8"
"./bin-instr/" # "tcas_v9"
"./bin-instr/" # "tcas_v10"
"./bin-instr/" # "math_10"
"./bin-instr/" # "math_46"
"./bin-instr/" # "math_60"
"./bin-instr/" # "time_1"
"./bin-instr/" # "commons-cli_v1-2"
"./bin-instr/" # "commons-cli_v2-3"
"./bin-instr/" # "commons-cli_v3-4"
"./bin-instr/" # "commons-cli_v4-5"
"./bin-instr/" # "commons-cli_v5-6"
"./bin-instr/" # "commons-cli_v1-2_noformat"
"./bin-instr/" # "commons-cli_v2-3_noformat"
"./bin-instr/" # "commons-cli_v3-4_noformat"
"./bin-instr/" # "commons-cli_v4-5_noformat"
"./bin-instr/" # "commons-cli_v5-6_noformat"
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
"v2_org.apache.commons.cli.CommandLine.setOpt(Lv2_org/apache/commons/cli/Option;)V:273,v2_org.apache.commons.cli.GnuParser.processArgs(Lv2_org/apache/commons/cli/Option;Ljava/util/ListIterator;)V:188,v2_org.apache.commons.cli.Option.validateOption(Ljava/lang/String;)V:160,v2_org.apache.commons.cli.Option.isValidOpt(C)Z:185,v2_org.apache.commons.cli.Option.isValidChar(C)Z:195,v2_org.apache.commons.cli.Option.setOptionalArg(Z)V:317,v2_org.apache.commons.cli.Option.hasOptionalArg()Z:327,v2_org.apache.commons.cli.Option.clone()Ljava/lang/Object;:539,v2_org.apache.commons.cli.OptionBuilder.hasOptionalArg()Lv2_org/apache/commons/cli/OptionBuilder;:232,v2_org.apache.commons.cli.OptionBuilder.create(Ljava/lang/String;)Lv2_org/apache/commons/cli/Option;:293,v2_org.apache.commons.cli.OptionBuilder.create()Lv2_org/apache/commons/cli/Option;:307,v2_org.apache.commons.cli.PosixParser.parse(Lv2_org/apache/commons/cli/Options;[Ljava/lang/String;Z)Lv2_org/apache/commons/cli/CommandLine;:184,v2_org.apache.commons.cli.PosixParser.processArgs(Lv2_org/apache/commons/cli/Option;Ljava/util/ListIterator;)V:286" # "commons-cli_v1-2"
"v3_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:187,v3_org.apache.commons.cli.CommandLine.addOption(Lv3_org/apache/commons/cli/Option;)V:302,v3_org.apache.commons.cli.GnuParser.flatten(Lv3_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:108,v3_org.apache.commons.cli.HelpFormatter.printUsage(Ljava/io/PrintWriter;ILjava/lang/String;Lv3_org/apache/commons/cli/Options;)V:275,v3_org.apache.commons.cli.HelpFormatter.printUsage(Ljava/io/PrintWriter;ILjava/lang/String;Lv3_org/apache/commons/cli/Options;)V:288,v3_org.apache.commons.cli.HelpFormatter.renderOptions(Ljava/lang/StringBuffer;ILv3_org/apache/commons/cli/Options;II)Ljava/lang/StringBuffer;:348,v3_org.apache.commons.cli.HelpFormatter.renderOptions(Ljava/lang/StringBuffer;ILv3_org/apache/commons/cli/Options;II)Ljava/lang/StringBuffer;:357,v3_org.apache.commons.cli.HelpFormatter\$StringBufferComparator.compare(Ljava/lang/Object;Ljava/lang/Object;)I:525,v3_org.apache.commons.cli.Option.isValidChar(C)Z:198,v3_org.apache.commons.cli.Option.setOptionalArg(Z)V:320,v3_org.apache.commons.cli.OptionBuilder.reset()V:111,v3_org.apache.commons.cli.OptionBuilder.hasOptionalArg()Lv3_org/apache/commons/cli/OptionBuilder;:252,v3_org.apache.commons.cli.OptionBuilder.create(Ljava/lang/String;)Lv3_org/apache/commons/cli/Option;:361,v3_org.apache.commons.cli.Options.addOptionGroup(Lv3_org/apache/commons/cli/OptionGroup;)Lv3_org/apache/commons/cli/Options;:117,v3_org.apache.commons.cli.Options.addOptionGroup(Lv3_org/apache/commons/cli/OptionGroup;)Lv3_org/apache/commons/cli/Options;:125,v3_org.apache.commons.cli.Options.getOptions()Ljava/util/Collection;:201,v3_org.apache.commons.cli.PosixParser.flatten(Lv3_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:141,v3_org.apache.commons.cli.Parser.processOption(Ljava/lang/String;Ljava/util/ListIterator;)V:270" # "commons-cli_v2-3"
"v4_org.apache.commons.cli.Parser.parse(Lv4_org/apache/commons/cli/Options;[Ljava/lang/String;Ljava/util/Properties;Z)Lv4_org/apache/commons/cli/CommandLine;:189,v4_org.apache.commons.cli.Parser.processProperties(Ljava/util/Properties;)V:254,v4_org.apache.commons.cli.OptionGroup.toString()Ljava/lang/String;:176,v4_org.apache.commons.cli.OptionBuilder.create()Lv4_org/apache/commons/cli/Option;:335,v4_org.apache.commons.cli.Option.clearValues()V:384,v4_org.apache.commons.cli.Option.getKey()Ljava/lang/String;:194,v4_org.apache.commons.cli.HelpFormatter.printUsage(Ljava/io/PrintWriter;ILjava/lang/String;Lv4_org/apache/commons/cli/Options;)V:282,v4_org.apache.commons.cli.HelpFormatter.renderOptions(Ljava/lang/StringBuffer;ILv4_org/apache/commons/cli/Options;II)Ljava/lang/StringBuffer;:390,v4_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:193,v4_org.apache.commons.cli.CommandLine.addOption(Lv4_org/apache/commons/cli/Option;)V:295" # "commons-cli_v3-4"
"v5_org.apache.commons.cli.CommandLine.hasOption(Ljava/lang/String;)Z:67,v5_org.apache.commons.cli.CommandLine.getOptionObject(Ljava/lang/String;)Ljava/lang/Object;:94,v5_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:148,v5_org.apache.commons.cli.HelpFormatter.printUsage(Ljava/io/PrintWriter;ILjava/lang/String;Lv5_org/apache/commons/cli/Options;)V:457,v5_org.apache.commons.cli.HelpFormatter.printUsage(Ljava/io/PrintWriter;ILjava/lang/String;Lv5_org/apache/commons/cli/Options;)V:478,v5_org.apache.commons.cli.HelpFormatter.appendOptionGroup(Ljava/lang/StringBuffer;Lv5_org/apache/commons/cli/OptionGroup;)V:501,v5_org.apache.commons.cli.HelpFormatter.appendOption(Ljava/lang/StringBuffer;Lv5_org/apache/commons/cli/Option;Z)V:540,v5_org.apache.commons.cli.HelpFormatter.appendOption(Ljava/lang/StringBuffer;Lv5_org/apache/commons/cli/Option;Z)V:550,v5_org.apache.commons.cli.HelpFormatter.findWrapPos(Ljava/lang/String;II)I:796,v5_org.apache.commons.cli.HelpFormatter\$OptionComparator.compare(Ljava/lang/Object;Ljava/lang/Object;)I:904,v5_org.apache.commons.cli.Option.addValue(Ljava/lang/String;)V:408,v5_org.apache.commons.cli.Option.processValue(Ljava/lang/String;)V:443,v5_org.apache.commons.cli.Option.add(Ljava/lang/String;)V:478,v5_org.apache.commons.cli.Option.getValue()Ljava/lang/String;:495,v5_org.apache.commons.cli.Option.getValue(I)Ljava/lang/String;:513,v5_org.apache.commons.cli.Option.getValues()[Ljava/lang/String;:542,v5_org.apache.commons.cli.OptionBuilder.reset()V:71,v5_org.apache.commons.cli.Options.addOption(Lv5_org/apache/commons/cli/Option;)Lv5_org/apache/commons/cli/Options;:157,v5_org.apache.commons.cli.Parser.processProperties(Ljava/util/Properties;)V:248,v5_org.apache.commons.cli.PatternOptionBuilder.isValueCode(C)Z:142" # "commons-cli_v4-5"
"v6_org.apache.commons.cli.Util.stripLeadingHyphens(Ljava/lang/String;)Ljava/lang/String;:37,v6_org.apache.commons.cli.TypeHandler.createNumber(Ljava/lang/String;)Ljava/lang/Number;:164,v6_org.apache.commons.cli.PosixParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:163,v6_org.apache.commons.cli.PosixParser.burstToken(Ljava/lang/String;Z)V:306,v6_org.apache.commons.cli.Parser.setOptions(Lv6_org/apache/commons/cli/Options;)V:46,v6_org.apache.commons.cli.Parser.getOptions()Lv6_org/apache/commons/cli/Options;:51,v6_org.apache.commons.cli.Parser.getRequiredOptions()Ljava/util/List;:55,v6_org.apache.commons.cli.Parser.checkRequiredOptions()V:312,v6_org.apache.commons.cli.Options.helpOptions()Ljava/util/List;:191,v6_org.apache.commons.cli.Option.<init>(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V:140,v6_org.apache.commons.cli.Option.getValues()[Ljava/lang/String;:543,v6_org.apache.commons.cli.Option.toString()Ljava/lang/String;:576,v6_org.apache.commons.cli.Option.toString()Ljava/lang/String;:578,v6_org.apache.commons.cli.Option.clone()Ljava/lang/Object;:649,v6_org.apache.commons.cli.Option.clearValues()V:665,v6_org.apache.commons.cli.Option.addValue(Ljava/lang/String;)Z:675,v6_org.apache.commons.cli.HelpFormatter.getOptionComparator()Ljava/util/Comparator;:302,v6_org.apache.commons.cli.HelpFormatter.setOptionComparator(Ljava/util/Comparator;)V:312,v6_org.apache.commons.cli.HelpFormatter.printUsage(Ljava/io/PrintWriter;ILjava/lang/String;Lv6_org/apache/commons/cli/Options;)V:524,v6_org.apache.commons.cli.HelpFormatter.appendOptionGroup(Ljava/lang/StringBuffer;Lv6_org/apache/commons/cli/OptionGroup;)V:587,v6_org.apache.commons.cli.HelpFormatter.appendOption(Ljava/lang/StringBuffer;Lv6_org/apache/commons/cli/Option;Z)V:634,v6_org.apache.commons.cli.HelpFormatter.renderWrappedText(Ljava/lang/StringBuffer;IILjava/lang/String;)Ljava/lang/StringBuffer;:846,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:77,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:84,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:95,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:114,v6_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:158" # "commons-cli_v5-6"
"v2_org.apache.commons.cli.CommandLine.setOpt(Lv2_org/apache/commons/cli/Option;)V:273,v2_org.apache.commons.cli.GnuParser.processArgs(Lv2_org/apache/commons/cli/Option;Ljava/util/ListIterator;)V:188,v2_org.apache.commons.cli.Option.validateOption(Ljava/lang/String;)V:160,v2_org.apache.commons.cli.Option.isValidOpt(C)Z:185,v2_org.apache.commons.cli.Option.isValidChar(C)Z:195,v2_org.apache.commons.cli.Option.setOptionalArg(Z)V:317,v2_org.apache.commons.cli.Option.hasOptionalArg()Z:327,v2_org.apache.commons.cli.Option.clone()Ljava/lang/Object;:539,v2_org.apache.commons.cli.OptionBuilder.hasOptionalArg()Lv2_org/apache/commons/cli/OptionBuilder;:232,v2_org.apache.commons.cli.OptionBuilder.create(Ljava/lang/String;)Lv2_org/apache/commons/cli/Option;:293,v2_org.apache.commons.cli.OptionBuilder.create()Lv2_org/apache/commons/cli/Option;:307,v2_org.apache.commons.cli.PosixParser.parse(Lv2_org/apache/commons/cli/Options;[Ljava/lang/String;Z)Lv2_org/apache/commons/cli/CommandLine;:184,v2_org.apache.commons.cli.PosixParser.processArgs(Lv2_org/apache/commons/cli/Option;Ljava/util/ListIterator;)V:286" # "commons-cli_v1-2_noformat"
"v3_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:187,v3_org.apache.commons.cli.CommandLine.addOption(Lv3_org/apache/commons/cli/Option;)V:302,v3_org.apache.commons.cli.GnuParser.flatten(Lv3_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:108,v3_org.apache.commons.cli.Option.isValidChar(C)Z:198,v3_org.apache.commons.cli.Option.setOptionalArg(Z)V:320,v3_org.apache.commons.cli.OptionBuilder.reset()V:111,v3_org.apache.commons.cli.OptionBuilder.hasOptionalArg()Lv3_org/apache/commons/cli/OptionBuilder;:252,v3_org.apache.commons.cli.OptionBuilder.create(Ljava/lang/String;)Lv3_org/apache/commons/cli/Option;:361,v3_org.apache.commons.cli.Options.addOptionGroup(Lv3_org/apache/commons/cli/OptionGroup;)Lv3_org/apache/commons/cli/Options;:117,v3_org.apache.commons.cli.Options.addOptionGroup(Lv3_org/apache/commons/cli/OptionGroup;)Lv3_org/apache/commons/cli/Options;:125,v3_org.apache.commons.cli.Options.getOptions()Ljava/util/Collection;:201,v3_org.apache.commons.cli.PosixParser.flatten(Lv3_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:141,v3_org.apache.commons.cli.Parser.processOption(Ljava/lang/String;Ljava/util/ListIterator;)V:270" # "commons-cli_v2-3_noformat"
"v4_org.apache.commons.cli.Parser.parse(Lv4_org/apache/commons/cli/Options;[Ljava/lang/String;Ljava/util/Properties;Z)Lv4_org/apache/commons/cli/CommandLine;:189,v4_org.apache.commons.cli.Parser.processProperties(Ljava/util/Properties;)V:254,v4_org.apache.commons.cli.OptionGroup.toString()Ljava/lang/String;:176,v4_org.apache.commons.cli.OptionBuilder.create()Lv4_org/apache/commons/cli/Option;:335,v4_org.apache.commons.cli.Option.clearValues()V:384,v4_org.apache.commons.cli.Option.getKey()Ljava/lang/String;:194,v4_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:193,v4_org.apache.commons.cli.CommandLine.addOption(Lv4_org/apache/commons/cli/Option;)V:295" # "commons-cli_v3-4_noformat"
"v5_org.apache.commons.cli.CommandLine.hasOption(Ljava/lang/String;)Z:67,v5_org.apache.commons.cli.CommandLine.getOptionObject(Ljava/lang/String;)Ljava/lang/Object;:94,v5_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:148,v5_org.apache.commons.cli.Option.addValue(Ljava/lang/String;)V:408,v5_org.apache.commons.cli.Option.processValue(Ljava/lang/String;)V:443,v5_org.apache.commons.cli.Option.add(Ljava/lang/String;)V:478,v5_org.apache.commons.cli.Option.getValue()Ljava/lang/String;:495,v5_org.apache.commons.cli.Option.getValue(I)Ljava/lang/String;:513,v5_org.apache.commons.cli.Option.getValues()[Ljava/lang/String;:542,v5_org.apache.commons.cli.OptionBuilder.reset()V:71,v5_org.apache.commons.cli.Options.addOption(Lv5_org/apache/commons/cli/Option;)Lv5_org/apache/commons/cli/Options;:157,v5_org.apache.commons.cli.Parser.processProperties(Ljava/util/Properties;)V:248,v5_org.apache.commons.cli.PatternOptionBuilder.isValueCode(C)Z:142" # "commons-cli_v4-5_noformat"
"v6_org.apache.commons.cli.Util.stripLeadingHyphens(Ljava/lang/String;)Ljava/lang/String;:37,v6_org.apache.commons.cli.TypeHandler.createNumber(Ljava/lang/String;)Ljava/lang/Number;:164,v6_org.apache.commons.cli.PosixParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:163,v6_org.apache.commons.cli.PosixParser.burstToken(Ljava/lang/String;Z)V:306,v6_org.apache.commons.cli.Parser.setOptions(Lv6_org/apache/commons/cli/Options;)V:46,v6_org.apache.commons.cli.Parser.getOptions()Lv6_org/apache/commons/cli/Options;:51,v6_org.apache.commons.cli.Parser.getRequiredOptions()Ljava/util/List;:55,v6_org.apache.commons.cli.Parser.checkRequiredOptions()V:312,v6_org.apache.commons.cli.Options.helpOptions()Ljava/util/List;:191,v6_org.apache.commons.cli.Option.<init>(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V:140,v6_org.apache.commons.cli.Option.getValues()[Ljava/lang/String;:543,v6_org.apache.commons.cli.Option.toString()Ljava/lang/String;:576,v6_org.apache.commons.cli.Option.toString()Ljava/lang/String;:578,v6_org.apache.commons.cli.Option.clone()Ljava/lang/Object;:649,v6_org.apache.commons.cli.Option.clearValues()V:665,v6_org.apache.commons.cli.Option.addValue(Ljava/lang/String;)Z:675,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:77,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:84,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:95,v6_org.apache.commons.cli.GnuParser.flatten(Lv6_org/apache/commons/cli/Options;[Ljava/lang/String;Z)[Ljava/lang/String;:114,v6_org.apache.commons.cli.CommandLine.getOptionValues(Ljava/lang/String;)[Ljava/lang/String;:158" # "commons-cli_v5-6_noformat"
)

declare -a num_dist_targets=(
"1" # "tcas_v1"
"1" # "tcas_v2"
"1" # "tcas_v3"
"1" # "tcas_v4"
"1" # "tcas_v5"
"1" # "tcas_v6"
"1" # "tcas_v7"
"1" # "tcas_v8"
"1" # "tcas_v9"
"2" # "tcas_v10"
"1" # "math_10"
"1" # "math_46"
"7" # "math_60"
"14" # "time_1"
"13" # "commons-cli_v1-2"
"18" # "commons-cli_v2-3"
"10" # "commons-cli_v3-4"
"20" # "commons-cli_v4-5"
"27" # "commons-cli_v5-6"
"13" # "commons-cli_v1-2_noformat"
"13" # "commons-cli_v2-3_noformat"
"8" # "commons-cli_v3-4_noformat"
"13" # "commons-cli_v4-5_noformat"
"21" # "commons-cli_v5-6_noformat"
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
total_number_experiments=$(( $total_number_subjects * $(($number_of_runs * 3)))) #3 because of fuzzing+symexe+hybrid

if [ "$(uname)" == "Darwin" ]; then
  echo "set DYLD_LIBRARY_PATH to ../../../../tool/symbolicexecution/jpf-symbc-differential/lib"
  export DYLD_LIBRARY_PATH=../../../../tool/symbolicexecution/jpf-symbc-differential/lib
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  echo "set LD_LIBRARY_PATH to ../../../../tool/symbolicexecution/jpf-symbc-differential/lib"
  export LD_LIBRARY_PATH=../../../../tool/symbolicexecution/jpf-symbc-differential/lib
else
  echo "OS not supported!"
  exit 1
fi

echo
echo "Run complete evaluation..."

cd ../subjects

# Run just fuzzing
for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  cd ./${subjects[i]}/fuzzing/
  for j in `seq 1 $number_of_runs`
  do
    run_counter=$(( $run_counter + 1 ))
    echo "[$run_counter/$total_number_experiments] Run fuzzing analysis for ${subjects[i]}, round $j .."

    mkdir ../fuzzer-out-$j/

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci -dist-target ${fuzz_dist_targets[i]} FuzzDriver @@ > ../fuzzer-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 nohup ../../../../tool/fuzzing/afl-differential/afl-fuzz -i in_dir -o ../fuzzer-out-$j -c regression -S afl -t 999999999 -r ${num_dist_targets[i]} ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface -t ${num_dist_targets[i]} @@ > ../fuzzer-out-$j/afl-log.txt &
    afl_pid=$!

    # Wait for timebound
    sleep $time_bound

    # Stop AFL and Kelinci server
    kill $afl_pid
    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10
  done
  cd ../../

  # Evaluate run
  python3 ../scripts/evaluate_regression_fuzz.py ${subjects[i]}/fuzzer-out- $number_of_runs $time_bound $step_size_eval

done

# Run just symexe
for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  cd ./${subjects[i]}/

  for j in `seq 1 $number_of_runs`
  do

    run_counter=$(( $run_counter + 1 ))
    echo "[$run_counter/$total_number_experiments] Run symexe analysis for ${subjects[i]}, round $j .."

    mkdir ./symexe-out-$j/

    cd ./symexe/

    # Start SPF
    nohup java -Xmx6144m -cp "../../../../tool/symbolicexecution/badger-differential/build/*:../../../../tool/symbolicexecution/badger-differential/lib/*:../../../../tool/symbolicexecution/jpf-symbc-differential/build/*:../../../../tool/symbolicexecution/jpf-symbc-differential/lib/*:../../../../tool/symbolicexecution/jpf-core/build/*" edu.cmu.sv.badger.app.BadgerRunner config_symexe $j > ../symexe-out-$j/spf-log.txt &
    spf_pid=$!

    # Wait for timebound
    sleep $time_bound

    # Stop SPF
    kill $spf_pid

    # Assess.
    cd ../fuzzing

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci -dist-target ${fuzz_dist_targets[i]} FuzzDriver @@ > ../symexe-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 ../../../../tool/fuzzing/afl-differential/afl-fuzz-import -i in_dir -o ../symexe-out-$j -c regression -S afl -t 999999999 -r ${num_dist_targets[i]} ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface -t ${num_dist_targets[i]} @@ > ../symexe-out-$j/afl-log.txt

    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10

    cd ../
  done

  cd ../

  # Evaluate run
  python3 ../scripts/evaluate_regression_symexe.py ${subjects[i]}/symexe-out- $number_of_runs $time_bound $step_size_eval

done

# Run just hybrid analysis
for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  cd ./${subjects[i]}/

  for j in `seq 1 $number_of_runs`
  do
    run_counter=$(( $run_counter + 1 ))
    echo "[$run_counter/$total_number_experiments] Run hybrid analysis for ${subjects[i]}, round $j .."

    mkdir ./hydiff-out-$j/

    cd ./fuzzing/

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci -dist-target ${fuzz_dist_targets[i]} FuzzDriver @@ > ../hydiff-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 nohup ../../../../tool/fuzzing/afl-differential/afl-fuzz -i in_dir -o ../hydiff-out-$j -c regression -S afl -t 999999999 -r ${num_dist_targets[i]} ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface -t ${num_dist_targets[i]} @@ > ../hydiff-out-$j/afl-log.txt &
    afl_pid=$!

    cd ../symexe/

    # Start SPF
    nohup java -Xmx6144m -cp "../../../../tool/symbolicexecution/badger-differential/build/*:../../../../tool/symbolicexecution/badger-differential/lib/*:../../../../tool/symbolicexecution/jpf-symbc-differential/build/*:../../../../tool/symbolicexecution/jpf-symbc-differential/lib/*:../../../../tool/symbolicexecution/jpf-core/build/*" edu.cmu.sv.badger.app.BadgerRunner config_hybrid $j > ../hydiff-out-$j/spf-log.txt &
    spf_pid=$!

    # Wait for timebound
    sleep $time_bound

    # Stop SPF, AFL and Kelinci server
    kill $spf_pid
    kill $afl_pid
    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10

    ####
    # Assess both runs, i.e. combine them in one instance afl-spf.
    cd ../fuzzing

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci -dist-target ${fuzz_dist_targets[i]} FuzzDriver @@ > ../hydiff-out-$j/server-afl+spf-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 ../../../../tool/fuzzing/afl-differential/afl-fuzz-import -i in_dir -o ../hydiff-out-$j -c regression -S afl-spf -t 999999999 -r ${num_dist_targets[i]} ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface @@ > ../hydiff-out-$j/afl+spf-log.txt
    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10


    ####
    # Assess only spf run to determine later the times for the first odiff.

    # Start Kelinci server
    nohup java -cp ${classpaths[i]} edu.cmu.sv.kelinci.Kelinci -dist-target ${fuzz_dist_targets[i]} FuzzDriver @@ > ../hydiff-out-$j/server-spf-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_IMPORT_FIRST=1 AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 ../../../../tool/fuzzing/afl-differential/afl-fuzz-import-spf -i in_dir -o ../hydiff-out-$j -c regression -S spf-replay -t 999999999 -r ${num_dist_targets[i]} ../../../../tool/fuzzing/kelinci-differential/fuzzerside/interface @@ > ../hydiff-out-$j/spf-replay-log.txt

    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10

    cd ../
  done

  cd ../

  # Evaluate run
  python3 ../scripts/evaluate_regression_hydiff.py ${subjects[i]}/hydiff-out- $number_of_runs $time_bound $step_size_eval

done
