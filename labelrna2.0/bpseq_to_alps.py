#bpseq_to_alps.py
#alps = alden and piesie combined format, has header with RNA sequence
#Converts bpseq to alps using alden program.
#Takes bpseq (must be pseudo knot free!)
#Creates a temporary alden file from which an alps file is created.
#Run make to compile and create executable(alden) before running bpseq_to_alps.py
#Must run bpseq_to_alps.py in the labelrnaA2.0 directory.
#Two options -nostrip takes the bpseq as is and converts to .alps,
# -strip removes noncanonical basepairs and lone base pairss and converts to .alpsx
#argv[1]: option
#argv[2]: bpseq file
import os
import subprocess
import sys
import pandas as pd
import numpy as np
from string import ascii_lowercase

def strip(bpseq):
	#Get bpseq
	bps = pd.read_csv(bpseq, sep=' ', header=None, skiprows=4)	
	header = open(bpseq).readlines()[:4]

	#Define canonical
	canonical = {('A', 'U'), ('U', 'A'), ('G', 'C'), ('C', 'G'), ('G', 'U'), ('U', 'G')}
	
	#Remove noncanonical base pairs
	for row in bps.itertuples():
		i = row[0]
		pair = row[3]
		if pair != 0:
			bp = (bps.ix[i, 1], bps.ix[pair-1, 1])
			if bp not in canonical:
				bps.ix[i, 2] = 0
				bps.ix[pair-1, 2] = 0
	
	#Remove lone base pairs
	for row in bps.itertuples():
		i = row[0]
		pair = row[3]
		if pair != 0 and bps.ix[i+1, 2] == 0:
			if i == 0:
				bps.ix[i, 2] = 0
				bps.ix[pair-1, 2] = 0
			elif bps.ix[i-1, 2] == 0:
				bps.ix[i, 2] = 0
				bps.ix[pair-1, 2] = 0
	
	#Output new stripped bpseq
	header = open(bpseq).readlines()[:4]
	stripped = os.path.splitext(bpseq)[0] + "1.bpseq"
	f= open(stripped, 'w')
	for line in header:
		f.write(line)
	bps.to_csv(f, sep=' ', header=None, index=None)
	f.close()

	return stripped

def bpseq_to_alps(bpseq, extension):
	
	#Create alden file from bpseq
	subprocess.call(["./alden", bpseq])

	#Read in alden file
	al = os.path.splitext(bpseq)[0]
	al1 = al + ".labels.csv"
	alden = pd.read_csv(al1, header=None, na_filter=False) 

	#Add columns
	alden.insert(3, "ie", np.nan)
	alden.insert(3, "ps", np.nan)
	alden.insert(3, "label2", np.nan)
	alden.insert(3, "label1", np.nan)
	alden.insert(3, "53", np.nan)
	
	#Define 5' and 3' helices
	helices = alden[alden[2] == "HELIX"]
	for row in helices.itertuples():
		if row[1] == row[11]:
			alden.ix[row[0], "53"] = "5'"
	 	else:
			alden.ix[row[0], "53"] = "3'"
	
	#Define 5' and 3' bulges and internals:
	internal = alden[2] == "INTERNAL"
	bulge = alden[2] == "BULGE"
	internals = alden[internal | bulge]
	for row in internals.itertuples():
		if alden.ix[row[0]-1, "53"] == "5'":
			alden.ix[row[0], "53"] = "5'"
		else:
			alden.ix[row[0], "53"] = "3'"

	#Define primary helices
	hairpins = alden[alden[2] == "HAIRPIN"]
	helix = 1
	for row in hairpins.itertuples():
		alden.ix[row[0], "label1"] = 'P'+str(helix)
		letter = 0
		up = row[0] + 1
		down = row[0] -1
		#initiation
		i = alden.ix[up, 3]
		alden.ix[alden[3]==i, "ps"] = "PRIMARY"
		alden.ix[alden[3]==i, "ie"] = "INITIATION"
		alden.ix[alden[3]==i, "label1"] = 'P'+str(helix)
		if row[1] == row[2]:
			alden.ix[alden[3]==i, "label2"] = 'x'
		else:
			alden.ix[alden[3]==i, "label2"] = ascii_lowercase[letter]
			letter += 1
		#elongation
		while up < len(alden.index) and alden.ix[up, 2] != "MULTISTEM" and alden.ix[up, 2] != "FREE" and down > 0 and alden.ix[down, 2] != "MULTISTEM" and alden.ix[down, 2] != "FREE":
			if alden.ix[up, 2] == "BULGE":
				i = alden.ix[up, 3]
				alden.ix[alden[3]==i, "label1"] = 'P'+str(helix)
				up += 1
			if alden.ix[down, 2] == "BULGE":
				i = alden.ix[down, 3]
				alden.ix[alden[3]==i, "label1"] = 'P'+str(helix)
				down -= 1
			if alden.ix[up, 2] == "INTERNAL":
				i = alden.ix[up, 3]
				alden.ix[alden[3]==i, "label1"] = 'P'+str(helix)
			if alden.ix[up, 2] == "HELIX" and alden.ix[up, "ie"] != "INITIATION" and alden.ix[up, "ie"] != "ELONGATION":
				i = alden.ix[up, 3]
				alden.ix[alden[3]==i, "ps"] = "PRIMARY"
				alden.ix[alden[3]==i, "ie"] = "ELONGATION"
				alden.ix[alden[3]==i, "label1"] = 'P'+str(helix)
				if alden.ix[up, 0] == alden.ix[up, 1]:
					alden.ix[alden[3]==i, "label2"] = 'x'
				else:
					alden.ix[alden[3]==i, "label2"] = ascii_lowercase[letter]
					letter += 1
			up += 1
			down -= 1
		helix += 1
	
	#Assign secondary helices
	is_helix = alden[2] == "HELIX"
	not_assigned = alden["ps"] != "PRIMARY"
	sec = alden[is_helix & not_assigned]
	stack = []
	helix = 1
	letter = 0
	for row in sec.itertuples():
		if len(stack) > 0 and row[9] == stack[len(stack)-1][9]:
			alden.ix[alden[3]==row[9], "ps"] = "SECONDARY"
			alden.ix[alden[3]==row[9], "label1"] = 'S'+str(helix)
			if row[0] < sec.size and  len(stack) > 0 and stack[len(stack)-1][0] > 0 and alden.ix[row[0]+1, 2] != "MULTISTEM" and alden.ix[row[0]+1, 2] != "FREE" and  alden.ix[stack[len(stack)-1][0]-1, 2] != "MULTISTEM" and alden.ix[stack[len(stack)-1][0]-1, 2] != "FREE":
				if alden.ix[row[0]+1, 2] == "BULGE" or alden.ix[row[0]+1, 2] == "INTERNAL":
					alden.ix[row[0]+1, "label1"] = 'S'+str(helix)
				if  alden.ix[stack[len(stack)-1][0]-1, 2] == "BULGE" or alden.ix[stack[len(stack)-1][0]-1, 2] == "INTERNAL":
					alden.ix[stack[len(stack)-1][0]-1, "label1"] = 'S'+str(helix) 
	 			if letter == 0:
					alden.ix[alden[3]==row[9], "ie"] = "INITIATION"
				else:
					alden.ix[alden[3]==row[9], "ie"] = "ELONGATION"
				if row[1] == row[2]:
					alden.ix[alden[3]==row[9], "label2"] = 'x'
				else:
					alden.ix[alden[3]==row[9], "label2"] = ascii_lowercase[letter]
					letter += 1
			else:
				if letter == 0:
					alden.ix[alden[3]==row[9], "ie"] = "INITIATION"
				else:
					alden.ix[alden[3]==row[9], "ie"] = "ELONGATION"
				if row[1] == row[2]:
					alden.ix[alden[3]==row[9], "label2"] = 'x'
				else:
					alden.ix[alden[3]==row[9], "label2"] = ascii_lowercase[letter]
				helix += 1
				letter = 0
			stack.pop()
		else:
			stack.append(row)	
	
	#Read in bpseq, skips the first 4 rows, if header is a different size change skiprows
	bpseq1 = pd.read_csv(bpseq, sep=' ', header=None, skiprows=4)
	header = open(bpseq).readlines()[:4]
	
	#Get sequence from bpseq
	seq = bpseq1[1].tolist()
	sequence = ""
	for i in seq:
		sequence += i

	#write output
	if extension == ".alpsx":
		al2 = al[:-1] + extension #filename
	else:
		al2 = al + extension #filename
	f = open(al2, 'w')
	for line in header: 
		f.write(line)
	f.write('Sequence: ' + sequence + '\n')
	alden.to_csv(f, sep=' ', header = None, index=None)
	f.close()

	#delete alden file
	subprocess.call(["rm", al1])

if len(sys.argv) != 3:
	print("Incorrect inputs, needs option and bpseq file.")
elif os.path.isfile(os.getcwd()+"/alden") == False:
	print("Need to run make and/or change directory to labelrna2.0")
elif sys.argv[1] == "-strip":
	bpseq = strip(sys.argv[2])
	bpseq_to_alps(bpseq, ".alpsx")
	#subprocess.call(["rm", bpseq])
elif sys.argv[1] == "-nostrip":
	bpseq_to_alps(sys.argv[2], ".alps")
else:
	print("Acceptable options are -strip and -nostrip.")

