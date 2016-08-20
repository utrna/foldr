package rheat.script;

import javax.script.*;

/**
 * Utilities for generating JavaScript code.  Typically
 * you will "import static rheat.script.JSUtil" to get
 * access to all functions directly (as such, the
 * functions should have short but descriptive names
 * that are likely to be unique).
 */
public class JSUtil {

    /**
     * Helper for warning the user about outdated method names.
     * Pass only the names of old and new methods (no "rheat.").
     */
    static public void jsDeprecationWarning(String oldName, String newName) throws ScriptException {
       ScriptMain._log(ScriptMain.WARN, "Script method rheat." + oldName + " is deprecated; please use rheat." + newName + " instead.");
    }

    /**
     * Given a “raw” value from code, returns the escaped value that
     * would be needed to parse back correctly in JavaScript.  (This
     * is useful when generating code that will be evaluated later.)
     *
     * NOTE: This is a heuristic that may need to be extended as
     * needed.  Currently handles replacing single backslashes with
     * double-backslashes (common in Windows pathnames).
     */
    static public String jsEscape(String inputString) {
        String result = inputString;
        result = result.replace("\\", "\\\\"); // backslash -> double-backslash
        return result;
    }

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

}
