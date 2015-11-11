#!/usr/bin/env python
import argparse
import operator
import sys

'''

Usage:

python piesie_annotation.py -i rnasequence.bpseq -o rnasequence.txt

'''
parser = argparse.ArgumentParser(description="Given an input .bpseq file, outputs a PieSie annotation file.")
parser.add_argument("-i", "--input", help=".bpseq file of interest. Stored in data/bpseq/5s or data/bpseq/16s.")
parser.add_argument("-o", "--output", help="Desired output file name. If none provided, will use the input name and place in data/piesie/ directory. Output format is tab-delimitted .txt.")
args = parser.parse_args()

outfile = str(args.output)

if not outfile.endswith('.txt'):
    try:
        outfile = args.input.strip('bpseq')+'txt'
    except:
        print "Input file either does not exist or is corrupted! Exception: "
        raise
    if '/' in outfile:
        outfile = outfile.split('/')[-1]
    if '5s' in outfile:
        outfile = 'data/piesie/5s/' + outfile
    elif '16s' in outfile:
        outfile = 'data/piesie/16s/' + outfile
    else:
        print "Not a proper format input .bpseq. There should be a 5s or a 16s in the input .bpseq name."
        sys.exit()

'''
input step
    -reads in necessary information from .bpseq and store in lists
'''

### retrieves the list of sequence pairs and returns a list
infile = open(args.input,'r')
temp = []
for line in infile.readlines():
    line = line.rstrip('\n')
    line = line.split()
    if line[0].isdigit():
        temp.append(line)

### takes input file of sequence base pairing and writes to a file
### variables to store data
holder     = []
hold2      = []
final      = []
primes     = []
p_num      = 0
alphabet   = 'abcdefghijklmnopqrstuvwxyz'

#print temp ### DEBUG

### declares 5 and 3 prime start and end    
start_5     = 0
end_5       = 0 
start_3     = 0
end_3       = 0

### go through sequence list and keep continuous stretches that are base pairing        
for i in range(len(temp)):
    cut = 0

    #print i ### DEBUG

    ### makes sure that we do not repeat base pair sequences. 
    ### I.E 2 pairs with 3 and 3 pairs with 2. would result in double    
    if temp[i][2] != '0' and int(temp[i][2]) > int(temp[i][0]):
        holder.append(temp[i])
        #print holder ### DEBUG

    if temp[i][2] =='0' and len(holder) != 0:
        ### makes sure segment basepairs are continuous.
        for j in range(1,len(holder)):
            if int(holder[j-1][2])-1 != int(holder[j][2]):
                cut = j
                #print "cut=",cut ### DEBUG

        holder = holder[cut:]

        #print holder ### DEBUG

        ### determines start and stop points
        start_5 = int(holder[0][0])
        end_3     = int(holder[0][2])

        if len(holder) > 1:                
            end_5     = int(holder[-1][0]) 
            start_3 = int(holder[-1][2])

        if len(holder) == 1:
            end_5     = int(start_5) 
            start_3 = int(end_3)
            
        #print "s5=",start_5,", e5=",end_5,", s3=",start_3,", e3=",end_3 ### DEBUG

        ### makes a list from the end of the previous continuous base pair sequence to the next
        ### continuous set of base pairs
        hold2 = temp[int(end_5):int(holder[0][2])]

        #print hold2 ### DEBUG

        ### used to determine if a non zero has appeared and will write out primary intitiations only
        ### count must be 1 and the 5 prime end must equal the first non-zero in this list
        count = 0

        for k in range(len(hold2)):
            if hold2[k][2] != '0':
                count += 1
                #print "count=",count ### DEBUG

            if hold2[k][2] == str(end_5) and count == 1:
                p_num += 1
                primes.append([start_5,end_5,start_3,end_3])
                #print "p_num=",p_num ### DEBUG

        holder = []

    hold2 = []    
    
######################################################        
######################################################

### this loop will retrieve primary elongations
final_elongs       = []
holder2            = []
holder             = []

### use a while loop so that you can go back to specific location if conditions met    
i = 0

while i < len(temp):
    ### cut determines cut off of elongation sequence. pi_spans will count how many PIs spanned
    cut            = 0
    pi_spans = 0

    ### makes sure the nucleotide pairs and is pairing with a larger nucleotide.
    ### this prevents redundancy        
    ###
    ### Logic error!    Need to include check for contiguous base pairs here?
    ###
    ### Not certain if this is the right place.    I can see some colliding cases
    ### that make this less straightforward.
    ###
    if temp[i][2] != '0' and int(temp[i][2]) > int(temp[i][0]):
        holder.append(temp[i])
        # print "PE phase: ",temp[i] ### DEBUG

    ### resets the list if a base pair does not pair and determines if current list
    ### is a PE.            
    ###
    ### Is this the source of the logic fault?    After finding a cut point, the break
    ### statement prevents the code from continuing across a helix with a bulge.
    ### Also, the code is only checking the 3' strand for discontinuties, which can
    ### also occur on the 5' strand.
    ###
    ### If we apply additional constraints sooner, does this code work as desired?
    ###
    ###
    ### Phrased another way: the current list is not being defined to guarantee that 
    ### it contains at most one PE.
    ###

    #print "holder=",holder ### DEBUG

    if temp[i][2] =='0' and len(holder) != 0: 
        if len(holder) > 1:
            for j in range(len(holder)-1):
                # print "j=",j,", holder=",holder ### DEBUG
                ### determines if there is a cut off in the list of sequential base pairing.            
                if int(holder[j][2]) - 1 != int(holder[j+1][2]):
                    cut = j
                    # print "cut=",cut ### DEBUG
                    break
                
        if cut != 0:
            holder = holder[:cut+1]
            i = i + cut - len(holder) - 2
            cut = 0
        
        ### stores the starts and stops as integers            
        pe5_start         = int(holder[0][0])
        pe5_end             = int(holder[-1][0]) 
        pe3_end             = int(holder[0][2])
        pe3_start         = int(holder[-1][2])
        holder                = []
        helix_spanned = 0

        for k in range(len(primes)):
            if pe5_start < int(primes[k][0]) and pe3_end > int(primes[k][3]):
                pi_spans += 1
                helix_spanned = k+1
                # print "pi_spans=",pi_spans," / helix_spanned=",helix_spanned ### DEBUG

        if pi_spans == 1:
            final_elongs.append([pe5_start, pe5_end, pe3_start, pe3_end, helix_spanned])

            # print(holder) ### DEBUG
            # print(primes) ### DEBUG

        holder = []
    i+=1

####################################################
####################################################

### this section picks out the Secondary Initiation sites. The SIs
all           = []
all_secondary = []
si            = []
se            = []
holder        = []
val           = 0 
i             = 0

while i < len(temp):
    cut     = 0
    spans = 0

    # print "SI Loop: ",temp[i] ### DEBUG

    if temp[i][2] != '0' and int(temp[i][2]) > int(temp[i][0]):
        holder.append(temp[i]) 

    # print "i=",i,", holder=",holder ### DEBUG
    
    if temp[i][2] =='0' and len(holder) != 0:
        for j in range(len(holder)-1):
            if int(holder[j][2]) - 1 != int(holder[j+1][2]):
                cut = j
                break

        if cut != 0:
            i = i + cut - len(holder)
            holder = holder[:cut+1]
            
        pe5_start = int(holder[0][0])
        pe5_end     = int(holder[-1][0]) 
        pe3_end     = int(holder[0][2])
        pe3_start = int(holder[-1][2])
        
        all.append([pe5_start,pe5_end,pe3_start,pe3_end])
        holder = []

        # print "i=",i,", all=",all ### DEBUG
    
    i += 1
    
copies = 0    

for i in range(len(all)): 
    for j in range(len(primes)):
        if all[i][0] == primes[j][0]:
            copies += 1
            
    for j in range(len(final_elongs)):
        if all[i][0] == final_elongs[j][0]:
            copies += 1
    
    if copies == 0:
        all_secondary.append(all[i])
        # print "i=",i,", all_secondary=",all_secondary ### DEBUG

    copies = 0

### will find the PIs each secondary helix spans and sorts the secondaries by 3' start

all_secondary = sorted(all_secondary, key = operator.itemgetter(2))
spans                 = 0
sec                     = []

# print(primes)

for i in range(len(all_secondary)):
    pi_spans = []

    # print(all_secondary[i])

    for j in range(len(primes)):
        if all_secondary[i][0] < primes[j][0] and all_secondary[i][3] > primes[j][3]:
            pi_spans.append(j)
            #print "i=",i,", j=",j,", pi_spans=",pi_spans ### DEBUG

    sec.append([int(x)+1 for x in pi_spans])

    #print "i=",i,", sec=",sec ### DEBUG   

### will pick out the SIs

si         = []
si_num = 0

#print "DEBUG before scanning len(sec)" ### DEBUB
#print "len(sec)=",len(sec) ### DEBUG

###
### BUG:    THIS LOOP IS NOT ENTERED WHEN ONLY ONE SECONDARY
### USING (len(sec)-1)
###
for i in range(len(sec)):
#for i in range(len(sec)-1):
    #print "i=",i,", all_secondary=",all_secondary[i] ### DEBUG
    if i == 0:
        si_num += 1

        si.append(all_secondary[i])

    else:
        if sec[i] != sec[i-1]:
            si_num += 1

            si.append(all_secondary[i])
        
#print "DEBUG after scanning len(sec)" ### DEBUB

### will pick out the extensions
ex_num = 0
si_num = 1

for i in range(1,len(sec)):
    if sec[i] == sec[i-1]:
        ex_num += 1
    else:
        ex_num = 0
        si_num += 1

'''
output step
    -reformats lists with information as needed
    -writes to output destination file
'''
out = open(outfile,'w')

pe_outs = sorted(final_elongs, reverse=True)
se_outs = zip(all_secondary, sec)

# print "len primes", len(primes)
# print "primes=", primes
# print '\n'
# print "len fe", len(final_elongs)
# print "final_elongs=", final_elongs 
# print '\n'
# print "len all_sec", len(all_secondary)
# print "all_secondary=", all_secondary
# print '\n'
# print "len sec", len(sec)
# print "sec=", sec
# print '\n'
# print "len si", len(si)
# print "si=", si
# print '\n'
# print 'se_outs=', se_outs

out.write("NAME\t5START\t5STOP\t3START\t3STOP\tTYPE\n")

nc = 1
pc = 1
sc = 1
for i in range(len(primes)):
    primes[i].insert(0, (str(nc)+'-P'+str(pc)+'a'))
    primes[i].insert(5, ('PI'))
    out.write('\t'.join([str(x) for x in primes[i]]))
    out.write('\n')
    alpha_i = 1
    alpha_k = 1
    for j in range(len(pe_outs)):
        if pe_outs[j][-1] == i+1:
            pe_outs[j].insert(0, (str(nc)+'-P'+str(pc)+alphabet[alpha_i]))
            pe_outs[j].insert(5, ('PE'))
            out.write('\t'.join([str(x) for x in pe_outs[j][:-1]]))
            out.write('\n')
            alpha_i += 1
    nc += 1
    if len(se_outs) != 0:
        if se_outs[0][1][-1] == pc:
            si_entry = se_outs.pop(0)[0]
            si_entry.insert(0, (str(nc)+'-S'+str(sc)+'a'))
            si_entry.insert(5, ('SI'))
            out.write('\t'.join([str(x) for x in si_entry]))
            out.write('\n')
            if len(se_outs) == 0:
                break
            while se_outs[0][1][-1] == pc:
                si_entry = se_outs.pop(0)[0]
                si_entry.insert(0, (str(nc)+'-S'+str(sc)+alphabet[alpha_k]))
                si_entry.insert(5, ('SI'))
                out.write('\t'.join([str(x) for x in si_entry]))
                out.write('\n')
                alpha_k += 1
                if len(se_outs) == 0:
                    break
            sc += 1
            nc += 1
    pc += 1

# print 'se_outs should be empty:', se_outs
