import csv
import sys
from operator import attrgetter
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("seqFile", help="File containing input sequence", default=None)
parser.add_argument("energyFile", help="File containing all possible helices and energies", default=None)
parser.add_argument("-gf", help="Include this flag to create files for 2d matrix to graph", action="store_true")
args = parser.parse_args()


# import logging
# import cProfile
# import re




class Helix:
	def __init__(self, **kwargs):
		helix = kwargs.get("newHelix")
		if not isinstance(helix, dict):
			raise TypeError("Expecting newHelix to be a dictionary")
		self.begin5   = int(helix["5_Begin"])
		self.end5     = int(helix["5_End"])
		self.begin3   = int(helix["3_Begin"])
		self.end3     = int(helix["3_End"])
		self.length   = int(helix["Length"])
		self.energy   = float(helix["Energy"])
		self.comp     = int(helix["Comparative"])
		self.isCompHp = int(helix["IsComparativeHairpin"])
		self.label    = helix["Label"]
		self.pHelix   = helix["Parent_Helix"]

		self.simpDist  = int(helix["LoopSize"])
		self.condDist  = self.simpDist
		self.modEnergy = self.energy/self.condDist

	def __str__(self):
		return str(self.begin5) + "," + str(self.end5) + "," + str(self.begin3) + "," + str(self.end3) + "," + str(self.simpDist) + "," + str(self.condDist) + "," + str(self.energy) + "," + str(self.modEnergy) + "," + str(self.comp)


class FoldingStrand:
	def __init__(self, sequence, allHelices, windowSize):
		self.sequence      = sequence
		self.allHelices	   = allHelices
		self.windowSize	   = windowSize
		self.totalLength   = len(sequence)
		self.currentLength = 0
		self.lowerRange	   = 1	# 1-indexed
		self.upperRange	   = 1	# 1-indexed

		self.consideredHelices   = [] # helices considered in each iteration
		self.transientHelices    = [] # helices "formed" in each iteration (tuple of (helix,hairpin))
		self.farTransientHelices = [] # transient helices that are "outside" of the window
		self.strand			     = [] # represents actual RNA strand

	# update upper/lower ranges / note: index starts at 1, NOT 0
	def __updateRange(self):
		self.upperRange = self.strand[-1].position # i tihnk this will always be true

		# if (self.upperRange - self.lowerRange >= self.windowSize):
		# 	self.lowerRange = self.upperRange - self.windowSize + 1 # window is inclusive of both ends
		"""
		reverse strand
		iterate up to window size and return the index found
		when hairpin is found, jump to other end of it and do not count the loop
			hairpin ends are still counted

		probably some of the worst code i have ever written
		"""
		reversedStrand = self.strand[::-1]
		# for h in reversedStrand: # debug strand
		# 	print h
		indicesLeft = self.windowSize
		baseIndexer = 0
		curBase = None
		nextBase = reversedStrand[0]

		for x in xrange(0, self.windowSize): #check off-by-one TypeError
			curBase = nextBase
			if curBase.position == 1:
				break
			if isinstance(curBase, HairpinLoop):
				if curBase.isHairpinEnd == False:
					baseIndexer += 1
					nextBase = reversedStrand[baseIndexer]
					continue
				baseIndexer += curBase.totalLength + 1 #len does not include base pair ends, list is also indexed at 0
				# print "hp: " + str(curBase) + " len: " + str(curBase.length) # debug
				# print "baseIndexer: " + str(baseIndexer) + " x:" + str(x) # debug
				nextBase = reversedStrand[baseIndexer]
			else:
				baseIndexer += 1
				try:
					nextBase = reversedStrand[baseIndexer]
				except IndexError:
					break

		self.lowerRange = curBase.position
		# self.lowerRange = 1

	# checks if sequence is done transcribing
	def isDone(self):
		return self.currentLength == self.totalLength

	# add next base in sequence (transcription)
	def addNextBase(self):
		if self.currentLength == self.totalLength:	
			raise Exception("End of sequence has been reached.")

		# increase length
		self.currentLength += 1

		# add next base to strand / (perhaps we want to form entire strand on load?)
		base = Base(self.currentLength, self.sequence[self.currentLength-1])
		self.strand.append(base) # ??

		# update upper/lower window ranges
		self.__updateRange()


	# do some speed testing later (list comp vs iterating)
	# returns all helices that can form in current window
	# NOTE: should probably be using CONDITIONAL DISTANCE..
		# initial naive/unfounded thought: h.end3 - h.condDist
	def findPossibleHelices(self):
		# newHelices = [h for h in allHelices if h.begin5 >= self.lowerRange and h.end3 <= self.upperRange]
		newHelices = []
		for h in self.allHelices:
			isTransient = False
			if (h.begin5 >= self.lowerRange) and (h.end3 <= self.upperRange):
				# do not add helix if it's already in transient list
				for th in self.transientHelices:
					if h is th[0]:
						isTransient = True
						break
				if not isTransient:
					newHelices.append(h)
		self.consideredHelices = newHelices


	"""
	== calc. conditional distance ==
	condDist = 0
	1. find distance between 5'end of curHelix and start of nearest transient helix (add to condDist)
	2. jump to 3'end of transient helix (do not add anything to condDist)
	3. continue counting/moving towards upperRange (add 1 to condDist per base)
		-repeat step 2 if any transient helices are encountered
	4. upon reaching 3'start, store condDist in curHelix. done.
	"""
	# calculate new conditional distances
	def calcNewCondDistEnergy(self):
		# print "Before condDist recalc"
		# for h in self.transientHelices:
		# 	print "\tin calccondist, curTranHelix " + str(h[0])

		for h in self.consideredHelices:
			# print "Before " + str(h)
			self.__calcCondDist(h)
			h.modEnergy = h.energy / h.condDist
			# print "After  " + str(h)

		# print "After condDist recalc"
		# for h in self.transientHelices:
		# 	print "\tin calccondist, curTranHelix " + str(h[0])

	def __calcCondDist(self, helix):
		# print helix
		newCondDist = 0
		# index = 0
		index = helix.end5 - 1 #strand indexed at 0
		while index < helix.begin3 - 1:
			# print index
			base = self.strand[index]
			# if base is a hairpin begin...
			if isinstance(base, HairpinLoop):
				if not (base.isHairpinEnd):
					# print "\thairpin begin found at " + str(index) + " cd: " + str(newCondDist)
					# jump to end
					"""
					this is wrong! figure it out....
					"""
					index += base.totalLength + 1
					# print "\t   -new index: " + str(index) + " cd: " + str(newCondDist)
					# newCondDist += 1
				else:
					# print "\thairpin end found (regularcase): " + str(index) + " cd: " + str(newCondDist)
					index += 1
				newCondDist += 1
			else:
				# print "\tregular base found: " + str(index) + " cd: " + str(newCondDist)
				index += 1
				newCondDist += 1
		helix.condDist = newCondDist
		# print "\thelix w/ conddist calc: " + str(helix)



	# add most stable helices to strand (form hairpins here)
	def formHelices(self):
		# self.printStrand(index = True)	# debug

		self.__updateTransientRange()

		# self.printTransients()

		# remove hairpins from strand
		for h in self.transientHelices:
			h[1].removeFromStrand()
			# print(h)


		# clear transientHelices list
		self.transientHelices[:] = []


		"""
		DANGER! This is wrong. Conditional distances should be calculated with formed structures.
		If the transientHelices list is cleared, then the new conditional distances are just simple distances...
		This seems wrong. The transient structures should exist while competing with other possible helices.
		So don't clear the list. Must figure something out for this.
		Finding possible, calc'ing new condDist, and sorting them just doesn't make sense when no transient structures are present.
		"""

		# find possible helices
		self.findPossibleHelices()
		# calc new conditional distances
		self.calcNewCondDistEnergy()
		# rank helices by energy (more negative = higher)
		self.__sortConsideredHelices()

		# print "Considered helices"
		# for h in self.consideredHelices:
		# 	print h

		# find legal transient helices
		if (len(self.consideredHelices) > 0):
			#automatically add first hairpin
			firstHairpin = self.consideredHelices[0]
			tryHairpin = self.__tryFormHairpin(firstHairpin)
			tryHairpin.addToStrand()
			self.transientHelices.append((firstHairpin, tryHairpin))
			self.calcNewCondDistEnergy()
			self.consideredHelices.sort(key = attrgetter("modEnergy"))

			indexer = 0
			while (indexer < len(self.consideredHelices)):
				h = self.consideredHelices[indexer]
				tryHairpin = self.__tryFormHairpin(h)

				# legal hairpin found.
				if (tryHairpin != None):
					tryHairpin.addToStrand()
					self.transientHelices.append((h, tryHairpin))
					self.calcNewCondDistEnergy()
					self.consideredHelices.sort(key = attrgetter("modEnergy"))

					# reset loop and start from beginning, recalc based on new transient.
					self.__updateRange()
					self.__updateTransientRange()
					self.calcNewCondDistEnergy()
					self.findPossibleHelices()
					indexer = 0
					continue

				indexer += 1

	#don't need to account for upper range, i think. upper range never changes.
	def __updateTransientRange(self):
		newTransientHelices = []
		# find far transients that should move into transient
		for h in self.farTransientHelices:
			if (h[0].begin5 >= self.lowerRange):
				newTransientHelices.append(h)

		# move far transients into transient
		for h in newTransientHelices:
			self.transientHelices.append(h)
			self.farTransientHelices.remove(h)

		newFarHelices = []
		# find transients that should move into far transient
		for h in self.transientHelices:
			if (h[0].begin5 < self.lowerRange):
				newFarHelices.append(h)

		# move transients into far transients
		for h in newFarHelices:
			self.farTransientHelices.append(h)
			self.transientHelices.remove(h)



	# attempts to create a single helix (hairpin). successful if all bases involved are currently not paired.
	def __tryFormHairpin(self, helix):
		# check if helix is legal
		# if transient is empty, automatic success
		# print helix 
		# print "-- cmp to helices below"

		unstableHelices = []

		if len(self.transientHelices) == 0:
			return HairpinLoop(helix, self.strand)
		else:
			# this can be more efficient. create a static list and slice it instead of using range.
			for h in self.transientHelices:
				# check if helix overlaps/conflicts with any current transient helices
				if (   helix.begin5 in range(h[0].begin5, h[0].end5+1) or helix.begin5 in range(h[0].begin3, h[0].end3+1)
					or helix.end5 in range(h[0].begin5, h[0].end5+1) or helix.end5 in range(h[0].begin3, h[0].end3+1)
					or helix.begin3 in range(h[0].begin5, h[0].end5+1) or helix.begin3 in range(h[0].begin3, h[0].end3+1)
					or helix.end3 in range(h[0].begin5, h[0].end5+1) or helix.end3 in range(h[0].begin3, h[0].end3+1)
				   ):

					# check if possible hairpin is more stable than current hairpin
					# doesn't really work as intended.

					# if (h[0].modEnergy > helix.modEnergy):
					# # 	# check if pseudoknot will form
					# 	if (    helix.begin5 in range(h[0].end5+1, h[0].begin3) and helix.begin3 not in range(h[0].end5+1, h[0].begin3)
					# 		 or helix.begin3 in range(h[0].end5+1, h[0].begin3) and helix.begin5 not in range(h[0].end5+1, h[0].begin3)
					# 	   ):							
					# 		conflictsExist = True
					# 		#continue removing until the new helix can form
					# 		while (conflictsExist):
					# 			print "removing"
					# 			conflictsExist = self.__removeConflictingTransientHairpins(helix)
					# 		return HairpinLoop(helix, self.strand)


					# print "cannot form " + str(helix)
					return None # conflict found; helix cannot form
				# check if pseudoknot will form
				if (    helix.begin5 in range(h[0].end5+1, h[0].begin3) and helix.begin3 not in range(h[0].end5+1, h[0].begin3)
					 or helix.begin3 in range(h[0].end5+1, h[0].begin3) and helix.begin5 not in range(h[0].end5+1, h[0].begin3)
				   ):

					return None # pseudoknot found; helix cannot form

		# print "can form"	
		# success! helix can form.
		return HairpinLoop(helix, self.strand)

	def __removeConflictingTransientHairpins(self, helix):
		for h in self.transientHelices:
			# check if helix overlaps/conflicts with any current transient helices
			if (   helix.begin5 in range(h[0].begin5, h[0].end5+1) or helix.begin5 in range(h[0].begin3, h[0].end3+1)
				or helix.end5 in range(h[0].begin5, h[0].end5+1) or helix.end5 in range(h[0].begin3, h[0].end3+1)
				or helix.begin3 in range(h[0].begin5, h[0].end5+1) or helix.begin3 in range(h[0].begin3, h[0].end3+1)
				or helix.end3 in range(h[0].begin5, h[0].end5+1) or helix.end3 in range(h[0].begin3, h[0].end3+1)
			   ):
				self.transientHelices.remove(h)
				return False
		return True


	# Sorts considered helices by energy
	def __sortConsideredHelices(self):
		self.consideredHelices.sort(key = attrgetter("modEnergy"))

	# eventually use an output stream
	def printStrand(self, **kwargs):
		s = ""
		if (kwargs.get("index") == True):
			for h in xrange(1, self.currentLength + 1):
				s += str(h % 10)
			s += "\n"
		if (kwargs.get("window")):
			for h in xrange(0, self.currentLength):
				if h == kwargs.get("window"):
					s += "W"
				else:
					s += " "
			s += "\n"
		for h in self.strand:
			if isinstance(h, HairpinLoop):
				if h.isHairpinEnd == False:
					s += "B"
				else:
					s += "E"
			elif isinstance(h, Base):
				s += "-"
			else:
				s += "?"
		# s += "\n"
		print s #return s when logging is implemented

	def printTransients(self, **kwargs):
		s = ""
		s += "Far Transients\n"
		for h in self.farTransientHelices:
			s += str(h[0]) + "\n"

		if len(self.farTransientHelices) == 0:
			s += "  None\n"

		s += "Transients\n"
		for h in self.transientHelices:
			s += str(h[0]) + "\n"

		if len(self.transientHelices) == 0:
			s +=  "  None\n"

		print s


class Base:
	def __init__(self, position, name):
		self.position = position
		self.name     = name
		self.isPaired = False
		self.pairedTo = None

	def __str__(self):
		return self.name + " " + str(self.position)

	# only set vars in self, not other base
	def pairTo(self, otherBase):
		if not isinstance(otherBase, Base):
			raise TypeError("Expecting otherBase to be a Base")
		self.isPaired = True
		self.pairedTo = otherBase

	def unPair(self):
		self.isPaired = False
		self.pairedTo = None


# probably going to have problems dealing with
# hairpins within hairpins
class HairpinLoop(Base):
	def __init__(self, helix, mainStrand, **kwargs):
		if not isinstance(mainStrand, list):
			raise TypeError("Expecting mainStrand to be a list")

		self.position    = -1
		self.beginIndex  = helix.begin5
		self.endIndex    = helix.end3
		self.totalLength = helix.end3 - helix.begin5 - 1	# hairpin loop doesn't include paired bases
		self.bases       = []				# contains bases within hairpin loop
		self.beginBase   = mainStrand[helix.begin5-1]
		self.endBase     = mainStrand[helix.end3-1]
		self.mainStrand  = mainStrand		# main rna strand
		self.hasUnfolded = False
		
		self.hairpinEnd   = None
		self.isHairpinEnd = False

		# form end hairpin (placeholder basically) and place it into mainStrand
		if not ("createHairpinEnd" in kwargs):
			self.hairpinEnd = HairpinLoop(helix, mainStrand, createHairpinEnd = True)
			self.position = helix.begin5

			# NOTE: this idea is invalid in new implementation. revise later.
			# first, place hairpinloopbegin at relevant index in mainStrand list
				# hmm.. do this in caller?
			# then move all bases involved from mainStrand list to internal list (bases)
			# last, place hairpinEnd right after hairpinloopbegin

		# else is used by the hairpinEnd instance, created above by
		else:
			self.position = helix.end3
			self.isHairpinEnd = True

	# add itself to strand, replacing corresponding base
	def addToStrand(self):
		self.mainStrand[self.beginIndex-1] = self
		self.mainStrand[self.endIndex-1]   = self.hairpinEnd

	# remove self from strand, replacing self w/ corresponding base
	def removeFromStrand(self):
		self.mainStrand[self.beginIndex-1] = self.beginBase
		self.mainStrand[self.endIndex-1]   = self.endBase

	# unlikely to work because shifting indexes will mess up other hairpins after the first. 
	def fold(self):
		pass


	# use this when removing hairpin loop. puts helices back into mainStrand list
	# this must be called before removing this object from any active list
	def unfold(self):
		self.hasUnfolded = True
		# place bases back into strand..?

	def __str__(self):
		return str(self.beginIndex) + "," + str(self.endIndex)

	# this attempts to check and make sure hairpins are unfolded before gc'd.
	# however not flawless. once folding is complete, it will still cause problems
	# unless hasUnfolded is set to True. So when folding ends, just set all hairpin objects
	# hasFolded to True.
	def __del__(self):
		if self.hasUnfolded == False:
			# print("Hairpin has been garbage collected without unfolding. Allowed if it occurs after folding completes.")
			# raise Exception("Hairpin has been removed without unfolding. Ignore this occurs at end of folding")
			pass



class WriteFiler():
	def __init__(self, name):
		self.name = name
		self.num  = 0

	# creates data file with helices data for graphing program
	def writeToGraphFile(self, *args):
		if self.num < 10:
			fileNum = "0" + str(self.num)
		else:
			fileNum = str(self.num)
		with open(self.name + fileNum + ".csv","w") as dataFile:
			dataFile.write("UID,5Begin,5End,3Begin,3End,Length,LoopSize,Energy,isComparative,isHairpin,roundAdmitted,condDist,condProb,foldType,influencedFolds\n")
			
			for helixList in args:
				for h in helixList:
					if isinstance(h, tuple):
						dataFile.write("0," + str(h[0].begin5) + "," + str(h[0].end5) + "," + str(h[0].begin3) + "," + str(h[0].end3) + "," + str(h[0].length) + "," + str(h[0].simpDist) + "," + str(h[0].energy) + "," + str(h[0].comp) + ",1,0," + str(h[0].condDist) + ",0,0,:\n")
					else:
						dataFile.write("0," + str(h.begin5) + "," + str(h.end5) + "," + str(h.begin3) + "," + str(h.end3) + "," + str(h.length) + "," + str(h.simpDist) + "," + str(h.energy) + "," + str(h.comp) +",0,0," + str(h.condDist) + ",0,0,:\n")
			self.num += 1

# max window size = 20
def rnaf_fold():
	# import sequence (unecessary as of now...)
	sqStr = ""
	with open (args.seqFile, "r") as inFile:
		sqStr = inFile.read()

	# import energetics
	allPossibleHelices = [] # list of all possible helices
	with open(args.energyFile,"rb") as csvfile:
		read = csv.DictReader(csvfile)
		for h in read:
			helix = Helix(newHelix = h)
			allPossibleHelices.append(helix)
	

	writer = WriteFiler("rnaGraphData")

	# folding
	rna = FoldingStrand(sequence = sqStr, allHelices = allPossibleHelices, windowSize = 20)
	while(rna.isDone() != True):
		# print "======\nRange: [" + str(rna.lowerRange) + " " + str(rna.upperRange) + "]"
		rna.addNextBase()
		rna.formHelices()

		if (args.gf):
			writer.writeToGraphFile(rna.transientHelices, rna.farTransientHelices, rna.consideredHelices)

	# folding done

	# rna.printStrand(index = True)
	rna.printTransients()

rnaf_fold()

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