/*
 * FilterDialog.java
 *
 * Created on May 26, 2016
 */

package rheat.GUI;

import javax.swing.JDialog;
import rheat.filter.Filter;

/**
 * @author Kevin Grant
 */
public interface FilterDialog {

    /**
     * Present the filter editor to the user, returning only
     * when finished.
     * @return A new Filter subclass that contains the settings from
     * this dialog, or null to indicate that the user cancelled.
     */
    public Filter run();

}
