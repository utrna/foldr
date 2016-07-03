/*
 * HelixImageGenerator.java
 *
 * Created on March 26, 2003, 3:52 PM
 */

package rheat.GUI;

import rheat.base.*;
import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import javax.swing.ImageIcon;

/**
 *
 * @author  jyzhang
 */
public class HelixImageGenerator {

    public enum HelixType {
        ACTUAL,
        PREDICTED
    };

    public enum RenderingType {
        NORMAL,
        OVERLAY
    };

    public static int VIEW_2D = 0;
    public static int VIEW_FLAT = 1;

    /**
     * Sent to PropertyChangeListener registered with observeSelectedHelix().
     */
    public static String PROPERTY_SELECTED_HELIX = "PROPERTY_SELECTED_HELIX";

    private int length;
    private double zoomFactor;
    private final double effectiveZoomFactor = 4.0; // a single point occupies this many square pixels at zoom level 1
    private int imageType = this.VIEW_2D;
    private boolean renderInProgress = false; // see beginRender()/endRender()
    private Point2D.Double clickPoint = new Point2D.Double(); // coordinates are relative to data (current zoom level)
    private Line2D.Double tmpLine = new Line2D.Double(); // used for various reasons (saves heap allocations)
    private BufferedImage helix2D;
    private BufferedImage helixFlat;
    // a "butt" cap is helpful because it shows exactly how long a line is
    // (the line is cut off at the end points instead of being extended
    // with a round or square shape); unfortunately this would make a
    // single point completely invisible so the length-1 case uses a shape
    private Stroke strokeLength1Helix = new BasicStroke(0.25f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private Stroke strokeNormalHelix = new BasicStroke(0.25f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private Stroke strokeSelectedHelix = new BasicStroke(0.75f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private Stroke strokeLength1SelectedHelix = new BasicStroke(0.75f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private Stroke strokeHelixHandle = new BasicStroke(0.1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private Stroke strokeFlatNormalHelix = new BasicStroke(0.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private Stroke strokeAxisLine = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private Stroke strokeClickLocation = new BasicStroke(0.25f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private Color helixColorActual = Color.blue;
    private Color helixColorPredicted = Color.red;
    private Color helixColorSelected = Color.blue;
    private Helix selectedHelix = null;
    private Helix originalSelection = null; // preserved by beginRender()/endRender()
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** Creates a new instance of HelixImageGenerator */
    public HelixImageGenerator(int l) {
        length = l;
        setZoomLevel(1);
    }

    /**
     * Arranges to notify the given listener (via propertyChange())
     * when the currently-selected helix changes.  This is especially
     * useful when responding to events, to make sure that the value
     * returned by getSelectedHelix() is consistent with the reaction
     * to the event.
     */
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    /**
     * Returns the width and height at current zoom level.
     */
    public Dimension getSize() {
        return new Dimension((int)(zoomFactor * length), (int)(zoomFactor * length));
    }

    /**
     * Current display-scale factor (both X and Y directions).
     * @return scale factor, such as 1.0 or 0.5 or 2.0
     */
    public double getZoomLevel() {
        return zoomFactor / effectiveZoomFactor;
    }

    /**
     * Changes the horizontal and vertical scaling factors and updates
     * internally-cached values to be consistent.
     */
    public void setZoomLevel(double z) {
        zoomFactor = z * effectiveZoomFactor;
    }

    /**
     * Returns a transformed version of the given image, based on the
     * most recent value given to setZoomLevel().
     *
     * Directly implement paintComponent() in a JComponent instead.
     */
    @Deprecated
    public BufferedImage zoomImage(BufferedImage img) {
        if (zoomFactor == 1.0) {
            return img;
        }
        BufferedImage tmp = new BufferedImage(length, length, BufferedImage.TYPE_INT_RGB);
        AffineTransformOp aop = new AffineTransformOp(AffineTransform.getScaleInstance(zoomFactor, zoomFactor), AffineTransformOp.TYPE_BILINEAR);
        tmp = aop.createCompatibleDestImage(img, img.getColorModel());
        aop.filter(img, tmp);
        return tmp;
    }

    /**
     * Specifies how to render the RNA data.
     * @param imgType set to VIEW_2D or VIEW_FLAT
     */
    public boolean setImageType(int imgType) {
        if (imageType != imgType) {
            imageType = imgType;
            return true;
        }
        return false;
    }

    /**
     * Returns the image for the current view.  If the image does not
     * exist yet, it is created.  If the image size no longer matches
     * the size appropriate for the zoom, a new image is created.
     */
    public BufferedImage getImage() {
        final int zoomDim = (int)(zoomFactor * length);
        if (imageType == this.VIEW_2D) {
            if ((this.helix2D == null) || (this.helix2D.getWidth() != zoomDim)) {
                this.helix2D = new BufferedImage(zoomDim, zoomDim, BufferedImage.TYPE_INT_RGB);
            }
            return this.helix2D;
        }
        if (imageType == this.VIEW_FLAT) {
            if ((this.helixFlat == null) || (this.helixFlat.getWidth() != zoomDim)) {
                this.helixFlat = new BufferedImage(zoomDim, zoomDim, BufferedImage.TYPE_INT_RGB);
            }
            return this.helixFlat;
        }
        return null;
    }

    /**
     * Returns the selected helix, or null.  See also the method
     * addPropertyChangeListener(), to find out about changes; it is
     * VITAL that you query the selected helix from a listener if it
     * is in response to a mouse event, as the clicked helix might
     * not be up-to-date within the original event handler.
     * @return a Helix object
     */
    public Helix getSelectedHelix() {
        return this.selectedHelix;
    }

    /**
     * Given a (mouse) point relative to a component that displays
     * this image, and the total size of that component view area,
     * changes the primary helix selection.  The image is not redrawn
     * until you call a method such as paintRNA().
     *
     * This is referred to as the primary selection because it may
     * eventually be possible to target multiple helices.
     *
     * @param x horizontal coordinate relative to origin of display component
     * @param y vertical coordinate relative to origin of display component
     * @param targetSize view dimensions of component (even if image is smaller)
     */
    public void setPrimarySelectionLocation(double x, double y, Dimension targetSize) {
        double xOffset = (zoomFactor * length - targetSize.getWidth()) / 2.0;
        double yOffset = (zoomFactor * length - targetSize.getHeight()) / 2.0;
        if (xOffset < 0) {
            // mouse origin does not match image origin
            this.clickPoint.setLocation((x + xOffset) / zoomFactor, (y + yOffset) / zoomFactor);
        } else {
            // entire image is displayed without padding
            this.clickPoint.setLocation(x / zoomFactor, y / zoomFactor);
        }
    }

    /**
     * Constructs a new Image object with a rendering of the given RNA
     * data for the current view (2D or flat).
     *
     * IMPORTANT: Image generation should be reserved for special
     * cases (like saving a file in an image format).  To render the
     * RNA data “live” for the user, you should use a JComponent and
     * implement paintComponent() to call paintRNA() on the graphics
     * context of the component.  See the RNADisplay class for more.
     */
    public BufferedImage drawImage(RNA rna) {
        BufferedImage result = getImage();
        Graphics2D g = result.createGraphics();
        paintRNA(rna, g, getSize(), RenderingType.NORMAL);
        return result;
    }

    /**
     * Call this before using a series of calls to paintRNA() or a
     * similar chain of methods.  It ensures that the selection
     * state is reset only once at the beginning.  See endRender().
     */
    public void beginRender() {
        if (renderInProgress) {
            throw new RuntimeException("already rendering; cannot beginRender() again");
        }
        this.originalSelection = this.selectedHelix;
        this.selectedHelix = null; // initially...
        renderInProgress = true;
    }

    /**
     * Call this at the end of a chain of rendering calls.  It will
     * finalize the selection state across all calls and trigger any
     * actions related to selection of helices.
     */
    public void endRender() {
        if (!renderInProgress) {
            throw new RuntimeException("no render in progress; cannot endRender()");
        }
        renderInProgress = false;
        if (this.selectedHelix != originalSelection) {
            // TODO: add text labels for base-pairs or other properties?
            // now notify observers (e.g. to update info pane)
            pcs.firePropertyChange(PROPERTY_SELECTED_HELIX, originalSelection, this.selectedHelix);
            this.originalSelection = null;
        }
    }

    /**
     * Directly renders the given RNA data in the specified graphics
     * context, based on the current view (2D or flat).
     * @param rna the data to display
     * @param g the context in which to draw
     * @param targetSize the size of the container in which the image will be centered
     */
    public void paintBackground(Graphics2D g, Dimension targetSize) {
        AffineTransform oldTransform = g.getTransform();
        try {
            transformGraphics(g, targetSize);
            // start to end inclusive but there is also extra
            // room for the dividing line (at 0) so there must
            // be an extra +1 in each direction
            g.setColor(Color.white);
            g.fillRect(0, 0, length, length);
            if (this.imageType == this.VIEW_FLAT) {
                //g.setColor(Color.black);
                //g.setStroke(strokeAxisLine);
                //g.drawLine(0, length / 2, length - 1, length / 2);
            } else {
                g.setColor(Color.black);
                g.setStroke(strokeAxisLine);
                // diagonal line covers background rectangle range above
                g.drawLine(0, 0, length, length);
            }
        } finally {
            g.setTransform(oldTransform);
        }
    }

    /**
     * Directly renders the given RNA data in the specified graphics
     * context, based on the current view (2D or flat), without any
     * background.
     *
     * To erase the background first and draw axes, call the method
     * paintBackground().  This separation allows multiple RNAs to be
     * superimposed on the same initial background.
     *
     * This must be called within a beginRender()/endRender() pair.
     *
     * @param rna the data to display
     * @param g the context in which to draw
     * @param targetSize the size of the container in which the image will be centered
     * @param renderingType whether or not to render an overlay
     */
    public void paintRNA(RNA rna, Graphics2D g, Dimension targetSize, RenderingType renderingType) {
        assert renderInProgress;
        AffineTransform oldTransform = g.getTransform();
        try {
            transformGraphics(g, targetSize);
            if (this.imageType == this.VIEW_FLAT) {
                paintFlatImage(rna, renderingType, g);
            } else {
                paint2DImage(rna, renderingType, g);
            }
        } finally {
            g.setTransform(oldTransform);
        }
    }

    /**
     * Helper method for paint2DImage(); may change the value of the
     * "selectedHelix" (based on the "clickPoint" field value).
     *
     * This must be called within a beginRender()/endRender() pair.
     */
    private void paintHelix2D(Helix h, HelixType helixType, RenderingType renderingType, Graphics2D helixGraphics) {
        assert renderInProgress;
        final double clickX = clickPoint.getX();
        final double clickY = clickPoint.getY();
        if (helixType == HelixType.ACTUAL) {
            helixGraphics.setColor(this.helixColorActual);
        } else {
            helixGraphics.setColor((renderingType == RenderingType.OVERLAY)
                                   ? Color.black // TODO: make this customizable?
                                   : this.helixColorPredicted);
        }
        /*
        if (h.getEnergy() < -6) {
            helixGraphics.setColor(Color.orange);
        }
        if (h.getEnergy() < -10) {
            helixGraphics.setColor(Color.yellow);
        }
        if (h.getEnergy() < -18) {
            helixGraphics.setColor(Color.green);
        }*/
        double x0, y0, x1, y1;
        y0 = h.getStartX();
        x0 = h.getStartY();
        if (helixType == HelixType.ACTUAL) {
            x1 = x0 + (h.getLength() - 1);
            y1 = y0 - (h.getLength() - 1);
        } else {
            x1 = x0 - (h.getLength() - 1);
            y1 = y0 + (h.getLength() - 1);
        }
        this.tmpLine.setLine(x0, y0, x1, y1);
        boolean becameSelected = false;
        if (this.selectedHelix == null) {
            // no helix has been selected in this iteration yet;
            // scan for clicks slightly outside the target line
            // as well, to make helices easier to select
            if (this.tmpLine.ptSegDist(clickX, clickY) < 0.5) {
                becameSelected = true;
            }
        }
        // TODO: repeatedly testing for intersection is not efficient
        // if it involves ALL helices; if the data can be rearranged
        // into a geometric form (such as an R-tree), the start point
        // can be used to greatly reduce the number of test points;
        // similarly, such a tree could be used to render only the
        // helices in a certain area and not necessarily everything
        if (becameSelected) {
            this.selectedHelix = h;
            helixGraphics.setColor(this.helixColorSelected);
            if (h.getLength() == 1) {
                // IMPORTANT: cannot use a cap-butt stroke type for a
                // single-pixel line (it will be invisible); need to
                // force a stroke that caps with a visible shape
                helixGraphics.setStroke(strokeLength1SelectedHelix);
            } else {
                helixGraphics.setStroke(strokeSelectedHelix);
            }
            helixGraphics.draw(this.tmpLine);
            // draw "handles" (blobs on each end) so that the
            // selection is more distinct
            helixGraphics.setStroke(strokeHelixHandle);
            helixGraphics.drawLine((int)x0 - 1, (int)y0 - 1, (int)x0 + 1, (int)y0 + 1);
            helixGraphics.drawLine((int)x1 - 1, (int)y1 - 1, (int)x1 + 1, (int)y1 + 1);
        } else {
            if (h.getLength() == 1) {
                helixGraphics.setStroke(strokeLength1Helix);
            } else {
                helixGraphics.setStroke(strokeNormalHelix);
            }
            helixGraphics.draw(this.tmpLine);
        }
    }

    /*
     * Draws a 2D view of the given data in the graphics context.
     *
     * This must be called within a beginRender()/endRender() pair.
     */
    private void paint2DImage(RNA rna, RenderingType renderingType, Graphics2D helixGraphics) {
        assert renderInProgress;
        if (rna == null) {
            // nothing to do
            return;
        }
        helixGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        helixGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        HelixStore actual = rna.getActual();
        // draw the actual helices on top
        if (actual != null) {
            Iterator itr = actual.iterator();
            while (itr.hasNext()) {
                Helix h = (Helix)itr.next();
                paintHelix2D(h, HelixType.ACTUAL, renderingType, helixGraphics);
            }
        }
        HelixStore hstore = rna.getHelices();
        if (hstore != null) {
            Iterator itr = hstore.iterator();
            while (itr.hasNext()) {
                Helix h = (Helix)itr.next();
                paintHelix2D(h, HelixType.PREDICTED, renderingType, helixGraphics);
            }
            if (false) {
                // debug: show click region
                final double clickX = clickPoint.getX();
                final double clickY = clickPoint.getY();
                Rectangle2D.Double clickArea = new Rectangle2D.Double
                                                   (clickX - 0.5, clickY - 0.5, 1, 1);
                helixGraphics.setStroke(strokeClickLocation);
                helixGraphics.setColor(Color.darkGray);
                helixGraphics.draw(clickArea);
            }
        } else {
            //System.out.println("No helices to draw");
        }
    }

    /*
     * Draws a flat view of the given data in the graphics context.
     *
     * This must be called within a beginRender()/endRender() pair.
     */
    private void paintFlatImage(RNA rna, RenderingType renderingType, Graphics2D helixGraphics) {
        assert renderInProgress;
        if (rna == null) {
            // nothing to do
            return;
        }
        helixGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        helixGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        HelixStore hstore = rna.getHelices();
        helixGraphics.setColor(Color.red);
        if (hstore != null) {
            Iterator itr = hstore.iterator();
            helixGraphics.setStroke(strokeFlatNormalHelix);
            while (itr.hasNext()) {
                Helix h = (Helix)itr.next();
                int x0, x1, y, hi, wi;
                wi = h.getLength();
                hi = (h.getStartX() - h.getStartY());
                x0 = h.getStartX();
                y = (int)(length) - hi;
                helixGraphics.setColor(Color.red);
                helixGraphics.drawLine(x0, y - 1, x0, (int)(length));
                x1 = h.getStartY();
                helixGraphics.setColor(Color.blue);
                helixGraphics.drawLine(x1, y - 1, x1, (int)(length));
                helixGraphics.setColor(Color.green);
                helixGraphics.drawLine(x0, y - 1, x1, y - 1);
            }
        }
    }

    private Image toImage(BufferedImage bufImage) {
        AffineTransformOp aop = new AffineTransformOp(new AffineTransform(), AffineTransformOp.TYPE_BILINEAR);
        BufferedImageFilter bif = new BufferedImageFilter(aop);
        FilteredImageSource fsource = new FilteredImageSource(bufImage.getSource(), bif);
        Image img = Toolkit.getDefaultToolkit().createImage(fsource);
        return img;
    }

    /**
     * Applies necessary transformations to a context prior to drawing.
     * @param g the context to modify
     * @param targetSize the size of the container in which the image will be centered
     */
    private void transformGraphics(Graphics2D g, Dimension targetSize) {
        double xOffset = (targetSize.getWidth() - zoomFactor * length) / 2.0;
        double yOffset = (targetSize.getHeight() - zoomFactor * length) / 2.0;
        g.translate(xOffset, yOffset);
        g.scale(zoomFactor, zoomFactor); // this scales drawing but not image size (see getImage())
    }
}
