/*
 * HelixFilterDialog.java
 *
 * Created on April 4, 2003, 1:33 PM
 */

package rheat.GUI;

import rheat.base.*;
import rheat.filter.Filter;
import rheat.filter.MaxMinFilter;
import javax.swing.JOptionPane;

/**
 *
 * @author  jyzhang
 */
public class HelixFilterDialog
extends javax.swing.JDialog
implements FilterDialog {

    /** Creates new form HelixFilterDialog */
    public HelixFilterDialog(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
    }

    /**
     * Implements FilterDialog interface.
     */
    public rheat.filter.Filter run() {
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true); // blocks until dialog is done
        return filter;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        minHelixField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        maxHelixField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        acceptBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Helix Filter");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel3.setText("Find Helices under these constraints");
        jPanel1.add(jLabel3);

        getContentPane().add(jPanel1);

        jPanel2.setLayout(new java.awt.GridLayout(2, 0));

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setText("Minimum Helix Length");
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 16));
        jPanel5.add(jLabel2);

        minHelixField.setColumns(5);
        minHelixField.setText("1");
        jPanel5.add(minHelixField);

        jPanel2.add(jPanel5);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Maximum Helix Length");
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 16));
        jPanel4.add(jLabel1);

        maxHelixField.setColumns(5);
        maxHelixField.setText("none");
        jPanel4.add(maxHelixField);

        jPanel2.add(jPanel4);

        getContentPane().add(jPanel2);

        acceptBtn.setText("Accept");
        acceptBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptBtnActionPerformed(evt);
            }
        });

        jPanel3.add(acceptBtn);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        jPanel3.add(cancelBtn);

        getContentPane().add(jPanel3);

        pack();
    }//GEN-END:initComponents

    private void acceptBtnActionPerformed(java.awt.event.ActionEvent evt) {
        MaxMinFilter newFilter = new MaxMinFilter();
        try {
            int min = 1; 
            int max = Integer.MAX_VALUE;
            min = Integer.parseInt(this.minHelixField.getText());
            String maxS = this.maxHelixField.getText();
            if (!(maxS.equals("NONE") || maxS.equals("none"))){
                max = Integer.parseInt(maxS);
            }
            if (min < 1){
                throw new RuntimeException ("Minimum must be >= 1");
            }
            if (min >= max){
                throw new RuntimeException ("Minimum cannot be >= maximum.");
            }
            this.setVisible(false);
            String d = "Minimum helix length: " + min + "\n";
            d += "Maxium helix length: " + max + "\n";
            newFilter.setArguments(max, min);
            newFilter.setDescription(d);
            this.close();
            this.filter = newFilter;
        }
        catch (NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Invalid Input: Bad number", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (RuntimeException ex){
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {
        close();
    }

    private void close() {
        dispose();
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new HelixFilterDialog(new javax.swing.JFrame()).setVisible(true);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton acceptBtn;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField maxHelixField;
    private javax.swing.JTextField minHelixField;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton cancelBtn;
    // End of variables declaration//GEN-END:variables
    private Filter filter; // null unless dialog was accepted by user

}
