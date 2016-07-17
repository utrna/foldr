/*
 *
 * Helix.java
 *
 * Created on February 27, 2003, 2:21 PM
 */

package rheat.base;

import java.util.HashSet;
import java.util.Set;

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
public class Helix implements java.io.Serializable {

    /** Denotes that this helix is in antiparallel orientation */    
    public static final int ANTIPARALLEL = 0;
    /** Denotes that this helix is in parallel orientation */    
    public static final int PARALLEL = 1;

    /** Creates a new instance of Helix given the starting position of X and Y and the
     * length.  Assumes antiparallel orientation.
     * @param posx The starting x position.  For example: <B>A</B>GCU____AGCU
     * @param posy The starting y position.  For example: AGCU____AGC<B>U</B>
     * @param hlen The length of the helix.  For example: AGCU____AGCU the length is 4.
     */
    public Helix(int posx, int posy, int hlen) {
        xPosition = posx;
        yPosition = posy;
        helixLength = hlen;
        orientation = this.ANTIPARALLEL;
        tags = null;
    }

    /**
     * See processHelixAnnotations() in the RNA class.
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<String>();
        }
        tags.add(tag);
    }

    public Set<String> getTags() {
        return tags; // may be null
    }

    /** Returns the orientation of this helix.
     * @return Either Helix.ANTIPARALLEL or Helix.PARALLEL
     */    
    public int getOrientation(){
        return orientation;
    }

    /** Returns the starting X position.
     * @return An integer index of the starting X position.
     */    
    public int getStartX(){
        return xPosition;
    }

    /** Returns the starting Y position.
     * @return An integer index of the starting Y position.
     */    
    public int getStartY(){
        return yPosition;
    }

    /** Returns the length of this helix.
     * @return An integer length of the helix.
     */    
    public int getLength(){
        return helixLength;
    }

    public void setEnergy(double energy){
        helixEnergy = energy;
    }

    public double getEnergy(){
        return helixEnergy;
    }

    /** Test if two helices are equivalent.  Two helices are equivalent iff they have
     * the same starting X and Y positions, length, and orientation.
     * @param o The other helix to be compared to.
     * @return True if equivalent, false otherwise.
     */    
    @Override
    public boolean equals(Object o){
        if (o instanceof Helix){
            Helix other = (Helix)o;
            if ((this.xPosition == other.xPosition) &&
                (this.yPosition == other.yPosition) &&
                (this.helixLength == other.helixLength) &&
                (this.orientation == other.orientation)){
                    return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * Returns pair representation of 3' range, inclusive.  NOTE that
     * although values are stored zero-based like array indices, the
     * pair representations are one-based (as they might appear when
     * described in a file).  This is because, in files, 0 might have
     * special meaning.
     */
    public void get3PrimeRange(SortedPair sp) {
        sp.setValues(xPosition + helixLength, xPosition + 1);
    }

    /**
     * Returns pair representation of 5' range, inclusive.  NOTE that
     * although values are stored zero-based like array indices, the
     * pair representations are one-based (as they might appear when
     * described in a file).  This is because, in files, 0 might have
     * special meaning.
     */
    public void get5PrimeRange(SortedPair sp) {
        sp.setValues(yPosition + 1 - helixLength + 1, yPosition + 1);
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
    private int orientation;
    private double helixEnergy;
    private HashSet<String> tags; // created on demand
}
