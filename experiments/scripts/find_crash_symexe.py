"""
    Script to aggregate the results from an experiment.

    Input: source folder path, e.g.
    python3 python3 evaluate.py blazer_login_unsafe/fuzzer-out-

"""
import sys
import csv
import statistics
import math
import numpy
import re

# do not change this parameters
START_INDEX = 1

if __name__ == '__main__':

    if len(sys.argv) != 4:
        raise Exception("usage: symexe-out-dir n timeout")

    symexeOutDir = sys.argv[1]
    NUMBER_OF_EXPERIMENTS = int(sys.argv[2])
    EXPERIMENT_TIMEOUT = int(sys.argv[3])

    fileNamePatternAFL = re.compile(r"src:\d{6}")
    fileNamePatternSPF = re.compile(r"id:\d{6}")

    collected_crash_data = []
    for i in range(START_INDEX, NUMBER_OF_EXPERIMENTS+1):
        experimentFolderPath = symexeOutDir + str(i)

        # Read export information.
        time_first_crash = -1
        dataFile = experimentFolderPath + "/spf/export-statistic.txt"
        with open(dataFile,'r') as csvfile:
            csvreader = csv.reader(csvfile, delimiter=',')
            next(csvreader) # skip first row
            for row in csvreader:
                if "+crash" in row:
                    time_first_crash = int(row[0])
                    break

        if time_first_crash == -1:
            time_first_crash = EXPERIMENT_TIMEOUT
        collected_crash_data.append(time_first_crash)

    mean_value = "{0:.2f}".format(sum(collected_crash_data)/float(NUMBER_OF_EXPERIMENTS))
    error_value = "{0:.2f}".format(1.960 * numpy.std(collected_crash_data)/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))
    min = min(collected_crash_data)
    print("\ntime +crash>0:\n" + str(mean_value) + " (+/- " + str(error_value) + ") min=" + str(min) + "\n" + str(collected_crash_data))

    exit(0)

    # Aggregate dataFile
    mean_values_outDiff = {}
    error_values_outDiff = {}
    mean_values_decDiff = {}
    error_values_decDiff = {}

    for i in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
        outDiff_values = []
        for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS):
            outDiff_values.append(collected_outDiff_data[j][i])
        mean_values_outDiff[i] = "{0:.2f}".format(sum(outDiff_values)/float(NUMBER_OF_EXPERIMENTS))
        error_values_outDiff[i] = "{0:.2f}".format(1.960 * numpy.std(outDiff_values)/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))

        decDiff_values = []
        for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS):
            decDiff_values.append(collected_decDiff_data[j][i])
        mean_values_decDiff[i] = "{0:.2f}".format(sum(decDiff_values)/float(NUMBER_OF_EXPERIMENTS))
        error_values_decDiff[i] = "{0:.2f}".format(1.960 * numpy.std(decDiff_values)/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))

    # Write collected data
    headers = ['seconds', 'avg_odiff', 'ci_odiff', 'avg_ddiff', 'ci_ddiff']
    outputFileName = symexeOutDir + "results-n=" + str(NUMBER_OF_EXPERIMENTS) + "-t=" + str(EXPERIMENT_TIMEOUT) + "-s=" + str(STEP_SIZE) + ".csv"
    print (outputFileName)
    with open(outputFileName, "w") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=headers)
        writer.writeheader()
        for timeBucket in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
            values = {'seconds' : int(timeBucket)}
            values['avg_odiff'] = mean_values_outDiff[timeBucket]
            values['ci_odiff'] = error_values_outDiff[timeBucket]
            values['avg_ddiff'] = mean_values_decDiff[timeBucket]
            values['ci_ddiff'] = error_values_decDiff[timeBucket]
            writer.writerow(values)

        time_values = list(time_first_odiff.values())
        min = min(time_values)
        if len(time_values) == NUMBER_OF_EXPERIMENTS:
            avg_time = "{0:.2f}".format(sum(time_values)/float(NUMBER_OF_EXPERIMENTS))
            error = "{0:.2f}".format(1.960 * numpy.std(time_values)/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))
            csv_file.write("\ntime +odiff>0:\n" + str(avg_time) + " (+/- " + str(error) + ") min=" + str(min) + "\n" + str(time_values))
        else:
            csv_file.write("\ntime +odiff>0: -\n" + "min=" + str(min) + "\n" + str(time_values))
