/*
 *
 * Helix.java
 *
 * Created on February 27, 2003, 2:21 PM
 */


package rheat.test;

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
    
    /** Returns a string representing his helix.
     * @return A string representing this helix.
     */    
    public String toString(){
        String ans = "";
        ans = ans + xPosition + " " + yPosition + " " + helixLength;
        return ans;
    }
    
    private int xPosition;
    private int yPosition;
    private int helixLength;
    private int orientation;
    private double helixEnergy;
    
    
}
