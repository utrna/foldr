/*
 *
 * BasePairRangeHelicesFilter.java
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

public class BasePairRangeHelicesFilter implements Filter {
    
    private int rangeMin, rangeMax;
    
    /** Creates a new instance of BasePairRangeHelicesFilter */
    public BasePairRangeHelicesFilter() {
        rangeMax = Integer.MAX_VALUE;
        rangeMin = 0;
    }
    
 /** 
  * @param rna The Input RNA
  * @return Modified RNA after application of this filter.    
  */  
/*
    public RNA apply(RNA rna) {
        ArrayList sequence = rna.getSequence();
        boolean[][] bp_temp = new boolean[sequence.size()][sequence.size()];            
        boolean[][] bp_master = new boolean[sequence.size()][sequence.size()];                    
        bp_master = rna.getBasePairs(); 
        
        // Creating a temp copy of base-pairs, to be used in the function
        for (int i = 0; i < rna.getBasePairs().length; ++i) {
            System.arraycopy(bp_master[i], 0, bp_temp[i], 0, rna.getBasePairs().length);                        
        }
        
        HelixStore OldStore = rna.getHelices();        
        HelixStore hstore = new HelixGrid(sequence.size()); 
        //helices = rna.getHelices();
        int helixCount = -1;
        for (int i = 0; i< (sequence.size()-1); ++i){
            for (int j = 0; j < i; ++j){
                if (bp_temp[i][j] == true){
                    int temp_i = i;
                    int temp_j = j;
                    int helixLength = 0;
                    int startX = i;
                    int startY = j;
                    do{
                        bp_temp[temp_i][temp_j] = false;
                        ++temp_i; --temp_j; 
                        ++helixLength;
                        if (temp_i==sequence.size() || (temp_j<0))
                            break;
                    } while((bp_temp[temp_i][temp_j] == true));                                         
                    
                    if ( ( (startX - startY) <= rangeMax ) && ( (startX - startY) >= rangeMin ) )
                    {
                        Helix h = new Helix(startX, startY, helixLength);
                        if (OldStore.hasHelix(h)) {
                            ++helixCount;
                            hstore.addHelix(h);
                        }
                    }
                }
            }//j
        }//i        
        System.out.println("\nTotal Number of Helices: " + helixCount);
        rna.setHelices(hstore);
        System.gc();
        return rna;                   
    } // apply
 
*/
    public RNA apply(RNA rna){
        HelixStore hs = rna.getHelices();
        HelixGrid hg = new HelixGrid(rna.getSequence().size());
        Iterator itr = hs.iterator();
        while(itr.hasNext()){
            Helix h = (Helix)itr.next();
            if ( ( ( h.getStartX() - h.getStartY() ) <= rangeMax) && ( ( h.getStartX() - h.getStartY() ) >= rangeMin) )
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

    public void setArguments(int RangeMin, int RangeMax){
        rangeMin = RangeMin;
        rangeMax = RangeMax;
    }
}
