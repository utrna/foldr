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
        HelixStore hs = rna.getHelices();
        ArrayList sequence = rna.getSequence();        
        boolean containsAUG = false, containsAAG = false, isELoop = false, noException;
        String a = " ", b = " ", c = " ", d = " ";              
        
        Iterator itr = hs.iterator();
        while(itr.hasNext()){
            isELoop = false;
            Helix h = (Helix)itr.next();
            HelixInfo hi = new HelixInfo(h, rna);                        
                      
            noException = true;
                    try {
                    // check for AAG goin into 3' end
                        a = ((String)sequence.get(hi.get3PrimeEnd() + 1)).toLowerCase();
                        b = ((String)sequence.get(hi.get3PrimeEnd() + 2)).toLowerCase();
                        c = ((String)sequence.get(hi.get3PrimeEnd() + 3)).toLowerCase();
                        } catch (Exception e) { noException = false; }
                    
                        if (noException && a.equals("g") && b.equals("a") && c.equals("a") )
                            containsAAG = true;

                    noException = true;
                    try {
                    // check for AUG coming out of 5' Start                  
                        a = ((String)sequence.get(hi.get5PrimeStart() - 1)).toLowerCase();
                        b = ((String)sequence.get(hi.get5PrimeStart() - 2)).toLowerCase();
                        c = ((String)sequence.get(hi.get5PrimeStart() - 3)).toLowerCase();
                        } catch (Exception e) { noException = false; }
                        
                    if ( noException && a.equals("a") && b.equals("u") && c.equals("g") )
                            containsAUG = true;

                        
                    if (containsAUG && containsAAG)
                            isELoop = true;
                        
                    
                ////////the other end of the helix/////////////
                    containsAUG = false;
                    containsAAG = false;
                    
                    noException = true;
                    try {                    
                    // check for GAA coming out of 3' start
                            a = ((String)sequence.get(hi.get3PrimeStart() - 1)).toLowerCase();
                            b = ((String)sequence.get(hi.get3PrimeStart() - 2)).toLowerCase();
                            c = ((String)sequence.get(hi.get3PrimeStart() - 3)).toLowerCase();
                            } catch (Exception e) { noException = false; }
                            
                    if (noException && a.equals("g") && b.equals("a") && c.equals("a") )
                                containsAAG = true;

                    noException = true;                    
                    try {                    
                    // check for AUG coming out of 5' end                            
                            a = ((String)sequence.get(hi.get5PrimeEnd() + 1)).toLowerCase();
                            b = ((String)sequence.get(hi.get5PrimeEnd() + 2)).toLowerCase();
                            c = ((String)sequence.get(hi.get5PrimeEnd() + 3)).toLowerCase();
                            } catch (Exception e) { noException = false; }
                            if (noException && a.equals("a") && b.equals("u") && c.equals("g") )
                                containsAUG = true;                                                    

                    
                if (containsAUG && containsAAG)
                            isELoop = true;
            
            if (isELoop) {
                h.addTag(Helix.InternalTags.TAG_MATCH_E_LOOP);
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
