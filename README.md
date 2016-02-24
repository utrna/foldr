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
	* data		- inputs for testing purposes and what not
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
		* Description:
			* reads a .bpseq file and produces a .piesie file
		* Inputs:
			* .bpseq file
		* Outputs:
			* .piesie annotation file
		* Usage example:
			* python bin/python/piesie_annotation.py -i data/bpseq/rnasequence.bpseq -o data/piesie/rnasequence.piesie
	* fold_sequence.py
		* Description:
			* Currently requires a sequence (input as either a .fasta, a .txt file, or manually via the command line) and an energy file .energetics containing the energies of all the possible helices of the sequence. Outputs a tab-delimited .predicted file with corresponding PI hairpin loop predictions. Can also include the .piesie as an option to denote which sequences are the actual sequences.
		* Inputs:
			* .fasta file, .energetics file, .piesie file (OPTIONAL)
		* Outputs:
			* .predicted file
		* Usage example:
			* python bin/python/fold_sequence.py -s data/fasta/rnasequence.fasta -e data/energetics/rnasequence.energetics -p data/piesie/rnasequence.piesie
	* graph_helices.py
		* Description:
			* Given an input sequence via .fasta or manual entry, calculates all possible helices and plots them. Can also include .piesie as an option to include known structures in the plot.
		* Inputs:
			* .fasta file OR manually enter the sequence via the command line
		* Outputs:
			* an interactive graph of all of the possible helices for an RNA sequence
		* Usage example:
			* python bin/python/graph_helices.py sequence.fasta
	* predict_helices.py
		* Description:
			* Given an input .fasta or manually entered sequence, will generate a .energetics file which contains all possible helices for the sequence.
		* Inputs:
			* .fasta file OR manually enter the sequence via the command line
		* Outputs:
			* .energetics file
		* Usage example:
			* python bin/python/predict_helices.py rnasequence.fasta
	* seq_search.py
		* Description:
			* Searches GenBank records for RNA sequences and will return a .fasta of the sequence. Needs to be run interactively unless the Accession ID is already known.
		* Inputs:
			* accession number OR manually interact with the program via the command line
		* Outputs:
			* prints GenBank records to command line
		* Usage example:
			* python bin/python/seq_search.py 

### Who do I talk to? ###

* If you have any questions, please contact Vishal or Ashton.
