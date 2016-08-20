/*
 * HelixGrid.java
 *
 * Created on February 27, 2003, 6:44 PM
 */

package rheat.base;

import java.util.*;

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
        maxHelixLength = 0;
    }

    @Override
    public void addHelix(Helix h) {
        int x = h.getStartX();
        int y = h.getStartY();
        final int helixLength = h.getLength();
        helices.add(h);
        int l = helixLength;
        do {
            pairings[x][y] = h;
            ++x;
            --y;
            --l;
        } while (l > 0);
        if (this.maxHelixLength < helixLength) {
            this.maxHelixLength = helixLength;
        }
    }

    @Override
    public void clear() {
        final int seqLength = getSequenceLength();
        helices.clear();
        pairings = new Helix[seqLength][seqLength]; // clear to zero
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
    public int getMaxHelixLength() {
        return this.maxHelixLength;
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

    @Override
    public Iterator<Helix> iterator(SortedPair threePrimeRange,
                                    SortedPair fivePrimeRange) {
        if (isEmpty()) {
            // short-cut for base case
            return this.iterator();
        }
        final int xStart = Helix.getXForThreePrimeRange(threePrimeRange);
        final int yStart = Helix.getYForFivePrimeRange(fivePrimeRange);
        final int length = (threePrimeRange.getB() - threePrimeRange.getA() + 1); // should not matter which range is chosen (should be the same)
        final int xPastEnd = (xStart + length);
        final int yPastEnd = (yStart + length);
        Set<Helix> helicesInRange = new HashSet<Helix>();
        for (int i = xStart; i < xPastEnd; ++i) {
            for (int j = yStart; j < yPastEnd; ++j) {
                // the same Helix reference will occur at multiple grid
                // locations for the entire helix length; the Set ensures
                // that it only appears once in the result
                if ((i >= pairings.length) || (j >= pairings.length)) {
                    // out of range (should not happen...)
                    continue;
                }
                Helix h = this.pairings[j][i];
                if (h != null) {
                    helicesInRange.add(h);
                }
            }
        }
        return helicesInRange.iterator();
    }

    private Helix[][] pairings;
    private ArrayList<Helix> helices;
    private int maxHelixLength;

}
