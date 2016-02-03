#!/usr/bin/env python
# @author <ashtoncberger@utexas.edu>
# ------------------------------------------------
import argparse
import csv
import os
import sys
import pandas as pd
from src import annotate
from src import structures as struct
'''
Folds an RNA sequence...eventually. Right now just accomplishes PI predictions.
'''
parser = argparse.ArgumentParser()
parser.add_argument("-s", "--seqFile", help="File containing input sequence", default=None)
parser.add_argument("-e", "--energyFile", help="File containing all possible helices and energies", default=None)
parser.add_argument("-p", "--piesie", help="File containing correct structure annotations (piesie file)", default=None)
parser.add_argument("-op", "--outpred", help="Include this flag to output predicted helices data frame", action="store_true")
args = parser.parse_args()

if args.seqFile != None:
    if args.seqFile.endswith('.fasta'):
        sq = annotate.fasta_iter(args.seqFile).next()[1]
    else:
        with open(args.seqFile, "r") as inFile:
            sq = inFile.read()
else:
    sq = raw_input("Enter a sequence and press enter: ")
sq = sq.upper()
sq = sq.replace(" ", "")
sq = sq.replace("T","U")

if args.energyFile != None:
    pis = struct.predict_pis(pd.read_csv(args.energyFile), sq)
else:
    print 'No energetics inputted'
    # TODO: get energetics

if args.piesie != None:
    pred_pis = struct.check_pi_preds(pis, pd.read_table(args.piesie, sep = '\t'))

if args.outpred:
    outfile = 'data/predictions/'+args.energyFile.rstrip('.energetics').split('energetics')[-1]+'.predicted'
    pred_pis.to_csv(outfile, index=False)

'''
Below is from previous student and is not involved with the above code. 
Right now, all that is accomplished by this program is PI structure predictions for feeding into Vishal's Java code. 
Ideally, we would use a wrapper script to automate feeding the PI structures into Vishal's extension algorithm or port his code to Python and include it below here.
'''

# # max window size = 20
# def rnaf_fold():
#     # import sequence (unecessary as of now...)
#     sqStr = ""
#     with open (args.seqFile, "r") as inFile:
#         sqStr = inFile.read()

#     # import energetics
#     allPossibleHelices = [] # list of all possible helices
#     with open(args.energyFile,"rb") as csvfile:
#         read = csv.DictReader(csvfile)
#         for h in read:
#             helix = Helix(newHelix = h)
#             allPossibleHelices.append(helix)

#     # folding
#     rna = FoldingStrand(sequence = sqStr, allHelices = allPossibleHelices, windowSize = 20)
#     while(rna.isDone() != True):
#         # print "======\nRange: [" + str(rna.lowerRange) + " " + str(rna.upperRange) + "]"
#         rna.addNextBase()
#         rna.formHelices()

#         if (args.gf):
#             writer.writeToGraphFile(rna.transientHelices, rna.farTransientHelices, rna.consideredHelices)

#     # folding done

#     # rna.printStrand(index = True)
#     rna.printTransients()

# rnaf_fold()

'''
** initial aim: get short-range folds
== folding == 
1. increment search window
2. find all helices that can form within window
3. recalc conditional distance for those helices (based on transient helices from last round)
4. choose most stable helices, store them in transient (after clearing transient of course)
5. repeat loop

naive "choosing" method (step 4 above):
    order by modified energy (from most stable to least stable)
    starting at beginning, add to transient list
    if helix does not conflict with any other currently on the list


ah! innerloops are important because they decrease conditional distances.
this allows helices with larger hairpin loops to form more easily.
hmm.. but what prevents them from staying together?
perhaps distances after big folds will force them apart.

== calc. conditional distance ==
condDist = 0
1. find distance between 5'end of curHelix and start of nearest transient helix (add to condDist)
2. jump to 3'end of transient helix (do not add anything to condDist)
3. continue counting/moving towards upperRange (add 1 to condDist per base)
    -repeat step 2 if any transient helices are encountered
4. upon reaching 3'start, store condDist in curHelix. done.

== hairpin loop ==
1. get range of helix
2. create HairpinLoop object

this object removes all bases involved from strand list and stores them in internal list
the bases are inserted back into the right places if this hairpinloop is removed


**note:
    currently the window is static (size 18)
    it does NOT take conditional distances into consideration. but it must.
'''