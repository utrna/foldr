/*
 * AllHelicesFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

/** This filter outputs 
 * all helices (length 1 or more), in antiparallel orientation only.
 */
     


package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 *
 * @author  TEAM MATRIX
 */

public class AllHelicesFilter
extends rheat.filter.Filter {
    
    /** Creates a new instance of AllHelicesFilter */
    public AllHelicesFilter() {
    }
    
 /** 
  * @param rna The Input RNA
  * @return Modified RNA after application of this filter.    
  */  
    public RNA apply(RNA rna) {
        ArrayList sequence = rna.getSequence();
        boolean[][] bp_temp = new boolean[sequence.size()][sequence.size()];            
        boolean[][] bp_master = new boolean[sequence.size()][sequence.size()];                    
        bp_master = rna.getBasePairs(); 
        
        // Creating a temp copy of base-pairs, to be used in the function
        for (int i = 0; i < rna.getBasePairs().length; ++i) {
            System.arraycopy(bp_master[i], 0, bp_temp[i], 0, rna.getBasePairs().length);                        
        }
            
        HelixStore hstore = new HelixGrid(sequence.size()); 
        //helices = rna.getHelices();
        int helixCount = -1;
        for (int i = 0; i< (sequence.size()-1); ++i){
            for (int j = 0; j < i; ++j){
                if (bp_temp[i][j] == true){
                    int temp_i = i;
                    int temp_j = j;
                    ++helixCount;
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

                    hstore.addHelix(new Helix(startX, startY, helixLength));
                }
            }//j
        }//i        
//        System.out.println("\nTotal Number of Helices: " + helixCount);
        rna.setHelices(hstore);
        System.gc();
        EnergyMaxMinFilter ef = new EnergyMaxMinFilter();
        rna = ef.apply(rna);
        return rna;
    } // apply
 
    
/** Sets Arguments for the Filter
 */    
    public void setArguments(){
    }
}
