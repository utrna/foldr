#System Imports
import json
import static
import sys
import os
import time
import random
import pandas as pd 
import numpy as np 
import glob 

from matplotlib import pyplot as plt



def build_dataframe_from_alps(filepath, phylum=''):

	if not os.path.isfile(filepath): 
		return False

	info = {}
	ff = open(filepath)
	l = ff.readline()
	while l[0] not in str(range(0,10)): 
		# print l
		es = l.rstrip().split(':')
		k = es[0].strip()
		v = ':'.join(es[1:])
		info[k]=v.strip()
		l = ff.readline()

	print info
	try: 
		accession = info['Accession Number']
	except: 
		accession = info['Accession Numbers']
	if ',' in accession:
		accession = accession.split(',')[0]
	organism = info['Organism']

	names = ['start', 'end', 'type', 'prime', 'helix', 'subhelix', 'primary', 'initiation', 'unique', 'preceding_unique']

	#SET DATAFRAME DEFAULTS
	df = pd.read_csv(filepath, na_filter=False, sep=' ', skiprows=[0,1,2,3,4], usecols=[0,1,2,3,4,5,6,7,8,9], names=names)
	df['accession'] = accession.strip() 
	df['organism'] = organism.strip()
	# df['kingdom'] = 'Bacteria'  # Bacteria, Archaea, Eukarya 
	df['phylum'] = phylum  
	df['molecule'] = '16S'  # 5S, 16S, or tRNA 
	# df['tRNA_AA'] = None

	sequence = info['Sequence']

	print accession + " : " + organism + ' : ' + sequence

	for n in sequence: 
		if n.upper() not in ['A', 'G', 'T', 'C', 'U']: 
			print 'aborting import because sequence contains non-AGTC character'
			return None

	df['seq'] = ''
	df['seq_length'] = 0

	if sequence: 
		for i, row in df.iterrows(): 
			row['start'] = row['start'] - 1
			s = ''.join(sequence[int(row['start']):int(row['end'])])
			if s: 
				df.set_value(i, 'seq', s)
				df.set_value(i, 'seq_length', len(s))

	df['prime_shift'] = df.prime.shift(1) 

	return df 



def build_alps_paths_by_phyla(): 

	phyla = '''actinobacteria
	alphaproteobacteria
	aquificae
	bacilli
	clostridium
	mollicutes'''.split('\n')

	#Gather Paths For Alden File 
	data_root = '/Users/red/Desktop/foldr_data/CRW_Data/SB.'
	# alden_paths = open('{}/alden_paths.txt') 
	alps_dict = {}

	for phylum in phyla: 
		# print specie
		if phylum == '': pass 
		pattern = '{}{}/*alps'.format(data_root, phylum)
		alps_files = glob.glob(pattern)
		print pattern + ' : ' + str(len(alps_files))
		alps_dict[phylum] = alps_files

	print alps_dict 
	for phylum, alps in alps_dict.iteritems(): 
		print phylum + ' ' + str(len(alps))
	return alps_dict




def flatten_array(array): 
	return [item for sublist in array for item in sublist]

# file_dict = build_alps_paths_by_phyla()
# testfile = file_dict['actinobacteria'][0]
testfile = '/Users/red/Desktop/foldr/data/bpseq/Ecoli_16s-rRNA.alps'
df = build_dataframe_from_alps(testfile, phylum='test')
print df 


fives = []
threes = [] 
records = 10 

act_alps = open('/Users/red/Desktop/foldr_data/CRW_Data/SB.actinobacteria/alps.txt').readlines()
for alp in act_alps[0:records]: 
	alp = alp.strip()
	df = build_dataframe_from_alps(alp, phylum='actinobacteria')

	internals = df.groupby('type').get_group('INTERNAL')
	fives.append(internals.groupby('prime_shift').get_group("5'")['seq'].tolist())
	threes.append(internals.groupby('prime_shift').get_group("3'")['seq'].tolist())


fives = flatten_array(fives)
threes = flatten_array(threes)

five_freqs = {}
three_freqs = {}

for five in set(fives): 
	five_freqs[five] = float(fives.count(five))/float(records)
for three in set(threes): 
	three_freqs[three] = float(threes.count(three))/float(records)


print five_freqs
print three_freqs 

freq_df = pd.DataFrame()

for seq in set(five_freqs.keys() + three_freqs.keys()): 
	if seq in five_freqs.keys(): 
		freq_df.set_value(seq, 'five_freq', five_freqs[seq])
	else: 
		freq_df.set_value(seq, 'five_freq', 0.0)
	if seq in three_freqs.keys(): 
		freq_df.set_value(seq, 'three_freq', three_freqs[seq])
	else: 
		freq_df.set_value(seq, 'three_freq', 0.0)


freq_df['five_over_three'] = freq_df['five_freq'] / freq_df['three_freq']
freq_df.sort('five_over_three')


plt.figure()
# plt.hist([five_freqs.values(), three_freqs.values()], five_freqs.keys())
# plt.show()

N = len(freq_df.index)

ind = np.arange(N)  # the x locations for the groups
width = 0.25     # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, freq_df['five_freq'], width, color='r')
rects2 = ax.bar(ind + width, freq_df['three_freq'], width, color='y')

# add some text for labels, title and axes ticks
ax.set_ylabel('Frequency Per Record')
ax.set_title('Occurence of Internal Seqs on 5 and 3 Of Helix')
ax.set_xticks(ind + width )
ax.set_xticklabels(freq_df.index, rotation='vertical')

ax.legend((rects1[0], rects2[0]), ('Five', 'Three'))


# def autolabel(rects):
#     # attach some text labels
#     for rect in rects:
#         height = rect.get_height()
#         ax.text(rect.get_x() + rect.get_width()/2., 1.05*height,
#                 '%d' % int(height),
#                 ha='center', va='bottom')

# autolabel(rects1)
# autolabel(rects2)

plt.show()




# panel_dict = {}
# for phylum, files in file_dict.iteritems(): 
# 	panel_dict[phylum] = {}
# 	for file in files: 
# 		species = file.split('/')[-1].replace('.alps','')
# 		df =  build_dataframe_from_alps(file)
# 		if isinstance(df, pd.DataFrame): 
# 			panel_dict[phylum][species] = df 

# for phylum, panels_by_species in panel_dict.iteritems(): 
# 	print phylum + " " + str(len(panels_by_species))


#### START STATISTICAL ANALYSIS 


# grouped = df.groupby(['helix'])

# stats = pd.DataFrame(index=grouped.groups.keys())
# seqs = {
# 	'hairpins': [],
# 	'internals': [] ,
# 	'bulges': [] 
# }

# for helix,items in grouped:
# 	# print items
# 	stats.set_value(helix, 'primary', items['primary'].tolist()[0])
# 	try: 
# 		helices = items.groupby('type').get_group('HELIX') 
# 		stats.set_value(helix, 'paired_bases', helices['seq_length'].sum())
# 		try: 
# 			internals = items.groupby('type').get_group('INTERNAL') 
# 			stats.set_value(helix, 'internal_bases', internals['seq_length'].sum())
# 			seqs['internals'].append(internals['seq'].tolist())
# 			# stats.set_value(helix, 'internal_seqs', internals['seq'].tolist())
# 		except: 
# 			pass
# 		try: 
# 			bulges = items.groupby('type').get_group('BULGE')
# 			stats.set_value(helix, 'bulge_bases', bulges['seq_length'].sum())
# 			seqs['bulges'].append(bulges['seq'].tolist())
# 			# stats.set_value(helix, 'bulge_seqs', bulges['seq'].tolist())
# 		except: 
# 			pass 
# 		try: 
# 			hairpins = items.groupby('type').get_group('HAIRPIN') 
# 			stats.set_value(helix, 'hairpin_bases', hairpins['seq_length'].sum())
# 			seqs['hairpins'].append(hairpins['seq'].tolist())
# 			# print bulges
# 		except: 
# 			pass 
# 	except: 
# 		pass

# def is_primary(r): 
# 	if r['primary'] == 'PRIMARY': 
# 		return 0.855
# 	else: 
# 		return 0.1232

# # stats['primary'] = stats.apply(is_primary, axis=1)
# stats = stats.fillna(value=0)
# stats['unpaired_bases'] = stats.apply(lambda x: np.nansum(x['internal_bases'] + x['bulge_bases']), axis=1)
# stats['hairpin_over_paired'] = stats['hairpin_bases']/stats['paired_bases']
# stats['paired_percent'] = stats['paired_bases']/(stats['unpaired_bases']+stats['paired_bases'])
# stats['internal_and_bulge'] = stats.apply(lambda r: True if np.isfinite(r['bulge_bases']) & np.isfinite(r['internal_bases']) else False, axis=1)

# print stats
# primaries = stats[stats['primary']=='PRIMARY']
# primaries = primaries.sort('hairpin_bases')
# ind = np.arange(len(primaries.index))


# primaries_by_hairpin_length = primaries.groupby('hairpin_bases')
# upmean = primaries_by_hairpin_length['unpaired_bases'].mean().tolist()
# pmean = primaries_by_hairpin_length['paired_bases'].mean().tolist()
# upstd = primaries_by_hairpin_length['unpaired_bases'].std().tolist()
# pstd = primaries_by_hairpin_length['paired_bases'].std().tolist()
# print primaries_by_hairpin_length.indices

# # plt.figure()
# # plt.bar(primaries_by_hairpin_length.indices.keys(), pmean, color='g', yerr=pstd)
# # plt.bar(primaries_by_hairpin_length.indices.keys(), upmean, color='r', yerr=upstd)
# # plt.show()


# plt.figure()
# plt.scatter(primaries['hairpin_bases'], primaries['paired_bases'])
# plt.title('Hairpin Length vs. Paired Bases in Primary Helices')
# plt.ylabel('Paired Bases')
# plt.xlabel('Hairpin Bases')
# plt.show()




# plt.figure()
# plt.scatter(primaries['hairpin_bases'], primaries['paired_bases'], color='g')
# plt.scatter(primaries['hairpin_bases'], primaries['unpaired_bases'], color='r')
# plt.show()



# plt.figure()
# plt.title('Paired vs. Unpaired Bases in Primary Helices')
# plt.ylabel('Unpaired Bases')
# plt.xlabel('Paired Bases')
# plt.scatter(primaries['paired_bases'], primaries['unpaired_bases'], color='g')
# m, b = np.polyfit(primaries['paired_bases'], primaries['unpaired_bases'], 1)
# print 1/m 
# print b 
# # from scipy.stats import linregress
# # print linregress(primaries['paired_bases'], primaries['unpaired_bases']) #x and y are arrays or lists.
# # plt.plot(primaries['paired_bases'], primaries['unpaired_bases'], '.')
# plt.plot(primaries['paired_bases'], primaries['paired_bases']*m + b, '-')
# plt.show()



# plt.figure()
# plt.boxplot(primaries['paired_percent'])
# plt.title('Percent Bases Paired in Primary Helices')
# plt.scatter(np.repeat(1, len(primaries['paired_percent'])), primaries['paired_percent'])
# plt.show()



# from collections import Counter
# a = df[df['type']=='HAIRPIN']['seq']
# letter_counts = Counter(a)
# print letter_counts
# plt.figure()
# plt.bar(letter_counts.keys(), letter_counts.values())
# plt.show()



