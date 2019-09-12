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
from numpy import mean
from numpy import std
from scipy.stats import ttest_ind
from scipy.stats import mannwhitneyu
from scipy.stats import wilcoxon
from scipy.stats import sem
from scipy.stats import t

# do not change this parameters
START_INDEX = 1

# significance level
alpha = 0.05




if __name__ == '__main__':

    n = 30

    # HyDiff-AFL
    data1=[575, 962, 942, 914, 577, 570, 576, 573, 571, 905, 583, 579, 950, 577, 599, 589, 576, 584, 920, 567, 570, 892, 565, 572, 946, 919, 578, 564, 942, 883]



    # HyDiff-SPF
    data2=[575, 569, 582, 567, 577, 570, 576, 573, 571, 582, 583, 579, 575, 577, 599, 589, 576, 584, 571, 567, 570, 567, 565, 572, 570, 579, 578, 564, 575, 572]


    if len(data1) != len(data2) or len(data1) != n:
        print("Wrong number of elements!")
        exit()


    print("n=" + str(len(data1)))
    print('data1: mean=%.2f error=+/-%.2f' % (mean(data1), 1.960 * numpy.std(data1)/float(math.sqrt(n))))
    print('data2 mean=%.2f error=+/-%.2f' % (mean(data2), 1.960 * numpy.std(data2)/float(math.sqrt(n))))


    for i in range(0, n):
        data1[i] = min(data1[i], data2[i])
    best=min(data1)

    print('merged_data mean=%.2f error=+/-%.2f best=%.0f' % (mean(data1), 1.960 * numpy.std(data1)/float(math.sqrt(n)), best))

    print(data1)
