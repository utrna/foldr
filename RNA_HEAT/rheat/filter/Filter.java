/*
 * Filter.java
 *
 * Created on April 4, 2003, 2:43 PM
 */

package rheat.filter;

import rheat.base.Helix;
import rheat.base.RNA;
import java.util.Iterator;

/**
 * Common base class for filter types.
 * @author  guru
 */
public abstract class Filter {

    /**
     * Modify the RNA data appropriately for the filter.
     * Typically this means iterating over all helices and adding
     * tags that are appropriate for the constraint subclass.
     */
    public abstract void applyConstraint(RNA rna);

    /**
     * Remove any constraint-applied effects from the given RNA.
     * Typically this means iterating over all helices and removing
     * tags that were added by applyConstraint().
     */
    public abstract void removeConstraint(RNA rna);

    /**
     * Utility for implementing removeConstraint(); iterates over
     * all helices in an RNA and calls removeTag() with the given
     * tag name(s).
     */
    protected void removeTagsAllPredictedHelices(RNA rna, String... tagNames) {
        Iterator<Helix> iter = rna.getPredictedHelices().iterator();
        while (iter.hasNext()) {
            Helix h = (Helix)(iter.next());
            for (String tagName : tagNames) {
                h.removeTag(tagName);
            }
        }
    }
    protected void removeTagAllPredictedHelices(RNA rna, String tagName) {
        removeTagsAllPredictedHelices(rna, tagName);
    }

}
