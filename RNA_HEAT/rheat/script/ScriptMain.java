package rheat.script;

import rheat.base.*;
import rheat.filter.*;
import rheat.GUI.RheatApp;

import java.util.*;
import javax.script.*;

/**
 * The "main" object type, from a scripting point of view.
 *
 * Since it is possible to run the program without using the GUI,
 * any GUI-dependent methods of this class will throw exceptions
 * when there is no GUI.  Methods should be divided in such a way
 * that they are exclusively GUI-dependent or independent.
 *
 * The classes in the "rheat.script" package clearly state the
 * features that are meant to be used by scripts.  Please use
 * only those interfaces (even though technically it is possible
 * to force Java to expose other objects).
 *
 * @author Kevin Grant
 */
public class ScriptMain {

    private AppMain appMain = null;

    public static final int INFO = AppMain.INFO;
    public static final int WARN = AppMain.WARN;
    public static final int ERROR = AppMain.ERROR;



    /**
     * Throws the specified exception as a ScriptException,
     * enveloping all of its stack-trace and message details.
     * @throw ScriptException with specified exception’s details
     */
    static private void rethrowAsScriptException(Exception e) throws ScriptException {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage());
        sb.append("\n");
        int lineNumber = 0;
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n");
            // arbitrarily stop after so many lines (the traces
            // tend to repeat and they have many more implementation
            // details than necessary)
            if (lineNumber > 7) {
                sb.append("...\n");
                break;
            }
            ++lineNumber;
        }
        throw new ScriptException(sb.toString());
    }

    /**
     * Creates a scripting interface for the given object, which
     * may or may not have an associated GUI.
     * @param appMain primary application object
     */
    public ScriptMain(AppMain appMain) {
        this.appMain = appMain;
    }

    /**
     * Script interface for AppMain.addFilter() using an AA/AG
     * filter object.
     *
     * Applies a new filter to show only E loop helices.
     *
     * NOTE: Should be consistent with ScriptFilterInterpreter.
     */
    public void addAAandAGFilter() throws ScriptException {
        try {
            AAandAGHelicesFilter newFilter = new AAandAGHelicesFilter();
            //newFilter.setArguments(...);
            appMain.addFilter(newFilter);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.addFilter() using a base pair
     * filter object.
     *
     * Applies a new filter to show only the specified helices.
     * Each base pair is identified by a string of two characters:
     * "AA", "AC", "AG", "AU", "CC", "CG", "CU", "GG", "GU" or "UU"
     * (or any reversal of these two-character combinations).  Any
     * other strings in the list will trigger an exception.
     *
     * NOTE: Should be consistent with ScriptFilterInterpreter.
     */
    public void addBasePairFilter(String... bpArgs) throws ScriptException {
        try {
            // translate two-character strings into constants
            int[] filterArgs = new int[bpArgs.length];
            for (int i = 0; i < bpArgs.length; ++i) {
                String pairChars = bpArgs[i];
                filterArgs[i] = -1;
                if (pairChars.length() == 2) {
                    // this call returns -1 on failure:
                    filterArgs[i] = BasepairType.getBasepairType(bpArgs[i].charAt(0), bpArgs[i].charAt(1));
                }
                if (filterArgs[i] == -1) {
                    throw new ScriptException("expected '" + pairChars + "' to be a two-character base pair identifier");
                }
            }
            BPFilter newFilter = new BPFilter();
            newFilter.setArguments(filterArgs);
            appMain.addFilter(newFilter);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.addFilter() using a complex
     * filter object.
     *
     * NOTE: Should be consistent with ScriptFilterInterpreter.
     */
    public void addComplexFilter(int complexDistance, int simpleDistance) throws ScriptException {
        try {
            ComplexFilter newFilter = new ComplexFilter();
            newFilter.setArguments(complexDistance, simpleDistance);
            appMain.addFilter(newFilter);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.addFilter() using a diagonal
     * distance filter object.
     *
     * NOTE: Should be consistent with ScriptFilterInterpreter.
     */
    public void addDiagonalDistanceFilter(int rangeMax, int rangeMin) throws ScriptException {
        try {
            BasePairRangeHelicesFilter newFilter = new BasePairRangeHelicesFilter();
            newFilter.setArguments(rangeMax, rangeMin);
            appMain.addFilter(newFilter);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.addFilter() using an E loop
     * filter object.
     *
     * Applies a new filter to show only E loop helices.
     *
     * NOTE: Should be consistent with ScriptFilterInterpreter.
     */
    public void addELoopFilter() throws ScriptException {
        try {
            ELoopHelicesFilter newFilter = new ELoopHelicesFilter();
            //newFilter.setArguments(...);
            appMain.addFilter(newFilter);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.addFilter() using an energy
     * filter object.
     *
     * Applies a new filter to show helices with certain energies.
     *
     * NOTE: Should be consistent with ScriptFilterInterpreter.
     */
    public void addEnergyFilter(float minEnergy, float maxEnergy) throws ScriptException {
        try {
            EnergyMaxMinFilter newFilter = new EnergyMaxMinFilter();
            newFilter.setArguments(maxEnergy, minEnergy);
            appMain.addFilter(newFilter);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.addFilter() using a helix
     * length filter object.
     *
     * NOTE: Should be consistent with ScriptFilterInterpreter.
     */
    public void addHelixLengthFilter(int maxLength, int minLength) throws ScriptException {
        try {
            MaxMinFilter newFilter = new MaxMinFilter();
            newFilter.setArguments(maxLength, minLength);
            appMain.addFilter(newFilter);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.removeFilters().
     *
     * Removes filters so that all helices are visible.
     */
    public void removeFilters() throws ScriptException {
        try {
            appMain.removeFilters();
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.log().
     */
    static public void log(int messageType, String text) throws ScriptException {
        try {
            AppMain.log(messageType, text);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.openRNA().
     */
    public void openRNA(String filePath) throws ScriptException {
        try {
            appMain.openRNA(filePath);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.runScript().
     */
    public void runScript(String filePath) throws ScriptException {
        try {
            appMain.runScript(filePath);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.setPreference().
     */
    public void setPreference(String key, String value) throws ScriptException {
        try {
            appMain.setPreference(key, value);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

}
