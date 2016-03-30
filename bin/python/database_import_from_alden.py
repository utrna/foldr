#System Imports
import json
import static
import sys
import os
import time
import random

import psycopg2

# test connection to foldr postgres db 
# try:
#     conn = psycopg2.connect("dbname='d1be1knu2m4p03' user='vmekkobwaemmtz' host='ec2-54-227-250-148.compute-1.amazonaws.com' password='lmlbGN0CDaUB6ChGv7bBdBFjFt'")
# except:
#     print "I am unable to connect to the foldr database"


import pandas as pd 

# test run 

try: 
	f = sys.argv[1]
except: 
	f = "/Users/red/Desktop/foldr_data/CRW_Data/SB.bacilli/DQ129518.alden"

info = {}
ff = open(f)
l = ff.readline()
while l[0] not in str(range(0,10)): 
	# print l
	es = l.rstrip().split(':')
	k = es[0].strip()
	v = ':'.join(es[1:])
	info[k]=v.strip()
	l = ff.readline()

print info
accession = info['Accession Numbers']
if ',' in accession:
	accession = accession.split(',')[0]
organism = info['Organism']

names = ['start', 'end', 'type', 'unique', 'preceding_unique']

#SET DATAFRAME DEFAULTS
df = pd.read_csv(f, skiprows=[0,1,2,3], usecols=[0,1,2,3,4], names=names)
df['accession'] = accession.strip() 
df['organism'] = organism.strip()
df['kingdom'] = 'Bacteria'  # Bacteria, Archaea, Eukarya 
df['phylum'] = 'actinobacteria' 
df['molecule'] = '16S'  # 5S, 16S, or tRNA 
df['tRNA_AA'] = None


# Skip all sequences with IUPAC code letters?
bpseq = '.'.join(f.split('.')[:-1]) + '.bpseq'
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

df['seq'] = ''
df['seq_length'] = 0

if sequence: 
	for i, row in df.iterrows(): 
		row['start'] = row['start'] - 1
		s = ''.join(sequence[int(row['start']):int(row['end'])])
		if s: 
			df.set_value(i, 'seq', s)
			df.set_value(i, 'seq_length', len(s))

from sqlalchemy import create_engine
engine = create_engine('postgres://vmekkobwaemmtz:lmlbGN0CDaUB6ChGv7bBdBFjFt@ec2-54-227-250-148.compute-1.amazonaws.com:5432/d1be1knu2m4p03')


df.to_sql('aldens', engine, if_exists='append')
reference.to_sql('references', engine, if_exists='append')









