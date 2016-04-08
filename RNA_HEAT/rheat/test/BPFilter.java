/*
 * BPFilter.java
 *
 * Created on February 21, 2003, 2:28 PM
 */

package rheat.test;

import java.util.ArrayList;
import java.util.BitSet;

/** This is a filter that finds base pairs in a RNA.  This must be the first filter
 * to be applied to any RNA.  Currently this filter only finds A-U, C-G, and G-U
 * basepairs.
 * @author Team MatriX
 */
public class BPFilter implements Filter{
    
    /** Creates a new instance of BPFilter given an RNA.
     * @param inputRna The RNA to apply this Filter to.
     */
    public BPFilter() {
        int[] defaultrule = {BasepairType.AU, BasepairType.CG, BasepairType.GU};
        bprules = BasepairType.returnBasepairTypes(defaultrule);
    }
    
    /** Apply this filter.
     * @return A new RNA due to the result of applying this Filter.
     */    
    public RNA apply(RNA rna){
        ArrayList sequence = rna.getSequence();
        boolean[][] bp = new boolean[sequence.size()][sequence.size()];
        for (int i = 0; i < sequence.size(); i++){
            for (int j = 0; j < i; j++){
                String a = ((String)sequence.get(i)).toLowerCase();
                String b = ((String)sequence.get(j)).toLowerCase();
                int r = BasepairType.getBasepairType(a, b);
                if (bprules.get(r)){
                    bp[i][j] = true;
                }
            }
        }
        rna.setBasePairs(bp);
        return rna;
    }
    
    public void setArguments(int[] array){
        bprules = BasepairType.returnBasepairTypes(array);
    }
    
    private RNA rna;
    private BitSet bprules;
    
}
