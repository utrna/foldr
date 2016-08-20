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
     * using their X, Y and length values.  See TreeSet(Comparator).
     */
    static public class CompareExtents
    implements Comparator<Helix> {
        public int compare(Helix h1, Helix h2) {
            // only consider xPosition, yPosition, helixLength
            if (h1.xPosition < h2.xPosition) {
                return -1;
            } else if (h1.xPosition > h2.xPosition) {
                return 1;
            }
            // equal X
            if (h1.yPosition < h2.yPosition) {
                return -1;
            } else if (h1.yPosition > h2.yPosition) {
                return 1;
            }
            // equal Y
            if (h1.helixLength < h2.helixLength) {
                return -1;
            } else if (h1.helixLength > h2.helixLength) {
                return 1;
            }
            // equal length
            return 0;
        }
    }

    /** Creates a new instance of Helix given the starting position of X and Y and the
     * length.  Assumes antiparallel orientation.
     * @param posx The starting x position.  For example: <B>A</B>GCU____AGCU
     * @param posy The starting y position.  For example: AGCU____AGC<B>U</B>
     * @param hlen The length of the helix.  For example: AGCU____AGCU the length is 4.
     */
    public Helix(int posx, int posy, int hlen) {
        this.init(posx, posy, hlen);
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
        // defer to other constructor (translate); this MUST
        // agree with interpretation in get3PrimeRange()
        // and get5PrimeRange()
        final int x = getXForThreePrimeRange(threePrimeRange);
        final int length3 = getRangeLength(threePrimeRange);
        final int y = getYForFivePrimeRange(fivePrimeRange);
        final int length5 = getRangeLength(fivePrimeRange);
        // the two ranges must be consistent
        if (length3 != length5) {
            throw new IllegalArgumentException("failed to create helix: 5' range of length " + length5 + " does not match 3' range of length " + length3 + " (given: " + fivePrimeRange + ", " + threePrimeRange + ")");
        }
        // store ranges in converted form (X, Y and length)
        this.init(x, y, length3);
        // perform one last sanity check; the intersects() method should
        // always succeed for the given ranges because the new helix
        // definition should represent those same ranges
        if (!intersects(fivePrimeRange, threePrimeRange)) {
            SortedPair p1 = new SortedPair(-1, -1);
            SortedPair p2 = new SortedPair(-1, -1);
            get3PrimeRange(p1);
            get5PrimeRange(p2);
            throw new IllegalArgumentException("failed to create helix: intersection test failed for " + p1 + ", " + p2 + " (given: " + fivePrimeRange + ", " + threePrimeRange + ")");
        }
    }

    static public int getXForThreePrimeRange(SortedPair threePrimeRange) {
        return (threePrimeRange.getA() - 1);
    }

    static public void setThreePrimeRangeFromXandLength(int x, int length, SortedPair threePrimeRange) {
        threePrimeRange.setValues(x + length, x + 1);
    }

    static public int getYForFivePrimeRange(SortedPair fivePrimeRange) {
        return (fivePrimeRange.getB() - 1);
    }

    static public void setFivePrimeRangeFromYandLength(int y, int length, SortedPair fivePrimeRange) {
        fivePrimeRange.setValues(y + 1 - length + 1, y + 1);
    }

    static public int getRangeLength(SortedPair fiveOrThreePrimeRange) {
        return (fiveOrThreePrimeRange.getB() - fiveOrThreePrimeRange.getA() + 1);
    }

    private void init(int posx, int posy, int hlen) {
        xPosition = posx;
        yPosition = posy;
        helixLength = hlen;
        tags = null;
        binNumber = -1;
        helixEnergy = 0;
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

    /** Returns the starting X position.
     * @return An integer index of the starting X position.
     */    
    public int getStartX() {
        return xPosition;
    }

    /** Returns the starting Y position.
     * @return An integer index of the starting Y position.
     */    
    public int getStartY() {
        return yPosition;
    }

    /** Returns the length of this helix.
     * @return An integer length of the helix.
     */    
    public int getLength() {
        return helixLength;
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
            if ((this.xPosition == other.xPosition) &&
                (this.yPosition == other.yPosition) &&
                (this.helixLength == other.helixLength) &&
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
     * Returns pair representation of 3' range, inclusive.  NOTE that
     * although values are stored zero-based like array indices, the
     * pair representations are one-based (as they might appear when
     * described in a file).  This is because, in files, 0 might have
     * special meaning.
     */
    public void get3PrimeRange(SortedPair sp) {
        // IMPORTANT: must agree with translation in Helix constructor (SortedPair)
        setThreePrimeRangeFromXandLength(xPosition, helixLength, sp);
    }

    /**
     * Returns pair representation of 5' range, inclusive.  NOTE that
     * although values are stored zero-based like array indices, the
     * pair representations are one-based (as they might appear when
     * described in a file).  This is because, in files, 0 might have
     * special meaning.
     */
    public void get5PrimeRange(SortedPair sp) {
        // IMPORTANT: must agree with translation in Helix constructor (SortedPair)
        setFivePrimeRangeFromYandLength(yPosition, helixLength, sp);
    }

    /**
     * Returns true only if the specified ranges match the helix.
     * Note that the minimum value for any given range is 1, not 0.
     * The values in this helix are compared in sorted order so it
     * does not matter which number is stored as X or Y.
     */
    public boolean intersects(SortedPair fivePrimeSide, SortedPair threePrimeSide) {
        // (this should match how HelixInfo interprets the data)
        // NOTE: specifications are one-based but storage is zero-based (like an array index)
        SortedPair p1 = new SortedPair(-1, -1);
        SortedPair p2 = new SortedPair(-1, -1);
        get5PrimeRange(p1);
        get3PrimeRange(p2);
        final boolean eq5P = p1.equals(fivePrimeSide);
        final boolean eq3P = p2.equals(threePrimeSide);
        //AppMain.log(0, "CHECK: " + p1 + "==" + fivePrimeSide + "," + p2 + "==" + threePrimeSide + ":" + eq5P + "," + eq3P); // debug
        return (eq5P && eq3P);
    }

    /** Returns a string representing his helix.
     * @return A string representing this helix.
     */    
    @Override
    public String toString() {
        String ans = "";
        ans = ans + xPosition + " " + yPosition + " " + helixLength;
        return ans;
    }

    private int xPosition;
    private int yPosition;
    private int helixLength;
    private int binNumber;
    private double helixEnergy;
    private HashMap<String, String> tags; // created on demand

}
