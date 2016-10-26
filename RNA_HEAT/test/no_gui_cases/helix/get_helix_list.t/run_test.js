#!/usr/bin/env rna_heat_no_gui

// The purpose of this test is to try the interface for reading
// helix lists from an RNA.

function println (s) {
    java.lang.System.out.println(s)
}

var i = 0

rheat.openRNA('test.bpseq')

// look at predicted helices (would appear bottom-left of diagonal)
rheat.setBasePairs('AA', 'AG', 'CG')
helices = rheat.eachPredictedHelix()
println("number of predicted helices, after filtering AA/AG/CG: " + helices.length())
if (helices.length() != 1790) {
    throw "TEST FAILED: wrong number of helices after applying AA/AG/CG"
}
rheat.setBasePairs('CC', 'UU')
helices = rheat.eachPredictedHelix()
println("number of predicted helices, after filtering CC/UU: " + helices.length())
if (helices.length() != 684) {
    throw "TEST FAILED: wrong number of helices after applying CC/UU"
}
rheat.setBasePairs('AU')
helices = rheat.eachPredictedHelix()
println("number of predicted helices, after filtering AU: " + helices.length())
if (helices.length() != 428) {
    throw "TEST FAILED: wrong number of helices after applying CC/UU"
}
rheat.addEnergyFilter(-1, -2)
helices = rheat.eachPredictedHelix()
var numWithEnergy = 0
for (i = 0; i < helices.length(); ++i) {
    helix = helices.next()
    if (helix.hasTag('_MATCH_ENERGY_')) {
        ++numWithEnergy
    }
}
println("number of predicted helices, after filtering energy: " + numWithEnergy)
var expNumWithEnergy = 12
if (numWithEnergy != expNumWithEnergy) {
    throw "TEST FAILED: wrong number of helices after applying energy filter (exp. " + expNumWithEnergy + " but saw " + numWithEnergy + ")"
}
test_indices =  [   2,    3,   11] // subset of total matching helices to use for testing
test_energies = [-1.3, -1.3, -1.1] // correct energy values at selected locations
test_lengths  = [   2,    2,    2] // correct length values at selected locations
var current_test = 0
var energy_index = 0
helices = rheat.eachPredictedHelix()
for (i = 0; i < helices.length(); ++i) {
    helix = helices.next()
    if (!helix.hasTag('_MATCH_ENERGY_')) {
        continue
    }
    // since there are many values, check only those chosen above
    if (test_indices[current_test] == energy_index) {
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
    ++energy_index
}

// look at actual helices (would appear top-right of diagonal)
expected_lengths =   [ 4,  3,  6,  2,  8,   7,  10]
expected_5p_starts = [31, 28, 18, 16, 79,  70,   1]
expected_5p_ends   = [34, 30, 23, 17, 86,  76,  10]
expected_3p_starts = [48, 54, 60, 67, 90, 100, 110]
expected_3p_ends   = [51, 56, 65, 68, 97, 106, 119]
helices = rheat.eachActualHelix()
println("number of actual helices: " + helices.length())
if (helices.length() != expected_lengths.length) {
    throw "TEST FAILED: wrong number of helices returned"
}
// alternate approach: while-loop with no length() check
var i = 0
errors = 0
while (true) {
    helix = helices.next()
    if (helix == null) {
        break
    }
    rheat.log(rheat.INFO, "actual helix #" + i + ": len=" + helix.length())
    if (expected_lengths[i] != helix.length()) {
        rheat.log(rheat.ERROR, "wrong helix length returned (#" + i + " exp. " + expected_lengths[i] + " but saw " + helix.length() + ")")
        ++errors
    }
    if (expected_5p_starts[i] != helix.fivePrimeStart()) {
        rheat.log(rheat.ERROR, "wrong helix 5' start returned (#" + i + " exp. " + expected_5p_starts[i] + " but saw " + helix.fivePrimeStart() + ")")
        ++errors
    }
    if (expected_5p_ends[i] != helix.fivePrimeEnd()) {
        rheat.log(rheat.ERROR, "wrong helix 5' end returned (#" + i + " exp. " + expected_5p_ends[i] + " but saw " + helix.fivePrimeEnd() + ")")
        ++errors
    }
    if (expected_3p_starts[i] != helix.threePrimeStart()) {
        rheat.log(rheat.ERROR, "wrong helix 3' start returned (#" + i + " exp. " + expected_3p_starts[i] + " but saw " + helix.threePrimeStart() + ")")
        ++errors
    }
    if (expected_3p_ends[i] != helix.threePrimeEnd()) {
        rheat.log(rheat.ERROR, "wrong helix 3' end returned (#" + i + " exp. " + expected_3p_ends[i] + " but saw " + helix.threePrimeEnd() + ")")
        ++errors
    }
    ++i
}
if (errors > 0) {
    throw "TEST FAILED: one or more errors (see above)"
}
