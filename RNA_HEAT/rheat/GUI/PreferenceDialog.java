/*
 * PreferenceDialog.java
 *
 * Created on April 25, 2003, 3:09 PM
 */

package rheat.GUI;

import rheat.base.AppMain;

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
        this.bpseqTextField.setText(appMain.getPrefHelixDataDir());
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        bpseqTextField = new javax.swing.JTextField();
        browse1Btn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        runDirTextField = new javax.swing.JTextField();
        browse2Btn = new javax.swing.JButton();

        contentPane.setLayout(new java.awt.GridLayout(4, 0));

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Input Directory");
        jPanel1.add(jLabel1);

        bpseqTextField.setColumns(40);
        jPanel1.add(bpseqTextField);

        browse1Btn.setText("...");
        browse1Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse1BtnActionPerformed(evt);
            }
        });

        jPanel1.add(browse1Btn);

        contentPane.add(jPanel1);

        jLabel2.setText("Output Directory");
        jPanel2.add(jLabel2);

        runDirTextField.setColumns(40);
        jPanel2.add(runDirTextField);

        browse2Btn.setText("...");
        browse2Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse2BtnActionPerformed(evt);
            }
        });

        jPanel2.add(browse2Btn);

        contentPane.add(jPanel2);
    }

    private void browse1BtnActionPerformed(java.awt.event.ActionEvent evt) {
        String s = askUserForDirectory();
        if (s != null) {
            this.bpseqTextField.setText(s);
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
            appMain.setPreference("BPSEQ", this.bpseqTextField.getText());
            appMain.setPreference("RunRootDir", this.runDirTextField.getText());
            appMain.savePreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private javax.swing.JTextField bpseqTextField;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton browse1Btn;
    private javax.swing.JButton browse2Btn;
    private javax.swing.JTextField runDirTextField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel1;

}
