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

sql_query = """
SELECT a.helix, sum(a.seq_length)
FROM alps a
WHERE a.type = 'HELIX'
GROUP BY a.helix
ORDER BY a.helix;
"""
helix_bases = query(sql_query)
sql_query = """
SELECT a.helix, sum(a.seq_length)
FROM alps a
GROUP BY a.helix
ORDER BY a.helix;
"""
total_bases = query(sql_query)




print 'Found {} Accessions To Query'.format(len(accessions))
# print accessions 

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
		print ''
		print "Bulges on 5': {}  3': {}   5/3: {}".format(fives, threes, float(float(fives)/float(threes)))




