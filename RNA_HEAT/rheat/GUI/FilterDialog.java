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
public abstract class FilterDialog extends RheatApp.RheatActionPanel {

    public FilterDialog(String title) {
        super(title);
    }

    /**
     * To be called after the dialog ends and the user has possibly
     * requested a new filter.
     * @return A new Filter subclass that contains the settings from
     * this dialog, or null to indicate that the user cancelled.
     */
    abstract public Filter getNewFilter();

}
