RNA HEAT: RNA Helix Elimination Acquisition Tool
------------------------------------------------

About
~~~~~

Version 1.0 originally written by:
    Gurushyam Hariharan (guru@ece.utexas.edu)
    James Zhang (jyzhang@mail.utexas.edu)
    Angie Li (aloysia@cs.utexas.edu)

Version 2.0 written by:
    Kevin Grant (kmgrant@utexas.edu)

These instructions written by Kevin Grant (kmgrant@utexas.edu).


Running
~~~~~~~

From a shell, you can use "make run".  This will automatically
build the code if it has not been built already (see below).

If you already have "rheat.jar", you should be able to open it
directly (e.g. from Windows Start menu).  If this does not work,
it should be possible to say "java.exe rheat.jar" from a command
prompt, in the same directory as the rheat.jar file.


Automated Testing
~~~~~~~~~~~~~~~~~

From a shell, you can use "make check".  Or, you can pick any of
the tests under "test/.../..." and run them individually.


Manual Testing
~~~~~~~~~~~~~~

Note that there is test data in the parent of this tree, under
"../data/bpseq/*.bpseq".


Building
~~~~~~~~

This is known to work with Java SE 1.7.0_75 on Mac OS X 10.11.
It has also been tested in the Gutell Lab on Windows and Linux.
(If you get it working with other systems, please update this
list.)

From a shell, run "make" (on Mac/Linux; on Windows, you need the
bash shell or Cygwin to run "make").  This is easier than setting
up an IDE such as NetBeans to build the Java code, though you
could do that too.

Once you have built everything once, future runs of "make" will
only build any code that has locally changed.  You can also run
"make clean" to erase local builds (like ".class" files and the
"rheat.jar") before building everything again.

Use "make doc" to build documentation; and, for convenience, use
"make viewdoc" to open the local HTML in your web browser.  (You
could also navigate to the "javadoc/index.html" file yourself.)


Directory Structure
~~~~~~~~~~~~~~~~~~~

test            <= regression test tree (see test/README.txt)
rheat           <= main project source code files (NOTE: must be
                   kept in this structure to match package names)
    GUI         <= the graphical end for RNA HEAT
    base        <= the backend base for RNA HEAT
    filter      <= many filter definition classes
    script      <= scripting interfaces
javadoc         <= documentation for code (created by "make doc")
Makefile        <= can be used to build ("make", "make doc",
                   "make viewdoc", "make run", "make clean")


Other Top-Level Files
~~~~~~~~~~~~~~~~~~~~~

Issues.txt      <= list of known problems, and resolved issues
ToDo.txt        <= enhancements that have been noted, and those
                   that have been recently implemented
rna_heat        <= on Linux/Mac, can be used to create executable
                   scripts (relied on by regression test tree;
                   see "test/no_gui_cases/*/run_test.js" for some
                   examples)
rna_heat_no_gui <= similar but suppresses GUI by default
.gitignore      <= file names and patterns that are not seen by
                   "git" commands by default (should include any
                   type of generated file but not source code)
