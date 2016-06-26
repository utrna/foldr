#!/usr/bin/env rna_heat_no_gui

// The purpose of this test is to show that it is possible to run
// other programs through the bound "rheat" object.

rheat.setTemporaryPreference("RunRootDir", rheat.makePath(rheat.getWorkingDir(), 'tmp'))
var exitStatus = rheat.runProgram('test.py', 'hello', 'world')
if (exitStatus != 0) {
    throw "TEST FAILED: exit status of " + exitStatus
}
