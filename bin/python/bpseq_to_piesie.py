#!/usr/bin/env python
# @author <ashtoncberger@utexas.edu>
# ------------------------------------------------
import argparse
import sys
from src import annotate

'''
Converts .bpseq files to .piesie files


Usage example:

python bin/python/piesie_annotation.py -i data/bpseq/rnasequence.bpseq -o data/piesie/rnasequence.piesie

'''
parser = argparse.ArgumentParser(description="Given an input .bpseq file with the -i argument, outputs a PieSie annotation file.")
parser.add_argument("-i", "--input", help=".bpseq file of interest. Stored in data/bpseq.")
parser.add_argument("-o", "--output", help="Desired output file name. If none provided, will use the input name and place in data/piesie/ directory. Output format is tab-delimitted .txt.")
args = parser.parse_args()

outfile = str(args.output)

if not outfile.endswith('.piesie'):
    try:
        outfile = args.input.strip('bpseq')+'piesie'
    except:
        print "Input file either does not exist or is corrupted! Exception: "
        raise
    if '/' in outfile:
        outfile = 'data/piesie/' + outfile.split('/')[-1]
    else:
        print "Not a proper format input .bpseq."
        sys.exit()

annotation_information = annotate.annotate_piesie(args.input)
annotate.output_piesie(annotation_information, outfile)
