/*
 * DiagonalFilterDialog.java
 *
 * Created on April 29, 2003, 11:20 AM
 */

package rheat.GUI;

import rheat.base.*;
import rheat.filter.DiagonalDistanceFilter;
import rheat.filter.Filter;
import javax.swing.JOptionPane;

/**
 *
 * @author  jyzhang
 */
public class DiagonalFilterDialog
extends FilterDialog {

    public DiagonalFilterDialog() {
        super("Diagonal Distance");
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        minDistanceField = new FocusingField.SingleLine();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        maxDistanceField = new FocusingField.SingleLine();

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Specify the helix range from the diagonal:");
        jPanel1.add(jLabel1);

        getContentPane().add(jPanel1);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setText("Minimum Distance");
        jLabel2.setPreferredSize(new java.awt.Dimension(120, 16));
        jPanel2.add(jLabel2);

        minDistanceField.setColumns(4);
        minDistanceField.setText("1");
        jPanel2.add(minDistanceField);

        getContentPane().add(jPanel2);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel3.setText("Maximum Distance");
        jLabel3.setPreferredSize(new java.awt.Dimension(120, 16));
        jPanel3.add(jLabel3);

        maxDistanceField.setColumns(4);
        maxDistanceField.setText("none");
        jPanel3.add(maxDistanceField);

        getContentPane().add(jPanel3);
    }//GEN-END:initComponents

    /**
     * Implementation of interface method; commits changes (as the
     * user has accepted the dialog).
     */
    void actionPanelAccepted() {
        DiagonalDistanceFilter newFilter = new DiagonalDistanceFilter();
        try {
            int min = 1; 
            int max = Integer.MAX_VALUE;
            min = Integer.parseInt(this.minDistanceField.getText());
            String maxS = this.maxDistanceField.getText();
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private FocusingField.SingleLine minDistanceField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    private FocusingField.SingleLine maxDistanceField;
    // End of variables declaration//GEN-END:variables
    private Filter filter; // null unless dialog was accepted by user

}
