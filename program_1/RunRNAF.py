#!/usr/bin/env python

"""
To run the program with input file:
    % python RunRNAF.py RunRNAF.in

To run program without input file (manually input sequence):
	% python RunRNAF.py

For help with arguments:
	% python RunRNAF.py -h
"""
import sys
import argparse
from RNAF import rnaf_exec

parser = argparse.ArgumentParser()
parser.add_argument("-minls", "--minloopsize", help="Minimum loop size", type=int, default=3)
parser.add_argument("-maxls", "--maxloopsize", help="Maximum loop size", type=int, default=max)
parser.add_argument("-minl", "--minlength", help="Minimum stack size", type=int, default=2)
parser.add_argument("-maxl", "--maxlength", help="Maximum stack size", type=int, default=12)
parser.add_argument("filename", nargs='?', help="File containing input sequence", default=None)
parser.add_argument("-g", "--graph", help="Include this flag to generate output for the graphing program instead of the folding program", action="store_true")
args = parser.parse_args()

rnaf_exec(sys.stdin, sys.stdout, args)