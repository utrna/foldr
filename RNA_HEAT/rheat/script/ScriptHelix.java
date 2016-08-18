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

    /**
     * Creates a scripting interface for the given object.
     * @param helix a raw helix data structure
     * @param sourceRNA where the helix comes from
     */
    public ScriptHelix(Helix helix, RNA sourceRNA) {
        this.rawHelix = helix;
        this.sourceRNA = sourceRNA;
    }

    /**
     * Script interface for Helix.addTag().
     */
    public void addTag(String tagName) throws ScriptException {
        try {
            rawHelix.addTag(tagName);
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for Helix.eachTag().
     *
     * Returns an object describing the tag range, which can be
     * used to find the count and to iterate.  ORDER UNSPECIFIED.
     */
    public ScriptIteration<String> eachTag() throws ScriptException {
        ScriptIteration<String> result = null;
        try {
            Set<String> tags = rawHelix.getTags(); // may be null
            result = new ScriptIteration<String>(new StringSetIterationDelegate(tags));
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
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

    /**
     * Script interface for HelixInfo.get5PrimeEnd().
     */
    public int fivePrimeEnd() throws ScriptException {
        int result = 0;
        try {
            result = getHelixInfo().get5PrimeEnd();
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for HelixInfo.get5PrimeSequence().
     */
    public String fivePrimeSequence() throws ScriptException {
        String result = "";
        try {
            result = getHelixInfo().get5PrimeSequence();
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for HelixInfo.get5PrimeStart().
     */
    public int fivePrimeStart() throws ScriptException {
        int result = 0;
        try {
            result = getHelixInfo().get5PrimeStart();
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for quick tag queries.
     *
     * Returns true if the eachTag() iteration contains the
     * given string, in a search that is faster than manual
     * iteration would be.
     */
    public boolean hasTag(String tag) throws ScriptException {
        boolean result = false;
        try {
            Set<String> tags = rawHelix.getTags();
            if (tags != null) {
                result = tags.contains(tag);
            }
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Deprecated name for hasTag().  Maintained for compatibility
     * with older scripts but this could be removed at some point.
     */
    public boolean tagsInclude(String tag) throws ScriptException {
        ScriptMain.deprecationWarning("tagsInclude", "hasTag");
        return hasTag(tag);
    }

    /**
     * Script interface for HelixInfo.get3PrimeEnd().
     */
    public int threePrimeEnd() throws ScriptException {
        int result = 0;
        try {
            result = getHelixInfo().get3PrimeEnd();
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for HelixInfo.get3PrimeSequence().
     */
    public String threePrimeSequence() throws ScriptException {
        String result = "";
        try {
            result = getHelixInfo().get3PrimeSequence();
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for HelixInfo.get3PrimeStart().
     */
    public int threePrimeStart() throws ScriptException {
        int result = 0;
        try {
            result = getHelixInfo().get3PrimeStart();
        } catch (Exception e) {
            ScriptMain.rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Since there is a small cost to finding helix info
     * (constructing sequences, etc.), only do it when it
     * is actually required for something.
     */
    private HelixInfo getHelixInfo() {
        if (helixInfo == null) {
            helixInfo = new HelixInfo(rawHelix, sourceRNA);
        }
        return helixInfo;
    }

    private Helix rawHelix = null;
    private HelixInfo helixInfo = null;
    private RNA sourceRNA = null;

}
