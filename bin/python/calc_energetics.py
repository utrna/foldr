#!/usr/bin/env python
import argparse
import csv
import os
import sys
import numpy as np
from src import rnaf 
from src import annotate

'''
To run the program with input file:
    >python bin/python/calc_energetics.py sequence.fasta

To run program without input file (manually input sequence):
    >python bin/python/calc_energetics.py

For help with arguments:
    >python bin/python/calc_energetics.py -h
'''
parser = argparse.ArgumentParser(description="Give an input .fasta or a user defined sequences, can either show or save a plot of all of the possible sequences as well as the correct sequences for the secondary structure (if applicable).")
parser.add_argument("-minls", "--minloopsize", help="Minimum loop size", type=int, default=3)
parser.add_argument("-maxls", "--maxloopsize", help="Maximum loop size", type=int, default=max)
parser.add_argument("-minl", "--minlength", help="Minimum stack size", type=int, default=2)
parser.add_argument("-maxl", "--maxlength", help="Maximum stack size", type=int, default=12)
parser.add_argument("filename", nargs='?', help="File containing input sequence", default=None)
args = parser.parse_args()


sq=""
outfile="possibleHelices.energetics"

if (args.filename != None):
    if args.filename.endswith('.fasta'):
        sq = annotate.fasta_iter(args.filename).next()[1]
        outfile = 'data/energetics/'+args.filename.rstrip('.fasta').split('fasta')[-1]+'.energetics'
    else:
        with open(args.filename, "r") as inFile:
            sq = inFile.read()
else:
    print("Enter a sequence; press CTRL+D twice when done.")
    sq = r.read()
sq = sq.lower()
sq = sq.replace(" ", "")
sq = sq.replace("\n", "")
sq =sq.replace("t","u")

possibleHelices = rnaf.rnaf_evalSequence(sq, args.minloopsize, args.maxloopsize, args.minlength, args.maxlength)
rnaf.rnaf_findEnergetics(possibleHelices, sq)

hFile = open(outfile, "w")
hFile.write("5START,5STOP,3START,3STOP,LENGTH,LOOPSIZE,ENERGY,COMPARATIVE,COMPARATIVE_HAIRPIN,LABEL,PARENT_HELIX")
for k, v in possibleHelices.iteritems():
    for h in v:
        hFile.write("\n" + str(k[0]) + "," + str(k[1]) + "," + str(h[0]) + "," + str(h[1]) + "," + str(k[1] - k[0] + 1) + "," + str(h[0] - k[1] - 1) + "," + str(h[2]) +",0,0,0,0")

