<!-- [![DOI](https://zenodo.org/badge/157902250.svg)](https://zenodo.org/badge/latestdoi/157902250) -->
# HyDiff: Hybrid Differential Software Analysis
This repository provides the tool and the evaluation subjects for the paper *HyDiff: Hybrid Differential Software Analysis* accepted for the technical track at ICSE'2020. A pre-print of the paper is available [here](https://yannicnoller.github.io/publications/icse2020_noller_hydiff.pdf).

Authors:
[Yannic Noller](https://yannicnoller.github.io),
[Corina S. Pasareanu](https://www.cylab.cmu.edu/directory/bios/pasareanu-corina.html),
[Marcel Böhme](https://mboehme.github.io),
[Youcheng Sun](https://sites.google.com/site/theyoucheng/),
[Hoang Lam Nguyen](https://github.com/hoanglam-nguyen),
and [Lars Grunske](https://www.informatik.hu-berlin.de/de/Members/lars-grunske).

The repository includes:
* a [setup](setup.sh) script,
* the experiment subjects: [experiments/subjects](./experiments/subjects),
* the summarized experiment results: [experiments/results](./experiments/results),
* the scripts to rerun all experiments: [experiments/scripts](./experiments/scripts),
* and the source code for both components of Hydiff: [tool/fuzzing](./tool/fuzzing), and [tool/symbolicexecution](./tool/symbolicexecution).

A pre-built version of HyDiff is also available as [Docker image](https://hub.docker.com/r/yannicnoller/hydiff):
```
docker pull yannicnoller/hydiff
docker run -it --rm yannicnoller/hydiff
```

## Tool
HyDiff's technical framework is built on top of [Badger](https://github.com/isstac/badger), [DifFuzz](https://github.com/isstac/diffuzz), and the [Symbolic PathFinder](https://github.com/SymbolicPathFinder).
We provide a complete snapshot of all tools and our extensions.

### Requirements
* Git, Ant, Build-Essentials, Gradle
* Java JDK = 1.8
* Python3, Numpy Package
* recommended: Ubuntu 18.04.1 LTS

### Folder Structure
The folder *tool* contains 2 subfolders: *fuzzing* and *symbolicexecution*, representing the both components of HyDiff.

#### fuzzing

* *afl-differential*:
The fuzzing component is built on top of DifFuzz and KelinciWCA (the fuzzing part of Badger).
Both use [AFL](http://lcamtuf.coredump.cx/afl/) as the underlying fuzzing engine.
In order to make it easy for the users, we provide our complete modified AFL variant in this folder.
Our modifications are based on [afl-2.52b](http://lcamtuf.coredump.cx/afl/releases/?O=D).

* *kelinci-differential*: 
Kelinci leverages a server-client architecture to make AFL applicable to Java applications, please refer to the Kelinci [poster-paper](https://dl.acm.org/citation.cfm?id=3138820) for more details.
We modified it to make usable in a general differential analysis.
It includes an *interface* program to connect the *Kelinci server* to the AFL fuzzer and the *instrumentor* project, which is used to instrument the Java bytecode.
The instrumentation handles the coverage reporting and the collection of our differential metrics.
The Kelinci server handles requests from AFL to execute a mutated input on the application.

#### symbolicexecution

* *jpf-core*:
Our symbolic execution is built on top of Symbolic PathFinder (SPF), which is an extension of [Java PathFinder](https://github.com/javapathfinder) (JPF), which makes it necessary to include the core implementation of JPF.

* *jpf-symbc-differential*:
In order to make SPF applicable to a differential analysis, we modified in several locations and added the ability to perform some sort of *shadow symbolic execution* (cf. [Complete Shadow Symbolic Execution with Java PathFinder](https://github.com/hub-se/jpf-shadow-plus)).
This folder includes the modified SPF project.

* *badger-differential*:
HyDiff performs a hybrid analysis by running fuzzing and symbolic execution in parallel.
This concept is based on Badger, which provides the technical basis for our implementation.
This folder includes the modified Badger project, which enables the differential hybrid analysis, incl. the differential dynamic symbolic execution.

### How to install the tool and run our evaluation
Be aware that the instructions have been tested for Unix systems only.

1. First you need to build the tool and the subjects.
We provide a script *setup.sh* to simply build everything.
Note: the script may override an existing site.properties file, which is required for JPF/SPF.

2. Test the installation: the best way to test the installation is to execute the evaluation of our example program (cf. Listing 1 in our paper).
You can execute the script [run_example.sh](./experiments/scripts/run_example.sh).
As it is, it will run each analysis (just differential fuzzing, just differential symbolic execution, and the hybrid analysis) once.
The values presented in our paper in Section 2.2 are averaged over 30 runs.
In order to perform 30 runs each, you can easily adapt the script, but for some first test runs you can leave it as it is.
The script should produce three folders:
    * experiments/subjects/example/fuzzer-out-1: results for differential fuzzing
    * experiments/subjects/example/symexe-out-1: results for differential symbolic execution
    * experiments/subjects/example/hydiff-out-1: results for HyDiff (hybrid combination)
It will also produce three csv files with the summarized statistics for each experiment:
    * experiments/subjects/example/fuzzer-out-results-n=1-t=600-s=30.csv
    * experiments/subjects/example/symexe-out-results-n=1-t=600-s=30.csv
    * experiments/subjects/example/hydiff-out-results-n=1-t=600-s=30-d=0.csv

3. After finishing the building process and testing the installation, you can use the provided *run* scripts ([experiments/scripts](./experiments/scripts)) to replay HyDiff's evaluation or to perform your own differential analysis.
HyDiff's evaluation contains three types of differential analysis.
For each of them you will find a separate run script:
    * [run_regression_evaluation.sh](./experiments/scripts/run_regression_evaluation.sh)
    * [run_sidechannel_evaluation.sh](./experiments/scripts/run_sidechannel_evaluation.sh)
    * [run_dnn_evaluation.sh](./experiments/scripts/run_dnn_evaluation.sh)

In the beginning of each run script you can define the experiment parameters:
* `number_of_runs`: `N`, the number of evaluation runs for each subject (30 for all experiments)
* `time_bound`: `T`, the time bound for the analysis (regression: 600sec, side-channel: 1800sec, and dnn: 3600sec)
* `step_size_eval`: `S`, the step size for the evaluation (30sec for all experiments)
* [`time_symexe_first`: `D`, the delay with which fuzzing gets started after symexe for the DNN subjects] (only DNN)

Each run script first executes differential fuzzing, then differential symbolic execution and then the hybrid analysis.
Please adapt our scripts to perform your own analysis.

For each *subject*, *analysis_type*, and experiment repetition *i* the scripts will produce folders like:
`experiments/subjects/<subject>/<analysis_type>-out-<i>`,
and will summarize the experiments in csv files like:
`experiments/subjects/<subject>/<analysis_type>-out-results-n=<N>-t=<T>-s=<S>-d=<D>.csv`.

### Complete Evaluation Reproduction
In order to reproduce our evaluation completely, you need to run the three mentioned run scripts.
They include the generation of all statistics.
Be aware that the mere runtime of all analysis parts is more than **53 days** because of the high runtimes and number of repetitions.
So it might be worthwhile to run it only for some specific subjects or to run the analysis on different machines in parallel or to modify the runtime or to reduce the number of repetitions.
Feel free to adjust the script or reuse it for your own purpose.

### Statistics
As mentioned earlier, the statistics will be automatically generated by our run script, which execute the python scripts from the *scripts* folder to aggregate the several experiment runs.
They will generate csv files with the information about the average result values.

For the regression analysis and the DNN analysis we use the scripts:
* [experiments/scripts/evaluate_regression_fuzz.py](./experiments/scripts/evaluate_regression_fuzz.py)
* [experiments/scripts/evaluate_regression_symexe.py](./experiments/scripts/evaluate_regression_symexe.py)
* [experiments/scripts/evaluate_regression_hydiff.py](./experiments/scripts/evaluate_regression_hydiff.py)

For the side-channel analysis we use the scripts:
* [experiments/scripts/evaluate_cost_fuzz.py](./experiments/scripts/evaluate_cost_fuzz.py)
* [experiments/scripts/evaluate_cost_symexe.py](./experiments/scripts/evaluate_cost_symexe.py)
* [experiments/scripts/evaluate_cost_hydiff.py](./experiments/scripts/evaluate_cost_hydiff.py)

All csv files for our experiments are included in [experiments/results](./experiments/results).
    
Feel free to adapt these evaluation scripts for your own purpose.

## Maintainers

* **Yannic Noller** (yannic.noller at acm.org)


## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
