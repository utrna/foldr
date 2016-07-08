#!/usr/bin/env rna_heat_no_gui

// The purpose of this test is to try the interface for reading
// helix lists from an RNA.

var i = 0

rheat.openRNA('test.bpseq')

// look at predicted helices (would appear bottom-left of diagonal)
rheat.addBasePairFilter('AA', 'AG', 'CG')
helices = rheat.eachPredictedHelix()
println("number of predicted helices, after filtering AA/AG/CG: " + helices.length())
if (helices.length() != 1790) {
    throw "TEST FAILED: wrong number of helices after applying AA/AG/CG"
}
rheat.addBasePairFilter('CC', 'UU')
helices = rheat.eachPredictedHelix()
println("number of predicted helices, after filtering CC/UU: " + helices.length())
if (helices.length() != 684) {
    throw "TEST FAILED: wrong number of helices after applying CC/UU"
}
rheat.addBasePairFilter('AU')
helices = rheat.eachPredictedHelix()
println("number of predicted helices, after filtering AU: " + helices.length())
if (helices.length() != 428) {
    throw "TEST FAILED: wrong number of helices after applying CC/UU"
}
rheat.addEnergyFilter(-1, -2)
helices = rheat.eachPredictedHelix()
println("number of predicted helices, after filtering energy: " + helices.length())
if (helices.length() != 12) {
    throw "TEST FAILED: wrong number of helices after applying energy filter"
}
test_indices =  [   2,    3,   11] // subset of total matching helices to use for testing
test_energies = [-1.3, -1.3, -1.1] // correct energy values at selected locations
test_lengths  = [   2,    2,    2] // correct length values at selected locations
var current_test = 0
for (i = 0; i < helices.length(); ++i) {
    helix = helices.next()
    // since there are many values, check only those chosen above
    if (test_indices[current_test] == i) {
        rheat.log(rheat.INFO, "predicted helix #" + i + ": energy=" + helix.energy())
        rheat.log(rheat.INFO, "predicted helix #" + i + ": len=" + helix.length())
        if (helix.energy() != test_energies[current_test]) {
            throw "TEST FAILED: wrong helix energy returned"
        }
        if (helix.length() != test_lengths[current_test]) {
            throw "TEST FAILED: wrong helix length returned"
        }
        ++current_test
    }
}

// look at actual helices (would appear top-right of diagonal)
expected_lengths = [4, 3, 6, 2, 8, 7, 10]
helices = rheat.eachActualHelix()
println("number of actual helices: " + helices.length())
if (helices.length() != expected_lengths.length) {
    throw "TEST FAILED: wrong number of helices returned"
}
for (i = 0; i < helices.length(); ++i) {
    helix = helices.next()
    rheat.log(rheat.INFO, "actual helix #" + i + ": len=" + helix.length())
    if (expected_lengths[i] != helix.length()) {
        throw "TEST FAILED: wrong helix length returned (#" + i + " exp. " + expected_lengths[i] + " but saw " + helix.length() + ")"
    }
}
