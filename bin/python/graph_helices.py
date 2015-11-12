#!/usr/bin/env python
import sys
import argparse
import matplotlib.pyplot as plt
import numpy as np
from src import graphing 
'''
To run the program with input file:
    >python bin/python/graph_helices.py sequence.fasta

To run program without input file (manually input sequence):
    >python bin/python/graph_helices.py

For help with arguments:
    >python bin/python/graph_helices.py -h
'''
parser = argparse.ArgumentParser(description="Give an input .fasta or a user defined sequences, can either show or save a plot of all of the possible sequences as well as the correct sequences for the secondary structure (if applicable).")
parser.add_argument("-minls", "--minloopsize", help="Minimum loop size", type=int, default=3)
parser.add_argument("-maxls", "--maxloopsize", help="Maximum loop size", type=int, default=max)
parser.add_argument("-minl", "--minlength", help="Minimum stack size", type=int, default=2)
parser.add_argument("-maxl", "--maxlength", help="Maximum stack size", type=int, default=12)
parser.add_argument("filename", nargs='?', help="File containing input sequence", default=None)
parser.add_argument("-g", "--graph", help="Include this flag to generate output for the graphing program instead of the folding program", action="store_true")
args = parser.parse_args()

possibleHelices = graphing.rnaf_exec(sys.stdin, sys.stdout, args)

for k,v in possibleHelices.iteritems():
    for h in v:
        print("("+str(k[0])+','+str(h[1])+"), ("+str(k[1])+','+str(h[0])+")")
        y = [k[0], k[1]]
        x = [h[1], h[0]]
        plt.plot(x, y,color='blue')
plt.plot([0,0],[120,120], color = 'black')


plt.show()

plt.close('all')
                        
