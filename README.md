# clique-trust

- src/: java source files (Simulator and BronKerbosch algorithm)
- lib/:  java libraries
- run.sh: run a single simulation (the recommender system), take as parameter a single configuration file
- config_example.txt -- an example of configuration file (simulation parameters)

```console 
fmessina@fmessina-ThinkPad-X250:~$ bash run.sh 
Usage: run.sh <config_file>
```

The results of the simulation are left into a file XXX-parameters.txt (es: gtest-parameters.txt). 
The first column of the file is the user ID, the second column is the measured Precision at the end of the simulation,
 the third column is the measured Recall at the end of the simulation. XXX is the user-provided key of the simulation (see file 
config_example.txt). Moreover, the simulation will compute the file containing all the values of reputation, with 
the path specified in the configuration file. It will leave also a file containing the 
computed trust matrix (e.g.: gtest_trust_ALPHA_0.5_DELTA_0.0.txt).

- runCliques.sh -- run the BronKerbosch algorithm on an existing network. 
It takes various argument from the command line.

```console
fmessina@fmessina-ThinkPad-X250:~$ bash runCliques.sh 
Usage: BronKerboschCliqueFinder <num_agents> <trust_file> <out_file> <minCliqueSize> <MaxCliqueSize> 
(given  0 parameters)
```

- compute_trust_alpha_delta_GTEST.sh: a script to run a number of simulations for different values 
of the paramaters alpha, Delta, threeshold (xi) and k (the maximum size of the set top_x^k). 
The script requires, as input, a simulation key and a configuration template.  

```console 
fmessina@fmessina-ThinkPad-X250:~$  bash compute_trust_alpha_delta_GTEST.sh 
Usage compute_trust_alpha_delta_GTEST.sh <simkey> <template_file>
```

- gconfig_template1kSC1-v2.txt: a template to run multiple simulations by means of the 
script  compute_trust_alpha_delta_GTEST.sh. The template will be automatically extended 
with the configuration items that will have different values in the different simulations (alpha, Delta, k, threeshold s). 

- elab_script/ -- various scripts to generate multiple configurations and manipulate multiple file containing results. 
