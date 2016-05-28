/*
 * Filter.java
 *
 * Created on April 4, 2003, 2:43 PM
 */

package rheat.filter;

import rheat.base.RNA;

/**
 * Common base class for filter types.
 * @author  guru
 */
public abstract class Filter {

    private String _description = null;

    /**
     * Modify the RNA data appropriately for the filter.
     * @return original object with modifications
     */
    public abstract RNA apply(RNA rna);

    /**
     * Return description of filter parameters suitable for UI.
     * @return value from setDescription()
     */
    public final String getDescription() {
        return _description;
    }

    /**
     * Describe filter parameters in a way suitable for UI.
     */
    public final void setDescription(String d) {
        _description = d;
    }

}
