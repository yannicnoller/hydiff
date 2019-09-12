"""
    Script to aggregate the results from an experiment.

    Input: source folder path, e.g.
    python3 evaluate.py blazer_login_unsafe/fuzzer-out-

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

    if len(sys.argv) != 5:
        raise Exception("usage: symexe-out-dir n timeout stepsize")

    symexeOutDir = sys.argv[1]
    NUMBER_OF_EXPERIMENTS = int(sys.argv[2])
    EXPERIMENT_TIMEOUT = int(sys.argv[3])
    STEP_SIZE = int(sys.argv[4])

    fileNamePatternAFL = re.compile(r"src:\d{6}")
    fileNamePatternSPF = re.compile(r"id:\d{6}")

    collected_data = []
    time_delta_greater_zero = {}
    for i in range(START_INDEX, NUMBER_OF_EXPERIMENTS+1):
        experimentFolderPath = symexeOutDir + str(i)

        data = {}

        # Read export information.
        timeInfo = {}
        dataFile = experimentFolderPath + "/spf/export-statistic.txt"
        with open(dataFile,'r') as csvfile:
            csvreader = csv.reader(csvfile, delimiter=',')
            next(csvreader) # skip first row
            for row in csvreader:
                fileName = fileNamePatternSPF.findall(row[2])[0]
                timeInfo[fileName] = int(row[0])

        # Read data.
        dataFile = experimentFolderPath + "/afl/path_costs.csv"
        with open(dataFile,'r') as csvfile:
            csvreader = csv.reader(csvfile, delimiter=';')
            timeBucket = STEP_SIZE
            next(csvreader) # skip first row
            previousHighscore = 0
            currentHighscore = 0
            for row in csvreader:
                if len(row) == 1:
                    continue
                fileName = fileNamePatternAFL.findall(row[1])
                if len(fileName) == 0:
                    continue
                fileName = fileName[0].replace("src", "id")
                if not fileName in timeInfo:
                    continue
                currentTime = timeInfo[fileName]
                fileNameInfo = row[1]
                containsHighscore = "highscore" in fileNameInfo
                currentScore = int(row[5])

                if i not in time_delta_greater_zero and currentScore > 0:
                    time_delta_greater_zero[i] = currentTime

                if containsHighscore:
                    currentHighscore = currentScore

                while (currentTime > timeBucket):
                    data[timeBucket] = previousHighscore
                    timeBucket += STEP_SIZE

                previousHighscore = currentHighscore

                if timeBucket > EXPERIMENT_TIMEOUT:
                    break

            # fill data with last known value if not enough information
            while timeBucket <= EXPERIMENT_TIMEOUT:
                data[timeBucket] = previousHighscore
                timeBucket += STEP_SIZE

        collected_data.append(data)

        if i not in time_delta_greater_zero:
            time_delta_greater_zero[i] = EXPERIMENT_TIMEOUT

    # Aggregate dataFile
    mean_values = {}
    error_values = {}
    max_values = {}

    delta_values = {}
    for i in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
        delta_values[i] = []
        for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS):
            delta_values[i].append(collected_data[j][i])
        mean_values[i] = "{0:.2f}".format(sum(delta_values[i])/float(NUMBER_OF_EXPERIMENTS))
        error_values[i] = "{0:.2f}".format(1.960 * numpy.std(delta_values[i])/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))
        max_values[i] = max(delta_values[i])

    # Write collected data
    headers = ['seconds', 'avg_highscore', 'ci', 'maximum']
    outputFileName = symexeOutDir + "results-n=" + str(NUMBER_OF_EXPERIMENTS) + "-t=" + str(EXPERIMENT_TIMEOUT) + "-s=" + str(STEP_SIZE) + ".csv"
    print (outputFileName)
    with open(outputFileName, "w") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=headers)
        writer.writeheader()
        for timeBucket in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
            values = {'seconds' : int(timeBucket)}
            values['avg_highscore'] = mean_values[timeBucket]
            values['ci'] = error_values[timeBucket]
            values['maximum'] = max_values[timeBucket]
            writer.writerow(values)

        time_values = list(time_delta_greater_zero.values())
        if len(time_values) == NUMBER_OF_EXPERIMENTS:
            avg_time = "{0:.2f}".format(sum(time_values)/float(NUMBER_OF_EXPERIMENTS))
            error = "{0:.2f}".format(1.960 * numpy.std(time_values)/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))
            csv_file.write("\ntime delta>0:\n" + str(avg_time) + " (+/- " + str(error) + ")\ndelta>0Times=" + str(time_values) + "\ndeltas=" + str(delta_values[EXPERIMENT_TIMEOUT]) + "\ndeltas(30s)=" + str(delta_values[30]))
        else:
            csv_file.write("\ntime delta>0: -\ndelta>0Times=" + str(time_values) + "\ndeltas=" + str(delta_values[EXPERIMENT_TIMEOUT]) + "\ndeltas(30s)=" + str(delta_values[30]))
