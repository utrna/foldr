
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







