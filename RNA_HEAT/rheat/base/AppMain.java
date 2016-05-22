package rheat.base;

import rheat.GUI.RheatApp;

import java.io.*;
import java.util.*;
import javax.script.*;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Main entry point to RNA HEAT application.
 */
public class AppMain {
    
    private String fileSep = System.getProperty("file.separator");
    private HashMap<String, String> pref = new HashMap<String, String>();
    private String preferencesDir = System.getProperty("user.home") + fileSep + ".rheat";
    private String preferencesFile = preferencesDir + fileSep + "pref.bin";
    private ArrayList<String> startupScripts = new ArrayList<String>();
    private ScriptEngine scriptEngine; // used to execute external scripts
    private boolean noGUI = false;
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
            System.err.println("Scripting is not available.");
        }
        if (args.length > 0) {
            // load any requested JavaScript files
            for (String argument : args) {
                if (argument.equals("-noGUI")) {
                    this.noGUI = true;
                    continue;
                }
                // treat anything else as a file location
                startupScripts.add(argument);
            }
        }
    }

    public Map<String, String> getPreferencesMap() {
        return pref;
    }

    public String getPrefHelixDataDir() {
        return pref.get("BPSEQ");
    }

    public String getPrefScriptDir() {
        return pref.get("BPSEQ"); // for now, assume same location for scripts/inputs
    }

    public String getPrefUndoDir() {
        return pref.get("Undo");
    }

    public boolean isMac() {
        return this.isMac;
    }

    public void runScript(String filePath) throws IOException, ScriptException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        // for convenience, ignore any Unix "#!" line (the default
        // JavaScript reader will NOT do this...)
        reader.mark(255/* max. char. to backtrack */);
        String line = reader.readLine();
        if ((line.length() > 1) && ((line.charAt(0) != '#') || (line.charAt(1) != '!'))) {
            // no #!-line; keep the original line
            reader.reset();
        }
        scriptEngine.eval(reader);
    }

    private void runStartupScripts() throws IOException, ScriptException {
        for (String scriptPath : startupScripts) {
            runScript(scriptPath);
        }
    }

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
     * Creates the scripting engine (JavaScript) and runs a
     * basic test to print a message.
     */
    private void initScriptingEngine() throws ScriptException {
        // set up an engine to interpret user scripts
        ScriptEngineManager engineMgr = new ScriptEngineManager();
        boolean debugEngines = false;
        if (debugEngines) {
            // might want to see what scripting languages are available…
            for (ScriptEngineFactory ef : engineMgr.getEngineFactories()) {
                System.err.println("INFO: Available scripting engine: '" + ef.getEngineName() + "'.");
            }
        }
        scriptEngine = engineMgr.getEngineByName("JavaScript");
        scriptEngine.eval("println('JavaScript engine loaded successfully.')");
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

            RheatApp gui = null;
            AppMain appMain = null;
            appMain = new AppMain(args);
            if (!appMain.noGUI) {
                // set cross-platform interface because GUI layout
                // currently does not look good when translated to
                // other systems (e.g. can be truncated on Mac)
                String laf = UIManager.getCrossPlatformLookAndFeelClassName();
                UIManager.setLookAndFeel(laf);

                // display the GUI
                gui = new RheatApp(appMain);
                gui.setVisible(true);
            }

            // run any scripts given on the command line
            try {
                appMain.runStartupScripts();
            } catch (Exception e) {
                if (gui != null) {
                    // can display the error graphically
                    JOptionPane.showMessageDialog(gui, e.getMessage(), "Error Running Script", JOptionPane.ERROR_MESSAGE);
                } else {
                    // no GUI; should not display graphically,
                    // only in the terminal
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }

            if (appMain.noGUI) {
                // nothing else to do
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
