/*
 * RNA.java
 *
 * Created on February 14, 2003, 3:43 PM
 */

package rheat.base;

import rheat.filter.EnergyMaxMinFilter;

import java.util.*;

/** This is an RNA class, representing an RNA to be studied.  This RNA class
 * contains the sequence of the RNA and some other relevant information.
 * @author Team MatriX
 */

public class RNA implements java.io.Serializable {

    /** Constructor for an RNA object.
     * @param u The unique identifying string for this RNA.
     * @param o The organism that this RNA is from.
     * @param a The accession number for this RNA.
     * @param d Other descriptive string for this RNA.
     * @param s A String for the sequence underlying this RNA.
     */
    public RNA(String u, String o, String a, String d, String s) {
        uid = u;
        organism = o;
        accession = a;
        description = d;
        seq = s;
        final int seqLength = s.length();
        //System.out.println("Size of the Sequence: " + seq.size());
        basePairs = new boolean[seqLength][seqLength];
        actualHelices = new HelixGrid(seqLength);
        predictedHelices = new HelixGrid(seqLength);
        minimumAnnotationCapacity = ((seqLength / 10) * (seqLength / 10)); // arbitrary estimate (can be set later)
        binTag = null;
        binCount = 30; // arbitrary default
        binMinValue = Double.NEGATIVE_INFINITY;
        binMaxValue = Double.POSITIVE_INFINITY;
    }

    /**
     * Returns a string consisting of some combination of nucleotides
     * 'A', 'C', 'G' and 'U' for the given inclusive range of this
     * RNA sequence (such as a "5'" or "3'" range of a helix).  The
     * letters represent Adenine, Cytosine, Guanine, and Uracil,
     * respectively.
     */
    public String getSequenceInRange(SortedPair inclusiveRange) {
        return getSequence().substring(inclusiveRange.getA() - 1, inclusiveRange.getB()/* IMPORTANT: end point of substring() is exclusive */);
    }
    public char getSequenceAt(int oneBasedValue) {
        return getSequence().charAt(oneBasedValue - 1);
    }

    /**
     * Gives a string representation for the RNA object.
     */
    public String toString() {
        String ans = "Unique ID: " + uid + "\n";
        ans += "Organism: " + organism + "\n";
        ans += "Accession #: " + accession + "\n";
        ans += "Description: " + description + "\n";
        ans += seq.toString();
        return ans;
    }

    /**
     * Stores a new annotation for a predicted helix that covers the
     * specified ranges.  You MUST call processHelixAnnotations()
     * after all annotations are complete in order for annotations to
     * have any effect.  (This approach is used so that individual
     * annotations do not have to incur expensive searches of the
     * helix data store.)  See also getTags() in the Helix class.
     *
     * Note that the same range may be specified more than once (in
     * different calls); all accumulated tags will apply to the helix
     * in the order they were set.
     *
     * @param range5Prime the range of nucleotide numbers on the 5' side
     * @param range3Prime the range of nucleotide numbers on the 3' side
     * @param tag the name of the annotation key
     * @param value the value of the annotation, or null for key-only
     * @param totalHint if this is the first of many annotations, provide
     * a hint as to the likely number of annotations to expect; use 0 if
     * this is unknown
     */
    public void addHelixAnnotation(SortedPair range5Prime, SortedPair range3Prime,
                                   String tag, String value) {
        if (tagValues == null) {
            // allocate on demand
            tag5Ps = new ArrayList<SortedPair>(this.minimumAnnotationCapacity);
            tag3Ps = new ArrayList<SortedPair>(this.minimumAnnotationCapacity);
            tagKeys = new ArrayList<String>(this.minimumAnnotationCapacity);
            tagValues = new ArrayList<String>(this.minimumAnnotationCapacity);
        }
        // IMPORTANT: all lists must be the same length
        tag5Ps.add(range5Prime);
        tag3Ps.add(range3Prime);
        tagKeys.add(tag);
        tagValues.add(value);
    }

    /**
     * Iterates over all annotations added by addHelixAnnotation()
     * and synchronizes data that is stored directly on helices.
     * Note that if an annotation is specified in error, this will
     * log warnings about ranges that did not match any helix.
     */
    public void processHelixAnnotations() {
        HelixStore targetHelices = getPredictedHelices();
        if ((tagValues != null) && (targetHelices != null)) {
            // FIXME: the helix storage is not optimized for quickly locating
            // a helix based on any property (part of the reason this method
            // exists at all); this is essentially linear in the number of
            // helices multiplied by the number of tags set on any helix, and
            // it will NOT be efficient for large data sets; should consider
            // arranging helices in a way that allows easier intersection
            // testing
            final boolean debug = false; // enable if problems are suspected below
            if (debug) {
                // print available annotations
                for (int i = 0; i < tagValues.size(); ++i) {
                    SortedPair p1 = tag5Ps.get(i);
                    SortedPair p2 = tag3Ps.get(i);
                    String key = tagKeys.get(i);
                    String value = tagValues.get(i);
                    AppMain.log(AppMain.INFO, "found annotation: " + p1 + "/" + p2 + "/" + key + "=" + value);
                }
            }
            Set<Integer> tagIndicesUsed = new TreeSet<Integer>();
            for (int i = 0; i < tagValues.size(); ++i) {
                // IMPORTANT: all lists must be the same length
                SortedPair p1 = tag5Ps.get(i);
                SortedPair p2 = tag3Ps.get(i);
                String key = tagKeys.get(i);
                String value = tagValues.get(i);
                int count = 0;
                Iterator<Helix> iter = targetHelices.iterator(p1, p2);
                while (iter.hasNext()) {
                    Helix h = iter.next();
                    if (h.hasRanges(p1, p2)) {
                        if (debug) {
                            AppMain.log(AppMain.INFO, "found helix for annotation: " + p1 + "/" + p2 + "/" + key + "=" + value); // debug
                        }
                        h.addTag(key, value);
                        tagIndicesUsed.add(i);
                    }
                    ++count;
                }
                //AppMain.log(AppMain.INFO, "helices considered for annotation: " + p1 + "/" + p2 + "/" + key + "=" + value + ": " + count); // debug
            }
            // if any annotations were never used, automatically create
            // new helices to represent the requested ranges
            if (tagIndicesUsed.size() != tagValues.size()) {
                Set<Helix> newHelices = new TreeSet<Helix>(new Helix.CompareExtents()); // MUST compare only helix location/length, not other properties
                for (int i = 0; i < tagValues.size(); ++i) {
                    if (!tagIndicesUsed.contains(i)) {
                        // since more than one tag may apply to a helix in
                        // the same range, initially just collect the new
                        // helices and ensure that at most one is created
                        // per unique range
                        SortedPair p1 = tag5Ps.get(i);
                        SortedPair p2 = tag3Ps.get(i);
                        String key = tagKeys.get(i);
                        String value = tagValues.get(i);
                        try {
                            Helix h = new Helix(p1, p2);
                            if (newHelices.contains(h)) {
                                // helix was already created for this range; no need to create a new helix
                                //AppMain.log(AppMain.INFO, "Found existing helix for range " + p1 + "/" + p2 + "; adding tag " + tag); // debug
                            } else {
                                //AppMain.log(AppMain.INFO, "Creating new helix from annotation that did not match any known helix: " + p1 + "/" + p2 + "/" + key); // debug
                                newHelices.add(h);
                                this.predictedHelices.addHelix(h);
                            }
                            h.addTag(key, value);
                        } catch (IllegalArgumentException e) {
                            // helix will fail to construct if the pair values
                            // are inconsistent (Forming different lengths)
                            //e.printStackTrace();
                            AppMain.log(AppMain.ERROR, e.getMessage());
                        }
                    }
                }
            }
            if (debug) {
                // show that helices now have tags
                Iterator<Helix> debugIter = targetHelices.iterator();
                while (debugIter.hasNext()) {
                    Helix h = debugIter.next();
                    Map<String, String> tags = h.getTags();
                    if (tags != null) {
                        AppMain.log(AppMain.INFO, "helix <" + h + "> has tags: " + tags);
                    }
                }
            }
        }
    }

    /**
     * Calls setBinNumber() on every Helix appropriately based on
     * the current binning tag (see setBinTag()), the current
     * annotations on helices, and other helix properties.
     */
    public void processHelixBins() {
        HelixStore targetHelices = getPredictedHelices();
        Iterator<Helix> helixIter = targetHelices.iterator();
        int numberErrors = 0;
        boolean refEnergy = ((this.binTag != null) &&
                             (this.binTag.equals(Helix.InternalTags.TAG_MATCH_ENERGY)));
        while (helixIter.hasNext()) {
            Helix h = helixIter.next();
            if (h == null) {
                continue;
            }
            int binNumber = Helix.NO_BIN;
            double referenceValue = 0;
            boolean valueOK = false;
            if (!refEnergy) {
                if (h.hasTag(this.binTag)) {
                    // use the value of the specified tag for binning purposes
                    try {
                        referenceValue = Double.parseDouble(h.getTagValue(this.binTag));
                        valueOK = true;
                    } catch (NumberFormatException e) {
                        ++numberErrors;
                    }
                }
            } else {
                // use the helix energy for binning purposes
                referenceValue = h.getEnergy();
                valueOK = true;
            }
            if (valueOK) {
                binNumber = AppMain.selectBin(referenceValue, this.binCount,
                                              this.binMinValue, this.binMaxValue);
            }
            h.setBinNumber(binNumber); // set new value, or set Helix.NO_BIN value
            if (refEnergy) {
                // formerly done by EnergyMaxMinFilter; set or clear tag to
                // indicate that the energy value is in the required range
                String constraintDesc = String.format("%.2f:%.2f", this.binMaxValue, this.binMinValue);
                if (binNumber != Helix.NO_BIN) {
                    h.addTag(Helix.InternalTags.TAG_MATCH_ENERGY, constraintDesc);
                } else {
                    h.removeTag(Helix.InternalTags.TAG_MATCH_ENERGY);
                }
            }
        }
        if (numberErrors > 0) {
            AppMain.log(AppMain.ERROR, "Binning is incomplete because some of the values for the helix annotation '" + this.binTag + "' are not numeric; affected helices: " + numberErrors);
        }
    }

    /** Returns a data structure representing possible basepairs that this RNA forms.
     * It is represented as a two dimentional array of boolean values.  The first array
     * represent the rows and the second represent the columns.  A true is present if
     * nucleotide at ith row and jth column forms a potential basepair.
     * @return A two dimensional array of boolean values.  True represents a potential basepair
     * and false is the lack thereof.
     */
    public boolean[][] getBasePairs() {
        return basePairs;
    }

    /**
     * Currently equivalent to using setBasePairs() to clear and rebuild
     * all helices without actually changing the base-pair values (useful
     * if something has since pruned the original set).
     */
    public void resetPredictedHelices() {
        boolean[][] newBasePairs = getBasePairs();
        final int seqLength = newBasePairs.length; // note: reads 1st array dimension only
        HelixStore targetStore = predictedHelices;
        if (targetStore == null) {
            return;
        }
        targetStore.clear();
        boolean[][] bp_temp = new boolean[seqLength][seqLength];
        // Creating a temp copy of base-pairs, to be used in the function
        for (int i = 0; i < seqLength; ++i) {
            System.arraycopy(newBasePairs[i], 0, bp_temp[i], 0, seqLength);
        }
        for (int i = 0; i< (seqLength - 1); ++i) {
            for (int j = 0; j < i; ++j) {
                if (bp_temp[i][j] == true) {
                    int temp_i = i;
                    int temp_j = j;
                    int helixLength = 0;
                    int startX = i;
                    int startY = j;
                    do {
                        bp_temp[temp_i][temp_j] = false;
                        ++temp_i; --temp_j; 
                        ++helixLength;
                        if ((temp_i == seqLength) || (temp_j < 0)) {
                            break;
                        }
                    } while (bp_temp[temp_i][temp_j] == true);
                    SortedPair threePrimeRange = new SortedPair(startX + 1, startX + 1 + helixLength - 1);
                    SortedPair fivePrimeRange = new SortedPair(startY + 1, startY + 1 - helixLength + 1);
                    targetStore.addHelix(new Helix(fivePrimeRange, threePrimeRange));
                }
            }
        }
        predictedHelicesChanged();
        // probability is high that memory profile has changed;
        // try to free some memory now
        System.gc();
        // NOTE: energies no longer assigned by default (this just forces
        // everything to be annotated, over a wide spectrum; better to
        // force the user to set a range)
        //EnergyMaxMinFilter energyCalc = new EnergyMaxMinFilter();
        //energyCalc.applyConstraint(this);
    }

    /**
     * Creates Helix objects by examining the given grid of on/off
     * values and determining where there are helices.  Any
     * previous Helix objects are cleared.  WARNING: References to
     * old Helix objects will become meaningless (e.g. as held by
     * script variables or otherwise).
     */
    public void setBasePairs(boolean[][] newBasePairs) {
        this.basePairs = newBasePairs;
        resetPredictedHelices();
    }

    /**
     * Changes the number of unique bin numbers there are (meaning
     * that this number of unique colors are necessary in the final
     * spectrum in order to see every bin at a glance).
     *
     * IMPORTANT: The effects are not seen until processHelixBins() is
     * called (this allows you to set multiple bin properties before
     * incurring the cost of updating the RNA).
     *
     * @param count the number of bins, which divide the region
     * between the maximum and minimum values
     */
    public void setBinCount(int count) {
        assert(count > 0);
        this.binCount = count;
    }

    /**
     * See setBinCount().
     *
     * @return the number of bins
     */
    public int getBinCount() {
        return this.binCount;
    }

    /**
     * Changes the largest numerical value for the binning tag
     * that is recognized as belonging to a bin.  Anything higher is
     * assigned to Helix.NO_BIN.
     *
     * IMPORTANT: The effects are not seen until processHelixBins() is
     * called (this allows you to set multiple bin properties before
     * incurring the cost of updating the RNA).
     *
     * @param value the new maximum value
     */
    public void setBinMaxValue(double value) {
        this.binMaxValue = value;
    }

    /**
     * Changes the smallest numerical value for the binning tag
     * that is recognized as belonging to a bin.  Anything lower is
     * assigned to Helix.NO_BIN.
     *
     * IMPORTANT: The effects are not seen until processHelixBins() is
     * called (this allows you to set multiple bin properties before
     * incurring the cost of updating the RNA).
     *
     * @param value the new minimum value
     */
    public void setBinMinValue(double value) {
        this.binMinValue = value;
    }

    /**
     * Specifies the tag to use for binning helices (represented by a
     * color spectrum in the display).
     *
     * IMPORTANT: The effects are not seen until processHelixBins() is
     * called (this allows you to set multiple bin properties before
     * incurring the cost of updating the RNA).
     *
     * IMPORTANT: Binning is currently only numerical so the string
     * values for the given tag must successfully convert into Double.
     * TODO: It is theoretically possible to allow statistical binning
     * of other types of values, e.g. based on alphabetical sorting of
     * strings or frequency sorting by number of times a value appears;
     * for now the binning is strictly a numerical range.
     *
     * @param tagName the key-value tag whose value on a helix decides
     * the bin for that helix; or, Helix.InternalTags.TAG_MATCH_ENERGY
     * to indicate that the Helix.getEnergy() value should be used to
     * decide the bin; or, null to remove all bin numbers
     */
    public void setBinTag(String tagName) {
        this.binTag = tagName;
    }

    /**
     * Returns the sequence of this RNA as a String, with each
     * character referring to a single A, C, G or U nucleotide.
     */
    public String getSequence() {
        return seq;
    }

    /**
     * Returns the below-diagonal helices, which includes any that
     * were calculated from the initial set of base-pairs and any
     * that were generated by unique annotations.
     */
    public HelixStore getPredictedHelices() {
        return predictedHelices;
    }

    /**
     * @return unique ID, taken from original input file
     */
    public String getUID() {
        return uid;
    }

    /**
     * @return organism name, taken from original input file
     */
    public String getOrganism() {
        return organism;
    }

    /**
     * @return accession number, taken from original input file
     */
    public String getAccession() {
        return accession;
    }

    /**
     * @return description, taken from original input file
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return number of base-pairs
     */
    public int getLength() {
        return seq.length();
    }

    /**
     * Returns the above-diagonal helices, which are typically
     * defined by reading an input RNA file such as a ".bpseq".
     */
    public HelixStore getActualHelices() {
        return actualHelices;
    }

    public void setActual(boolean[][] actual) {
        final int seqLength = seq.length();
        actualHelices.clear();
        // IMPORTANT: array indices are zero-based but displayed
        // numbers are one-based; therefore, index 0 is described
        // as nucleotide #1, etc.
        for (int i = 0; i < (seqLength - 1); ++i) {
            for (int j = 0; j < i; ++j) {
                if (actual[i][j] == true) {
                    // look in both diagonal directions for full helix; clear
                    // flags during checks so that future loop iterations do
                    // not try to create redundant helices
                    int startX = i;
                    int startY = j;
                    int helixLength = 0;
                    // see if helix is connected lower-left from start point
                    for (int x = i, y = j;
                         ((x >= 0) && (y < seqLength) && (actual[x][y]));
                         --x, ++y) {
                        ++helixLength;
                        startX = x;
                        startY = y;
                        actual[x][y] = false;
                    }
                    // see if helix is connected upper-right from start point
                    for (int x = (i + 1), y = (j - 1);
                         ((x < seqLength) && (y >= 0) && (actual[x][y]));
                         ++x, --y) {
                        ++helixLength;
                        actual[x][y] = false;
                    }
                    // create helix from lowest-left with full length
                    SortedPair threePrimeRange = new SortedPair(startX + 1, startX + 1 + helixLength - 1); // convert from 0-based to 1-based
                    SortedPair fivePrimeRange = new SortedPair(startY + 1, startY + 1 - helixLength + 1);
                    Helix h = new Helix(fivePrimeRange, threePrimeRange);
                    h.setActual();
                    actualHelices.addHelix(h);
                }
            }
        }
    }

    /**
     * Call whenever modifying the "predictedHelices" store.
     */
    private void predictedHelicesChanged() {
        // the new helices could hold references to entirely different
        // instances of Helix objects that are equivalent, and they
        // may not be tagged at all (e.g. created anew by filtering);
        // therefore, revisit all annotations and apply them again
        processHelixAnnotations();
        processHelixBins();
    }

    private String uid; // unique ID of the RNA sequence.
    private String organism; // organism where the RNA is from.
    private String accession; // accession number of the RNA.
    private String description; // description of the RNA.
    private String binTag; // see setBinTag()
    private String seq; // the sequence of the RNA.
    private ArrayList<SortedPair> tag5Ps; // 5' ranges for tagged helices
    private ArrayList<SortedPair> tag3Ps; // 3' ranges for tagged helices
    private ArrayList<String> tagKeys; // tag keys for tagged helices
    private ArrayList<String> tagValues; // tag values for tagged helices (may contain null)
    private int minimumAnnotationCapacity; // a hint to the likely length of "tagValues", etc.
    private int binCount; // see processHelixBins()
    private double binMinValue; // see processHelixBins()
    private double binMaxValue; // see processHelixBins()
    private boolean[][] basePairs; // the possible basepairs.
    private HelixStore predictedHelices; // the possible helices
    private HelixStore actualHelices; // the actual helices

}
