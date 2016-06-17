package rheat.base;

import rheat.filter.AllHelicesFilter;
import rheat.filter.Filter;
import rheat.GUI.RheatApp;
import rheat.script.ScriptMain;

import java.io.*;
import java.util.*;
import javax.script.*;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Main entry point to RNA HEAT application.
 */
public class AppMain {

    /**
     * For the log() method.
     */
    public static final int INFO = 0;
    public static final int WARN = 1;
    public static final int ERROR = 2;

    private String fileSep = System.getProperty("file.separator");
    private HashMap<String, String> preferencesMap = new HashMap<String, String>();
    private String preferencesDir = System.getProperty("user.home") + fileSep + ".rheat";
    private String preferencesScript = preferencesDir + fileSep + "prefs.js";
    private String historyScript = preferencesDir + fileSep + "history.js";
    private ArrayList<String> historyCommands = new ArrayList<String>();
    private ArrayList<String> startupScripts = new ArrayList<String>();
    private String previousDir = null; // see beginOpenFile()
    private ScriptEngine scriptEngine; // used to execute external scripts
    private rheat.GUI.RheatApp gui = null; // may be null
    private int undoMax = 20;
    private int currentUndo = 0;
    static private boolean isMac = false;
    public RNA rnaData = null;

    /**
     * Main application class; "args" should come from
     * the main() command line.
     */
    public AppMain(String[] args) throws IOException, ScriptException {
        try {
            initScriptingEngine();
        } catch (ScriptException e) {
            e.printStackTrace();
            log(ERROR, "Scripting is not available.");
        }
        boolean createGUI = true;
        if (args.length > 0) {
            // load any requested JavaScript files
            for (String argument : args) {
                if (argument.equals("-noGUI")) {
                    createGUI = false;
                    continue;
                }
                // treat anything else as a file location
                startupScripts.add(argument);
            }
        }
        if (createGUI) {
            // set cross-platform interface because GUI layout
            // currently does not look good when translated to
            // other systems (e.g. can be truncated on Mac)
            try {
                String laf = UIManager.getCrossPlatformLookAndFeelClassName();
                UIManager.setLookAndFeel(laf);
            } catch (Exception e) {
                log(WARN, "Unable to change look-and-feel; GUI might not be displayed correctly.");
            }
            this.gui = new rheat.GUI.RheatApp(this);
        }
    }

    /**
     * Logs message with implicit StringBuilder.
     * @param messageType use INFO, WARN or ERROR
     * @param parts strings to join to form the message
     */
    static public void log(int messageType, String[] parts) {
        StringBuilder sb = new StringBuilder();
        String prefix = ((messageType == INFO)
                         ? "INFO: "
                         : ((messageType == ERROR)
                            ? "ERROR: "
                            : ((messageType == WARN)
                               ? "WARNING: "
                               : "")));
        sb.append(prefix);
        for (String s : parts) {
            sb.append(s);
        }
        System.err.println(sb.toString());
    }

    /**
     * Logs simple messages.
     * @param messageType use INFO, WARN or ERROR
     * @param text the content of the log message
     */
    static public void log(int messageType, String text) {
        String[] stringArray = new String[]{ text };
        log(messageType, stringArray);
    }

    /**
     * Sets the object that is available as "rheat" in scripts.
     */
    void setScriptMain(ScriptMain scriptInterface) {
        // IMPORTANT: define a script variable named "rheat" that allows
        // for interaction with the current application (otherwise it is
        // not possible to write very useful scripts)
        scriptEngine.put("rheat", scriptInterface);
    }

    /**
     * Sets a particular preference key and value.  Currently the
     * recognized keys are: "BPSEQ" to set the location for the
     * helix data and scripts, and "Undo" to set the location for
     * undo-files.
     */
    public void setPreference(String key, String value) {
        preferencesMap.put(key, value);
    }

    /**
     * Returns all user preferences as a map.  Avoid doing this
     * except for file I/O; prefer more specific methods below.
     */
    public Map<String, String> getPreferencesMap() {
        return preferencesMap;
    }

    /**
     * Returns user-specified directory for helix data.
     */
    public String getPrefHelixDataDir() {
        return preferencesMap.get("BPSEQ");
    }

    /**
     * Returns user-specified directory for scripts.
     */
    public String getPrefScriptDir() {
        return preferencesMap.get("BPSEQ"); // for now, assume same location for scripts/inputs
    }

    /**
     * Returns user-specified directory for undo-files.
     */
    public String getPrefUndoDir() {
        return preferencesMap.get("Undo");
    }

    /**
     * Returns true if this is a Mac OS X machine.
     */
    public boolean isMac() {
        return this.isMac;
    }

    /**
     * Useful for scripts; keeps track of current directory so
     * that files can be opened only by name if desired (such as
     * in scripts).  Must be balanced by call to endOpenFile().
     * Must be balanced by call to endOpenFile().
     * @throws IOException if the file cannot be found, for instance
     * @returns the absolute path to the specified file
     */
    private String beginOpenFile(String filePath) throws IOException {
        log(INFO, new String[]{"Opening: '", filePath, "'."});
        // since scripts might contain references to files with relative
        // locations (e.g. file name only), a logical starting point has
        // to be set; choose the location of the script itself
        String result = filePath;
        File fileObj = new File(filePath);
        if (!fileObj.isAbsolute()) {
            try {
                File canonFileObj = fileObj.getCanonicalFile(); // may throw...
                String parentDir = canonFileObj.getParent(); // may throw...
                result = canonFileObj.getAbsolutePath();
                //log(INFO, new String[]{"Actual: '", result, "'."});
                this.previousDir = System.getProperty("user.dir");
                if (System.setProperty("user.dir", parentDir) == null) {
                    throw new IOException("Unable to set working directory to script location '" + parentDir + "'.");
                }
            } catch (IOException e) {
                // ignore
                log(WARN, e.getMessage());
            }
        }
        String newDir = System.getProperty("user.dir");
        if ((newDir != null) && (this.previousDir != null) &&
            (!newDir.equals(this.previousDir))) {
            log(INFO, new String[]{"Changed to dir.: '", System.getProperty("user.dir"), "'."});
        }
        return result;
    }

    /**
     * Balances a call to beginOpenFile() by restoring any
     * previous change to the "user.dir" property, as needed.
     */
    private void endOpenFile() {
        if (this.previousDir != null) {
            String tmpDir = System.getProperty("user.dir");
            if (System.setProperty("user.dir", this.previousDir) == null) {
                log(WARN, "Unable to restore previous working directory.");
            }
            if ((tmpDir != null) && (this.previousDir != null) &&
                (!tmpDir.equals(this.previousDir))) {
                log(INFO, new String[]{"Changed to dir.: '", System.getProperty("user.dir"), "'."});
            }
            this.previousDir = null;
        }
    }

    /**
     * Opens the specified helix data file, which must be in a
     * supported format such as ".bpseq".  If there is a GUI, it
     * will be refreshed automatically.
     */
    public void openRNA(String filePath) throws IOException {
        String realPath = this.beginOpenFile(filePath);
        try {
            rheat.base.Reader reader = new rheat.base.Reader(realPath);
            this.rnaData = reader.readBPSEQ();
            removeFilters(); // initialize data and update display
            //if (this.gui != null) {
            //    this.gui.refreshForNewRNA();
            //}
        } finally {
            this.endOpenFile();
        }
    }

    /**
     * Returns the selected helix, or null.
     * @return a Helix object
     */
    public Helix getSelectedHelix() {
        Helix result = null;
        if (this.gui != null) {
            result = this.gui.getSelectedHelix();
        }
        return result;
    }

    /**
     * Adds another string to the list of lines to be
     * written to "~/.rheat/history.js" implicitly.
     * The GUI may also display this command list.
     */
    public void addHistoryCommand(String commandLines) {
        historyCommands.add(commandLines);
        try {
            saveHistory();
        } catch (Exception e) {
            e.printStackTrace();
            log(WARN, "Failed to save the history file.");
        }
    }

    /**
     * Erases the list; see addHistoryCommand().
     */
    public void clearHistoryCommands() {
        historyCommands.clear();
    }

    /**
     * Creates another snapshot of "rnaData".
     */
    public void snapshotRNAData() throws IOException {
        String undoFile = getPrefUndoDir() + fileSep + "undo" + currentUndo;
        ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(undoFile));
        ois.writeObject(this.rnaData);
    }

    /**
     * Adds 1 to the current undo index.
     */
    public void incrementUndo() {
        currentUndo = (currentUndo + 1) % undoMax;
    }

    /**
     * Returns the current undo level.
     */
    public int getUndoIndex() {
        return currentUndo;
    }

    /**
     * Replaces "rnaData" with a previous version, as
     * captured by a call to snapshotRNAData().
     */
    public void revertToPreviousRNA(int undoIndex) throws ClassNotFoundException, IOException {
        String s = getPrefUndoDir() + File.separator + "undo" + undoIndex;
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
        RNA old = (RNA)ois.readObject();
        this.rnaData = old;
        this.currentUndo = undoIndex;
    }

    /**
     * Adds a filter to the history list and applies its effects to
     * the currently-displayed RNA.
     */
    public void addFilter(Filter filter) {
        // FIXME: create low-level history list (in GUI for now)
        RNA newData = filter.apply(this.rnaData);
        if (newData == null) {
            log(ERROR, "Filter failed to apply.");
        } else {
            this.rnaData = newData;
            if (this.gui != null) {
                this.gui.refreshForNewRNA();
            }
        }
    }

    /**
     * Clears the history list and shows all helices.
     */
    public void removeFilters() {
        // FIXME: use low-level history list (in GUI for now)
        AllHelicesFilter filter = new AllHelicesFilter();
        RNA newData = filter.apply(this.rnaData);
        if (newData == null) {
            log(ERROR, "All-helices filter failed to apply.");
        } else {
            this.rnaData = newData;
            if (this.gui != null) {
                this.gui.clearHistory();
                this.gui.refreshForNewRNA();
            }
        }
    }

    /**
     * Runs the specified code.  The same caveats apply to
     * this method as for the runScript() method.
     * @throws ScriptException for script-triggered errors
     */
    public void evaluateScriptCode(String code) throws ScriptException {
        ScriptContext scriptContext = scriptEngine.getContext();
        // reset current file
        scriptContext.setAttribute(ScriptEngine.FILENAME, "(inline script)", ScriptContext.ENGINE_SCOPE);
        try {
            // run JavaScript code
            scriptEngine.eval(code);
        } finally {
            // reset current file
            scriptContext.setAttribute(ScriptEngine.FILENAME, "(no file specified)", ScriptContext.ENGINE_SCOPE);
        }
    }

    /**
     * Runs the specified script.  You should ensure that
     * setScriptMain() has been called first, otherwise
     * any references to "rheat" in the script will fail.
     * @throws IOException for issues such as nonexistent files
     * @throws ScriptException for script-triggered errors
     */
    public void runScript(String filePath) throws IOException, ScriptException {
        ScriptContext scriptContext = scriptEngine.getContext();
        String realPath = this.beginOpenFile(filePath);
        BufferedReader reader = new BufferedReader(new FileReader(realPath));
        // for convenience, ignore any Unix "#!" line (the default
        // JavaScript reader will NOT do this...)
        reader.mark(255/* max. char. to backtrack */);
        String line = reader.readLine();
        if ((line.length() > 1) && ((line.charAt(0) != '#') || (line.charAt(1) != '!'))) {
            // no #!-line; keep the original line
            reader.reset();
        }
        try {
            // specify the file so errors are easier to understand
            scriptContext.setAttribute(ScriptEngine.FILENAME, realPath, ScriptContext.ENGINE_SCOPE);
            // run the program
            scriptEngine.eval(reader);
        } finally {
            this.endOpenFile();
            // reset current file
            scriptContext.setAttribute(ScriptEngine.FILENAME, "(no file specified)", ScriptContext.ENGINE_SCOPE);
        }
    }

    /**
     * Calls runScript() for any scripts found in the main
     * argument list at startup time, and for any existing
     * preferences file.  If there is no preferences file,
     * a new one is created with default values.
     */
    private void runStartupScripts() throws IOException, ScriptException {
        if (new File(this.preferencesScript).exists()) {
            log(INFO, "Running script to restore user preferences…");
            try {
                runScript(this.preferencesScript);
            } catch (Exception e) {
                log(WARN, "Unable to restore preferences: '" + e.getMessage() + "'.");
            }
        } else {
            // initialize new preferences file (the keys used below
            // should be consistent with other uses of the keys)
            log(INFO, "Creating a new preferences file.");
            preferencesMap.put("BPSEQ", System.getProperties().getProperty("user.dir"));
            preferencesMap.put("Undo", System.getProperties().getProperty("user.dir"));
            savePreferences();
        }
        for (String scriptPath : startupScripts) {
            runScript(scriptPath);
        }
    }

    /**
     * Writes the history list to disk.
     */
    public void saveHistory() throws IOException {
        new File(this.preferencesDir).mkdirs(); // ensure parent directories exist; ignore "boolean" result
        PrintWriter pw = new PrintWriter(this.historyScript);
        for (String line : this.historyCommands) {
            pw.println(line);
        }
        pw.close();
    }

    /**
     * Writes the preferences map to disk.
     */
    public void savePreferences() throws IOException {
        new File(this.preferencesDir).mkdirs(); // ensure parent directories exist; ignore "boolean" result
        PrintWriter pw = new PrintWriter(this.preferencesScript);
        for (Map.Entry<String, String> entry : this.preferencesMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // IMPORTANT: if the value contains backslashes (e.g. Windows paths),
            // it will not be read back correctly by JavaScript later; translate
            // any backslashes into double-backslash to escape them
            value = value.replace("\\", "\\\\");
            pw.println("rheat.setPreference(\'" + key + "\', \'" + value + "\')");
        }
        pw.close();
    }

    /**
     * Creates the scripting engine (JavaScript).  Note that the
     * engine is not going to be very useful until a main object
     * has been set with setScriptingMain().
     */
    private void initScriptingEngine() throws ScriptException {
        // set up an engine to interpret user scripts
        ScriptEngineManager engineMgr = new ScriptEngineManager();
        boolean debugEngines = false;
        if (debugEngines) {
            // might want to see what scripting languages are available…
            for (ScriptEngineFactory ef : engineMgr.getEngineFactories()) {
                log(INFO, new String[]{"Available scripting engine: '", ef.getEngineName(), "'."});
            }
        }
        scriptEngine = engineMgr.getEngineByName("JavaScript");
    }

    /**
     * Performs clean-up when closing the current session.
     */
    public void cleanUp(){
        rnaData = null;
        System.gc();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // can be useful to determine the platform sometimes
            String osName = System.getProperty("os.name");
            System.err.println("RNA HEAT for " + osName);
            if (osName.contains("Mac")) {
                isMac = true;
            }

            // create an object to manage all application state (this
            // may or may not include a GUI, depending on user options)
            AppMain appMain = new AppMain(args);
            if (appMain.gui != null) {
                // display the graphical user interface
                appMain.gui.setVisible(true);
            }

            // run any scripts given on the command line, and run any
            // other special scripts (such as the preferences file)
            try {
                appMain.setScriptMain(new ScriptMain(appMain)); // sets "rheat" variable in scripts
                appMain.scriptEngine.eval("rheat.log(rheat.INFO, 'JavaScript engine loaded successfully.')"); // trivial test
                appMain.runStartupScripts(); // execute any scripts given on the command line
            } catch (Exception e) {
                e.printStackTrace();
                if (appMain.gui != null) {
                    // can display the error graphically
                    JTextArea msg = new JTextArea(e.getMessage());
                    msg.setColumns(65);
                    msg.setRows(7);
                    msg.setLineWrap(true);
                    msg.setWrapStyleWord(true);
                    JScrollPane scrollPane = new JScrollPane(msg);
                    JOptionPane.showMessageDialog(appMain.gui, scrollPane, "Error Running Script", JOptionPane.ERROR_MESSAGE);
                } else {
                    // no GUI; should not display graphically,
                    // only in the terminal
                    //e.printStackTrace(); // can enable this for more debugging information
                    log(ERROR, e.getMessage());
                    System.exit(1);
                }
            }

            if (appMain.gui == null) {
                // nothing else to do
                System.exit(0);
            } else {
                // at exit time, save history (can also save it sooner)
                appMain.saveHistory();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
