/*
 * HelixStore.java
 *
 * Created on February 27, 2003, 6:20 PM
 */

package rheat.base;

import java.util.Iterator;

/**
 * This is a generic interface specifying the functionality of a HelixStore.  The
 * function of a HelixStore is to store a set of helices of a RNA.
 *
 * @author Team Matrix
 */
public interface HelixStore extends java.io.Serializable {

    /** Add an Helix to the HelixStore.
     * @param h Helix to be added.
     */    
    public void addHelix(Helix h);

    /** Test to see if a helix is present in the HelixStore
     * @param h Helix to be tested for its presence.
     * @return True if found, false otherwise.
     */    
    public boolean hasHelix(Helix h);

    public Iterator<Helix> iterator();

    public int getCount();

    public int getLength();
}
