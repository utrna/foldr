#!/usr/bin/env python
import argparse
from Bio import Entrez, SeqIO
import sys

'''
To run the program with input file:
    >python bin/python/graph_helices.py sequence.fasta

To run program without input file (manually input sequence):
    >python bin/python/graph_helices.py

For help with arguments:
    >python bin/python/graph_helices.py -h
'''

Entrez.email = "robin.gutell@mail.utexas.edu"

#if no accession id, prompt the interactive search

#interactive search error-checking
searchterm = Entrez.read(Entrez.espell(term=raw_input("Enter the name of the organism you which to search: ")))
if len(searchterm['CorrectedQuery']) > 0:
    to_correct = raw_input('Did you mean to search for \"'+str(searchterm['CorrectedQuery'])+'\"? (yes/no) ')
    while True:
        if to_correct.lower() == 'yes':
            searchterm = searchterm['CorrectedQuery']
            break
        elif to_correct.lower() == 'no':
            searchterm = searchterm['Query']
            break
        else:
            to_correct = raw_input('Type yes or no and press Enter: ')
else:
    searchterm = searchterm['Query']
seqtype = raw_input("Enter the sequence type: (5s/16s/23s) ")
if seqtype not in ['5s', '16s', '23s']:
    while True:
        if seqtype not in ['5s', '16s', '23s']:
            seqtype = raw_input("Type 5s, 16s, or 23s and press Enter: ")
        else:
            break
retlimit = raw_input("Enter the maximum number of results you wish you see as an integer: ")
if not type(retlimit) is int:
    while True:
        try:
            retlimit = int(retlimit)
        except:
            retlimit = raw_input("Type an integer and press Enter: ")
        else:
            break

searchhandle = Entrez.esearch(db="nucleotide", retmax=retlimit, term=str(searchterm)+'[orgn] '+str(seqtype)+ ' ribosomal RNA', property='biomol_rRNA')
record = Entrez.read(searchhandle)
searchhandle.close()

print record
# # Download E. coli K12 genome:
# download_handle = Entrez.efetch(db="nucleotide", id="CP009685", rettype="gb", retmode="text")
# data = download_handle.read()
# download_handle.close()

# # Store data into file "Ecoli_K12.gb":
# out_handle = open("Ecoli_K12.gb", "w")
# out_handle.write(data)
# out_handle.close()

# in_handle = open("Ecoli_K12.gb", "r")
# record = SeqIO.read(in_handle, "genbank")
# in_handle.close()

# count = 0
# for feature in record.features:
#     if feature.type == 'CDS':
#         count += 1
# print "Number of coding sequences in the E. coli K12 genome:", count

# in_handle = open("Ecoli_K12.gb", "r")
# record = SeqIO.read(in_handle, "genbank")
# in_handle.close()

# count = 0
# for feature in record.features:
#     if feature.type == 'CDS':
#         if "product" in feature.qualifiers:
#             if feature.qualifiers["product"][0] == "hypothetical protein":
#                 count += 1
# print "Number of hypothetical proteins in the E. coli K12 genome:", count

# in_handle = open("Ecoli_K12.gb", "r")
# record = SeqIO.read(in_handle, "genbank")
# in_handle.close()

# imax = 5 # number of CDSs to print
# i = 0
# for feature in record.features:
#     if feature.type == 'CDS':
#         print feature.qualifiers["locus_tag"][0]
#         print feature.location
#         seq = feature.location.extract(record).seq
#         print seq
#         print seq.translate()
#         print
#         i += 1
#     if i >= imax:
#         break