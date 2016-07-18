/*
 * RNA.java
 *
 * Created on February 14, 2003, 3:43 PM
 */

package rheat.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.io.PrintStream;

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
     * @param s An ArrayList for the sequence underlying this RNA.
     */
    public RNA(String u, String o, String a, String d, ArrayList<String> s) {
        uid = u;
        organism = o;
        accession = a;
        description = d;
        seq = s;
        //System.out.println("Size of the Sequence: " + seq.size());
        basePairs = new boolean[seq.size()][seq.size()];
    }

    /** Gives a string representation for the RNA object.
     * @return A string representing this RNA.
     */
    public String toString() {
        String ans = "Unique ID: " + uid + "\n";
        ans += "Organism: " + organism + "\n";
        ans += "Accession #: " + accession + "\n";
        ans += "Description: " + description + "\n";
        ans += seq.toString();
        //predictedHelices = new HelixGroup(seq.size());
        //outputBP();
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
     */
    public void addHelixAnnotation(SortedPair range5Prime, SortedPair range3Prime,
                                   String tag) {
        if (tagValues == null) {
            // allocate on demand
            tag5Ps = new ArrayList<SortedPair>();
            tag3Ps = new ArrayList<SortedPair>();
            tagValues = new ArrayList<String>();
        }
        // IMPORTANT: all lists must be the same length
        tag5Ps.add(range5Prime);
        tag3Ps.add(range3Prime);
        tagValues.add(tag);
    }

    /**
     * Iterates over all annotations added by addHelixAnnotation()
     * and synchronizes data that is stored directly on helices.
     * Note that if an annotation is specified in error, this will
     * log warnings about ranges that did not match any helix.
     */
    public void processHelixAnnotations() {
        if (tagValues != null) {
            HelixStore targetHelices = getHelices();
            Iterator<Helix> iter = targetHelices.iterator();
            Set<Integer> tagIndicesUsed = new TreeSet<Integer>();
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
                    String tag = tagValues.get(i);
                    AppMain.log(AppMain.INFO, "found annotation: " + p1 + "/" + p2 + "/" + tag);
                }
            }
            while (iter.hasNext()) {
                Helix h = iter.next();
                // IMPORTANT: all lists must be the same length
                for (int i = 0; i < tagValues.size(); ++i) {
                    SortedPair p1 = tag5Ps.get(i);
                    SortedPair p2 = tag3Ps.get(i);
                    String tag = tagValues.get(i);
                    if (h.intersects(p1, p2)) {
                        if (debug) {
                            AppMain.log(AppMain.INFO, "found helix for annotation: " + p1 + "/" + p2 + "/" + tag); // debug
                        }
                        h.addTag(tag);
                        tagIndicesUsed.add(i);
                    }
                }
            }
            // display warning messages about any annotations that were
            // never used (could indicate an improperly-constructed file)
            if (tagIndicesUsed.size() != tagValues.size()) {
                for (int i = 0; i < tagValues.size(); ++i) {
                    if (!tagIndicesUsed.contains(i)) {
                        SortedPair p1 = tag5Ps.get(i);
                        SortedPair p2 = tag3Ps.get(i);
                        String tag = tagValues.get(i);
                        AppMain.log(AppMain.WARN, "Helix annotation ignored (did not match any known helix): " + p1 + "/" + p2 + "/" + tag);
                    }
                }
            }
            if (debug) {
                // show that helices now have tags
                iter = targetHelices.iterator();
                while (iter.hasNext()) {
                    Helix h = iter.next();
                    Set<String> tags = h.getTags();
                    if (tags != null) {
                        AppMain.log(AppMain.INFO, "helix <" + h + "> has tags: " + tags);
                    }
                }
            }
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

    /** Changes the current set of possible basepairs.  This should be called inside a
     * Filter's apply() method when changing the basepairing rules or refining the base
     * pairing rules due to the application of the Filter.
     * @param newbp The new set of all possible basepairs that is to replace the original one.
     */
    public void setBasePairs(boolean[][] newbp) {
        basePairs = newbp;
    }

    /** This is a method for debugging purposes only.  It outputs the basepairs of this
     * RNA in a half grid shaped format to the specified PrintStream.  For large RNA
     * this may take a long time.
     * @param ps The PrintStream to use for output.  If you don't know which one to use, then you
     * can use System.out which prints everything on the screen.
     */
    public void outputBP(PrintStream ps) {
        for (int i = 0; i < (seq.size()); i++) {
            for (int j = 0; j <= i; j++) {
                if (basePairs[i][j]) {
                    ps.print("1 ");
                }
                else {
                    ps.print("0 ");
                }
            }
            ps.println();
        }
    }

    /** Returns the sequence of this RNA in an ArrayList, with each item of the
     * ArrayList to be a single nucleotide.
     * @return The sequences in an ArrayList.
     */
    public ArrayList<String> getSequence() {
        return seq;
    }

    /** Returns the current set of Helices in this RNA.  This is computed by the
     * previous applications of various filters on this RNA object.
     * @return A HelixStore representing the possible helices that currently are predicted by
     * the application of various filters.
     */
    public HelixStore getHelices() {
        return predictedHelices;
    }

    /** Changes the current set of predicted helices.  This should be called inside a
     * Filter's apply() method when changing the helices that are to appear due to the
     * rules applied by the Filter.
     * @param newHelices The new set of Helices that is to replace the current set.
     */
    public void setHelices(HelixStore newHelices) {
        if (newHelices == null) {
            throw new RuntimeException("illegal to set helix store to null");
        }
        predictedHelices = newHelices;
        // the new helices could hold references to entirely different
        // instances of Helix objects that are equivalent, and they
        // may not be tagged at all (e.g. created anew by filtering);
        // therefore, revisit all annotations and apply them again
        processHelixAnnotations();
    }

    /** This method is for debugging purposes only.  It outputs the possible helices in
     * a grid based format to the specified PrintStream.  This may take a REALLY LONG
     * time to complete.
     * @param ps The PrintStream used for output.
     * @return number of helices
     */
    public int outputHelices(PrintStream ps) {
        int result = 0;
        ArrayList<String> seq = getSequence();
        result = ((HelixGrid)predictedHelices).debugOutput(ps, seq);
        return result;
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
        return seq.size();
    }

    /**
     * @return helices identified by original input file
     */
    public HelixStore getActual() {
        return actualHelices;
    }

    public void setActual(boolean[][] actual) {
        final int seqLength = seq.size();
        this.actualHelices = new HelixGrid(seqLength);
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
                    Helix h = new Helix(startX, startY, helixLength);
                    this.actualHelices.addHelix(h);
                }
            }
        }
    }

    private String uid; // unique ID of the RNA sequence.
    private String organism; // organism where the RNA is from.
    private String accession; // accession number of the RNA.
    private String description; // description of the RNA.
    private ArrayList<String> seq; // the sequence of the RNA.
    private ArrayList<SortedPair> tag5Ps; // 5' ranges for tagged helices
    private ArrayList<SortedPair> tag3Ps; // 3' ranges for tagged helices
    private ArrayList<String> tagValues; // tag values for tagged helices
    private boolean[][] basePairs; // the possible basepairs.
    private HelixStore predictedHelices; // the possible helices
    private HelixStore actualHelices; // the actual helices
}
