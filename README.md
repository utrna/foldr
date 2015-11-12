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

### Who do I talk to? ###

* If you have any questions, please contact Vishal or Ashton.