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
        Iterator itr = rna.getPredictedHelices().iterator();
        boolean atStart = false, atEnd = false, Selected = false;        
        char a = ' ', b = ' ', c = ' ', d = ' ';        

        String sequence = rna.getSequence();
        
        while(itr.hasNext()){
            Helix h = (Helix)itr.next();

            Selected = false;
            atStart = false;
            atEnd = false;

            //Check for AU or AA at the end of the helix
            try {
                    a = Character.toLowerCase(rna.getSequenceAt(h.get5PrimeStart() - 1));
                    b = Character.toLowerCase(rna.getSequenceAt(h.get3PrimeEnd() + 1));
                } catch (Exception e) { atStart = true;}

            try {
                    c = Character.toLowerCase(rna.getSequenceAt(h.get5PrimeEnd() + 1));
                    d = Character.toLowerCase(rna.getSequenceAt(h.get3PrimeStart() - 1));
                } catch (Exception e) { atEnd = true;}
              
                    
             if (!atStart) {
                        if ( ((a == 'a') && (b == 'a')) ||
                             ((a == 'a') && (b == 'g')) ||
                             ((a == 'g') && (b == 'a')) ) {
                                Selected = true;
                        }
                }

             if (!atEnd) {
                        if ( ((c == 'a') && (d == 'a')) ||
                             ((c == 'a') && (d == 'g')) ||
                             ((c == 'g') && (d == 'a')) ) {
                                Selected = true;
                        }
                }            
            

             if (Selected) {
                 h.addTag(Helix.InternalTags.TAG_MATCH_AA_AG, null);
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
