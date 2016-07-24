/*
 * HelixGrid.java
 *
 *
 *
 * Created on February 27, 2003, 6:44 PM
 */

package rheat.base;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
//sequence
import java.util.ArrayList;
////


/** HelixGrid is an implementation of a HelixStore.  Its responsibility is to store
 * a set of helices that are currently in an RNA that has been generated due to
 * various applications of Filters.
 * <p>
 * HelixGrid stores the helices in a 2D array.  The first array are the rows, and
 * the second array are the columns.  If there is no helix at a given point, then a
 * null is present instead of a Helix.  For basepairs that are of the same helix,
 * then this helix is stored repeatedly for each basepair.
 * @author Team MatriX
 */
public class HelixGrid implements HelixStore {
    
    /** Creates a new instance of HelixGrid by passing it the length of the RNA
     * sequence.  It then constructs a (size x size) array where every possible
     * position against another is available.
     * <p>
     * Therefore, given any basepair, the HelixGrid is able to generate the helix that
     * this basepair belongs to (if any) and the starting position and ending position
     * and length of this helix.
     * @param size The length of the RNA sequence.
     */
    public HelixGrid(int size) {
        _store = new Helix[size][size];
        numHelix = 0;
        helices = new ArrayList<Helix>();
    }
    
    /** Add a Helix to the HelixStore.
     * @param h A Helix to be added to the Helix Grid
     */    
    public void addHelix(Helix h) {
        int x = h.getStartX();
        int y = h.getStartY();
        int length = h.getLength();
        helices.add(h);
        do {
            _store[x][y] = h;
            x++; y--; length--;
        } while (length > 0);
        numHelix++;
    }
    
    /** Test to see if a Helix is already present in this HelixGrid.
     * @param h Helix to be tested for its presence
     * @return True if found, false otherwise.
     */    
    public boolean hasHelix(Helix h) {
        return helices.contains(h);
    }
    
    /** This method outputs helices 
     * @param ps The PrintStream where the output should be generated to.
     * @return Number Of Helices in the range of lengths.
     */
    public int debugOutput(PrintStream ps, ArrayList seq){
        String a = " ";        
        Helix PresentHelix;
        int Startx, Starty;
        int PresentHelixStartX=0, PresentHelixStartY=0;
        int NumberOfHelices = 0;
        for (int i = 0; i < _store.length; i++){
            a = ((String)seq.get(i)).toUpperCase();    //printing the sequence        
            ps.print( a + " "); //for printing the sequence            
            for (int j = 0; j <= i; j++){
                PresentHelix = _store[i][j];
                if (PresentHelix != null){
                    Startx = PresentHelix.getStartX();
                    Starty = PresentHelix.getStartY();
                    int HelixLength = PresentHelix.getLength();
                        ps.print(PresentHelix.getLength() + " ");
                        if ((Startx != PresentHelixStartX) && (Starty != PresentHelixStartY)){
                            //Checking if Its the same helix. If it is different, increment counter
                            // and reassign identifier values.
                            ++NumberOfHelices;
                            PresentHelixStartX = Startx;
                            PresentHelixStartY = Starty;
                        }
                }
                else {
                    ps.print("0 ");
                }
            }
            ps.println();
        }
                
        //printing horizontal sequence//
            ps.print(" ");
            ps.print(" ");
            for (int i = 0; i < _store.length; i++){
                a = ((String)seq.get(i)).toUpperCase();    //printing the sequence        
                ps.print( a + " "); //for printing the sequence            
            }
        ///////////
            ps.println();        
        return NumberOfHelices;
    }  

    
    /**
     *
     */
    public Iterator<Helix> iterator() {
        return java.util.Collections.unmodifiableCollection(helices).iterator();
    }
    
    public int getCount(){
        return numHelix;
    }
    
    public int getLength(){
        return _store.length;
    }

    Helix[][] _store;
    ArrayList<Helix> helices;
    int numHelix;
}
