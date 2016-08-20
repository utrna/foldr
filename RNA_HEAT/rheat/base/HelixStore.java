/*
 * HelixStore.java
 *
 * Created on February 27, 2003, 6:20 PM
 */

package rheat.base;

import java.util.Iterator;

/**
 * Specifies the features that a class must have in order to
 * store the helices of an RNA.
 *
 * @author Team Matrix
 */
public interface HelixStore extends java.io.Serializable {

    /**
     * @param h the Helix to add to the store
     */
    public void addHelix(Helix h);

    /**
     * erase the helix store
     */
    public void clear();

    /**
     * @return the number of times that addHelix() has been called
     */
    public int getHelixCount();

    /**
     * @return the maximum number of Helix references that can be stored
     */
    public int getHelixStoreSize();

    /**
     * @return the length of the longest helix added using addHelix()
     * (reset to zero after a clear())
     */
    public int getMaxHelixLength();

    /**
     * @return the number of base-pairs in the original sequence
     */
    public int getSequenceLength();

    /**
     * @return true only if specified Helix is found
     */
    public boolean hasHelix(Helix h);

    /**
     * @return true only if getHelixCount() would be 0
     */
    public boolean isEmpty();

    /**
     * @return an iterator over all Helix objects in the store
     * in NO PARTICULAR ORDER
     */
    public Iterator<Helix> iterator();

    /**
     * @return an iterator with a SUGGESTED subset range (the
     * caller is still expected to verify helices before using
     * them), allowing an operation in a certain region to
     * check fewer helices overall; in NO PARTICULAR ORDER
     */
    public Iterator<Helix> iterator(SortedPair threePrimeRange,
                                    SortedPair fivePrimeRange);

}
