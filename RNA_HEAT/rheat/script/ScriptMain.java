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
     * @throws ScriptException with specified exception’s details
     */
    static public void rethrowAsScriptException(Exception e) throws ScriptException {
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
    public void addEnergyFilter(float maxEnergy, float minEnergy) throws ScriptException {
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
     * Script interface for AppMain.eachActualHelix().
     *
     * Returns an object describing the helix range, which can be
     * used to find the count and to iterate.
     */
    public ScriptIteration<ScriptHelix> eachActualHelix() throws ScriptException {
        ScriptIteration<ScriptHelix> result = null;
        try {
            final RNA sourceRNA = appMain.rnaData;
            final HelixStore actualHelices = sourceRNA.getActual();
            result = new ScriptIteration<ScriptHelix>(new HelixStoreIterationDelegate(actualHelices, sourceRNA));
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for AppMain.eachPredictedHelix().
     *
     * Returns an object describing the helix range, which can be
     * used to find the count and to iterate.
     */
    public ScriptIteration<ScriptHelix> eachPredictedHelix() throws ScriptException {
        ScriptIteration<ScriptHelix> result = null;
        try {
            final RNA sourceRNA = appMain.rnaData;
            HelixStore predictedHelices = sourceRNA.getHelices(); // FIXME: bad name
            result = new ScriptIteration<ScriptHelix>(new HelixStoreIterationDelegate(predictedHelices, sourceRNA));
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for AppMain.getSelectedHelix().
     *
     * Returns an object representing the selected helix.
     */
    public ScriptHelix getSelectedHelix() throws ScriptException {
        ScriptHelix result = null;
        try {
            Helix rawHelix = appMain.getSelectedHelix();
            // TODO: maybe return RNA as well, in case it can be different?
            result = new ScriptHelix(rawHelix, appMain.rnaData);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for AppMain.log().  See discussion for the
     * non-static version of this method below.
     */
    static public void _log(int messageType, String text) throws ScriptException {
        try {
            AppMain.log(messageType, text);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Although log() could be a static method, this non-static
     * version is defined so that "rheat.log()" syntax works.  In
     * the older Rhino JavaScript engine, rheat.log() would find the
     * static method.  In the newer Nashorn engine (Java 1.8+), it
     * does NOT find the static method so a special non-static
     * version is needed.
     */
    public void log(int messageType, String text) throws ScriptException {
        _log(messageType, text);
    }

    /**
     * Script interface for AppMain.msDelay().
     */
    public void msDelay(int millisecondCount) throws ScriptException {
        try {
            appMain.msDelay(millisecondCount);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.makePath().
     */
    public String makePath(String... elements) throws ScriptException {
        String result = null;
        try {
            result = appMain.makePath(elements);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
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
     * Script interface for AppMain.openOverlayRNA().
     */
    public void openOverlayRNA(String filePath, String color) throws ScriptException {
        try {
            // the color string can be in a format supported by decoding,
            // such as a triplet of RGB hexadecimal values
            java.awt.Color asColorObject = java.awt.Color.decode(color);
            appMain.openOverlayRNA(filePath, asColorObject);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.openTags().
     */
    public void openTags(String filePath) throws ScriptException {
        try {
            appMain.openTags(filePath);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.setHelixTagColor().
     */
    public void setHelixTagColor(String tag, String color) throws ScriptException {
        try {
            // the color string can be in a format supported by decoding,
            // such as a triplet of RGB hexadecimal values
            java.awt.Color asColorObject = java.awt.Color.decode(color);
            appMain.setHelixTagColor(tag, asColorObject);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.setHelixTagsVisible().
     */
    public void showHelixTag(String tag) throws ScriptException {
        showHelixTags(tag); // alias
    }
    public void showHelixTags(String... tags) throws ScriptException {
        try {
            appMain.setHelixTagsVisible(true, tags);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.setHelixTagsVisible().
     */
    public void hideHelixTag(String tag) throws ScriptException {
        hideHelixTags(tag); // alias
    }
    public void hideHelixTags(String... tags) throws ScriptException {
        try {
            appMain.setHelixTagsVisible(false, tags);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.scrollTo().
     */
    public void scrollTo(int x, int y) throws ScriptException {
        try {
            appMain.scrollTo(x, y);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.zoomTo().
     */
    public void zoomTo(double level) throws ScriptException {
        try {
            appMain.zoomTo(level);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.newExperiment().
     */
    public void newExperiment() throws ScriptException {
        try {
            appMain.newExperiment();
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.runProgram().
     */
    public int runProgram(String... arguments) throws ScriptException {
        int result = -1;
        try {
            result = appMain.runProgram(arguments);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
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
     * Script interface for AppMain.getWorkingDir().
     */
    public String getWorkingDir() throws ScriptException {
        String result = null;
        try {
            result = appMain.getWorkingDir();
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for AppMain.setWorkingDir().
     */
    public void setWorkingDir(String newDir) throws ScriptException {
        try {
            appMain.setWorkingDir(newDir);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for AppMain.setWorkingDirToPrevious().
     */
    public void setWorkingDirToPrevious() throws ScriptException {
        try {
            appMain.setWorkingDirToPrevious();
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

    /**
     * Script interface for AppMain.setTemporaryPreference().
     *
     * Allows temporary overrides to preferences (that will not be
     * saved), which can be useful in scripts.  The keys should match
     * any that would appear in "~/.rheat/prefs.js".  See also the
     * method getWorkingDir(), which can be useful when overriding
     * values.
     */
    public void setTemporaryPreference(String key, String value) throws ScriptException {
        try {
            appMain.setTemporaryPreference(key, value);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

}
