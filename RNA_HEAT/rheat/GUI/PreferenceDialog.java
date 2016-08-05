/*
 * PreferenceDialog.java
 *
 * Created on April 25, 2003, 3:09 PM
 */

package rheat.GUI;

import rheat.base.AppMain;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Allows the user to customize certain properties.
 *
 * @author  jyzhang
 * @author  Kevin Grant
 */
public class PreferenceDialog
extends RheatApp.RheatActionPanel
implements ColorEditor.ChangeHandler {

    class SpectrumSampleView extends JComponent {

        private ArrayList<Color> colors = new ArrayList<Color>();
        private Rectangle2D.Double tmpRect = new Rectangle2D.Double(); // reused below

        /**
         * Specifies the color components.  Each color will occupy the
         * same proportional of the total range, as a filled rectangle
         * that spans one dimension of the view.
         *
         * The new values will be used on the next repaint.
         */
        void setColors(ArrayList<Color> colors) {
            this.colors = colors;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // object is actually Graphics2D type
            assert(g instanceof Graphics2D);
            Graphics2D g2D = (Graphics2D)g;
            // for now, always rendered horizontally (could be configurable)
            final double elementWidth = (getSize().getWidth() / (this.colors.size() - 1));
            double offset = 0;
            for (int i = 0; i < colors.size(); ++i) {
                g2D.setColor(this.colors.get(i));
                this.tmpRect.setRect(offset, 0, elementWidth, getSize().getHeight());
                g2D.fill(this.tmpRect);
                offset += elementWidth;
            }
        }

    }

    private AppMain appMain = null;
    private JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

    public PreferenceDialog(AppMain appMain) {
        super("Preferences");
        initComponents();
        this.appMain = appMain;
        this.programsDirTextField.setText(appMain.getPrefProgramsDir());
        this.inputDirTextField.setText(appMain.getPrefHelixDataDir());
        this.runDirTextField.setText(appMain.getPrefRunRootDir());
        this.gridSizeField.setText(String.format("%.2f", appMain.getPrefGridFraction()));
        this.defaultHelixColorEditor.setColorString(appMain.getPrefDefaultHelixColor());
        this.defaultTagColorEditor.setColorString(appMain.getPrefDefaultHelixAnnotationColor());
        this.spectrumStartColorEditor.setColorString(appMain.getPrefSpectrumStartColor());
        this.spectrum50PercentColorEditor.setColorString(appMain.getPrefSpectrum50PercentColor());
        this.spectrumEndColorEditor.setColorString(appMain.getPrefSpectrumEndColor());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    protected boolean isResizable() { return true; }

    /**
     * Called by ColorEditor when the color is changed; responds
     * by synchronizing the spectrum sample display.
     */
    @Override
    public void colorEditorDidChange(ColorEditor editor) {
        // NOTE: the "editor" is not checked because it currently
        // doesn’t matter (it is assumed that the update must be
        // referring to the spectrum edits); if that changes, an
        // "if" may need to be added to verify which editor changed
        ArrayList<Color> colors = new ArrayList<Color>();
        Color start = spectrumStartColorEditor.getColor();
        Color middle = spectrum50PercentColorEditor.getColor();
        Color end = spectrumEndColorEditor.getColor();
        // current color edit may not be valid (e.g. blank field)
        if (start == null) {
            return;
        }
        if (middle == null) {
            return;
        }
        if (end == null) {
            return;
        }
        HelixImageGenerator.generateGradient(start, middle, end, colors);
        this.spectrumSampleView.setColors(colors);
        this.spectrumSampleView.repaint();
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

        labelsPane = new javax.swing.JPanel();
        itemsPane = new javax.swing.JPanel();

        jPanel0 = new javax.swing.JPanel();
        programsDirTextField = new javax.swing.JTextField();
        browse0Btn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        inputDirTextField = new javax.swing.JTextField();
        browse1Btn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        runDirTextField = new javax.swing.JTextField();
        browse2Btn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        gridSizeField = new javax.swing.JTextField();
        defaultHelixColorEditor = new rheat.GUI.ColorEditor();
        defaultTagColorEditor = new rheat.GUI.ColorEditor();
        spectrumStartColorEditor = new rheat.GUI.ColorEditor();
        spectrum50PercentColorEditor = new rheat.GUI.ColorEditor();
        spectrumEndColorEditor = new rheat.GUI.ColorEditor();
        spectrumSampleView = new SpectrumSampleView();

        contentPane.setLayout(new BorderLayout());
        // HOW TO ADD NEW ROWS:
        // - increment "numRows" below
        // - add a JLabel for the new row to "labelsPane"
        // - put the new setting’s controls in a new container (like a JPanel)
        // - add that container to "itemsPane"
        // (see examples below!)
        final int numRows = 10;
        labelsPane.setLayout(new GridLayout(numRows, 0));
        itemsPane.setLayout(new GridLayout(numRows, 0));

        jPanel0.setLayout(new BorderLayout());
        jPanel0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        programsDirTextField.setColumns(40);
        programsDirTextField.setToolTipText("The first place that is searched for external programs, before any system path.");
        jPanel0.add(programsDirTextField, BorderLayout.CENTER);

        browse0Btn.setText("…");
        browse0Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse0BtnActionPerformed(evt);
            }
        });

        jPanel0.add(browse0Btn, BorderLayout.EAST);

        labelsPane.add(new JLabel("Programs Directory: "));
        itemsPane.add(jPanel0);

        jPanel1.setLayout(new BorderLayout());
        jPanel1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        inputDirTextField.setColumns(40);
        inputDirTextField.setToolTipText("The default location when browsing for input RNA files.");
        jPanel1.add(inputDirTextField, BorderLayout.CENTER);

        browse1Btn.setText("…");
        browse1Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse1BtnActionPerformed(evt);
            }
        });

        jPanel1.add(browse1Btn, BorderLayout.EAST);

        labelsPane.add(new JLabel("Input Directory: "));
        itemsPane.add(jPanel1);

        jPanel2.setLayout(new BorderLayout());
        jPanel2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        runDirTextField.setColumns(40);
        runDirTextField.setToolTipText("The directory in which to create experiment subdirectories (organized by date).");
        jPanel2.add(runDirTextField, BorderLayout.CENTER);

        browse2Btn.setText("…");
        browse2Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse2BtnActionPerformed(evt);
            }
        });

        jPanel2.add(browse2Btn, BorderLayout.EAST);

        labelsPane.add(new JLabel("Output Tree Root: "));
        itemsPane.add(jPanel2);

        jPanel3.setLayout(new BorderLayout());
        jPanel3.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        gridSizeField.setColumns(6); // arbitrary
        gridSizeField.setToolTipText("Grid spacing as fraction of total width (e.g. 0.125 is 8 squares across).  Since grid is centered, should be less than 0.5.  Very small values may also be ignored.");
        jPanel3.add(gridSizeField, BorderLayout.WEST);
        jPanel3.add(new JLabel(" (fraction of total width)"), BorderLayout.CENTER);

        labelsPane.add(new JLabel("Default Grid Size: "));
        itemsPane.add(jPanel3);

        //defaultHelixColorEditor.setChangeHandler(this);
        defaultHelixColorEditor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        defaultHelixColorEditor.setTitle("Default Helix Color");
        defaultHelixColorEditor.setToolTipText("The color to use by default for helices.");

        labelsPane.add(new JLabel("Default Helix Color: "));
        itemsPane.add(defaultHelixColorEditor);

        //defaultTagColorEditor.setChangeHandler(this);
        defaultTagColorEditor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        defaultTagColorEditor.setTitle("Default Annotation Color");
        defaultTagColorEditor.setToolTipText("The color to use by default for helices that have been annotated.  (Specific tag colors may override this.)");

        labelsPane.add(new JLabel("Default Annotation Color: "));
        itemsPane.add(defaultTagColorEditor);

        spectrumStartColorEditor.setChangeHandler(this);
        spectrumStartColorEditor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        spectrumStartColorEditor.setTitle("Spectrum Start Color");
        spectrumStartColorEditor.setToolTipText("For helices colored using a gradient, this determines the beginning of the spectrum of colors.");

        labelsPane.add(new JLabel("Spectrum Start Color: "));
        itemsPane.add(spectrumStartColorEditor);

        spectrum50PercentColorEditor.setChangeHandler(this);
        spectrum50PercentColorEditor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        spectrum50PercentColorEditor.setTitle("Spectrum 50% Color");
        spectrum50PercentColorEditor.setToolTipText("For helices colored using a gradient, this determines the middle of the spectrum of colors.");

        labelsPane.add(new JLabel("Spectrum 50% Color: "));
        itemsPane.add(spectrum50PercentColorEditor);

        spectrumEndColorEditor.setChangeHandler(this);
        spectrumEndColorEditor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        spectrumEndColorEditor.setTitle("Spectrum End Color");
        spectrumEndColorEditor.setToolTipText("For helices colored using a gradient, this determines the end of the spectrum of colors.");

        labelsPane.add(new JLabel("Spectrum End Color: "));
        itemsPane.add(spectrumEndColorEditor);

        spectrumSampleView.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.white));

        labelsPane.add(new JLabel(""));
        itemsPane.add(spectrumSampleView);

        contentPane.add(labelsPane, BorderLayout.WEST);
        contentPane.add(itemsPane, BorderLayout.CENTER);
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
            appMain.setPreference("GridFraction", this.gridSizeField.getText());
            appMain.setPreference("DefaultHelixColor", this.defaultHelixColorEditor.getColorString());
            appMain.setPreference("DefaultHelixAnnotationColor", this.defaultTagColorEditor.getColorString());
            appMain.setPreference("SpectrumStartColor", this.spectrumStartColorEditor.getColorString());
            appMain.setPreference("Spectrum50PercentColor", this.spectrum50PercentColorEditor.getColorString());
            appMain.setPreference("SpectrumEndColor", this.spectrumEndColorEditor.getColorString());
            appMain.savePreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private javax.swing.JTextField programsDirTextField;
    private javax.swing.JTextField inputDirTextField;
    private javax.swing.JTextField runDirTextField;
    private javax.swing.JTextField gridSizeField;
    private rheat.GUI.ColorEditor defaultHelixColorEditor;
    private rheat.GUI.ColorEditor defaultTagColorEditor;
    private rheat.GUI.ColorEditor spectrumStartColorEditor;
    private rheat.GUI.ColorEditor spectrum50PercentColorEditor;
    private rheat.GUI.ColorEditor spectrumEndColorEditor;
    private SpectrumSampleView spectrumSampleView;
    private javax.swing.JPanel labelsPane;
    private javax.swing.JPanel itemsPane;
    private javax.swing.JPanel jPanel0;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton browse0Btn;
    private javax.swing.JButton browse1Btn;
    private javax.swing.JButton browse2Btn;

}
