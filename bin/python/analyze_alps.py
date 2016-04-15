#System Imports
import json
import static
import sys
import os
import time
import random
import pandas as pd 
import numpy as np 
import glob 


phyla = '''actinobacteria
alphaproteobacteria
aquificae
bacilli
clostridium
mollicutes'''.split('\n')

#Gather Paths For Alden File 
data_root = '/Users/red/Desktop/foldr_data/CRW_Data/SB.'
# alden_paths = open('{}/alden_paths.txt') 
alps_dict = {}

for phylum in phyla: 
	# print specie
	if phylum == '': pass 
	pattern = '{}{}/*alps'.format(data_root, phylum)
	print pattern 
	alps_files = glob.glob(pattern)
	alps_dict[phylum] = alps_files

print alps_dict 
for phylum, alps in alps_dict.iteritems(): 
	print phylum + ' ' + str(len(alps))


testfile = '/Users/red/Desktop/foldr_data/CRW_Data/SB.actinobacteria/AB184486.alps'


def build_dataframe_from_alps(filepath, phylum=''):

	if not os.path.isfile(filepath): 
		return False

	info = {}
	ff = open(filepath)
	l = ff.readline()
	while l[0] not in str(range(0,10)): 
		# print l
		es = l.rstrip().split(':')
		k = es[0].strip()
		v = ':'.join(es[1:])
		info[k]=v.strip()
		l = ff.readline()

	print info
	try: 
		accession = info['Accession Number']
	except: 
		accession = info['Accession Numbers']
	if ',' in accession:
		accession = accession.split(',')[0]
	organism = info['Organism']

	names = ['start', 'end', 'type', 'helix', 'subhelix', 'primary', 'initiation', 'unique', 'preceding_unique']

	#SET DATAFRAME DEFAULTS
	df = pd.read_csv(filepath, sep=' ', skiprows=[0,1,2,3,4], usecols=[0,1,2,3,4,5,6,7,8], names=names)
	df['accession'] = accession.strip() 
	df['organism'] = organism.strip()
	# df['kingdom'] = 'Bacteria'  # Bacteria, Archaea, Eukarya 
	df['phylum'] = phylum  
	df['molecule'] = '16S'  # 5S, 16S, or tRNA 
	# df['tRNA_AA'] = None


	# Skip all sequences with IUPAC code letters?
	bpseq = '.'.join(filepath.split('.')[:-1]) + '.bpseq'
	print 'checking for bpseq file: ' + bpseq 
	if os.path.isfile(bpseq): 
		print 'bpseq file found, extracting sequence'
		sequence_array = []
		bpseqf = open(bpseq)
		l = bpseqf.readline()
		while l[0] not in str(range(0,10)): 
			# print 'not used headers: '
			# print l
			l = bpseqf.readline()
		ls = bpseqf.readlines()
		for l in ls: 
			# print l 
			c = l.split(' ')[1]
			sequence_array.append(c)
		sequence = ''.join(sequence_array)
		reference = pd.DataFrame({'accession':accession, 'name':organism, 'record_id':accession, 'seq':sequence}, index=[0])
	else: 
		print 'bpseq file not available, retrieving sequence from genbank'
		from Bio import Entrez, SeqIO 
		Entrez.email = "russdurrett@utexas.edu"
		handle = Entrez.efetch(db="nuccore", id=accession, rettype="gb", retmode="text")
		record = SeqIO.parse(handle, 'gb').next()
		reference = pd.DataFrame({'accession':accession, 'name':record.name, 'record_id':record.id, 'seq':str(record.seq)}, index=[0])
		sequence = record.seq
	######

	print accession + " : " + organism + ' : ' + sequence

	for n in sequence: 
		if n.upper() not in ['A', 'G', 'T', 'C', 'U']: 
			print 'aborting import because sequence contains non-AGTC character'
			quit()

	df['seq'] = ''
	df['seq_length'] = 0

	if sequence: 
		for i, row in df.iterrows(): 
			row['start'] = row['start'] - 1
			s = ''.join(sequence[int(row['start']):int(row['end'])])
			if s: 
				df.set_value(i, 'seq', s)
				df.set_value(i, 'seq_length', len(s))
	return df 






# df = build_dataframe_from_alps(testfile, phylum='test')
# print df 






