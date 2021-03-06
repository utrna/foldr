#!/usr/bin/env rna_heat_no_gui

// The purpose of this test is to try the interface for loading
// helix color files, and the interface for inspecting the tag
// properties of a helix.

var i = 0

rheat.openRNA('test.bpseq')
rheat.setBasePairs('CG', 'AU', 'GU')
rheat.openTags('test.helixcolor')

// look at predicted helices (would appear bottom-left of diagonal)
helices = rheat.eachPredictedHelix()
rheat.log(0, "length " + helices.length())
if (helices.length() != 1711) {
    throw "TEST FAILED: wrong number of helices"
}
test_indices = [ 425, 520, 993 ] // subset of total matching helices to use for testing
test_tags    = [   2,   1,   3 ] // one expected tag value at selected location (there can be others)
test_nontags = [ 'A',   3,   1 ] // tag value NOT expected at selected location
var current_test = 0
for (i = 0; i < helices.length(); ++i) {
    helix = helices.next()
    // since there are many values, check only those chosen above
    if (test_indices[current_test] == i) {
        tags = helix.eachTag()
        for (var j = 0; j < tags.length(); ++j) {
            this_tag = tags.next()
            rheat.log(rheat.INFO, "predicted helix #" + i + ": tag=" + this_tag)
        }
        if (!helix.hasTag(test_tags[current_test])) {
            throw "TEST FAILED: helix tag '" + test_tags[current_test] + "' not found when it should be found"
        }
        if (helix.hasTag(test_nontags[current_test])) {
            throw "TEST FAILED: helix tag '" + test_nontags[current_test] + "' found when it should NOT be found"
        }
        ++current_test
    }
}

// perform basic test of removal API
helices = rheat.eachPredictedHelix()
while (true) {
    helix = helices.next()
    if (helix == null) {
        break
    }
    if (helix.hasTag('1')) {
        helix.removeTag('1')
        if (helix.hasTag('1')) {
            throw "TEST FAILED: helix tag found when it should have been removed"
        }
    }
}
