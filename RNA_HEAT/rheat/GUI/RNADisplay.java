/*
 * RNADisplay.java
 *
 * Created on June 9, 2016
 */

package rheat.GUI;

import rheat.base.RNA;
import java.awt.Color;
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
    private ArrayList<Color> overlayColors = null;

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
        this.overlayColors = null;
    }

    /**
     * Specifies additional data to render on top of normal data.
     */
    public void addOverlayRNA(RNA overlay, Color color) {
        if (this.overlayData == null) {
            this.overlayData = new ArrayList<RNA>();
        }
        if (this.overlayColors == null) {
            this.overlayColors = new ArrayList<Color>();
        }
        this.overlayData.add(overlay);
        this.overlayColors.add(color);
    }

    /**
     * Ensures correct scrolling behavior.
     * @return the size of the view
     */
    public Dimension getPreferredSize() {
        if (helixImageGenerator != null) {
            return helixImageGenerator.getSize();
        }
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
                // FIXME: make base helix color customizable
                helixImageGenerator.paintRNA(rnaData, Color.red, g2D, getSize(), HelixImageGenerator.RenderingType.NORMAL);
                if (overlayData != null) {
                    // color and data lists must be the same size
                    for (int i = 0; i < overlayData.size(); ++i) {
                        RNA otherRNA = overlayData.get(i);
                        Color color = overlayColors.get(i);
                        helixImageGenerator.paintRNA(otherRNA, color, g2D, getSize(), HelixImageGenerator.RenderingType.OVERLAY);
                    }
                }
            } finally {
                helixImageGenerator.endRender();
            }
        }
    }

}
