/*
 * ELoopHelicesFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * Identifies E-loop helices.
 *
 * @author  TEAM MATRIX
 */
public class ELoopHelicesFilter
extends rheat.filter.Filter {

    public ELoopHelicesFilter() {
    }

    @Override
    public void applyConstraint(RNA rna) {
        HelixStore hs = rna.getPredictedHelices();
        boolean containsAUG = false, containsAAG = false, isELoop = false, noException;
        char a = ' ', b = ' ', c = ' ', d = ' ';              
        
        Iterator itr = hs.iterator();
        while(itr.hasNext()){
            isELoop = false;
            Helix h = (Helix)itr.next();
            noException = true;
                    try {
                    // check for AAG goin into 3' end
                        a = Character.toLowerCase(rna.getSequenceAt(h.get3PrimeEnd() + 0));
                        b = Character.toLowerCase(rna.getSequenceAt(h.get3PrimeEnd() + 1));
                        c = Character.toLowerCase(rna.getSequenceAt(h.get3PrimeEnd() + 2));
                        } catch (Exception e) { noException = false; }
                    
                    if (noException && (a == 'g') && (b == 'a') && (c == 'a') ) {
                        containsAAG = true;
                    }

                    noException = true;
                    try {
                    // check for AUG coming out of 5' Start                  
                        a = Character.toLowerCase(rna.getSequenceAt(h.get5PrimeStart() - 0));
                        b = Character.toLowerCase(rna.getSequenceAt(h.get5PrimeStart() - 1));
                        c = Character.toLowerCase(rna.getSequenceAt(h.get5PrimeStart() - 2));
                        } catch (Exception e) { noException = false; }
                        
                    if ( noException && (a == 'a') && (b == 'u') && (c == 'g') ) {
                        containsAUG = true;
                    }

                        
                    if (containsAUG && containsAAG) {
                        isELoop = true;
                    }
                        
                    
                ////////the other end of the helix/////////////
                    containsAUG = false;
                    containsAAG = false;
                    
                    noException = true;
                    try {                    
                    // check for GAA coming out of 3' start
                        a = Character.toLowerCase(rna.getSequenceAt(h.get3PrimeStart() - 0));
                        b = Character.toLowerCase(rna.getSequenceAt(h.get3PrimeStart() - 1));
                        c = Character.toLowerCase(rna.getSequenceAt(h.get3PrimeStart() - 2));
                            } catch (Exception e) { noException = false; }
                            
                    if (noException && (a == 'g') && (b == 'a') && (c == 'a') ) {
                        containsAAG = true;
                    }

                    noException = true;                    
                    try {                    
                    // check for AUG coming out of 5' end                            
                        a = Character.toLowerCase(rna.getSequenceAt(h.get5PrimeEnd() + 0));
                        b = Character.toLowerCase(rna.getSequenceAt(h.get5PrimeEnd() + 1));
                        c = Character.toLowerCase(rna.getSequenceAt(h.get5PrimeEnd() + 2));
                            } catch (Exception e) { noException = false; }
                    if (noException && (a == 'a') && (b == 'u') && (c == 'g') ) {
                        containsAUG = true;                                                    
                    }

                    
                if (containsAUG && containsAAG) {
                    isELoop = true;
                }
            
            if (isELoop) {
                h.addTag(Helix.InternalTags.TAG_MATCH_E_LOOP, null);
            } else {
                h.removeTag(Helix.InternalTags.TAG_MATCH_E_LOOP);
            }
        }
    }

    @Override
    public void removeConstraint(RNA rna) {
        removeTagAllPredictedHelices(rna, Helix.InternalTags.TAG_MATCH_E_LOOP);
    }

}
