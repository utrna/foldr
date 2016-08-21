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
extends FilterDialog {

    public HelixFilterDialog() {
        super("Helix Length");
        initComponents();
    }

    /**
     * Implements FilterDialog interface.
     */
    public rheat.filter.Filter getNewFilter() {
        return filter;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        minHelixField = new FocusingField.SingleLine();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        maxHelixField = new FocusingField.SingleLine();

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

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
    }//GEN-END:initComponents

    /**
     * Implementation of interface method; commits changes (as the
     * user has accepted the dialog).
     */
    void actionPanelAccepted() {
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
            if (min > max){
                throw new RuntimeException ("Minimum cannot be > maximum.");
            }
            this.setVisible(false);
            newFilter.setArguments(max, min);
            this.filter = newFilter;
        }
        catch (NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Invalid Input: Bad number", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (RuntimeException ex){
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jLabel1;
    private FocusingField.SingleLine maxHelixField;
    private FocusingField.SingleLine minHelixField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    // End of variables declaration//GEN-END:variables
    private Filter filter; // null unless dialog was accepted by user

}
