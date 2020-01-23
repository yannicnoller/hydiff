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
    data1=[3, 3, 1, 2, 3, 2, 3, 3, 3, 3, 4, 3, 3, 2, 3, 5, 4, 3, 2, 2, 1, 4, 1, 3, 2, 2, 2, 5, 3, 1]

    # Fuzzing / Symexe
    data2=[3, 5, 4, 2, 3, 3, 4, 4, 3, 3, 3, 3, 3, 3, 3, 1, 2, 2, 3, 2, 5, 3, 4, 5, 4, 3, 2, 2, 3, 4]


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
