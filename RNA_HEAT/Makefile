# Build script for rheat
#
# Kevin Grant (kmgrant@utexas.edu)

SHELL := /bin/bash

CLASSPATH := .

CLASS_PREFIXES := 
CLASS_PREFIXES += rheat/base/AppMain
CLASS_PREFIXES += rheat/base/BasepairType
CLASS_PREFIXES += rheat/base/BPSeqReader
CLASS_PREFIXES += rheat/base/Helix
CLASS_PREFIXES += rheat/base/HelixColorReader
CLASS_PREFIXES += rheat/base/HelixGrid
CLASS_PREFIXES += rheat/base/HelixInfo
CLASS_PREFIXES += rheat/base/HelixStore
CLASS_PREFIXES += rheat/base/RNA
CLASS_PREFIXES += rheat/base/SortedPair
CLASS_PREFIXES += rheat/filter/AAandAGHelicesFilter
CLASS_PREFIXES += rheat/filter/BPFilter
CLASS_PREFIXES += rheat/filter/ComplexFilter
CLASS_PREFIXES += rheat/filter/DiagonalDistanceFilter
CLASS_PREFIXES += rheat/filter/ELoopHelicesFilter
CLASS_PREFIXES += rheat/filter/EnergyMaxMinFilter
CLASS_PREFIXES += rheat/filter/Filter
CLASS_PREFIXES += rheat/filter/MaxMinFilter
CLASS_PREFIXES += rheat/GUI/AAandAGFilterDialog
CLASS_PREFIXES += rheat/GUI/AboutFrame
CLASS_PREFIXES += rheat/GUI/BasepairFilterDialog
CLASS_PREFIXES += rheat/GUI/ColorEditor
CLASS_PREFIXES += rheat/GUI/ComplexFilterDialog
CLASS_PREFIXES += rheat/GUI/DiagonalFilterDialog
CLASS_PREFIXES += rheat/GUI/ELoopFilterDialog
CLASS_PREFIXES += rheat/GUI/EnergyFilterDialog
CLASS_PREFIXES += rheat/GUI/FilterDialog
CLASS_PREFIXES += rheat/GUI/FocusingField
CLASS_PREFIXES += rheat/GUI/HelixFilterDialog
CLASS_PREFIXES += rheat/GUI/HelixImageGenerator
CLASS_PREFIXES += rheat/GUI/HelpFrame
CLASS_PREFIXES += rheat/GUI/ImageFileFrame
CLASS_PREFIXES += rheat/GUI/LogFrame
CLASS_PREFIXES += rheat/GUI/MiniFrame
CLASS_PREFIXES += rheat/GUI/PreferenceDialog
CLASS_PREFIXES += rheat/GUI/RheatApp
CLASS_PREFIXES += rheat/GUI/RNADisplay
CLASS_PREFIXES += rheat/GUI/ScriptEntryFrame
CLASS_PREFIXES += rheat/GUI/TextFileFrame
CLASS_PREFIXES += rheat/script/ConstraintInterpreter
CLASS_PREFIXES += rheat/script/HelixStoreIterationDelegate
CLASS_PREFIXES += rheat/script/IterationDelegate
CLASS_PREFIXES += rheat/script/JSUtil
CLASS_PREFIXES += rheat/script/ScriptHelix
CLASS_PREFIXES += rheat/script/ScriptIteration
CLASS_PREFIXES += rheat/script/ScriptMain
CLASS_PREFIXES += rheat/script/StringSetIterationDelegate

HELP_FILES := 
HELP_FILES += $(foreach X,$(shell find help -name '*.html'),$(X))
HELP_FILES += $(foreach X,$(shell find help -name '*.css'),$(X))

# first rule is built by default when "make" runs
all: rheat.jar

# build an archive to make the program easy to move as a unit
rheat.jar: $(foreach PREFIX,$(CLASS_PREFIXES),$(PREFIX).class)  $(HELP_FILES)
	@# since additional class files may be generated for
	@# inner classes and they have dollar signs, a search
	@# is performed and the results are quoted
	jar cmvf META-INF/MANIFEST.MF $@ \
		$(HELP_FILES) \
		$(foreach X,$(shell find rheat -name '*.class'),'$(X)')

# build source code
%.class: %.java
	javac -cp $(CLASSPATH) -encoding UTF-8 -Xlint:deprecation -Xlint:unchecked $^

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
	$(CURDIR)/rna_heat $(RUN_ARGS)

# alias for running test cases from "test/",
# except that it also triggers an implicit build
check: all
	$(MAKE) -C test/ check

# remove files that can be rebuilt
clean:
	rm -f rheat.jar
	@# remove classes
	find rheat -name '*.class' -exec rm -f {} +
	@# remove temporary files (marked as tmp-*)
	rm -f tmp-*
	@# remove documentation files
	rm -Rf javadoc/
	@# remove temporary test results
	$(MAKE) -C test/ clean

# tell "make" not to treat these as file names:
.PHONY: all check clean doc run viewdoc
