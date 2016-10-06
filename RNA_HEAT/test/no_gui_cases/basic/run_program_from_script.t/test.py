# test program to demonstrate subprocesses
import sys
print " ".join(sys.argv[1:])

# verify that input file was copied
import os.path
assert os.path.exists('test.in') # should be copied into "tmp/<experiment>/" run area (where this script runs from)
