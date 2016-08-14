/*
 * HelixGrid.java
 *
 * Created on February 27, 2003, 6:44 PM
 */

package rheat.base;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * HelixGrid is an implementation of a HelixStore.  Its responsibility is to store
 * a set of helices that are currently in an RNA that has been generated due to
 * various applications of Filters.
 *
 * HelixGrid stores the helices in a 2D array.  The first array are the rows, and
 * the second array are the columns.  If there is no helix at a given point, then a
 * null is present instead of a Helix.  For basepairs that are of the same helix,
 * then this helix is stored repeatedly for each basepair.
 *
 * @author Team MatriX
 */
public class HelixGrid implements HelixStore {

    /**
     * Creates a new instance of HelixGrid by passing it the length of the RNA
     * sequence.  It then constructs a (size x size) array where every possible
     * position against another is available.
     *
     * Therefore, given any basepair, the HelixGrid is able to generate the helix that
     * this basepair belongs to (if any) and the starting position and ending position
     * and length of this helix.
     *
     * @param size The length of the RNA sequence.
     */
    public HelixGrid(int size) {
        pairings = new Helix[size][size];
        helices = new ArrayList<Helix>();
    }

    @Override
    public void addHelix(Helix h) {
        int x = h.getStartX();
        int y = h.getStartY();
        int length = h.getLength();
        helices.add(h);
        do {
            pairings[x][y] = h;
            x++; y--; length--;
        } while (length > 0);
    }

    @Override
    public int getHelixCount() {
        return helices.size();
    }

    @Override
    public int getHelixStoreSize() {
        if (pairings.length == 0) {
            return 0;
        }
        return (pairings.length * pairings[0].length);
    }

    @Override
    public int getSequenceLength() {
        return pairings.length; // note: refers only to 1st dimension of array
    }

    @Override
    public boolean hasHelix(Helix h) {
        return helices.contains(h);
    }

    @Override
    public boolean isEmpty() {
        return helices.isEmpty();
    }

    @Override
    public Iterator<Helix> iterator() {
        return java.util.Collections.unmodifiableCollection(helices).iterator();
    }

    private Helix[][] pairings;
    private ArrayList<Helix> helices;

}
