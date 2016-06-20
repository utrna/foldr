/*
 * RNADisplay.java
 *
 * Created on June 9, 2016
 */

package rheat.GUI;

import rheat.base.RNA;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * @author Kevin Grant
 */
public class RNADisplay extends javax.swing.JComponent {

    private HelixImageGenerator helixImageGenerator = null;
    private RNA rnaData = null;

    public RNADisplay() {
    }

    /**
     * Specifies the renderer.
     */
    public void setHelixImageGenerator(HelixImageGenerator hig) {
        this.helixImageGenerator = hig;
    }

    /**
     * Specifies the data to render.
     */
    public void setRNA(RNA rna) {
        this.rnaData = rna;
    }

    /**
     * Ensures correct scrolling behavior.
     * @return the size of the view
     */
    public Dimension getPreferredSize() {
        return getSize();
    }

    /**
     * Called by the Java runtime to draw the component.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // object is actually Graphics2D type
        assert(g instanceof Graphics2D);
        Graphics2D g2D = (Graphics2D)g;
        if (helixImageGenerator != null) {
            // draw the RNA view
            helixImageGenerator.paintBackground(g2D, getSize());
            helixImageGenerator.paintRNA(rnaData, g2D, getSize());
        }
    }

}
