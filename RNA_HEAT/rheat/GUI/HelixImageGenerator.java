/*
 * HelixImageGenerator.java
 *
 * Created on March 26, 2003, 3:52 PM
 */

package rheat.GUI;

import rheat.test.*;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageFilter;
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
    private int maxX;
    private int maxY;
    private float zoom;
    private int imageType;
    private BufferedImage helix2D;
    private BufferedImage helixFlat;
    private Point clicked;
    private JTextPane textArea;
    
    /** Creates a new instance of HelixImageGenerator */
    public HelixImageGenerator(int l) {
        length = l;
        maxX = 3 * l; maxY = 3 * l;
        imageType = this.VIEW_2D;
        zoom = 1;
        clicked = null;
        this.helix2D = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
        this.helixFlat = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
    }
    
    /**
     *
     */
    public void setZoomLevel(float z){
        zoom = z;
    }
    
    public Image zoomImage(Image i){
        BufferedImage img = (BufferedImage)i;
        BufferedImage tmp = new BufferedImage((int)(maxX * zoom), (int)(maxY *zoom), BufferedImage.TYPE_INT_RGB);
        AffineTransformOp aop = new AffineTransformOp(AffineTransform.getScaleInstance(zoom, zoom), AffineTransformOp.TYPE_BILINEAR);
        
        tmp = aop.createCompatibleDestImage(img, img.getColorModel());
        aop.filter(img, tmp);
        return tmp;
    }
    
    /**
     *
     */
    public boolean setImageType(int imgType){
        if (imageType != imgType){
            imageType = imgType;
            return true;
        }
        return false;
    }
    
    /**
     *
     */
    public Image getImage(){
        if (imageType == this.VIEW_2D){
            return this.helix2D;
        }
        else if (imageType == this.VIEW_FLAT){
            return this.helixFlat;
        }
        else {
            return null;
        }
    }
    
    public Image drawImage(RNA rna){
        if (imageType == this.VIEW_2D){
            return draw2DImage(rna);
        }
        else {
            return drawFlatImage(rna);
        }
    }
    
    private Image draw2DImage(RNA rna){
        //this.helix2D = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
        Graphics2D helixGraphics = this.helix2D.createGraphics();
        helixGraphics.setColor(Color.white);
        helixGraphics.fillRect(0, 0, maxX, maxY);
        helixGraphics.setColor(Color.black);
        helixGraphics.drawLine(0, 0, maxY, maxY);
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
        System.out.println("redrawing 2D image.");
        if (hstore != null){
            //helixGraphics.setColor(Color.red);
            Iterator itr = hstore.iterator();
            while (itr.hasNext()){
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
                    //System.out.println("Found Point... " + clicked);
                    helixGraphics.setColor(Color.blue);
                    helixGraphics.draw(line);
                    // draw "handles" (blobs on each end) so that the
                    // selection is more distinct
                    helixGraphics.fillRect(x0 - 1, y0 - 1, 3, 3);
                    helixGraphics.fillRect(x1 - 1, y1 - 1, 3, 3);
                    setInfoText(h, rna);
                    clicked = null;
                    helixGraphics.setColor(Color.red);
                }
                else {
                    helixGraphics.draw(line);
                }
                //helixGraphics.draw(line);
            }
            if (clicked != null){
                textArea.setText("You did not select a helix.");
                clicked = null;
            }
        }
        else {
            //System.out.println("No helices to draw");
        }
        //doZoom(helix2D, this.VIEW_2D);
        return helix2D;
    }
    
    private Image drawFlatImage(RNA rna){
        //this.helixFlat = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
        Graphics2D helixGraphics = this.helixFlat.createGraphics();
        helixGraphics.setColor(Color.white);
        helixGraphics.fillRect(0, 0, maxX, maxY);
        helixGraphics.setColor(Color.black);
        helixGraphics.drawLine(0, maxY/2, maxX, maxY/2);
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
        //doZoom(helixFlat, this.VIEW_FLAT);
        return helixFlat;
    }
    
    private Image toImage(BufferedImage bufImage){
        AffineTransformOp aop = new AffineTransformOp(new AffineTransform(), AffineTransformOp.TYPE_BILINEAR);
        BufferedImageFilter bif = new BufferedImageFilter(aop);
        FilteredImageSource fsource = new FilteredImageSource(bufImage.getSource(), bif);
        Image img = Toolkit.getDefaultToolkit().createImage(fsource);
        return img;
    }
    
    public void clicked(double x, double y, JTextPane text){
        int xpt = (int)(x / (zoom));
        int ypt = (int)(y / (zoom));
        textArea = text;
        //System.out.println("Clicked near: " + xpt + "; " + ypt);
        clicked = new Point(xpt, ypt);
        //return clicked;
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
        String s = "Helix Length: " + info.getLength() + "; Helix Energy: " + info.getEnergy() +"\n";
        s += "5'...." + info.get5PrimeSequence() + "....3'\n";
        s += "3'...." + info.get3PrimeSequence() + "....5'\n";
        s += "5' Start: " + (info.get5PrimeStart() + 1) + ";\t 5' End: " + (info.get5PrimeEnd() + 1) + "\n";
        s += "3' Start: " + (info.get3PrimeStart() + 1) + ";\t 3' End: " + (info.get3PrimeEnd() + 1);
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
