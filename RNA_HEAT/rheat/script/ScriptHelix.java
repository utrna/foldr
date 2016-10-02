package rheat.script;

import rheat.base.*;
import rheat.filter.*;
import static rheat.script.JSUtil.*;

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
     *
     * IMPORTANT: This does not trigger a display update
     * because individual helices do not have enough
     * information to do so.  You must perform a display
     * action of some kind on the "rheat" variable to
     * see changes rendered.
     */
    public void addTag(String tagName) throws ScriptException {
        try {
            rawHelix.addTag(tagName, null);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for Helix.addTag().
     *
     * IMPORTANT: This does not trigger a display update
     * because individual helices do not have enough
     * information to do so.  You must perform a display
     * action of some kind on the "rheat" variable to
     * see changes rendered.
     */
    public void addTag(String tagName, String tagValue) throws ScriptException {
        try {
            rawHelix.addTag(tagName, tagValue);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Script interface for Helix.eachTag().
     *
     * Returns an object describing the tag range, which can be
     * used to find the count and to iterate.  ORDER UNSPECIFIED.
     * Note that this returns only keys; if a tag has a value,
     * the ScriptHelix method getTagValue() must be called.
     */
    public ScriptIteration<String> eachTag() throws ScriptException {
        ScriptIteration<String> result = null;
        try {
            Map<String, String> tags = rawHelix.getTags(); // may be null
            if (tags == null) {
                result = new ScriptIteration<String>(new StringSetIterationDelegate(new HashSet<String>()));
            } else {
                result = new ScriptIteration<String>(new StringSetIterationDelegate(tags.keySet()));
            }
        } catch (Exception e) {
            rethrowAsScriptException(e);
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
            rethrowAsScriptException(e);
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
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for Helix.get5PrimeEnd().
     */
    public int fivePrimeEnd() throws ScriptException {
        int result = 0;
        try {
            result = rawHelix.get5PrimeEnd();
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for RNA.getSequenceInRange() for helix 5' range.
     */
    public String fivePrimeSequence() throws ScriptException {
        String result = "";
        try {
            SortedPair range = new SortedPair();
            rawHelix.get5PrimeRange(range);
            result = sourceRNA.getSequenceInRange(range);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for HelixInfo.get5PrimeStart().
     */
    public int fivePrimeStart() throws ScriptException {
        int result = 0;
        try {
            result = rawHelix.get5PrimeStart();
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for Helix.getTagValue().
     */
    public String getTagValue(String key) throws ScriptException {
        String result = null;
        try {
            result = rawHelix.getTagValue(key);
        } catch (Exception e) {
            rethrowAsScriptException(e);
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
            Map<String, String> tags = rawHelix.getTags();
            if (tags != null) {
                result = tags.containsKey(tag);
            }
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for Helix.removeTag().
     *
     * IMPORTANT: This does not trigger a display update
     * because individual helices do not have enough
     * information to do so.  You must perform a display
     * action of some kind on the "rheat" variable to
     * see changes rendered.
     */
    public void removeTag(String tagName) throws ScriptException {
        try {
            rawHelix.removeTag(tagName);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
    }

    /**
     * Deprecated name for hasTag().  Maintained for compatibility
     * with older scripts but this could be removed at some point.
     */
    public boolean tagsInclude(String tag) throws ScriptException {
        jsDeprecationWarning("tagsInclude", "hasTag");
        return hasTag(tag);
    }

    /**
     * Script interface for HelixInfo.get3PrimeEnd().
     */
    public int threePrimeEnd() throws ScriptException {
        int result = 0;
        try {
            result = rawHelix.get3PrimeEnd();
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for RNA.getSequenceInRange() for helix 3' range.
     */
    public String threePrimeSequence() throws ScriptException {
        String result = "";
        try {
            SortedPair range = new SortedPair();
            rawHelix.get3PrimeRange(range);
            result = sourceRNA.getSequenceInRange(range);
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    /**
     * Script interface for HelixInfo.get3PrimeStart().
     */
    public int threePrimeStart() throws ScriptException {
        int result = 0;
        try {
            result = rawHelix.get3PrimeStart();
        } catch (Exception e) {
            rethrowAsScriptException(e);
        }
        return result;
    }

    private Helix rawHelix = null;
    private RNA sourceRNA = null;

}
