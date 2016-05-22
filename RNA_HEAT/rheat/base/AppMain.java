package rheat.base;

import rheat.GUI.RheatApp;
import rheat.script.ScriptMain;

import java.io.*;
import java.util.*;
import javax.script.*;
import javax.swing.JOptionPane;
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
    private HashMap<String, String> pref = new HashMap<String, String>();
    private String preferencesDir = System.getProperty("user.home") + fileSep + ".rheat";
    private String preferencesFile = preferencesDir + fileSep + "pref.bin";
    private ArrayList<String> startupScripts = new ArrayList<String>();
    private String previousDir = null; // see beginOpenFile()
    private ScriptEngine scriptEngine; // used to execute external scripts
    private rheat.GUI.RheatApp gui = null; // may be null
    static private boolean isMac = false;
    public RNA rnaData = null;

    /**
     * Main application class; "args" should come from
     * the main() command line.
     */
    public AppMain(String[] args) throws IOException, ScriptException {
        initPreferences();
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
     * @param text strings to join to form the message
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
     * Returns all user preferences as a map.  Avoid doing this
     * except for file I/O; prefer more specific methods below.
     */
    public Map<String, String> getPreferencesMap() {
        return pref;
    }

    /**
     * Returns user-specified directory for helix data.
     */
    public String getPrefHelixDataDir() {
        return pref.get("BPSEQ");
    }

    /**
     * Returns user-specified directory for scripts.
     */
    public String getPrefScriptDir() {
        return pref.get("BPSEQ"); // for now, assume same location for scripts/inputs
    }

    /**
     * Returns user-specified directory for undo-files.
     */
    public String getPrefUndoDir() {
        return pref.get("Undo");
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
        log(INFO, new String[]{"Request to open: '", filePath, "'."});
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
    public void openHelixFile(String filePath) throws IOException {
        String realPath = this.beginOpenFile(filePath);
        try {
            rheat.base.Reader reader = new rheat.base.Reader(realPath);
            this.rnaData = reader.readBPSEQ();
            if (this.gui != null) {
                this.gui.refreshForNewHelixFile();
            }
        } finally {
            this.endOpenFile();
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
            ScriptContext scriptContext = scriptEngine.getContext();
            scriptContext.setAttribute(ScriptEngine.FILENAME, realPath, ScriptContext.ENGINE_SCOPE);
            // run the program
            scriptEngine.eval(reader);
        } finally {
            this.endOpenFile();
        }
    }

    /**
     * Calls runScript() for any scripts found in the main
     * argument list at startup time.
     */
    private void runStartupScripts() throws IOException, ScriptException {
        for (String scriptPath : startupScripts) {
            runScript(scriptPath);
        }
    }

    /**
     * Writes the preferences map to disk.
     */
    public void savePreferences() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(preferencesFile))));
        oos.writeObject(pref);
        oos.flush();
    }

    /**
     * Initialize and read the preference files.  Creates a new preference file
     * if the old one does not exist.
     */
    private void initPreferences(){
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            new File(preferencesDir).mkdirs(); // ensure parent directories exist; ignore "boolean" result
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(preferencesFile)));
            @SuppressWarnings({"unchecked"}) HashMap<String, String> newPref =
                                             (HashMap<String, String>)ois.readObject();
            pref = newPref;
        }
        catch (java.io.FileNotFoundException ex){
            //String err = "Your preference file either does not exist, or is corrupted.  A new one will be created.";
            //JOptionPane.showMessageDialog(this, err, "Cannot find Preferences", JOptionPane.ERROR_MESSAGE);
            pref.put("BPSEQ", System.getProperties().getProperty("user.dir"));
            pref.put("Undo", System.getProperties().getProperty("user.dir"));
            try {
                oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(preferencesFile))));
                oos.writeObject(pref);
            }
            catch (Exception ex2){
                ex2.printStackTrace();
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        finally{
            if (oos != null){
                try {
                    oos.close();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            if (ois != null){
                try {
                    ois.close();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
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

            // run any scripts given on the command line
            try {
                appMain.setScriptMain(new ScriptMain(appMain)); // sets "rheat" variable in scripts
                appMain.scriptEngine.eval("rheat.log(rheat.INFO, 'JavaScript engine loaded successfully.')"); // trivial test
                appMain.runStartupScripts(); // execute any scripts given on the command line
            } catch (Exception e) {
                if (appMain.gui != null) {
                    // can display the error graphically
                    JOptionPane.showMessageDialog(appMain.gui, e.getMessage(), "Error Running Script", JOptionPane.ERROR_MESSAGE);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
