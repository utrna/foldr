#!/usr/bin/env python
# @author <ashtoncberger@utexas.edu>
# ------------------------------------------------
import argparse
import csv
import os
import sys
import matplotlib.pyplot as plt
import numpy as np
import mpld3
from src import rnaf 

'''
Plots possible helices for an RNA sequence.

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

possibleHelices = rnaf.rnaf_exec(sys.stdin, sys.stdout, args)
fig = plt.figure(1)

for k,v in possibleHelices.iteritems():
    for h in v:
        line = None
        if h[2] > -1.0:
            y = [k[0], k[1]]
            x = [h[1], h[0]]
            line = plt.plot(x, y,color='#0d253d')
        elif -2.0 < h[2] < -1.0:
            y = [k[0], k[1]]
            x = [h[1], h[0]]
            line = plt.plot(x, y,color='#005d7d')
        elif -3.0 < h[2] < -2.0:
            y = [k[0], k[1]]
            x = [h[1], h[0]]
            line = plt.plot(x, y,color='#2fade9')
        elif -4.0 < h[2] < -3.0:
            y = [k[0], k[1]]
            x = [h[1], h[0]]
            line = plt.plot(x, y,color='#19d1ff')
        elif h[2] < -4.0:
            y = [k[0], k[1]]
            x = [h[1], h[0]]
            line = plt.plot(x, y,color='#8fffe0')

        if line is not None:
            labelText = ["5' Begin: "+str(k[0]) +","+ " 5' End: "+str(k[1])]
            mpld3.plugins.connect(fig, mpld3.plugins.LineLabelTooltip(line[0], label=labelText))

if args.includecorrect:
    if args.filename != None and args.filename.endswith('.fasta'):
        root_search = str(args.filename).split('/')[-1].strip('fasta')
        if root_search+'piesie' in os.listdir('data/piesie'):
            with open('data/piesie/'+root_search+'piesie', 'rb') as r:
                reader = csv.reader(r, delimiter='\t')
                next(reader, None)
                for row in reader:
                    line = None
                    coords = [int(x) for x in row[1:5]]
                    x = [coords[0], coords[1]]
                    y = [coords[3], coords[2]]
                    print coords
                    line = plt.plot(x, y, color='#ff0000')
                    if line is not None:
                        labelText = ["5' Begin: "+str(y[0]) +","+ " 5' End: "+str(y[1])]
                        mpld3.plugins.connect(fig, mpld3.plugins.LineLabelTooltip(line[0], label=labelText))
            r.close()
        elif root_search+'bpseq' in os.listdir('data/bpseq'):
            pass
            #generate piesie and plt data
        else:
            print 'Could not find correct helices for the input .fasta'
        
mpld3.show()            
