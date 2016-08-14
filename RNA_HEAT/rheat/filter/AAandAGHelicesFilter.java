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
                    b = ((String)sequence.get(hi.get3PrimeEnd() + 1)).toLowerCase();
                } catch (Exception e) { atStart = true;}

            try {
                    c = ((String)sequence.get(hi.get5PrimeEnd() + 1)).toLowerCase();
                    d = ((String)sequence.get(hi.get3PrimeStart() - 1)).toLowerCase();
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
