#!/usr/bin/env python
# @author <ashtoncberger@utexas.edu>
# ------------------------------------------------
'''
Module for predicting possible helices, mostly written by other class.
'''
import re
import sys
import itertools
from annotate import fasta_iter

# test_sq = "agucuacu"

# searched pairings:
# AU
# GC
# GU

# G matches U or C
# U matches G or A

# ends of many helices tend to be AA or GG 90% of the time
# 5'A G 3'  (last unpaired)
#   A-U 
#   x-x

# dict of base pair neighbor energies, used to calc energetics for helices
rnaEnergetics = {
("au","au"):-0.9, ("au","cg"):-2.2, ("au","gc"):-2.1, ("au","gu"):-0.6, ("au","ua"):-1.1, ("au","ug"):-1.4,
("cg","au"):-2.1, ("cg","cg"):-3.3, ("cg","gc"):-2.4, ("cg","gu"):-1.4, ("cg","ua"):-2.1, ("cg","ug"):-2.1,
("gc","au"):-2.4, ("gc","cg"):-3.4, ("gc","gc"):-3.3, ("gc","gu"):-1.5, ("gc","ua"):-2.2, ("gc","ug"):-2.5,
("gu","au"):-1.3, ("gu","cg"):-2.5, ("gu","gc"):-2.1, ("gu","gu"):-0.5, ("gu","ua"):-1.4, ("gu","ug"):+1.3,
("ua","au"):-1.3, ("ua","cg"):-2.4, ("ua","gc"):-2.1, ("ua","gu"):-1.0, ("ua","ua"):-0.9, ("ua","ug"):-1.3,
("ug","au"):-1.0, ("ug","cg"):-1.5, ("ug","gc"):-1.4, ("ug","gu"):+0.3, ("ug","ua"):-0.6, ("ug","ug"):-0.5
}

# dict of regexes used to form regex that searches for complementary sequences
complementaryBaseRegex = {"u":"(g|a)", "g":"(u|c)", "a":"u", "c":"g"}

# given a base pair, rnaf_findHelices will search for start indexes of that pair in a sequence
def rnaf_findHelices(bp,sq):
    f = [m.start() for m in re.finditer("(?=" + bp + ")", sq)]
    return f

def rnaf_createPermRegex(bp):
    reg = ""
    r_bp = bp[::-1] #reversed base pair
    for c in r_bp:
        reg += complementaryBaseRegex.get(c) # look up and add matching regex
    return reg

# given a sequence, rnaf_evalSequence iterates through the squence,
# starting with two base pairs and going up to (length of the seq)/2 base pairs
# and finds start/ends of complementary base pairs that could form a helix
# returns a dictionary matching a sequence with other possible sequences in anti-parallel
def rnaf_evalSequence(sq,minLoopSize,maxLoopSize,minLength,maxLength):
    ret = {}
    for searchLen in xrange(minLength, maxLength):
        # print "=== i:" + str(searchLen) + " ==="  
        # iterate through sq, starting at x and taking i base pairs at a time
        for sqStart in xrange(0, len(sq) - (searchLen - 1)):
            sqEnd = sqStart + searchLen
            antiParallelRegex = rnaf_createPermRegex(sq[sqStart:sqEnd])
            foundSequences = rnaf_findHelices(antiParallelRegex, sq[sqEnd+minLoopSize:]) #minLoopSize right here: sq[sqEnd+minLoopSize:]
            matchedHelices = [(fsqStart+sqEnd+minLoopSize+1, fsqStart+sqEnd+minLoopSize+searchLen) for fsqStart in foundSequences]
            # print "sqStart = " + str(sqStart) + " sqEnd = " + str(sqEnd)
            # print "found seq " + str(foundSequences)
            # print "matchedHelices " + str(matchedHelices)
            # print "---"

            if len(matchedHelices) > 0:
                ret[(sqStart+1,sqEnd)] = matchedHelices
    d = ret 
    rsq = sq[::-1]  # reverse sq
    # for each key (5' side)
    for k, v in d.iteritems():
        matchingEnergies = []
        helixLength = k[1]-k[0]
        # for each matching sequence (3' side) paired with the key
        for msq in v:
            totalEnergy = 0
            for i in xrange(0, helixLength):
                kStart = k[0]-1 + i
                kEnd   = kStart + 1
                cStart = len(sq) - msq[1] + i
                cEnd   = cStart + 1
                bp1    = sq[kStart] + rsq[cStart]
                bp2    = sq[kEnd]   + rsq[cEnd]
                totalEnergy += rnaEnergetics[(bp1,bp2)]
            matchingEnergies.append( (msq[0], msq[1], round(totalEnergy, 2)) )  # new dict entry
        d[k] = matchingEnergies # overwrite current dict entry with energetics
    return d 

# formats data for javascript-based web graph
def rnaf_writeToFileWebGraph(d):
    hFile = open("possibleHelices.csv", "w")
    hFile.write("UID,5Begin,5End,3Begin,3End,Length,LoopSize,Energy,isComparative,isHairpin,roundAdmitted,condDist,condProb,foldType,influencedFolds\n")
    for k, v in d.iteritems():
        for h in v:
            hFile.write("0," + str(k[0]) + "," + str(k[1]) + "," + str(h[0]) + "," + str(h[1]) + "," + str(k[1] - k[0]) +",0," + str(h[2]) +",0,0,0,0,0,0,:\n")

# formats data for david bell's program
def rnaf_writeToFileGauss(d):
    hFile = open("possibleHelices.csv", "w")
    hFile.write("5_Begin,5_End,3_Begin,3_End,Length,LoopSize,Energy,Comparative,IsComparativeHairpin,Label,Parent_Helix")
    for k, v in d.iteritems():
        for h in v:
            hFile.write("\n" + str(k[0]) + "," + str(k[1]) + "," + str(h[0]) + "," + str(h[1]) + "," + str(k[1] - k[0] + 1) + "," + str(h[0] - k[1] - 1) + "," + str(h[2]) +",0,0,0,0")

#pretty print dictionary
def rnaf_pDict(d,w):
    for k, v in d.iteritems():
        w.write(str(k) + '\t' + str(v) + '\n')

def rnaf_exec(r,w,args):
    sq=""

    if (args.filename != None):
        if args.filename.endswith('.fasta'):
            sq = fasta_iter(args.filename).next()[1]
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

    possibleHelices = rnaf_evalSequence(sq, args.minloopsize, args.maxloopsize, args.minlength, args.maxlength)
    rnaf_findEnergetics(possibleHelices, sq)
        
    # rnaf_pDict(possibleHelices, w)
    if args.graph:
        rnaf_writeToFileWebGraph(possibleHelices)
    if args.helicesout:
        rnaf_writeToFileGauss(possibleHelices)
        return possibleHelices
    else:
        return possibleHelices
