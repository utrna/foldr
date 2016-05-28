/*
 *
 * NonDiagonalHelicesFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

/** This Filter outputs helices with length in a given range
 * 
 */    


package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 *
 * @author  TEAM MATRIX
 */

public class NonDiagonalHelicesFilter
extends rheat.filter.Filter {

    private int rangeMin, rangeMax;
    
    /** Creates a new instance of NonDiagonalHelicesFilter */
    public NonDiagonalHelicesFilter() {
        rangeMax = Integer.MAX_VALUE;
        rangeMin = 0;
    }
    
    
    public RNA apply(RNA rna){
        HelixStore hs = rna.getHelices();
        HelixGrid hg = new HelixGrid(rna.getSequence().size());
        Iterator itr = hs.iterator();
        while(itr.hasNext()){
            Helix h = (Helix)itr.next();
            if (!( ( ( h.getStartX() - h.getStartY() ) <= rangeMax) && ( ( h.getStartX() - h.getStartY() ) >= rangeMin) ))
            {
                hg.addHelix(h);
            }
        }
        rna.setHelices(hg);
        return rna;
    }    
 
    
/** Sets Arguments for the Filter
 * @param range The distance (range) between both positions that are 
 * Base Paired
 */

    public void setArguments(int RangeMax, int RangeMin){
        rangeMin = RangeMin;
        rangeMax = RangeMax;
    }
}
