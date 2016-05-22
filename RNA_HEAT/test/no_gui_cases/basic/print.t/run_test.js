#!/usr/bin/env rna_heat_no_gui

// The purpose of this test is simply to show that the interface
// between the Java code and the script is working.  If the
// printed statement is executed, the test has passed.

println("(This print-out is a test of the script integration.  If you can see this, it works.)")

rheat.log(rheat.INFO, "Sample information message.")
rheat.log(rheat.WARN, "Sample warning message.")
rheat.log(rheat.ERROR, "Sample error message.")
