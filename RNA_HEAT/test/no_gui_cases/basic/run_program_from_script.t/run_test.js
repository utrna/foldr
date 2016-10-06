#!/usr/bin/env rna_heat_no_gui

// The purpose of this test is to show that it is possible to run
// other programs through the bound "rheat" object.

// override temporarily to keep test results self-contained
rheat.setTemporaryPreference("RunRootDir", rheat.makePath(rheat.getWorkingDir(), 'tmp'))

// test copy function (should make "test.in" visible to program
// in its run directory)
rheat.copyFilesToExperimentDir('test.in')

var exitStatus = rheat.runProgram('test.py', 'hello', 'world')
if (exitStatus != 0) {
    expDir = rheat.getCurrentExperimentDir()
    throw "TEST FAILED: exit status of " + exitStatus + " (see 'log.txt' in '" + expDir + "')"
}
