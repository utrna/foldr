#!/usr/bin/env python
# @author <ashtoncberger@utexas.edu>
# ------------------------------------------------
import argparse
import csv
import os
import sys
import numpy as np
import pandas as pd
import rnaf 
import annotate
import structures as struct
import json

'''
All possible helices predictions, mainly written by previous class.
'''
parser = argparse.ArgumentParser(description="Give an input .fasta or a user defined sequence and this will predict and filter primary helices. May provide configuration parameter file and reference PieSie for validation.")
parser.add_argument("filename", nargs='?', help="File containing input sequence", default=None)
parser.add_argument("-p", "--params", help="File containing json filtering parameters", default=None)
parser.add_argument("-r", "--piesie", help="File containing correct structure annotations (piesie file)", default=None)
parser.add_argument("-o", "--outpred", help="Include this flag to output filtered helices data frame", default=None)
args = parser.parse_args()


if (args.filename != None):
    if args.filename.endswith('.fasta'):
        sq = annotate.fasta_iter(args.filename).next()[1]
        outfile = args.filename.replace('.fasta', '.helices')
else:
    sq = raw_input("Enter a sequence and press enter: ")
sq = sq.lower()
sq = sq.replace(" ", "")
sq =sq.replace("t","u")

if args.params != None: 
    params_file = open(args.params)
    params = json.loads(params_file.read())
else: 
    print 'You Can Input Params -p <file>'
    print "Just using default params for this run"
    params = {
        "minloopsize": 3,
        "maxloopsize": 15,
        "minlength": 2, 
        "maxlength":15}

print "Using these params:" + str(params)

possibleHelices = rnaf.rnaf_evalSequence(sq, params['minloopsize'], params['maxloopsize'], params['minlength'], params['maxlength'])
# rnaf.rnaf_findEnergetics(possibleHelices, sq)

to_write = []
for k, v in possibleHelices.iteritems():
    for h in v:
        to_write.append([str(k[0]),str(k[1]),str(h[0]),str(h[1]),str(k[1]-k[0]+1),str(h[0]-k[1]-1),str(h[2])])
to_write.sort(key=lambda x: (int(x[0]),int(x[1])))
 

tmp_handle = 'tmpfile.allhelices'
tmp_file = open(tmp_handle,'w')
tmp_file.write("5START,5STOP,3START,3STOP,LENGTH,LOOPSIZE,ENERGY\n")
for helix in to_write: 
    tmp_file.write(",".join(helix)+'\n')
tmp_file.close()


### END OF ASHTONS CODE ###
#Read In Raw Predicted Helices And Apply Stats 

df = pd.read_csv(tmp_handle)
#filter out the structures with loops that are too big and energies that are too high
pis = df[(df.LOOPSIZE < params['maxloopsize']) & (df.LOOPSIZE >= params['minloopsize']) & (df.ENERGY < 0)].copy()


#add sequence info to potential helices 
pis['5SEQ'] = [sq[x:y] for x,y in zip(pis['5START'].tolist(), pis['5STOP'].tolist())]
pis['3SEQ'] = [sq[x:y] for x,y in zip(pis['3START'].tolist(), pis['3STOP'].tolist())]
pis['LOOPSEQ'] = [sq[x:y-1] for x,y in zip(pis['5STOP'].tolist(), pis['3START'].tolist())]


# pis object now contains data neccesary for filtering 

# FILTER ON HAIRPIN LOOP SEQ
# PROXY UNTIL WE LOAD IN THE REAL THING 
loop_score_df = pd.DataFrame({'Seq':['uucg', 'cuug'], 'Score': [5.0, 5.0]})
#If Loop Seq Contains A Sequence In LoopDF, Take Max 
pis['LOOP_SCORE'] = pis['LOOPSEQ'].apply(lambda s: loop_score_df[loop_score_df.apply(lambda x: x['Seq'] in s, axis=1)]['Score'].max())  
pis['LOOP_SCORE'].fillna(0, inplace=True)
# IF Starts With G and Ends With A. Sloppy but practical... 
pis['LOOP_SCORE'] = pis.apply(lambda row: row['LOOP_SCORE']+5 if (row['LOOPSEQ'][0]=='g') & (row['LOOPSEQ'][-1]=='a') else row['LOOP_SCORE'], axis=1)


# CALCULATE MODIFIED ENERGY FROM LOOP_SCORE AND LOOP SIZE 
pis['E_MOD'] = (pis["ENERGY"] - (pis["LOOP_SCORE"])) / pis["LOOPSIZE"]
pis = pis.sort_values(by="E_MOD")
pis.reset_index(inplace=True, drop=True)



print 'Constructed DataFrame Of {} Predicted Primary Helices'.format(len(pis))

print 'Filtering Out Redundant Helices'
pislist = pis.values.tolist()
filtered_pis = [] 
for pi in pislist: 
    surrounding = pis[(pis['5START'] <= pi[0]) & (pis['5STOP'] >= pi[1]) & (pis['3START'] <= pi[3]) & (pis['3STOP'] >= pi[4])]
    if len(surrounding) > 1: 
        pass 
    else: 
        filtered_pis.append(pi)

df = pd.DataFrame.from_records(filtered_pis)
df.columns = pis.columns 
pis = df 

print 'Filtered DataFrame Of {} Predicted, Nonredundant Primary Helices'.format(len(pis))
print pis.head()


if args.piesie != None:
    pred_pis = struct.check_pi_preds(pis, pd.read_table(args.piesie, sep = '\t'))

if args.outpred:
    outfile = 'outfile'
    pred_pis.to_csv(outfile, index=False)



    
