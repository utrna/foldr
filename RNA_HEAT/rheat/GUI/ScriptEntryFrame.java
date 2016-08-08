package rheat.GUI;

import rheat.base.AppMain;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        setMinimumSize(new java.awt.Dimension(610, 80));
        setNormalBounds(new java.awt.Rectangle(500, 450, 650, 250)); // arbitrary
        setBounds(new java.awt.Rectangle(500, 450, 650, 250));
        this.gui.getAppMain().addPropertyChangeListener(AppMain.PROPERTY_WORKING_DIR, this);
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
        this.commandPane = new JTextArea();
        this.commandPane.setToolTipText("Type new scripting commands here, and click Run to try them.");
        this.commandPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.commandPane.setEditable(true);
        this.commandPane.setFont(RheatApp.getMonospacedFont(this.commandPane.getFont(), 12));
        this.commandPane.setRows(2);
        this.scrollCommandPane.setViewportView(this.commandPane);
        this.scrollCommandPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3)); // arbitrary insets
        JLabel dirLabel = new JLabel("Current directory: ");
        dirLabel.setEnabled(false); // make gray
        this.workingDirPathLabel = new JLabel("");
        this.workingDirPathLabel.setEnabled(false); // make gray
        this.workingDirPathLabel.setToolTipText("The location of files specified only by name.  Use 'rheat.setWorkingDir()' to set new value (may be relative to old one).");
        JPanel dirPanel = new JPanel();
        dirPanel.setLayout(new BorderLayout());
        dirPanel.add(new JSeparator(), BorderLayout.NORTH);
        dirPanel.add(dirLabel, BorderLayout.WEST);
        dirPanel.add(this.workingDirPathLabel, BorderLayout.CENTER);
        this.runButton = new JButton("Run");
        this.runButton.setToolTipText("Interprets given text as a script file.  Any errors are displayed; otherwise, commands move into the history area.");
        this.runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runEnteredCommand();
            }
        });
        JPanel paneCommandControls = new JPanel();
        paneCommandControls.setLayout(new BorderLayout());
        paneCommandControls.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        paneCommandControls.add(this.runButton, BorderLayout.NORTH);
        JPanel paneCommands = new JPanel();
        paneCommands.setLayout(new BorderLayout());
        paneCommands.add(this.scrollCommandPane, BorderLayout.CENTER);
        paneCommands.add(paneCommandControls, BorderLayout.EAST);
        paneCommands.add(dirPanel, BorderLayout.SOUTH);
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
     * Evaluates the specified code and presents any errors to
     * the user.  If there are no errors, the commands are added
     * to the history buffer and the entry pane is cleared.
     */
    private void runCommandLines(String scriptingCommands) {
        try {
            // NOTE: the side effects of evaluating code could
            // be almost anything (e.g. this could cause files
            // to open, settings to change, displays to update,
            // etc.)
            this.gui.evaluateScriptCode(scriptingCommands);
            // success; add to history
            this.historyPane.append(scriptingCommands);
            if (!this.historyPane.getText().endsWith("\n")) {
                this.historyPane.append("\n");
            }
            this.commandPane.setText("");
            saveHistory();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
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

    private JScrollPane scrollCommandPane;
    private JScrollPane scrollHistoryPane;
    private JTextArea commandPane;
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
