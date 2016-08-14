/*
 *
 * MaxMinFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

/** This Filter outputs helices with length in the range MinLength and 
 * MaxLength
 */    



package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 *
 * @author  TEAM MATRIX
 */

public class MaxMinFilter
extends rheat.filter.Filter {

    private int MaxLength;
    private int MinLength;

    /** Creates a new instance of MaxMinFilter */
    public MaxMinFilter() {
        MaxLength = Integer.MAX_VALUE;
        MinLength = 1;
    }

    public int getMaxLength() {
        return this.MaxLength;
    }

    public int getMinLength() {
        return this.MinLength;
    }

    public RNA apply(RNA rna){
        HelixStore hs = rna.getHelices();
        HelixGrid hg = new HelixGrid(rna.getSequence().size());
        Iterator itr = hs.iterator();
        while(itr.hasNext()){
            Helix h = (Helix)itr.next();
            if ((h.getLength() >= MinLength) && (h.getLength() <= MaxLength)){
                hg.addHelix(h);
            }
        }
        rna.setHelices(hg);
        return rna;
    }
    
/** Sets Arguments for the Filter
 * @param max The Maximum Length of Helix
 * @param min The Minimum Length of Helix
 */    
    public void setArguments(int max, int min) {
        MaxLength = max;
        MinLength = min;
    }
}
