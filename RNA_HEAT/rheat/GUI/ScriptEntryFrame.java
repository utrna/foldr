package rheat.GUI;

import rheat.base.AppMain;
import static rheat.script.JSUtil.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A window for entering script commands.
 *
 * @author Kevin Grant
 */
public class ScriptEntryFrame
extends javax.swing.JInternalFrame
implements PropertyChangeListener {

    ScriptEntryFrame(RheatApp gui) {
        this.gui = gui;
        this.preferencesDir = gui.getAppMain().makePath(System.getProperty("user.home"), ".rheat");
        this.historyScript = gui.getAppMain().makePath(this.preferencesDir, "history.js");
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("Scripting Console");
        setMinimumSize(new java.awt.Dimension(610, 200));
        setNormalBounds(new java.awt.Rectangle(500, 400, 650, 300)); // arbitrary
        setBounds(new java.awt.Rectangle(500, 400, 650, 300));
        this.gui.getAppMain().addPropertyChangeListener(AppMain.PROPERTY_WORKING_DIR, this);
        // automatically restore any history from last time (may fail silently)
        try (BufferedReader reader = new BufferedReader(new FileReader(this.historyScript))) {
            historyPane.read(reader, this.historyScript/* description object */);
            scrollToBottom();
        } catch (FileNotFoundException e) {
            // not an error
            this.gui.getAppMain().log(AppMain.INFO, "No previous history file was found.");
        } catch (IOException e) {
            // not an error
            e.printStackTrace();
        }
    }

    /**
     * Implements PropertyChangeListener; used to update info display.
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(AppMain.PROPERTY_WORKING_DIR)) {
            this.workingDirPathLabel.setText((String)event.getNewValue());
        }
    }

    /**
     * Writes command history to disk.
     */
    public void saveHistory() throws IOException {
        new File(this.preferencesDir).mkdirs(); // ensure parent directories exist; ignore "boolean" result
        PrintWriter pw = new PrintWriter(this.historyScript);
        pw.write(this.historyPane.getText());
        pw.flush();
        pw.close();
    }

    /**
     * Creates and configures GUI elements.
     */
    private void initComponents() {
        final RheatApp gui = this.gui;
        this.scrollHistoryPane = new JScrollPane();
        JLabel historyLabel = new JLabel("Command History:");
        this.runSelectionButton = new JButton("Run Selected");
        this.runSelectionButton.setToolTipText("Reruns commands from history that have been selected with the mouse.");
        this.runSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runSelectedLines();
            }
        });
        this.addDividerButton = new JButton("Add Divider");
        this.addDividerButton.setToolTipText("Inserts a comment that has a dividing line.  Useful for partitioning history fragments.");
        this.addDividerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // insert a comment with a dividing-line
                runCommandLines("// -----------------------------------------------------------------");
            }
        });
        this.addTimeStampButton = new JButton("Add Timestamp");
        this.addTimeStampButton.setToolTipText("Inserts a comment that includes the current date and time.");
        this.addTimeStampButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // insert a comment with the current date and time
                runCommandLines("// " + AppMain.getDateTimeString());
            }
        });
        this.clearButton = new JButton("Clear…");
        this.clearButton.setToolTipText("DELETES all history commands.");
        this.clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // delete everything from the script history
                if (gui.confirm("Clear all scripting commands from the history area?")) {
                    historyPane.setText("");
                    commandPane.setText("");
                    try {
                        saveHistory();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Box paneHistoryControls = new Box(BoxLayout.X_AXIS);
        paneHistoryControls.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        paneHistoryControls.add(Box.createHorizontalStrut(10));
        paneHistoryControls.add(historyLabel);
        paneHistoryControls.add(Box.createHorizontalStrut(20));
        paneHistoryControls.add(this.runSelectionButton);
        paneHistoryControls.add(Box.createHorizontalStrut(5));
        paneHistoryControls.add(this.addDividerButton);
        paneHistoryControls.add(Box.createHorizontalStrut(5));
        paneHistoryControls.add(this.addTimeStampButton);
        paneHistoryControls.add(Box.createHorizontalStrut(5));
        paneHistoryControls.add(this.clearButton);
        this.historyPane = new JTextArea();
        this.historyPane.setBackground(gui.getBackground());
        this.historyPane.setEditable(false);
        this.historyPane.setFont(RheatApp.getMonospacedFont(this.historyPane.getFont(), 12));
        this.historyPane.setRows(7);
        this.scrollHistoryPane.setViewportView(this.historyPane);
        this.scrollHistoryPane.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white)); // arbitrary insets
        JPanel paneHistory = new JPanel();
        paneHistory.setLayout(new BorderLayout());
        paneHistory.add(paneHistoryControls, BorderLayout.NORTH);
        paneHistory.add(this.scrollHistoryPane, BorderLayout.CENTER);
        JPanel padPaneHistory = new JPanel();
        padPaneHistory.setLayout(new BorderLayout());
        padPaneHistory.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        padPaneHistory.add(paneHistory, BorderLayout.CENTER);
        this.scrollCommandPane = new JScrollPane();
        this.commandPane = new FocusingField.MultiLine(this.scrollCommandPane/* border component */);
        this.commandPane.setToolTipText("Type new scripting commands here, and click Run to try them.");
        this.commandPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.commandPane.setEditable(true);
        this.commandPane.setFont(RheatApp.getMonospacedFont(this.commandPane.getFont(), 12));
        this.commandPane.setRows(2);
        this.scrollCommandPane.setViewportView(this.commandPane);
        this.scrollCommandPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3)); // arbitrary insets
        this.runButton = new JButton("Run");
        this.runButton.setToolTipText("Interprets given text as a script file.  Any errors are displayed; otherwise, commands move into the history area.");
        this.runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runEnteredCommand();
            }
        });
        JLabel colorLabel = new JLabel("Color helper: ");
        colorLabel.setEnabled(false); // make gray
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BorderLayout());
        colorPanel.add(colorLabel, BorderLayout.WEST);
        colorPanel.add(new ColorEditor(), BorderLayout.CENTER); // color is not used; just helps when writing scripts
        JLabel dirLabel = new JLabel("Current directory: ");
        dirLabel.setEnabled(false); // make gray
        this.workingDirPathLabel = new JLabel(""); this.workingDirPathLabel.setEnabled(false); // make gray
        this.workingDirPathLabel.setToolTipText("The location of files specified only by name.  Use 'rheat.setWorkingDir()' to set new value (may be relative to old one).");
        JButton browseButton = new JButton("…");
        browseButton.setToolTipText("For convenience; browse to select a directory, then 'rheat.setWorkingDir()' will be automatically called for you.");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JFileChooser fc = new JFileChooser(workingDirPathLabel.getText());
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fc.showOpenDialog(gui) == fc.APPROVE_OPTION) {
                    String dir = fc.getSelectedFile().getAbsolutePath();
                    // IMPORTANT: if the value contains backslashes (e.g. Windows paths),
                    // it will not be read back correctly by JavaScript later; translate
                    // any backslashes into double-backslash to escape them
                    String command = "rheat.setWorkingDir(\'" + jsEscape(dir) + "\')";
                    runCommandLines(command);
                }
            }
        });
        JPanel dirPanel = new JPanel();
        dirPanel.setLayout(new BorderLayout());
        dirPanel.add(dirLabel, BorderLayout.WEST);
        dirPanel.add(this.workingDirPathLabel, BorderLayout.CENTER);
        dirPanel.add(browseButton, BorderLayout.EAST);
        Box paneHelpers = new Box(BoxLayout.Y_AXIS);
        paneHelpers.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        paneHelpers.add(colorPanel);
        paneHelpers.add(dirPanel);
        paneHelpers.add(new JSeparator());
        JPanel paneCommandControls = new JPanel();
        paneCommandControls.setLayout(new BorderLayout());
        paneCommandControls.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        paneCommandControls.add(this.runButton, BorderLayout.NORTH);
        JPanel paneCommands = new JPanel();
        paneCommands.setLayout(new BorderLayout());
        paneCommands.add(paneHelpers, BorderLayout.NORTH);
        paneCommands.add(this.scrollCommandPane, BorderLayout.CENTER);
        paneCommands.add(paneCommandControls, BorderLayout.EAST);
        JPanel padPaneCommands = new JPanel();
        padPaneCommands.setLayout(new BorderLayout());
        padPaneCommands.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        padPaneCommands.add(paneCommands, BorderLayout.CENTER);
        final boolean LIVE_REPAINT = true; // see JSplitPane constructor
        JSplitPane joinTopBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, LIVE_REPAINT, padPaneHistory, padPaneCommands);
        joinTopBottom.setResizeWeight(1.0); // when window resizes, prefer sizing the history region
        this.setLayout(new BorderLayout());
        this.getContentPane().add(joinTopBottom, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        pack();
    }

    /**
     * Normally you should call runCommandLines() to execute the
     * commands, after which (upon success) they will be added to
     * history automatically using this command.  In rare cases
     * though, perhaps because you need to run the command manually,
     * you can call this directly to add it to history anyway.
     */
    public void addCommandToHistory(String scriptingCommands) {
        this.historyPane.append(scriptingCommands);
        if (!this.historyPane.getText().endsWith("\n")) {
            this.historyPane.append("\n");
        }
        scrollToBottom();
    }

    /**
     * Evaluates the specified code and presents any errors to
     * the user.  If there are no errors, the commands are added
     * to the history buffer and the entry pane is cleared.
     */
    public void runCommandLines(String scriptingCommands) {
        try {
            // NOTE: the side effects of evaluating code could
            // be almost anything (e.g. this could cause files
            // to open, settings to change, displays to update,
            // etc.)
            this.gui.evaluateScriptCode(scriptingCommands);
            // success; add to history
            addCommandToHistory(scriptingCommands);
            this.commandPane.setText("");
            saveHistory();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            AppMain.log(AppMain.INFO, "Script failed: " + scriptingCommands);
            this.gui.showScriptError(e);
        }
    }

    /**
     * Evaluates the entered code and presents any errors to
     * the user.  If there are no errors, the command is added
     * to the history buffer and the entry pane is cleared.
     */
    private void runEnteredCommand() {
        runCommandLines(this.commandPane.getText());
    }

    /**
     * Evaluates any currently-selected history lines as if
     * the user had entered each of them manually.
     */
    private void runSelectedLines() {
        String targetCode = this.historyPane.getSelectedText();
        runCommandLines(targetCode);
    }

    /**
     * Scrolls the history to the bottom so new commands are visible.
     */
    private void scrollToBottom() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollHistoryPane.getVerticalScrollBar().setValue(scrollHistoryPane.getVerticalScrollBar().getMaximum());
            }
        });
    }

    private JScrollPane scrollCommandPane;
    private JScrollPane scrollHistoryPane;
    private FocusingField.MultiLine commandPane;
    private JTextArea historyPane;
    private JButton addDividerButton;
    private JButton addTimeStampButton;
    private JButton clearButton;
    private JButton runButton;
    private JButton runSelectionButton;
    private JLabel workingDirPathLabel;
    private String preferencesDir;
    private String historyScript;
    private RheatApp gui;

}
