/*
 * RNADisplay.java
 *
 * Created on June 9, 2016
 */

package rheat.GUI;

import rheat.base.AppMain;
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
    private Color defaultHelixColor = Color.red; // may change
    private Color defaultAnnotatedHelixColor = Color.green; // may change

    public RNADisplay() {
    }

    /**
     * Specifies a new color to use for painting helices that have
     * no other associated color (such as an annotation).
     *
     * The new value is not used until the next paint.
     */
    public void setDefaultHelixColor(String colorEncodedName) {
        try {
            Color newColor = Color.decode(colorEncodedName);
            this.defaultHelixColor = newColor;
        } catch (NumberFormatException e) {
            AppMain.log(AppMain.ERROR, "Color name '" + colorEncodedName + "' is not valid; ignoring.");
        }
    }

    /**
     * Specifies a new color to use for painting any annotated helix
     * whose annotation(s) do not have any associated colors.  (This
     * ensures that annotated helices will always look different.)
     *
     * The new value is not used until the next paint.
     */
    public void setDefaultAnnotatedHelixColor(String colorEncodedName) {
        try {
            Color newColor = Color.decode(colorEncodedName);
            this.defaultAnnotatedHelixColor = newColor;
        } catch (NumberFormatException e) {
            AppMain.log(AppMain.ERROR, "Color name '" + colorEncodedName + "' is not valid; ignoring.");
        }
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
                helixImageGenerator.paintRNA(rnaData, this.defaultHelixColor, this.defaultAnnotatedHelixColor,
                                             g2D, getSize(), HelixImageGenerator.RenderingType.NORMAL);
                if (overlayData != null) {
                    // color and data lists must be the same size
                    for (int i = 0; i < overlayData.size(); ++i) {
                        RNA otherRNA = overlayData.get(i);
                        Color color = overlayColors.get(i);
                        helixImageGenerator.paintRNA(otherRNA, color, this.defaultAnnotatedHelixColor,
                                                     g2D, getSize(), HelixImageGenerator.RenderingType.OVERLAY);
                    }
                }
            } finally {
                helixImageGenerator.endRender();
            }
        }
    }

}
