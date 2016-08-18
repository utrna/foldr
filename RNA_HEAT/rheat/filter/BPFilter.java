/*
 * BPFilter.java
 *
 * Created on February 21, 2003, 2:28 PM
 */

package rheat.filter;

import rheat.base.*;

import java.util.ArrayList;
import java.util.BitSet;

/** This is a filter that finds base pairs in a RNA.  This must be the first filter
 * to be applied to any RNA.  Currently this filter only finds A-U, C-G, and G-U
 * basepairs.
 * @author Team MatriX
 */
public class BPFilter
extends rheat.filter.Filter {

    public BPFilter() {
        int[] defaultrule = {BasepairType.AU, BasepairType.CG, BasepairType.GU};
        bprules = BasepairType.returnBasepairTypes(defaultrule);
    }

    public BitSet getBasePairs() {
        return bprules;
    }

    @Override
    public void applyConstraint(RNA rna) {
        ArrayList sequence = rna.getSequence();
        boolean[][] bp = new boolean[sequence.size()][sequence.size()];
        for (int i = 0; i < sequence.size(); i++) {
            for (int j = 0; j < i; j++) {
                String a = ((String)sequence.get(i)).toLowerCase();
                String b = ((String)sequence.get(j)).toLowerCase();
                int r = BasepairType.getBasepairType(a, b);
                if (bprules.get(r)) {
                    bp[i][j] = true;
                }
            }
        }
        rna.setBasePairs(bp); // clears and rebuilds helices
    }

    @Override
    public void removeConstraint(RNA rna) {
        AppMain.log(AppMain.ERROR, "Unable to Undo a base-pair setting; just apply a new one.");
    }

    public void setArguments(int[] array) {
        bprules = BasepairType.returnBasepairTypes(array);
    }

    private BitSet bprules;

}
