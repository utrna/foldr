/*
 * Helix.java
 *
 * Created on February 27, 2003, 2:01 PM
 */

package rheat.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

/** This is the first implementation of a HelixStore.  Because if it's inefficiency
 * and difficulty with certain operations, it will not be used.  Use HelixGrid
 * instead.
 * @author Team MatriX
 */
public class HelixArray implements HelixStore {
    
    /** Creates a new instance of HelixGroup */
    public HelixArray(int size) {
        _helix = new ArrayList(size);
        for (int i = 0; i < size; i++){
            _helix.add(i, new LinkedList());
        }
        numHelix = 0;
    }
    
    /**
     * Adds a Helix to the HelixGroup.
     *
     */
    public void addHelix(Helix h){
        int x = h.getStartX();
        int y = h.getStartY();
        int length = h.getLength();
        for (int i = 0; i < length; i++){
            LinkedList l = (LinkedList)_helix.get(x + i);
            //if (!l.contains(h)){
                l.add(h);
            //}
        }
        for (int i = 0; i < length; i++){
            LinkedList l = (LinkedList)_helix.get(y + i);
            //if (!l.contains(h)){
                l.add(h);
            //}
        }
    }
    
    /**
     * Determines if this HelixGroup contains a given Helix.
     */
    public boolean hasHelix(Helix h){
        return false;
    }
    
    public Iterator iterator() {
        return null;
    }    
    
    public int getCount() {
        return numHelix;
    }
    
    public int getLength() {
        return _helix.size();
    }
    
    private ArrayList _helix;
    private int numHelix;
}