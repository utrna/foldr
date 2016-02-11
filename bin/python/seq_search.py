#!/usr/bin/env python
# @author <ashtoncberger@utexas.edu>
# ------------------------------------------------
import argparse
from Bio import Entrez, SeqIO
import sys

'''
Searches GenBank records for RNA sequences and returns a .fasta.

'''

Entrez.email = "robin.gutell@mail.utexas.edu"

#if no accession id, prompt the interactive search
#TODO: allow accession id input

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

#print records for now, eventually want to extend API to allow direct usability of record .fastas with folding programs
print record
