
#System Imports
import json
import static
import sys
import os
import time
import random
import pandas as pd 
import psycopg2


print '' 
#test connection to foldr postgres db 
print 'Testing PostgreSQL Connection...'
try:
    conn = psycopg2.connect("dbname='d1be1knu2m4p03' user='vmekkobwaemmtz' host='ec2-54-227-250-148.compute-1.amazonaws.com' password='lmlbGN0CDaUB6ChGv7bBdBFjFt'")
except:
    print "I am unable to connect to the foldr database"
else: 
		print 'Connected'

print '' 


def query(sql): 
	cursor = conn.cursor()
	cursor.execute(sql)
	return cursor.fetchall()


# GATHER ACCESSIONS TO QUERY 
sql_query = '''
SELECT DISTINCT(a.accession)
FROM aldens a
WHERE a.type = 'BULGE'; 
'''
accessions = query(sql_query)
print 'Found {} Accessions To Query'.format(len(accessions))
# print accessions 

fives_total = []
threes_total = []

print "Testing Accessions For Bulge 5'/3' Distribution:"
for accession in accessions: 
	accession = accession[0]
	sys.stdout.write('Examining {}'.format(accession))
	# 5' or 3' Distribution of Elements 
	sql_query = '''
	SELECT *
	FROM aldens a
	WHERE a.type = 'BULGE'
	AND a.accession = '{}'; 
	'''.format(accession)
	bulges = query(sql_query)

	fives = 0
	threes = 0 
	if len(bulges)==0: 
		break
	else: 
		for row in bulges: 
			sql_query = '''
				SELECT *
				FROM aldens a
				WHERE a.type = 'HELIX'
				AND a.unique = {}
				AND a.accession = '{}'
				ORDER BY a.index; 
				'''.format(row[5], accession)
			cursor = conn.cursor()
			cursor.execute(sql_query)
			# print row
			helices = cursor.fetchall()
			if len(helices)==0: break
			# print 'helices: {}'.format(helices)
			# print '' 
			if helices[0][0] == row[0]-1: 
				# print '5'
				fives+=1
			elif helices[1][0] == row[0]-1: 
				# print '3'
				threes+=1
			else: 
				# print 'confused'
				pass
			sys.stdout.write('.')
			sys.stdout.flush()
		fives_total.append(fives)
		threes_total.append(threes)
		print ''
		print "Bulges on 5': {}  3': {}   5/3: {}".format(fives, threes, float(float(fives)/float(threes)))
		print fives_total
		print threes_total






# HAIRPIN LENGTH DISTRIBUTION 
sql_query = '''
SELECT char_length(a.seq), count(char_length(a.seq))
FROM aldens a
WHERE a.type = 'HAIRPIN'
GROUP BY char_length(a.seq)
ORDER BY char_length(a.seq)
'''

cursor = conn.cursor()
cursor.execute(sql_query)
result = cursor.fetchall()

print 'HAIRPIN LENGTH DISTRIBUTION:'
for row in result: 
	print row
print ''




# HELIX LENGTH DISTRIBUTION 
sql_query = '''
SELECT a.seq_length, count(a.seq_length)
FROM aldens a
WHERE a.type = 'HELIX'
GROUP BY a.seq_length
ORDER BY a.seq_length
'''

cursor = conn.cursor()
cursor.execute(sql_query)
result = cursor.fetchall()

print 'HELIX LENGTH DISTRIBUTION:'
for row in result: 
	print row
print ''






#BULGE SEQUENCE DISTRIBUTION 
sql_query = '''
SELECT
  a.seq,
  COUNT (*)
FROM
  aldens a
WHERE
  a.type = 'BULGE'
GROUP BY
  a.seq
ORDER BY
  COUNT (*) DESC
LIMIT 20
'''


cursor.execute(sql_query)
result = cursor.fetchall()

print 'BULGE SEQUENCE DISTRIBUTION:'
for row in result: 
	print row









