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

    # HyDiff
    data1=[575, 569, 582, 567, 577, 570, 576, 573, 571, 582, 583, 579, 575, 577, 599, 589, 576, 584, 571, 567, 570, 567, 565, 572, 570, 579, 578, 564, 575, 572]


    # Fuzzing
    data2=[570, 586, 588, 573, 587, 575, 578, 575, 593, 574, 589, 575, 595, 582, 576, 582, 579, 582, 598, 571, 585, 579, 583, 580, 585, 584, 579, 574, 586, 590]


    if len(data1) != len(data2) or len(data1) != n:
        print("Wrong number of elements!")
        exit()


    print("n=" + str(len(data1)))
    print('data1: mean=%.2f stdv=%.2f' % (mean(data1), std(data1)))
    print('data2 mean=%.2f stdv=%.2f' % (mean(data2), std(data2)))

    print()

    print("Student's t-test:")
    stat, p = ttest_ind(data1, data2)
    print('Statistics=%.2f, p=%.2f' % (stat, p))
    if p > alpha:
	       print('Same distributions (fail to reject H0)')
    else:
	       print('Different distributions (reject H0)')

    print()

    print("Mann-Whitney U Test")
    stat, p = mannwhitneyu(data1, data2)
    print('Statistics=%.2f, p=%.2f' % (stat, p))
    if p > alpha:
        print('Same distributions (fail to reject H0)')
    else:
        print('Different distributions (reject H0)')

    print()

    print("> Wilcoxon Signed-Rank Test")
    stat, p = wilcoxon(data1, data2)
    print('Statistics=%.2f, p=%.2f' % (stat, p))
    if p > alpha:
        print('Same distributions (fail to reject H0)')
    else:
        print('Different distributions (reject H0)')

    print()
