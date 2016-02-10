# README #

### What is this repository for? ###
* RNA Folding project code for Robin Gutell's Lab
	* Initial code created Spring 2014 and onward...
	* Repository/source control initiated Fall 2015

### Contributors ###
* Contributors:
	* Robin Gutell		- Principle Investigator, Gutell Lab
	* Jamie Cannone		- Research Associate, Gutell Lab
	* Ashton Berger		- Fall 2015 BIO321G student researcher
	* Vishal Patel		- Fall 2015 BIO321G student researcher

### Contribution guidelines ###

* We should probably discuss and outline these...
* I think for right now, it's probably best for each individual to fork his/her own branch and make changes there. Once you feel you have a significant amount of work stored there, we can all meet, discuss the changes, and then incorporate the changes into the master branch.

### Organization ###
	* bin		- directory housing run code
	* src		- package directory that houses various modules for calling within run code
	* inputs	- inputs for testing purposes and what not
	* tests		- will house code tests
	
### Installation ###
* First, update your PYTHONPATH via the command line or else you will get ImportErrors:
	* export PYTHONPATH=${PYTHONPATH}:/your/local/directory/with/copy/of/foldrr
* Next, install using the following command:
	* python setup.py install
* Last, clean up your directory and start folding!
	* python setup.py clean

### Usage ###
* bin/python/
	* bpseq_to_piesie.py	
		* Given an input .bpseq, outputs a corresponding .piesie annotation file
	* fold_sequence.py
		* Currently requires a sequence (input as either a .fasta, a .txt file, or manually via the command line) and an energy file .energetics containing the energies of all the possible helices of the sequence. Outputs a tab-delimited .predicted file with corresponding PI hairpin loop predictions. Can also include the .piesie as an option to denote which sequences are the actual sequences.
	* graph_helices.py
		* Given an input sequence via .fasta or manual entry, calculates all possible helices and plots them. Can also include .piesie as an option to include known structures in the plot.
	* predict_helices.py
		* Given an input .fasta or manually entered sequence, will generate a .energetics file which contains all possible helices for the sequence.
	* seq_search.py
		* Searches GenBank records for RNA sequences and will return a .fasta of the sequence. Needs to be run interactively unless the Accession ID is already known.



### Who do I talk to? ###

* If you have any questions, please contact Vishal or Ashton.
