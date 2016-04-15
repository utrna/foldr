#bpseq_to_alps.py
#alps = alden and piesie combined format, has header with RNA sequence
#Converts bpseq to alps using alden program.
#Takes bpseq (must be pseudo knot free!)
#Creates a temporary alden file from which an alps file is created.
#Run make to compile and create executable(alden) before running bpseq_to_alps.py
#Must run bpseq_to_alps.py in the labelrnaA2.0 directory.
#argv[1]: bpseq file
#argv[2]: path to output directory 
import os
import subprocess
import sys
import pandas as pd
import numpy as np
from string import ascii_lowercase

def bpseq_to_alps():
	#Create alden file from bpseq
	subprocess.call(["alden", sys.argv[1]])

	#Read in alden file
	al = os.path.splitext(sys.argv[1])[0]
	al1 = al + ".labels.csv"
	alden = pd.read_csv(al1, header=None, na_filter=False) 

	#Add columns
	alden.insert(3, "ie", np.nan)
	alden.insert(3, "ps", np.nan)
	alden.insert(3, "label2", np.nan)
	alden.insert(3, "label1", np.nan)

	#Define primary helices
	hairpins = alden[alden[2] == "HAIRPIN"]
	helix = 1
	for row in hairpins.itertuples():
		alden.ix[row[0], "label1"] = 'P'+str(helix)
		up = row[0] + 1 
		down = row[0] - 1
		letter = 0
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
		if len(stack) > 0 and row[8] == stack[len(stack)-1][8]:
			alden.ix[alden[3]==row[8], "ps"] = "SECONDARY"
			alden.ix[alden[3]==row[8], "label1"] = 'S'+str(helix)
			if alden.ix[row[0]+1, 2] != "MULTISTEM" and alden.ix[row[0]+1, 2] != "FREE" and  alden.ix[stack[len(stack)-1][0]-1, 2] != "MULTISTEM" and alden.ix[stack[len(stack)-1][0]-1, 2] != "FREE":
				if alden.ix[row[0]+1, 2] == "BULGE" or alden.ix[row[0]+1, 2] == "INTERNAL":
					alden.ix[row[0]+1, "label1"] = 'S'+str(helix)
				if  alden.ix[stack[len(stack)-1][0]-1, 2] == "BULGE" or alden.ix[stack[len(stack)-1][0]-1, 2] == "INTERNAL":
					alden.ix[stack[len(stack)-1][0]-1, "label1"] = 'S'+str(helix) 
	 			if letter == 0:
					alden.ix[alden[3]==row[8], "ie"] = "INITIATION"
				else:
					alden.ix[alden[3]==row[8], "ie"] = "ELONGATION"
				if row[1] == row[2]:
					alden.ix[alden[3]==row[8], "label2"] = 'x'
				else:
					alden.ix[alden[3]==row[8], "label2"] = ascii_lowercase[letter]
					letter += 1
			else:
				if letter == 0:
					alden.ix[alden[3]==row[8], "ie"] = "INITIATION"
				else:
					alden.ix[alden[3]==row[8], "ie"] = "ELONGATION"
				if row[1] == row[2]:
					alden.ix[alden[3]==row[8], "label2"] = 'x'
				else:
					alden.ix[alden[3]==row[8], "label2"] = ascii_lowercase[letter]
				helix += 1
				letter = 0
			stack.pop()
		else:
			stack.append(row)	
	
	#Read in bpseq, skips the first 4 rows, if header is a different size change skiprows
	bpseq = pd.read_csv(sys.argv[1], sep=' ', header=None, skiprows=4)
	header = open(sys.argv[1]).readlines()[:3]
	print header
	#Get sequence from bpseq
	seq = bpseq[1].tolist()
	sequence = ""
	for i in seq:
		sequence += i

	#write output
	al2 = al + ".alps" #filename
	f = open(al2, 'w')
	for line in header: 
		f.write(line)
		print line 
	f.write('Sequence: ' + sequence + '\n')
	alden.to_csv(f, sep=' ', header = None, index=None)
	f.close()

	#delete alden file
	# subprocess.call(["rm", al1])

if len(sys.argv) != 2:
	print("Need to input bpseq")
elif os.path.isfile("/usr/local/bin/alden"):
	bpseq_to_alps()
else:
	print("You need alden on your path (/usr/local/bin/alden) to work!")

