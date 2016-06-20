/*
 * HelixImageGenerator.java
 *
 * Created on March 26, 2003, 3:52 PM
 */

package rheat.GUI;

import rheat.base.*;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageFilter;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.awt.Point;
import java.awt.geom.Line2D;
import javax.swing.JTextPane;

/**
 *
 * @author  jyzhang
 */
public class HelixImageGenerator {

    public static int VIEW_2D = 0;
    public static int VIEW_FLAT = 1;

    private int length;
    private int baseMaxX; // without zoom
    private int baseMaxY; // without zoom
    private int maxX;
    private int maxY;
    private float zoom;
    private int imageType;
    private BufferedImage helix2D;
    private BufferedImage helixFlat;
    private Point clicked; // coordinates are relative to data (zoom level 1)
    private Helix selectedHelix = null;
    private JTextPane textArea;

    /** Creates a new instance of HelixImageGenerator */
    public HelixImageGenerator(int l) {
        length = l;
        baseMaxX = 3 * l;
        baseMaxY = 3 * l;
        // as long as image is used, nonzero width/height needed
        // (will be overwritten if any file is opened)
        if (baseMaxX <= 0) {
            baseMaxX = 1;
            maxX = 1;
        }
        if (baseMaxY <= 0) {
            baseMaxY = 1;
            maxY = 1;
        }
        imageType = this.VIEW_2D;
        clicked = null;
        setZoomLevel(1);
    }

    /**
     * Returns the width and height at current zoom level.
     */
    public Dimension getSize() {
        return new Dimension(maxX, maxY);
    }

    /**
     * Current display-scale factor (both X and Y directions).
     * @return scale factor, such as 1.0 or 0.5 or 2.0
     */
    public float getZoomLevel() {
        return zoom;
    }

    /**
     * Changes the horizontal and vertical scaling factors and updates
     * internally-cached values to be consistent.
     */
    public void setZoomLevel(float z) {
        zoom = z;
        maxX = (int)((float)baseMaxX * zoom);
        maxY = (int)((float)baseMaxY * zoom);
    }

    /**
     * Returns a transformed version of the given image, based on the
     * most recent value given to setZoomLevel().
     *
     * Directly implement paintComponent() in a JComponent instead.
     */
    @Deprecated
    public BufferedImage zoomImage(BufferedImage img) {
        if (zoom == 1.0) {
            return img;
        }
        BufferedImage tmp = new BufferedImage((int)maxX, (int)maxY, BufferedImage.TYPE_INT_RGB);
        AffineTransformOp aop = new AffineTransformOp(AffineTransform.getScaleInstance(zoom, zoom), AffineTransformOp.TYPE_BILINEAR);
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
        if (imageType == this.VIEW_2D) {
            if ((this.helix2D == null) || (this.helix2D.getWidth() != maxX)) {
                this.helix2D = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
            }
            return this.helix2D;
        }
        if (imageType == this.VIEW_FLAT) {
            if ((this.helixFlat == null) || (this.helixFlat.getWidth() != maxX)) {
                this.helixFlat = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
            }
            return this.helixFlat;
        }
        return null;
    }

    /**
     * Returns the selected helix, or null.
     * @return a Helix object
     */
    public Helix getSelectedHelix() {
        return this.selectedHelix;
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
        paintRNA(rna, g, new Dimension(maxX, maxY));
        return result;
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
        transformGraphics(g, targetSize);
        int zoomedMaxX = (int)(maxX / zoom);
        int zoomedMaxY = (int)(maxY / zoom);
        g.setColor(Color.white);
        g.fillRect(0, 0, zoomedMaxX, zoomedMaxY);
        if (this.imageType == this.VIEW_FLAT) {
            g.setColor(Color.black);
            g.drawLine(0, zoomedMaxY / 2, zoomedMaxX, zoomedMaxY / 2);
        } else {
            g.setColor(Color.black);
            g.drawLine(0, 0, zoomedMaxX, zoomedMaxY);
        }
        g.setTransform(oldTransform);
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
     * @param rna the data to display
     * @param g the context in which to draw
     * @param targetSize the size of the container in which the image will be centered
     */
    public void paintRNA(RNA rna, Graphics2D g, Dimension targetSize) {
        AffineTransform oldTransform = g.getTransform();
        transformGraphics(g, targetSize);
        if (this.imageType == this.VIEW_FLAT) {
            paintFlatImage(rna, g);
        } else {
            paint2DImage(rna, g);
        }
        g.setTransform(oldTransform);
    }

    private void paint2DImage(RNA rna, Graphics2D helixGraphics) {
        if (rna == null) {
            // nothing to do
            return;
        }
        //helixGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //helixGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        HelixStore actual = rna.getActual();
        // draw the actual helices on top.
        if (actual != null){
            helixGraphics.setColor(Color.blue);
            Iterator itr = actual.iterator();
            while (itr.hasNext()){
                Helix h = (Helix)itr.next();
                int x0, y0, x1, y1;
                y0 = 3 * (h.getStartX());
                x0 = 3 * (h.getStartY());
                //System.out.println("" + x0 + "; " + y0);
                x1 = x0 + 3 * h.getLength();
                y1 = y0 - 3 * h.getLength();
                Line2D.Double line = new Line2D.Double(x0, y0, x1, y1);
                helixGraphics.draw(line);
            }
        }
        HelixStore hstore = rna.getHelices();
        //System.out.println("redrawing 2D image.");
        this.selectedHelix = null; // initially...
        if (hstore != null) {
            //helixGraphics.setColor(Color.red);
            Iterator itr = hstore.iterator();
            boolean helixSelected = false;
            while (itr.hasNext()) {
                Helix h = (Helix)itr.next();
                helixGraphics.setColor(Color.red);
                /*
                if (h.getEnergy() < -6){
                    helixGraphics.setColor(Color.orange);
                }
                if (h.getEnergy() < -10){
                    helixGraphics.setColor(Color.yellow);
                }
                if (h.getEnergy() < -18){
                    helixGraphics.setColor(Color.green);
                }*/
                int x0, y0, x1, y1;
                y0 = 3 * (h.getStartX() + 1);
                x0 = 3 * (h.getStartY() + 1);
                //System.out.println("" + x0 + "; " + y0);
                x1 = x0 - 3 * h.getLength();
                y1 = y0 + 3 * h.getLength();
                Line2D.Double line = new Line2D.Double(x0, y0, x1, y1);
                
                // scan for clicks slightly outside the target line
                // as well, to make helices easier to select
                if (clicked != null &&
                    (this.contains(x0, y0, x1, y1, clicked) ||
                     this.contains(x0, y0 - 1, x1, y1 - 1, clicked) ||
                     this.contains(x0, y0 + 1, x1, y1 + 1, clicked))) {
                    helixSelected = true;
                    //System.out.println("Found Point... " + clicked);
                    helixGraphics.setColor(Color.blue);
                    helixGraphics.draw(line);
                    // draw "handles" (blobs on each end) so that the
                    // selection is more distinct
                    helixGraphics.fillRect(x0 - 1, y0 - 1, 2, 2);
                    helixGraphics.fillRect(x1 - 1, y1 - 1, 2, 2);
                    this.selectedHelix = h;
                    setInfoText(h, rna);
                    //clicked = null;
                    helixGraphics.setColor(Color.red);
                } else {
                    helixGraphics.draw(line);
                }
            }
            if ((!helixSelected) && (textArea != null)) {
                textArea.setText("You did not select a helix.");
            }
        } else {
            //System.out.println("No helices to draw");
        }
    }

    private void paintFlatImage(RNA rna, Graphics2D helixGraphics) {
        if (rna == null) {
            // nothing to do
            return;
        }
        //helixGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //helixGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        HelixStore hstore = rna.getHelices();
        helixGraphics.setColor(Color.red);
        if (hstore != null){
            
            Iterator itr = hstore.iterator();
            while (itr.hasNext()){
                Helix h = (Helix)itr.next();
                //System.out.println(h.toString());
                int x0, x1, y, hi, wi;
                wi = h.getLength();
                hi = (h.getStartX() - h.getStartY());
                x0 = 3 * h.getStartX();
                y = maxY/2 - hi;
                helixGraphics.setColor(Color.red);
                helixGraphics.drawLine(x0, y - 1, x0, maxY/2);
                x1 = 3 * h.getStartY();
                helixGraphics.setColor(Color.blue);
                helixGraphics.drawLine(x1, y - 1, x1, maxY/2);
                helixGraphics.setColor(Color.green);
                helixGraphics.drawLine(x0, y - 1, x1, y -1);
                //helixGraphics.drawRect(x - 1, y - 1, wi, hi);
                //helixGraphics.fillRect(x + 3*h.getLength(), y, hi, wi);
                //x = 3*h.getStartY();
                //helixGraphics.fillRect(x, y, wi, hi);
            }
        }
    }

    private Image toImage(BufferedImage bufImage){
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
        double xOffset = (targetSize.getWidth() - maxX) / 2.0;
        double yOffset = (targetSize.getHeight() - maxY) / 2.0;
        g.translate(xOffset, yOffset);
        g.scale(this.zoom, this.zoom); // this scales drawing but not image size (see getImage())
    }

    public int getUnzoomedX(double x) {
        return (int)(x / zoom);
    }

    public int getUnzoomedY(double y) {
        return (int)(y / zoom);
    }

    /**
     * The given coordinates must be relative to the data
     * (zoom level 1); see getUnzoomedX().
     */
    public void clicked(double x, double y, JTextPane text){
        int xpt = (int)x;
        int ypt = (int)y;
        textArea = text;
        //System.out.println("Clicked near: " + xpt + "; " + ypt);
        clicked = new Point(xpt, ypt);
    }

    private boolean contains(int x0, int y0, int x1, int y1, Point pt){
        do{
            if (x0 == (int)pt.getX() && y0 == (int)pt.getY()){
                return true;
            }
            x0--;
            y0++;
        }while (x0 != x1 && y0 != y1);
        return false;
    }

    private void setInfoText(Helix h, RNA rna){
        HelixInfo info = new HelixInfo(h, rna);
        String s = "Helix Length: " + info.getLength() + "\nHelix Energy: " + info.getEnergy() +"\n";
        s += "5'...." + info.get5PrimeSequence() + "....3'\n";
        s += "3'...." + info.get3PrimeSequence() + "....5'\n";
        s += "5' Start: " + (info.get5PrimeStart() + 1) + "\n5' End: " + (info.get5PrimeEnd() + 1) + "\n";
        s += "3' Start: " + (info.get3PrimeStart() + 1) + "\n3' End: " + (info.get3PrimeEnd() + 1);
        textArea.setText(s);
    }

    /* OLD VERSION
    private void setInfoText(Helix h, RNA rna){
        int l = h.getLength();
        int x = h.getStartX();
        int y = h.getStartY();
        int dis = Math.abs(y - x) -1;
        String s = "Helix Length: " + h.getLength() + "\n";
     
     
        String seq = "";
        for (int i = 0; i < l; i++){
            seq = rna.getSequence().get(y) +seq;
            y--;
        }
        seq += "..(" + dis + ")..";
        for (int i = 0; i < l; i++){
            seq += rna.getSequence().get(x);
            x++;
        }
        s += seq;
     
        textArea.setText(s);
    }
     */
}
