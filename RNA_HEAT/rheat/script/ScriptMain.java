package rheat.script;

import rheat.base.*;
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
     * Creates a scripting interface for the given object, which
     * may or may not have an associated GUI.
     * @param appMain primary application object
     */
    public ScriptMain(AppMain appMain) {
        this.appMain = appMain;
    }

    /**
     * Script interface for AppMain.log().
     */
    static public void log(int messageType, String text) throws ScriptException {
        try {
            AppMain.log(messageType, text);
        } catch (Exception e) {
            throw new ScriptException(e.getMessage());
        }
    }

    /**
     * Script interface for AppMain.openHelixFile().
     */
    public void openHelixFile(String filePath) throws ScriptException {
        try {
            appMain.openHelixFile(filePath);
        } catch (Exception e) {
            throw new ScriptException(e.getMessage());
        }
    }

    /**
     * Script interface for AppMain.runScript().
     */
    public void runScript(String filePath) throws ScriptException {
        try {
            appMain.runScript(filePath);
        } catch (Exception e) {
            throw new ScriptException(e.getMessage());
        }
    }

    /**
     * Script interface for AppMain.setPreference().
     */
    public void setPreference(String key, String value) throws ScriptException {
        try {
            appMain.setPreference(key, value);
        } catch (Exception e) {
            throw new ScriptException(e.getMessage());
        }
    }

}
