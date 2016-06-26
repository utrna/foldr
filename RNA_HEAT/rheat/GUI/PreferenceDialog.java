/*
 * PreferenceDialog.java
 *
 * Created on April 25, 2003, 3:09 PM
 */

package rheat.GUI;

import rheat.base.AppMain;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * Allows the user to customize certain properties.
 *
 * @author  jyzhang
 */
public class PreferenceDialog
extends RheatApp.RheatActionPanel {

    private AppMain appMain = null;
    private JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

    public PreferenceDialog(AppMain appMain) {
        super("Preferences");
        initComponents();
        this.appMain = appMain;
        this.programsDirTextField.setText(appMain.getPrefProgramsDir());
        this.inputDirTextField.setText(appMain.getPrefHelixDataDir());
        this.runDirTextField.setText(appMain.getPrefRunRootDir());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    private String askUserForDirectory() {
        int val = fc.showOpenDialog(this);
        if (val == fc.APPROVE_OPTION){
            String dir = fc.getSelectedFile().getAbsolutePath();
            return dir;
        }
        else{
            return null;
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        JComponent contentPane = this;
        jPanel0 = new javax.swing.JPanel();
        jLabel0 = new javax.swing.JLabel();
        programsDirTextField = new javax.swing.JTextField();
        browse0Btn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        inputDirTextField = new javax.swing.JTextField();
        browse1Btn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        runDirTextField = new javax.swing.JTextField();
        browse2Btn = new javax.swing.JButton();

        contentPane.setLayout(new java.awt.GridLayout(3, 0));

        jPanel0.setLayout(new BorderLayout());

        jLabel0.setText("Programs Directory: ");
        jPanel0.add(jLabel0, BorderLayout.WEST);

        programsDirTextField.setColumns(40);
        programsDirTextField.setToolTipText("The first place that is searched for external programs, before any system path.");
        jPanel0.add(programsDirTextField, BorderLayout.CENTER);

        browse0Btn.setText("...");
        browse0Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse0BtnActionPerformed(evt);
            }
        });

        jPanel0.add(browse0Btn, BorderLayout.EAST);

        contentPane.add(jPanel0);

        jPanel1.setLayout(new BorderLayout());

        jLabel1.setText("Input Directory: ");
        jPanel1.add(jLabel1, BorderLayout.WEST);

        inputDirTextField.setColumns(40);
        inputDirTextField.setToolTipText("The default location when browsing for input RNA files.");
        jPanel1.add(inputDirTextField, BorderLayout.CENTER);

        browse1Btn.setText("...");
        browse1Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse1BtnActionPerformed(evt);
            }
        });

        jPanel1.add(browse1Btn, BorderLayout.EAST);

        contentPane.add(jPanel1);

        jPanel2.setLayout(new BorderLayout());

        jLabel2.setText("Output Tree Root: ");
        jPanel2.add(jLabel2, BorderLayout.WEST);

        runDirTextField.setColumns(40);
        runDirTextField.setToolTipText("The directory in which to create experiment subdirectories (organized by date).");
        jPanel2.add(runDirTextField, BorderLayout.CENTER);

        browse2Btn.setText("...");
        browse2Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse2BtnActionPerformed(evt);
            }
        });

        jPanel2.add(browse2Btn, BorderLayout.EAST);

        contentPane.add(jPanel2);
    }

    private void browse0BtnActionPerformed(java.awt.event.ActionEvent evt) {
        String s = askUserForDirectory();
        if (s != null) {
            this.programsDirTextField.setText(s);
        }
    }

    private void browse1BtnActionPerformed(java.awt.event.ActionEvent evt) {
        String s = askUserForDirectory();
        if (s != null) {
            this.inputDirTextField.setText(s);
        }
    }

    private void browse2BtnActionPerformed(java.awt.event.ActionEvent evt) {
        String s = askUserForDirectory();
        if (s != null) {
            this.runDirTextField.setText(s);
        }
    }

    /**
     * Implementation of interface method; commits changes (as the
     * user has accepted the dialog).
     */
    void actionPanelAccepted() {
        try {
            appMain.setPreference("ProgramsDir", this.programsDirTextField.getText());
            appMain.setPreference("BPSEQ", this.inputDirTextField.getText());
            appMain.setPreference("RunRootDir", this.runDirTextField.getText());
            appMain.savePreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private javax.swing.JTextField programsDirTextField;
    private javax.swing.JTextField inputDirTextField;
    private javax.swing.JTextField runDirTextField;
    private javax.swing.JPanel jPanel0;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton browse0Btn;
    private javax.swing.JButton browse1Btn;
    private javax.swing.JButton browse2Btn;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel0;

}
