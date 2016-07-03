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
import java.util.ArrayList;
import javax.swing.JComponent;

/**
 * @author Kevin Grant
 */
public class RNADisplay extends javax.swing.JComponent {

    private HelixImageGenerator helixImageGenerator = null;
    private RNA rnaData = null;
    private ArrayList<RNA> overlayData = null;

    public RNADisplay() {
    }

    /**
     * Specifies the renderer.
     */
    public void setHelixImageGenerator(HelixImageGenerator hig) {
        this.helixImageGenerator = hig;
    }

    /**
     * Specifies new data to render and removes any overlays.
     */
    public void setRNA(RNA rna) {
        this.rnaData = rna;
        this.overlayData = null;
    }

    /**
     * Specifies additional data to render on top of normal data.
     */
    public void addOverlayRNA(RNA overlay) {
        if (this.overlayData == null) {
            this.overlayData = new ArrayList<RNA>();
        }
        this.overlayData.add(overlay);
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
            try {
                helixImageGenerator.beginRender();
                helixImageGenerator.paintBackground(g2D, getSize());
                helixImageGenerator.paintRNA(rnaData, g2D, getSize(), HelixImageGenerator.RenderingType.NORMAL);
                if (overlayData != null) {
                    for (RNA otherRNA : overlayData) {
                        // TODO: vary the colors
                        helixImageGenerator.paintRNA(otherRNA, g2D, getSize(), HelixImageGenerator.RenderingType.OVERLAY);
                    }
                }
            } finally {
                helixImageGenerator.endRender();
            }
        }
    }

}
