/*
 *
 * AAandAGHelicesFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

/** This Filter outputs helices which have AA or AG at their ends.
 */    


package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 *
 * @author  TEAM MATRIX
 */

public class AAandAGHelicesFilter
extends rheat.filter.Filter {
        
    /** Creates a new instance of AAandAGHelicesFilter */
    public AAandAGHelicesFilter() {
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
        
        
        HelixStore hstore = new HelixGrid(sequence.size()); 
        HelixStore OldStore = rna.getHelices();
        int helixCount = -1;
        boolean atStart = false, atEnd = false, Selected = false;
        String a = " ", b = " ", c = " ", d = " ";
        for (int i = 0; i< (sequence.size()-1); ++i){            
            for (int j = 0; j < i; ++j){
                Selected = false;
                atStart = false;
                atEnd = false;
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
                    
                    //Check for AU or AA at the end of the helix
                    try {
                    a = ((String)sequence.get(startX + 1)).toLowerCase();
                    b = ((String)sequence.get(startY - 1)).toLowerCase();
                    } catch (Exception e) { atStart = true;}
                    
                    try {
                    c = ((String)sequence.get(startX + helixLength)).toLowerCase();
                    d = ((String)sequence.get(startY - helixLength)).toLowerCase();
                    } catch (Exception e) { atEnd = true;}
                    
                    if (!atStart) {
                        if ( (a.equals("a") && b.equals("a")) ||
                             (a.equals("a") && b.equals("g")) ||
                             (a.equals("g") && b.equals("a"))  )
                                Selected = true;
                    }

                    if (!atEnd) {
                        if ( (c.equals("a") && d.equals("a")) ||
                             (c.equals("a") && d.equals("g")) ||
                             (c.equals("g") && d.equals("a"))  )
                                Selected = true;
                    }

                    if (Selected){
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
        boolean atStart = false, atEnd = false, Selected = false;        
        String a = " ", b = " ", c = " ", d = " ";        

        ArrayList sequence = rna.getSequence();
        
        while(itr.hasNext()){
            Helix h = (Helix)itr.next();
            HelixInfo hi = new HelixInfo(h, rna);                        
                            
            Selected = false;
            atStart = false;
            atEnd = false;

            //Check for AU or AA at the end of the helix
            try {
                    a = ((String)sequence.get(hi.get5PrimeStart() - 1)).toLowerCase();
                    b = ((String)sequence.get(hi.get3PrimeStart() + 1)).toLowerCase();
                } catch (Exception e) { atStart = true;}

            try {
                    c = ((String)sequence.get(hi.get5PrimeEnd() + 1)).toLowerCase();
                    d = ((String)sequence.get(hi.get3PrimeEnd() - 1)).toLowerCase();
                } catch (Exception e) { atEnd = true;}
              
                    
             if (!atStart) {
                        if ( (a.equals("a") && b.equals("a")) ||
                             (a.equals("a") && b.equals("g")) ||
                             (a.equals("g") && b.equals("a"))  )
                                Selected = true;
                }

             if (!atEnd) {
                        if ( (c.equals("a") && d.equals("a")) ||
                             (c.equals("a") && d.equals("g")) ||
                             (c.equals("g") && d.equals("a"))  )
                                Selected = true;
                }            
            

             if (Selected){
                hg.addHelix(h);
             }                        

        }
        rna.setHelices(hg);
        return rna;
    }

        
    
/** Sets Arguments for the Filter
 */    
    public void setArguments(){
    }
}
