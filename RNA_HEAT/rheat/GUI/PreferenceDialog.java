/*
 * PreferenceDialog.java
 *
 * Created on April 25, 2003, 3:09 PM
 */

package rheat.GUI;

import java.util.HashMap;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import javax.swing.JFileChooser;
/**
 *
 * @author  jyzhang
 */
public class PreferenceDialog extends javax.swing.JDialog {
    
    private HashMap<String, String> pref;
    private String preffile;
    private JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
    
    /** Creates new form PreferenceDialog */
    public PreferenceDialog(java.awt.Frame parent, boolean modal, String file, HashMap<String, String> p) {
        super(parent, modal);
        initComponents();
        pref = p;
        preffile = file;
        this.bpseqTextField.setText(pref.get("BPSEQ"));
        this.undoTextField.setText(pref.get("Undo"));
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        bpseqTextField = new javax.swing.JTextField();
        browse1Btn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        undoTextField = new javax.swing.JTextField();
        browse2Btn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        acceptButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridLayout(4, 0));

        setTitle("Preferences");
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("BPSEQ Directory");
        jPanel1.add(jLabel1);

        bpseqTextField.setColumns(20);
        jPanel1.add(bpseqTextField);

        browse1Btn.setText("...");
        browse1Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse1BtnActionPerformed(evt);
            }
        });

        jPanel1.add(browse1Btn);

        getContentPane().add(jPanel1);

        jLabel2.setText("Undo Temp Directory");
        jPanel2.add(jLabel2);

        undoTextField.setColumns(18);
        jPanel2.add(undoTextField);

        browse2Btn.setText("...");
        browse2Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse2BtnActionPerformed(evt);
            }
        });

        jPanel2.add(browse2Btn);

        getContentPane().add(jPanel2);

        acceptButton.setText("Accept");
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });

        jPanel3.add(acceptButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel3.add(cancelButton);

        getContentPane().add(jPanel3);

        pack();
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
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        close();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
        try {
            pref.put("BPSEQ", this.bpseqTextField.getText());
            pref.put("Undo", this.undoTextField.getText());
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(preffile))));
            oos.writeObject(pref);
            oos.flush();
        }
        catch (Exception ex2){
            
        }
        close();
    }//GEN-LAST:event_acceptButtonActionPerformed
    
    private void close(){
        dispose();
    }
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        close();
    }//GEN-LAST:event_closeDialog
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new PreferenceDialog(new javax.swing.JFrame(), true, null, null).show();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bpseqTextField;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton browse1Btn;
    private javax.swing.JButton acceptButton;
    private javax.swing.JButton browse2Btn;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField undoTextField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
