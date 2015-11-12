#!/usr/bin/env python
import argparse
import csv
import os
import sys
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
parser.add_argument("-o", "--helicesout", help="Include this flag to output the possible helices used to generate the graph", action="store_true")
parser.add_argument("-incl", "--includecorrect", help="Include this flag to include correct helices in the graph (if applicable)", action="store_true")
args = parser.parse_args()

possibleHelices = graphing.rnaf_exec(sys.stdin, sys.stdout, args)

for k,v in possibleHelices.iteritems():
    for h in v:
        # print("("+str(k[0])+','+str(h[1])+"), ("+str(k[1])+','+str(h[0])+")")
        y = [k[0], k[1]]
        x = [h[1], h[0]]
        plt.plot(x, y,color='blue')
plt.plot([120,0],[120,0], color = 'black')

if args.includecorrect:
    if args.filename != None and args.filename.endswith('.fasta'):
        root_search = str(args.filename).split('/')[-1].strip('fasta')
        if root_search+'piesie' in os.listdir('data/piesie'):
            with open('data/piesie/'+root_search+'piesie', 'rb') as r:
                reader = csv.reader(r, delimiter='\t')
                next(reader, None)
                for row in reader:
                    coords = [int(x) for x in row[1:5]]
                    x = [coords[0], coords[1]]
                    y = [coords[3], coords[2]]
                    print coords
                    plt.plot(x, y, color='red')
            r.close()
        elif root_search+'bpseq' in os.listdir('data/bpseq'):
            pass
            #generate piesie and plt data
        else:
            print 'Could not find correct helices for the input .fasta'

plt.show()              
