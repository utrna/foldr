package rheat.script;

import java.awt.Color;
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
     * Given a Color object, returns an HTML-style version of it
     * that is a suitable argument for scripting functions.
     * @return an HTML color string such as "#ab0078"
     */
    static public String jsColor(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * @return a comma-separated list
     */
    static public String jsCSV(String... strArray) {
        StringBuilder sb = new StringBuilder();
        jsCSVHelper(sb, strArray);
        return sb.toString();
    }
    static private void jsCSVHelper(StringBuilder sb, String... strArray) {
        int i = 0;
        for (String s : strArray) {
            ++i;
            sb.append(s);
            if (i != strArray.length) {
                sb.append(", ");
            }
        }
    }

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
     * double-backslashes (common in Windows pathnames), and fixing
     * any quotation marks.
     */
    static public String jsEscape(String inputString) {
        String result = inputString;
        result = result.replace("\\", "\\\\"); // backslash -> double-backslash
        result = result.replace("\"", "\\\""); // double-quote -> backslash double-quote
        result = result.replace("\'", "\\\'"); // quote -> backslash quote
        return result;
    }

    /**
     * Useful for generating complete function calls.
     * @return a function name and parenthesized comma-separated list
     */
    static public String jsFunction(String funcName, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(funcName);
        jsParamsHelper(sb, args);
        return sb.toString();
    }

    /**
     * Useful for generating parenthesized function parameter lists.
     * See also jsFunction().
     * @return a parenthesized comma-separated list
     */
    static public String jsParams(String... strArray) {
        StringBuilder sb = new StringBuilder();
        jsParamsHelper(sb, strArray);
        return sb.toString();
    }
    static private void jsParamsHelper(StringBuilder sb, String... strArray) {
        sb.append("(");
        jsCSVHelper(sb, strArray);
        sb.append(")");
    }

    /**
     * @return given string, surrounded by quotes
     */
    static public String jsQuote(String s) {
        return ("'" + s + "'");
    }

    /**
     * Calls jsColor() automatically on "c", then jsQuote().
     */
    static public String jsQuoteColor(Color c) {
        return jsQuote(jsColor(c));
    }

    /**
     * Calls jsEscape() automatically on "s", then jsQuote().
     * This is almost certainly the appropriate call when converting
     * raw data into a JavaScript string for later use.
     * @return given string, escaped, surrounded by quotes
     */
    static public String jsQuoteEscape(String s) {
        return jsQuote(jsEscape(s));
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
