#
# Customize build options by host domain
#

DOMAIN := $(shell hostname -f | cut -f 2 -d .)
ifeq ($(DOMAIN),lecs)
	CXXFLAGS := -O3 -DNDEBUG -Wall -Wold-style-cast -Woverloaded-virtual -Wsign-promo -Wno-deprecated 
	#-Weffc++
#	CXXFLAGS := -O3 -DNDEBUG
#	CXXFLAGS := -pg -O3 -DNDEBUG
else
ifeq ($(DOMAIN),cs)
	CXXFLAGS := -O3 -DNDEBUG
else
$(error Unknown host domain '$(DOMAIN)')
endif
endif


OBJS := $(patsubst %.cc,%.o,$(wildcard *.cc))


#
# Primary build rules
#

.PHONY: all
all: $(OBJS)

.PHONY: clean
clean: 
	rm -f *.o

.PHONY: depend
depend:
	makedepend -Y *.cc *.h
# DO NOT DELETE

Histogram.o: util.h exceptions.h Histogram.h Array.h Bag.h
Multitype.o: util.h exceptions.h Multitype.h
ParameterFile.o: ParameterFile.h ParameterSet.h exceptions.h Multitype.h
ParameterFile.o: util.h
ParameterSet.o: ParameterSet.h exceptions.h Multitype.h util.h
PlottableBinaryTree.o: PlottableBinaryTree.h
Random.o: Random.h
util.o: util.h exceptions.h
Array.o: Bag.h
ArrayList.o: Array.h Bag.h
DiscreteStatistics.o: Bag.h
Histogram.o: Array.h Bag.h
ParameterFile.o: ParameterSet.h exceptions.h Multitype.h util.h
ParameterSet.o: exceptions.h Multitype.h util.h
util.o: exceptions.h
