# Test runners for rheat
#
# Note that tests may be run individually; the Makefile is just
# useful for automatically running several things at once.
#
# Since the parent Makefile also has a "make check" that will
# automatically build as needed, it is probably more convenient
# to run "make check" from the parent directory and not from
# this directory.
#
# Kevin Grant (kmgrant@utexas.edu)

SHELL := /bin/bash

export PATH := $(CURDIR)/..:$(PATH)

# first rule is built by default when "make" runs
all: check

# run all tests
check:
	@for T in $(shell find 'no_gui_cases' -name '*.t' -type d) ; do \
		echo "=============================" ; \
		echo "  $$T" ; \
		echo "=============================" ; \
		echo $${T}/run_test.js ; \
		$${T}/run_test.js  && echo "Test PASSED." ; \
	done
	@echo "============================="
	@echo "  Test iterations complete."
	@echo "============================="

# remove temporary test results
clean:
	@# remove temporary files based on name
	find no_gui_cases -name 'tmp*' -exec rm -f {} +
	@# remove generated pref.bin
	find . -name 'pref.bin' -exec rm -f {} +

# tell "make" not to treat these as file names:
.PHONY: all check clean
