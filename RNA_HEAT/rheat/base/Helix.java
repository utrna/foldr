/*
 * Helix.java
 *
 * Created on February 27, 2003, 2:21 PM
 */

package rheat.base;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/** This class represents one helix that could form.  Helix is defined as a
 * consecutive sequence of basepairs, with length of one or more.  A single
 * basepair is a helix of length 1.
 * <p>
 * Some confusion may arise from how the Helix is defined.  The starting X and
 * starting Y position is viewed from a 2 dimensional grid view.  The X is the
 * vertical (ie. row #) and Y is the horizontal (ie. col #).  But since the
 * sequence on the X and Y are the same sequence, only the bottom half of the grid
 * is relevant.
 * <p>
 * For Example: Consider the following helix (in linear view), assume 
 * antiparallel orientation: <br>
 * xxxx<b>A</b>GGGCUxxxxAGCCC<b>U</b>xxxx  <br>
 * The first boldfaced A is the starting position of X.  The second boldfaced
 * U is the starting position of Y. The length of this helix is 6.
 * @author Team MatriX
 */
public class Helix
implements Comparable<Helix>, java.io.Serializable {

    /**
     * For use with Helix.addTag(), typically to keep track of the
     * helices that matched the settings of a particular constraint.
     * By convention, built-in tags have underscores at start/end so
     * they are unlikely to conflict with anything set by the user.
     */
    static public interface InternalTags {

        /**
         * Helix is from original data file, above diagonal; see setActual().
         */
        public static final String TAG_ACTUAL = "_ACTUAL_";

        /**
         * Helix was successfully assigned an energy value by a constraint.
         */
        public static final String TAG_MATCH_ENERGY = "_MATCH_ENERGY_";

        /**
         * Helix matches the AAandAGHelicesFilter object.
         */
        public static final String TAG_MATCH_AA_AG = "_MATCH_AA_AG_";

        /**
         * Helix matches the ComplexFilter object.
         */
        public static final String TAG_MATCH_COMPLEX_DISTANCE = "_MATCH_COMPLEX_DISTANCE_";

        /**
         * Helix matches the DiagonalDistanceFilter object, NORMAL mode.
         */
        public static final String TAG_MATCH_DIAGONAL_DISTANCE = "_MATCH_DIAGONAL_DISTANCE_";

        /**
         * Helix matches the DiagonalDistanceFilter object, INVERTED mode.
         */
        public static final String TAG_MATCH_NON_DIAGONAL_DISTANCE = "_MATCH_NON_DIAGONAL_DISTANCE_";

        /**
         * Helix matches the ELoopHelicesFilter object.
         */
        public static final String TAG_MATCH_E_LOOP = "_MATCH_E_LOOP_";

        /**
         * Helix matches the MaxMinFilter object.
         */
        public static final String TAG_MATCH_LENGTH = "_MATCH_LENGTH_";

    }

    /**
     * Used with setBinNumber() to indicate a lack of bin membership.
     */
    public static final int NO_BIN = -1;

    /**
     * In some cases it is useful to be able to compare helices using
     * a subset of their properties; this class compares helices ONLY
     * using their range values.  See TreeSet(Comparator).
     */
    static public class CompareExtents
    implements Comparator<Helix> {
        public int compare(Helix h1, Helix h2) {
            // only consider 5'/3' ranges
            int a, b;
            a = h1.fivePrimeRange.getA();
            b = h2.fivePrimeRange.getA();
            if (a != b) {
                return ((a < b) ? -1 : 1);
            }
            a = h1.fivePrimeRange.getB();
            b = h2.fivePrimeRange.getB();
            if (a != b) {
                return ((a < b) ? -1 : 1);
            }
            a = h1.threePrimeRange.getA();
            b = h2.threePrimeRange.getA();
            if (a != b) {
                return ((a < b) ? -1 : 1);
            }
            a = h1.threePrimeRange.getB();
            b = h2.threePrimeRange.getB();
            if (a != b) {
                return ((a < b) ? -1 : 1);
            }
            // equal in all checked propreties
            return 0;
        }
    }

    /**
     * Constructs a Helix expressed in terms of ranges.  Note that
     * unlike X/Y initialization, the given ranges are 1-based (0 is
     * not a valid value) and they are inclusive (the length is the
     * last minus the first, plus 1).
     *
     * See also get3PrimeRange() and get5PrimeRange().
     */
    public Helix(SortedPair fivePrimeRange, SortedPair threePrimeRange) {
        this.tags = null;
        this.binNumber = NO_BIN;
        this.helixEnergy = 0;
        // IMPORTANT: caller should also assert that the helix range
        // maximum values do not exceed the base-pair count (the RNA
        // information is not available from here)
        if ((fivePrimeRange.getA() < 1) ||
            (fivePrimeRange.getB() < 1) ||
            (threePrimeRange.getA() < 1) ||
            (threePrimeRange.getB() < 1)) {
            throw new IllegalArgumentException("failed to create helix: 5' or 3' range contains a value less than 1 (given: " + fivePrimeRange + ", " + threePrimeRange + ")");
        }
        this.fivePrimeRange = new SortedPair(fivePrimeRange.getA(), fivePrimeRange.getB());
        this.threePrimeRange = new SortedPair(threePrimeRange.getA(), threePrimeRange.getB());
        // the two ranges must be consistent
        final int length3 = threePrimeRange.getLength();
        final int length5 = fivePrimeRange.getLength();
        if (length3 != length5) {
            throw new IllegalArgumentException("failed to create helix: 5' range of length " + length5 + " does not match 3' range of length " + length3 + " (given: " + fivePrimeRange + ", " + threePrimeRange + ")");
        }
        // perform one last sanity check; the hasRanges() method should
        // always succeed for the given ranges because the new helix
        // definition should represent those same ranges
        if (!hasRanges(fivePrimeRange, threePrimeRange)) {
            SortedPair p1 = new SortedPair(-1, -1);
            SortedPair p2 = new SortedPair(-1, -1);
            get3PrimeRange(p1);
            get5PrimeRange(p2);
            throw new IllegalArgumentException("failed to create helix: intersection test failed for " + p1 + ", " + p2 + " (given: " + fivePrimeRange + ", " + threePrimeRange + ")");
        }
    }

    /**
     * See processHelixAnnotations() in the RNA class.
     */
    public void addTag(String key, String value) {
        if (tags == null) {
            tags = new HashMap<String, String>();
        }
        tags.put(key, value);
    }

    /**
     * Deletes string tag set by addTag().
     */
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    /**
     * Returns the value of the given tag or null if it does not have
     * a value.  Note that hasTag() can be used to tell when a
     * key-only annotation is not set at all.
     */
    public String getTagValue(String tagName) {
        String result = null;
        Map<String, String> tags = getTags();
        if (tags != null) {
            result = tags.get(tagName);
        }
        return result;
    }

    /**
     * Returns true if the annotations of this helix include
     * the given tag, in a search that is faster than manual
     * iteration would be.
     */
    public boolean hasTag(String tagName) {
        boolean result = false;
        Map<String, String> tags = getTags();
        if (tags != null) {
            result = tags.keySet().contains(tagName);
        }
        return result;
    }

    /**
     * Returns any annotations on this helix.  May be null.
     */
    public Map<String, String> getTags() {
        return tags; // may be null
    }

    /**
     * Specifies that this Helix OBJECT represents an actual helix
     * (as opposed to other Helix objects that may be equal in
     * range but rendered below the diagonal).
     */
    public void setActual() {
        addTag(InternalTags.TAG_ACTUAL, null);
    }

    /**
     * @return true only if setActual() was called
     */
    public boolean isActualHelix() {
        return hasTag(InternalTags.TAG_ACTUAL);
    }

    /**
     * Returns the value set by setBinNumber().
     */
    public int getBinNumber() {
        return binNumber; // may be -1
    }

    /**
     * If the helix is being binned (grouped with other helices
     * according to some criteria), set this value to a zero-based
     * index; otherwise, set to NO_BIN.  The renderer may use this
     * to pick a color or other annotation from a list.  If the bin
     * number is outside the range of available annotations, it may
     * cause no special annotation at all.
     */
    public void setBinNumber(int binNumber) {
        this.binNumber = binNumber;
    }

    /**
     * Returns the number of nucleotide pairs.
     */    
    public int getLength() {
        // NOTE: constructor guarantees that 5'/3' lengths match
        return this.threePrimeRange.getLength(); // arbitrary
    }

    public void setEnergy(double energy) {
        helixEnergy = energy;
    }

    public double getEnergy() {
        return helixEnergy;
    }

    /**
     * Implements Comparable.
     */
    @Override
    public int compareTo(Helix otherHelix) {
        if (this.equals(otherHelix)) {
            return 0;
        }
        return ((System.identityHashCode(this) < System.identityHashCode(otherHelix))
                ? -1 : 1);
    }

    /**
     * Overrides equals() in Object to test another object against this one.
     * See also compareTo().
     */    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Helix) {
            Helix other = (Helix)o;
            boolean eqTags = ((this.tags == null) && (other.tags == null));
            if (!eqTags) {
                if ((this.tags != null) && (other.tags != null)) {
                    eqTags = this.tags.equals(other.tags);
                }
            }
            if (this.threePrimeRange.equals(other.threePrimeRange) &&
                this.fivePrimeRange.equals(other.fivePrimeRange) &&
                (this.helixEnergy == other.helixEnergy) &&
                (eqTags)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Returns pair representation of 3' range, inclusive.
     * Minimum value is 1, as specified in input files.
     */
    public void get3PrimeRange(SortedPair sp) {
        sp.setValues(this.threePrimeRange.getA(), this.threePrimeRange.getB());
    }

    /**
     * Returns the smaller value of the 3' range.
     * See also get3PrimeRange().
     */
    public int get3PrimeStart() {
        return threePrimeRange.getA();
    }

    /**
     * Returns the larger value of the 3' range.
     * See also get3PrimeRange().
     */
    public int get3PrimeEnd() {
        return threePrimeRange.getB();
    }

    /**
     * Returns pair representation of 5' range, inclusive.
     * Minimum value is 1, as specified in input files.
     */
    public void get5PrimeRange(SortedPair sp) {
        sp.setValues(this.fivePrimeRange.getA(), this.fivePrimeRange.getB());
    }

    /**
     * Returns the smaller value of the 5' range.
     * See also get5PrimeRange().
     */
    public int get5PrimeStart() {
        return fivePrimeRange.getA();
    }

    /**
     * Returns the larger value of the 5' range.
     * See also get5PrimeRange().
     */
    public int get5PrimeEnd() {
        return fivePrimeRange.getB();
    }

    /**
     * Returns true only if the specified ranges match the helix.
     * Note that the minimum value for any given range is 1, not 0.
     * The values in this helix are compared in sorted order so it
     * does not matter which number is stored as X or Y.
     */
    public boolean hasRanges(SortedPair fivePrimeSide, SortedPair threePrimeSide) {
        // NOTE: specifications are one-based but storage is zero-based (like an array index)
        final boolean eq5P = this.fivePrimeRange.equals(fivePrimeSide);
        final boolean eq3P = this.threePrimeRange.equals(threePrimeSide);
        //AppMain.log(0, "CHECK: " + p1 + "==" + fivePrimeSide + "," + p2 + "==" + threePrimeSide + ":" + eq5P + "," + eq3P); // debug
        return (eq5P && eq3P);
    }

    /** Returns a string representing his helix.
     * @return A string representing this helix.
     */    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append("5'=");
        sb.append(this.fivePrimeRange.toString());
        sb.append(" 3'=");
        sb.append(this.threePrimeRange.toString());
        sb.append(" bin=");
        sb.append(this.binNumber);
        sb.append(" e=");
        sb.append("" + this.helixEnergy);
        sb.append(" ntags=");
        if (tags == null) {
            sb.append("0");
        } else {
            sb.append("" + tags.size());
        }
        sb.append(">");
        return sb.toString();
    }

    private SortedPair fivePrimeRange;
    private SortedPair threePrimeRange;
    private int binNumber;
    private double helixEnergy;
    private HashMap<String, String> tags; // created on demand

}
