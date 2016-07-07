/*
 * RNA.java
 *
 * Created on February 14, 2003, 3:43 PM
 */

package rheat.base;

import java.util.ArrayList;
import java.io.OutputStream;
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
    public RNA(String u, String o, String a, String d, ArrayList s) {
        uid = u;
        organism = o;
        accession = a;
        description = d;
        seq = s;
        System.out.println("Size of the Sequence: " + seq.size());
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
    public ArrayList getSequence() {
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
    }

    /** This method is for debugging purposes only.  It outputs the possible helices in
     * a grid based format to the specified PrintStream.  This may take a REALLY LONG
     * time to complete.
     * @param ps The PrintStream used for output.
     * @return number of helices
     */
    public int outputHelices(PrintStream ps) {
        int result = 0;
        ArrayList seq = getSequence();
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
    private ArrayList seq; // the sequence of the RNA.
    private boolean[][] basePairs; // the possible basepairs.
    private HelixStore predictedHelices; // the possible helices
    private HelixStore actualHelices; // the actual helices
}
