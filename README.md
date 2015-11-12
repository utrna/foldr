# README #

### What is this repository for? ###

* RNA Folding project code for Robin Gutell's Lab
	* Initial code created Spring 2014
	* Repository initiated Fall 2015

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
	* src		- directory that will house various modules for calling within run code
	* data		- directories housing various inputs and outputs 
	* tests		- will house code tests

### Installation ###
* First, you will have to update your PYTHONPATH, or else your import statements will not work. Enter this into the command line:
	* export PYTHONPATH=${PYTHONPATH}:/path/to/your/clone/of/foldrr
* Next, run the following from within your cloned directory of foldrr:
	* python setup.py install
* Lastly, clean up with the following command and you'll be good to go:
	* python setup.py clean

### Who do I talk to? ###

* If you have any questions about the software, contact Ashton or Vishal.