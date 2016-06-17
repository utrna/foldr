package rheat.script;

import rheat.base.*;
import rheat.filter.*;

import java.util.*;
import javax.script.*;

/**
 * The features of a helix that are available to scripts.  This
 * directly defines what is possible to access from JavaScript
 * when a helix is returned, such as the currently-selected
 * helix object.
 *
 * @author Kevin Grant
 */
public class ScriptHelix {

    private Helix rawHelix = null;



    /**
     * Creates a scripting interface for the given object.
     * @param helix a raw helix data structure
     */
    public ScriptHelix(Helix helix) {
        this.rawHelix = helix;
    }

    /**
     * Script interface for Helix.getEnergy().
     *
     * NOTE: Energy is not set unless an energy filter is applied
     * (see ScriptMain.addEnergyFilter()).
     */
    public double energy() throws ScriptException {
        double result = 0;
        try {
            result = rawHelix.getEnergy();
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for Helix.getLength().
     */
    public int length() throws ScriptException {
        int result = 0;
        try {
            result = rawHelix.getLength();
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

}
