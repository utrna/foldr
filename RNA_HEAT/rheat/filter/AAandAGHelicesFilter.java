/*
 * AAandAGHelicesFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * Finds helices which have AA or AG at their ends.
 * Note that this looks outside the normal boundaries
 * of a helix.
 *
 * @author  TEAM MATRIX
 */
public class AAandAGHelicesFilter
extends rheat.filter.Filter {

    public AAandAGHelicesFilter() {
    }

    @Override
    public void applyConstraint(RNA rna) {
        Iterator itr = rna.getHelices().iterator();
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
            

             if (Selected) {
                 h.addTag(Helix.InternalTags.TAG_MATCH_AA_AG);
             } else {                     
                 h.removeTag(Helix.InternalTags.TAG_MATCH_AA_AG);
             }
        }
    }

    @Override
    public void removeConstraint(RNA rna) {
        removeTagAllPredictedHelices(rna, Helix.InternalTags.TAG_MATCH_AA_AG);
    }

}
