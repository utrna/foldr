/*
 * PreferenceDialog.java
 *
 * Created on April 25, 2003, 3:09 PM
 */

package rheat.GUI;

import rheat.base.AppMain;

import java.util.Map;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 *
 * @author  jyzhang
 */
public class PreferenceDialog
extends RheatApp.RheatActionPanel {

    private AppMain appMain = null;
    private JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

    /** Creates new form PreferenceDialog */
    public PreferenceDialog(AppMain appMain) {
        super("Preferences");
        initComponents();
        this.appMain = appMain;
        this.bpseqTextField.setText(appMain.getPrefHelixDataDir());
        this.undoTextField.setText(appMain.getPrefUndoDir());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    private String getDirectory(){
        int val = fc.showOpenDialog(this);
        if (val == fc.APPROVE_OPTION){
            String dir = fc.getSelectedFile().getAbsolutePath();
            return dir;
        }
        else{
            return null;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        JComponent contentPane = this;
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        bpseqTextField = new javax.swing.JTextField();
        browse1Btn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        undoTextField = new javax.swing.JTextField();
        browse2Btn = new javax.swing.JButton();

        contentPane.setLayout(new java.awt.GridLayout(4, 0));

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("BPSEQ Directory");
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

        jLabel2.setText("Undo Temp Directory");
        jPanel2.add(jLabel2);

        undoTextField.setColumns(40);
        jPanel2.add(undoTextField);

        browse2Btn.setText("...");
        browse2Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse2BtnActionPerformed(evt);
            }
        });

        jPanel2.add(browse2Btn);

        contentPane.add(jPanel2);
    }//GEN-END:initComponents

    private void browse2BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browse2BtnActionPerformed
        String s = getDirectory();
        if (s != null){
            this.undoTextField.setText(s);
        }
    }//GEN-LAST:event_browse2BtnActionPerformed

    private void browse1BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browse1BtnActionPerformed
        String s = getDirectory();
        if (s != null){
            this.bpseqTextField.setText(s);
        }
    }//GEN-LAST:event_browse1BtnActionPerformed

    /**
     * Implementation of interface method; commits changes (as the
     * user has accepted the dialog).
     */
    void actionPanelAccepted() {
        try {
            Map<String, String> pref = appMain.getPreferencesMap();
            pref.put("BPSEQ", this.bpseqTextField.getText());
            pref.put("Undo", this.undoTextField.getText());
            appMain.savePreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bpseqTextField;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton browse1Btn;
    private javax.swing.JButton browse2Btn;
    private javax.swing.JTextField undoTextField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

}
