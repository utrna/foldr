Test Cases
----------

About
~~~~~

This directory contains input files and test programs that show
how RNA HEAT works.  Note that since the scripting interface is
helpful for automation, test cases are typically implemented as
scripts.

These instructions written by Kevin Grant (kmgrant@utexas.edu).


Running
~~~~~~~

From a shell, you can use "make check".  This will automatically
attempt a build in the parent directory and then run all tests
and report results.

The Makefile is only for convenience; you may run any test
individually by invoking a script in a subdirectory of "cases".


New Tests
~~~~~~~~~

To enable automation, test scripts should follow the Unix style
of returning ZERO (0) for SUCCESS and any other value to show a
failure.  When "make check" is used, tests will be reported as
failures whenever they do not return zero.

Please follow a consistent form across test cases so that it is
easy to understand different tests and automate them.  Use the
existing tests as examples.  New tests should probably be made by
copying another test first.


Directory Structure
~~~~~~~~~~~~~~~~~~~

cases           <= each subdirectory is a category; directories
                   with a ".t" suffix should be test cases (by
                   convention, to distinguish them from the
                   directories used to organize test cases)
Makefile        <= can be used to run tests ("make", "make check",
                   "make clean"); may also run tests separately
