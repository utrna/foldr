# Build script for rheat
#
# Kevin Grant (kmgrant@utexas.edu)

SHELL := /bin/bash

CLASSPATH := .

CLASS_PREFIXES := 
CLASS_PREFIXES += rheat/base/Reader
CLASS_PREFIXES += rheat/filter/FilterInfo
CLASS_PREFIXES += rheat/GUI/AAandAGFilterDialog
CLASS_PREFIXES += rheat/GUI/BasepairFilterDialog
CLASS_PREFIXES += rheat/GUI/BusyWaitDialog
CLASS_PREFIXES += rheat/GUI/ComplexFilterDialog
CLASS_PREFIXES += rheat/GUI/DiagonalFilterDialog
CLASS_PREFIXES += rheat/GUI/ELoopFilterDialog
CLASS_PREFIXES += rheat/GUI/EnergyFilterDialog
CLASS_PREFIXES += rheat/GUI/FilterController
CLASS_PREFIXES += rheat/GUI/HelixFilterDialog
CLASS_PREFIXES += rheat/GUI/HelixImageGenerator
CLASS_PREFIXES += rheat/GUI/HelpAboutJFrame
CLASS_PREFIXES += rheat/GUI/HelpContentJFrame
CLASS_PREFIXES += rheat/GUI/PreferenceDialog
CLASS_PREFIXES += rheat/GUI/RheatApp
CLASS_PREFIXES += rheat/test/AAandAGHelicesFilter
CLASS_PREFIXES += rheat/test/AllHelicesFilter
CLASS_PREFIXES += rheat/test/BasePairRangeHelicesFilter
CLASS_PREFIXES += rheat/test/BasepairType
CLASS_PREFIXES += rheat/test/BPFilter
CLASS_PREFIXES += rheat/test/ComplexFilter
CLASS_PREFIXES += rheat/test/ELoopHelicesFilter
CLASS_PREFIXES += rheat/test/EnergyMaxMinFilter
CLASS_PREFIXES += rheat/test/ExactLengthHelicesFilter
CLASS_PREFIXES += rheat/test/Filter
CLASS_PREFIXES += rheat/test/Helix
CLASS_PREFIXES += rheat/test/HelixArray
CLASS_PREFIXES += rheat/test/HelixFilter
CLASS_PREFIXES += rheat/test/HelixGrid
CLASS_PREFIXES += rheat/test/HelixInfo
CLASS_PREFIXES += rheat/test/HelixStore
CLASS_PREFIXES += rheat/test/MaxMinFilter
CLASS_PREFIXES += rheat/test/NonDiagonalHelicesFilter
CLASS_PREFIXES += rheat/test/RNA
CLASS_PREFIXES += rheat/test/Tester

# first rule is built by default when "make" runs
all: rheat.jar

# build an archive to make the program easy to move as a unit
rheat.jar: $(foreach PREFIX,$(CLASS_PREFIXES),$(PREFIX).class)
	@# since additional class files may be generated for
	@# inner classes and they have dollar signs, a search
	@# is performed and the results are quoted
	jar cmvf META-INF/MANIFEST.MF $@ \
		rheat/GUI/AboutIcon.gif \
		rheat/GUI/Clock.gif \
		$(foreach X,$(shell find rheat -name '*.class'),'$(X)')

# build source code
%.class: %.java
	javac -cp $(CLASSPATH) -Xlint:deprecation -Xlint:unchecked $^

# build documentation
javadoc/index.html: all
	javadoc -d javadoc \
		-sourcepath . -classpath $(CLASSPATH) \
		$(foreach X,$(CLASS_PREFIXES),$(X).java)

# short-cut for documentation
doc: javadoc/index.html

# open web browser to view documentation (create if necessary)
viewdoc: javadoc/index.html
	python -m webbrowser 'file://$(CURDIR)/javadoc/index.html'

# run program
run: all
	java \
		-Dapple.laf.useScreenMenuBar="true" \
		-jar rheat.jar \
		$(RUN_ARGS)

# remove files that can be rebuilt
clean:
	rm -f rheat.jar
	@# remove classes
	find rheat -name '*.class' -exec rm -f {} +
	@# remove temporary files (marked as tmp-*)
	rm -f tmp-*
	@# remove documentation files
	rm -Rf javadoc/

# tell "make" not to treat these as file names:
.PHONY: all clean doc run viewdoc
