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

    // TODO: range-specific interfaces (e.g. iterator with
    // suggested nucleotide boundaries, returning a subset)

}
