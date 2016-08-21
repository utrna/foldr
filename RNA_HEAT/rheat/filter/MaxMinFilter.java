/*
 * MaxMinFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * Finds helices with length in the given range.
 *
 * @author  TEAM MATRIX
 */
public class MaxMinFilter
extends rheat.filter.Filter {

    private int MaxLength;
    private int MinLength;

    public MaxMinFilter() {
        MaxLength = Integer.MAX_VALUE;
        MinLength = 1;
    }

    public int getMaxLength() {
        return this.MaxLength;
    }

    public int getMinLength() {
        return this.MinLength;
    }

    @Override
    public void applyConstraint(RNA rna) {
        String constraintDesc = String.format("%.2f:%.2f", getMaxLength(), getMinLength());
        Iterator itr = rna.getHelices().iterator();
        while (itr.hasNext()) {
            Helix h = (Helix)itr.next();
            if ((h.getLength() >= MinLength) && (h.getLength() <= MaxLength)) {
                h.addTag(Helix.InternalTags.TAG_MATCH_LENGTH, constraintDesc);
            } else {
                h.removeTag(Helix.InternalTags.TAG_MATCH_LENGTH);
            }
        }
    }

    @Override
    public void removeConstraint(RNA rna) {
        removeTagAllPredictedHelices(rna, Helix.InternalTags.TAG_MATCH_LENGTH);
    }

/** Sets Arguments for the Filter
 * @param max The Maximum Length of Helix
 * @param min The Minimum Length of Helix
 */    
    public void setArguments(int max, int min) {
        MaxLength = max;
        MinLength = min;
    }

}
