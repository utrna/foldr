package rheat.base;

import rheat.filter.AllHelicesFilter;
import rheat.filter.BPFilter;
import rheat.filter.Filter;
import rheat.GUI.RheatApp;
import rheat.script.ScriptMain;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private HashMap<String, String> preferencesMap = new HashMap<String, String>(); // use setPreference() and getPreference()
    private HashMap<String, String> tmpPrefsMap = new HashMap<String, String>(); // use setTemporaryPreference() and getPreference()
    private HashSet<String> validPrefKeys = new HashSet<String>();
    private String preferencesDir = System.getProperty("user.home") + fileSep + ".rheat";
    private String preferencesScript = preferencesDir + fileSep + "prefs.js";
    private String historyScript = preferencesDir + fileSep + "history.js";
    private ArrayList<String> historyCommands = new ArrayList<String>();
    private ArrayList<String> startupScripts = new ArrayList<String>();
    private Stack<String> previousDirs = new Stack<String>(); // see changeDirectory()
    private String currentExperimentDir = null; // see newExperiment()
    private String currentRNAFilePath = null;
    private ScriptEngine scriptEngine; // used to execute external scripts
    private rheat.GUI.RheatApp gui = null; // may be null
    private int undoMax = 20;
    private int currentUndo = 0;
    static private boolean isMac = false;
    public RNA rnaData = null;
    public RNA overlayData = null;
    public ArrayList<Filter> filterList = new ArrayList<Filter>();

    /**
     * Main application class; "args" should come from
     * the main() command line.
     */
    public AppMain(String[] args) throws IOException, ScriptException {
        validPrefKeys.add("BPSEQ");
        validPrefKeys.add("ProgramsDir");
        validPrefKeys.add("RunRootDir");
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
     * Sets a value only if it has no value for the key.
     */
    public void setDefaultPreference(String key, String defaultValue) {
        if (!preferencesMap.containsKey(key)) {
            setPreference(key, defaultValue);
        }
    }

    /**
     * Sets a particular preference key and value.  Currently the
     * recognized keys are: "BPSEQ" to set the location for the
     * helix data and scripts, and "RunRootDir" to set the
     * location for experiments (running external programs).
     */
    public void setPreference(String key, String value) {
        if (key.equals("Undo")) {
            // legacy files might contain this
            log(WARN, "Preference key 'Undo' is no longer used; ignoring.");
            return;
        }
        if (!validPrefKeys.contains(key)) {
            throw new RuntimeException("invalid preference key: '" + key + "'");
        }
        preferencesMap.put(key, value);
    }

    /**
     * Overrides a preference key for the duration of the session,
     * preventing query methods from returning the real values (the
     * temporary values are not saved however).
     * @param key a key, as if using setPreference()
     * @param value a value, as if using setPreference(); or null to clear
     */
    public void setTemporaryPreference(String key, String value) {
        if (!validPrefKeys.contains(key)) {
            throw new RuntimeException("invalid preference key: '" + key + "'");
        }
        if (value == null) {
            tmpPrefsMap.remove(key);
        } else {
            tmpPrefsMap.put(key, value);
        }
    }

    /**
     * Helper for preference query methods.
     */
    private String getPreference(String key) {
        return getPreference(key, true/* allow temporary overrides */);
    }

    /**
     * Helper for preference query methods.
     */
    private String getPreference(String key, boolean allowTmp) {
        String result = preferencesMap.get(key);
        if ((allowTmp) && (tmpPrefsMap.containsKey(key))) {
            //log(INFO, "Return temporary override: '" + key + "' = '" + result + "'");
            result = tmpPrefsMap.get(key);
        }
        return result;
    }

    /**
     * Returns user-specified directory for helix data.
     */
    public String getPrefHelixDataDir() {
        return getPreference("BPSEQ");
    }

    /**
     * Returns user-specified directory for external programs.  This
     * is implicitly the first place searched for programs (the
     * system may also consult its search path).
     */
    public String getPrefProgramsDir() {
        return getPreference("ProgramsDir");
    }

    /**
     * Returns user-specified directory for the root of experiments.
     * When other programs are launched, a directory tree is created
     * to organize results; this is the top of that tree.
     */
    public String getPrefRunRootDir() {
        return getPreference("RunRootDir");
    }

    /**
     * Returns user-specified directory for scripts.
     */
    public String getPrefScriptDir() {
        return getPreference("BPSEQ"); // for now, assume same location for scripts/inputs
    }

    /**
     * Returns the directory for storing undo-files.
     */
    public String getUndoDir() {
        // return temporary directory, or current directory if none
        return System.getProperty("java.io.tmpdir", getWorkingDir());
    }

    /**
     * Returns the current directory, which is set automatically as
     * scripts run.  (Look for "user.dir" in the code.)  This
     * determines the meaning of relative path names when looking for
     * files.
     * @return the current directory’s path name
     */
    public String getWorkingDir() {
        return System.getProperty("user.dir");
    }

    /**
     * Returns the given strings joined by the directory separator.
     * This is primarily meant for scripts, to aid portability.
     * @return a string joined by the path separator
     */
    public String makePath(String... elements) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String e : elements) {
            sb.append(e);
            ++i;
            if (i != elements.length) {
                sb.append(fileSep);
            }
        }
        return sb.toString();
    }

    /**
     * Returns true if this is a Mac OS X machine.
     */
    public boolean isMac() {
        return this.isMac;
    }

    /**
     * Changes the current directory and tracks the previous one.  In
     * most cases, this should only be called in a try...finally pair
     * where restoreDirectory() is called to restore at the end.
     * @throws IOException if the directory cannot be found, for instance
     */
    private void changeDirectory(String newDir) throws IOException {
        if (!new File(newDir).exists()) {
            throw new IOException("Proposed working directory does not exist: '" + newDir + "'.");
        }
        String oldDir = System.getProperty("user.dir");
        if (System.setProperty("user.dir", newDir) == null) {
            throw new IOException("Unable to set working directory to '" + newDir + "'.");
        }
        previousDirs.push(oldDir);
        String actualNewDir = System.getProperty("user.dir");
        if ((actualNewDir != null) &&
            ((oldDir == null) ||
             (!actualNewDir.equals(oldDir)))) {
            log(INFO, new String[]{"Changed to dir.: '", actualNewDir, "'."});
        }
    }

    /**
     * Balances a call to changeDirectory() by setting the "user.dir"
     * (current directory) to the top entry on the stack and removing
     * it from the stack.  Has no effect if there is nothing on the
     * directory stack.
     */
    private void restoreDirectory() {
        if (!previousDirs.empty()) {
            String oldDir = System.getProperty("user.dir");
            String stackTop = previousDirs.pop();
            if (System.setProperty("user.dir", stackTop) == null) {
                log(WARN, "Unable to restore previous working directory.");
            }
            if ((oldDir != null) &&
                ((stackTop == null) ||
                 (!oldDir.equals(stackTop)))) {
                log(INFO, new String[]{"Changed back to dir.: '", System.getProperty("user.dir"), "'."});
            }
        }
    }

    /**
     * Useful for scripts; keeps track of current directory so
     * that files can be opened only by name if desired (such as
     * in scripts).  Must be balanced by call to endOpenFile().
     * @throws IOException if the file cannot be found, for instance
     * @return the absolute path to the specified file
     */
    private String beginOpenFile(String filePath) throws IOException {
        log(INFO, new String[]{"Request to open: '", filePath, "'."});
        // since scripts might contain references to files with relative
        // locations (e.g. file name only), a logical starting point has
        // to be set; choose the location of the script itself
        String result = filePath;
        File fileObj = new File(filePath);
        try {
            File canonFileObj = fileObj.getCanonicalFile(); // may throw...
            String parentDir = canonFileObj.getParent(); // may throw...
            changeDirectory(parentDir);
            result = canonFileObj.getPath();
        } catch (IOException e) {
            // ignore
            log(WARN, e.getMessage());
        }
        return result;
    }

    /**
     * Cleans up a call to beginOpenFile() (use in "finally").
     */
    private void endOpenFile(String filePath) {
        // undo any temporary steps taken in beginOpenFile()
        restoreDirectory();
    }

    /**
     * Opens the specified helix data file, which must be in a
     * supported format such as ".bpseq".  If there is a GUI, it
     * will be refreshed automatically.
     */
    public void openRNA(String filePath) throws IOException {
        String realPath = beginOpenFile(filePath);
        try {
            rheat.base.Reader reader = new rheat.base.Reader(realPath);
            this.rnaData = reader.readBPSEQ();
            this.overlayData = null;
            this.currentRNAFilePath = realPath;
            removeFilters(); // initialize data and update display
            //if (this.gui != null) {
            //    this.gui.refreshForNewRNA();
            //}
        } finally {
            endOpenFile(filePath);
        }
    }

    /**
     * Opens the specified helix data file, which must be in a
     * supported format such as ".bpseq".  If there is a GUI, it
     * will be refreshed automatically.
     */
    public void openOverlayRNA(String filePath) throws IOException {
        String realPath = beginOpenFile(filePath);
        try {
            rheat.base.Reader reader = new rheat.base.Reader(realPath);
            this.overlayData = reader.readBPSEQ();
            // apply same transforms to overlay data
            for (Filter filter : this.filterList) {
                if (filter instanceof BPFilter) {
                    this.overlayData = filter.apply(this.overlayData);
                }
            }
            if (this.gui != null) {
                this.gui.refreshForNewRNA();
            }
        } finally {
            endOpenFile(filePath);
        }
    }

    /**
     * Provides the location for output in the current experiment.
     * This will be the result of getPrefRunRootDir() combined with
     * at least one subdirectory.  The first time this is requested,
     * the appropriate subdirectory will be created automatically.
     * See also newExperiment().
     * @return a directory path, or null if directory cannot be created
     */
    public String getCurrentExperimentDir() {
        if (this.currentExperimentDir == null) {
            // set path if this has not been set yet
            newExperiment();
        }
        String result = this.currentExperimentDir;
        File dirObj = new File(result);
        if (!dirObj.exists()) {
            if (!dirObj.mkdirs()) {
                result = null;
            }
        }
        return result;
    }

    /**
     * Specifies that output should go into a new subdirectory of the
     * user-designated output directory, based on the date and time.
     * The directory is only created by getCurrentExperimentDir().
     * Note that this will be called automatically the first time it
     * is needed; you only need to call newExperiment() again if you
     * want to separate results during the same session.
     */
    public void newExperiment() {
        String newDir = makePath(getPrefRunRootDir(), getDateTimeString());
        this.currentExperimentDir = newDir;
    }

    /**
     * Returns the current date and time as a formatted string,
     * suitable for use in the generation of files or directories.
     * @return a date string that should be unique and sort well
     */
    public String getDateTimeString() {
        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.applyPattern("yyyy-MM-dd-HHmmss"); // may throw IllegalArgumentException
        return formatter.format(new Date(System.currentTimeMillis()));
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
        String undoFile = makePath(getUndoDir(), "undo" + currentUndo);
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
        String s = getUndoDir() + File.separator + "undo" + undoIndex;
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
        RNA old = (RNA)ois.readObject();
        this.rnaData = old;
        this.filterList.remove(this.filterList.size() - 1);
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
            this.filterList.add(filter);
            if ((filter instanceof BPFilter) && (this.overlayData != null)) {
                // if a base-pair filter is used, keep the overlay in sync
                RNA newOverlayData = filter.apply(this.overlayData);
                if (newOverlayData == null) {
                    log(ERROR, "Filter failed to apply to overlay data.");
                } else {
                    this.overlayData = newData;
                }
            }
            if (this.gui != null) {
                this.gui.refreshForNewRNA(EnumSet.of(RheatApp.RNADisplayFeature.ZOOM_LEVEL));
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
            this.filterList.clear();
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
     * Runs the specified program, in the current experiment space.
     * Waits for termination and returns exit status (0 = success).
     * Output from the program is automatically logged; if more than
     * one program has run in the same experiment area, the logs will
     * be appended together.  See also runScript().
     * @return process exit status (0 = success) or -1 for interruptions
     * @throws IOException for file-related errors
     */
    public int runProgram(String... arguments) throws IOException {
        String runDir = getCurrentExperimentDir();
        // assume that the program being launched will need access to the
        // current RNA data; for simplicity, copy the original file into
        // the experiment directory so that the program can assume the
        // name and location instead of requiring a parameter
        String extension = "";
        if (currentRNAFilePath != null) {
            int i = currentRNAFilePath.lastIndexOf('.');
            if (i > 0) {
                extension = currentRNAFilePath.substring(i);
            }
            String copiedLocation = makePath(runDir, "input" + extension);
            log(INFO, "Copying '" + currentRNAFilePath + "' to '" + copiedLocation + "'.");
            Files.copy(Paths.get(currentRNAFilePath), Paths.get(copiedLocation),
                       REPLACE_EXISTING, COPY_ATTRIBUTES);
        }
        // by default, search the preferred program space; if the program
        // is found then run it from that location (otherwise, the system
        // may use its own default search path)
        String program = arguments[0];
        List<String> modifiedArgs = new CopyOnWriteArrayList<String>(arguments);
        if (!new File(program).isAbsolute()) {
            String foundProgram = null;
            for (String searchDir : new String[]{getPrefProgramsDir(), System.getProperty("user.dir")}) {
                log(INFO, "Search in '" + searchDir + "'...");
                String candidate = makePath(searchDir, program);
                if (new File(candidate).exists()) {
                    log(INFO, "Using '" + candidate + "'.");
                    foundProgram = candidate;
                    modifiedArgs.set(0, candidate);
                    break;
                }
            }
            if (foundProgram == null) {
                log(INFO, "Program '" + program + "' not found in preferred programs directory; will defer to system search path.");
            }
        }
        // automatically add interpreters to the command line so that
        // it will “just work” if the program file is given first
        if (program.endsWith(".py")) {
            modifiedArgs.add(0, "python");
        } else if (program.endsWith(".jar")) {
            // run with "java -jar ..." (insertion order matters)
            modifiedArgs.add(0, "-jar");
            modifiedArgs.add(0, "java");
        }
        ProcessBuilder pb = new ProcessBuilder(modifiedArgs);
        //Map<String, String> env = pb.environment(); // not used for now
        pb.directory(new File(runDir));
        File logFile = new File(pb.directory(), "log.txt");
        log(INFO, "Running: " + modifiedArgs);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        // TODO: this should probably be backgrounded with SwingWorker
        // or an equivalent method; for now, execute synchronously
        Process process = pb.start();
        int exitStatus = -1;
        try {
            exitStatus = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            exitStatus = -1;
        }
        log(INFO, "Exited (status " + exitStatus + "); output is in '" + logFile.getPath() + "'.");
        // TODO: there might be file system race conditions here; may
        // want a mechanism for delaying or otherwise retrying the
        // opening of output files
        if ((exitStatus == 0) && (this.gui != null)) {
            // after a successful run, if a designated output file exists
            // then automatically import it as an overlay
            String outputPath = makePath(runDir, "output" + extension);
            if (new File(outputPath).exists()) {
                log(INFO, "Located results in '" + outputPath + "'; opening as overlay…");
                openOverlayRNA(outputPath);
            }
            // if any explicit text or image output exists, open the file in a new window
            String[] candidates = new String[]{ "txt", "png", "jpg", "jpeg" };
            for (String s : candidates) {
                outputPath = makePath(runDir, "output." + s);
                File asFile = new File(outputPath);
                if (asFile.exists() && (asFile.length() > 0)) {
                    log(INFO, "Located results in '" + outputPath + "'; opening as data…");
                    this.gui.openDataFile(outputPath);
                }
            }
            // append the log output to the log area
        }
        return exitStatus;
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
        try {
            String realPath = beginOpenFile(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(realPath));
            // for convenience, ignore any Unix "#!" line (the default
            // JavaScript reader will NOT do this...)
            reader.mark(255/* max. char. to backtrack */);
            String line = reader.readLine();
            if ((line.length() > 1) && ((line.charAt(0) != '#') || (line.charAt(1) != '!'))) {
                // no #!-line; keep the original line
                reader.reset();
            }
            // specify the file so errors are easier to understand
            scriptContext.setAttribute(ScriptEngine.FILENAME, realPath, ScriptContext.ENGINE_SCOPE);
            // run the program
            scriptEngine.eval(reader);
        } finally {
            endOpenFile(filePath);
            // reset current file
            scriptContext.setAttribute(ScriptEngine.FILENAME, "(no file specified)", ScriptContext.ENGINE_SCOPE);
        }
    }

    /**
     * Runs commands in any existing preferences file.  If there is
     * no preferences file, a new one is created.  Any missing values
     * are given default values.  The result is saved.
     */
    private void initPreferences() {
        if (new File(this.preferencesScript).exists()) {
            log(INFO, "Running script to restore user preferences…");
            try {
                runScript(this.preferencesScript);
            } catch (Exception e) {
                log(WARN, "Unable to restore preferences: '" + e.getMessage() + "'.");
            }
        }
        String currentDir = getWorkingDir();
        setDefaultPreference("ProgramsDir", currentDir);
        setDefaultPreference("BPSEQ", currentDir);
        setDefaultPreference("RunRootDir", makePath(currentDir, "Experiments"));
        try {
            savePreferences();
        } catch (IOException e) {
            log(WARN, "Unable to save preferences: '" + e.getMessage() + "'.");
        }
    }

    /**
     * Calls runScript() for any scripts found in the main
     * argument list at startup time, and for any existing
     * preferences file.  If there is no preferences file,
     * a new one is created with default values.
     */
    private void runStartupScripts() throws IOException, ScriptException {
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
        log(INFO, "Preferences file has been saved.");
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
        overlayData = null;
        currentRNAFilePath = null;
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
                appMain.initPreferences();
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
