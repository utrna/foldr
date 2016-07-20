package rheat.GUI;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Displays miniature version of RNA while zooming.
 *
 * @author Kevin Grant
 */
public class MiniFrame extends javax.swing.JInternalFrame {

    /**
     * This component handles the drawing of the mini-view.
     * (Whereas, MiniFrame creates the whole mini-window.)
     */
    class MiniDrawing extends JComponent {

        public MiniDrawing() {
        }

        /**
         * Specifies the proportional view rectangle; each element is
         * normalized between 0.0 and 1.0 as a function of width/height.
         */
        public void setNormalizedViewRect(double nx, double ny, double nw, double nh) {
            this.normalizedViewLocationX = nx;
            this.normalizedViewLocationY = ny;
            this.normalizedViewSizeWidth = nw;
            this.normalizedViewSizeHeight = nh;
        }

        /**
         * Specifies how the image should be drawn.
         * @param imgType set to VIEW_2D or VIEW_FLAT
         */
        public void setViewType(HelixImageGenerator.ViewType viewType) {
            this.viewType = viewType;
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
            g2D.setColor(Color.lightGray);
            g2D.fill(getBounds());
            if (this.viewType == HelixImageGenerator.ViewType.VIEW_2D) {
                g2D.setColor(Color.black);
                this.tmpLine.setLine(0, 0, getWidth(), getHeight());
                g2D.draw(this.tmpLine);
            }
            Rectangle2D viewRect = new Rectangle2D.Double(getWidth() * this.normalizedViewLocationX,
                                                          getHeight() * this.normalizedViewLocationY,
                                                          getWidth() * this.normalizedViewSizeWidth,
                                                          getHeight() * this.normalizedViewSizeHeight);
            g2D.setColor(this.colorViewRect);
            g2D.fill(viewRect);
            g2D.setColor(Color.black);
            g2D.setStroke(strokeViewRectBounds);
            g2D.draw(viewRect);
        }

        private final Stroke strokeViewRectBounds = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        private double normalizedViewLocationX = 0.0;
        private double normalizedViewLocationY = 0.0;
        private double normalizedViewSizeWidth = 1.0;
        private double normalizedViewSizeHeight = 1.0;
        private HelixImageGenerator.ViewType viewType = HelixImageGenerator.ViewType.VIEW_2D;
        private Line2D.Double tmpLine = new Line2D.Double(); // used for various reasons (saves heap allocations)
        private Color colorViewRect = new Color(1.0f, 1.0f, 1.0f, 0.65f/* alpha */); // semi-transparent so that diagonal line shows through
    }

    /**
     * Sent to PropertyChangeListener registered with addPropertyChangeListener().
     */
    public static String PROPERTY_DRAGGED_VIEW_FRAME = "PROPERTY_DRAGGED_VIEW_FRAME";

    MiniFrame() {
        initComponents();
        setClosable(false);
        setMaximizable(false);
        setIconifiable(false);
        setResizable(false);
        setTitle("View Frame");
        setLocation(800, 100);
    }

    /**
     * Returns the dimensions of the mini-frame image view.
     */
    public Dimension getImageSize() {
        return new Dimension((int)imageWidth, (int)imageHeight);
    }

    /**
     * Specifies the proportional view rectangle; each element is
     * normalized between 0.0 and 1.0 as a function of width/height.
     */
    public void setNormalizedViewRect(double nx, double ny, double nw, double nh) {
        drawingPane.setNormalizedViewRect(nx, ny, nw, nh);
        drawingPane.repaint();
    }

    /**
     * Specifies how the image should be drawn.
     * @param imgType set to VIEW_2D or VIEW_FLAT
     */
    public void setViewType(HelixImageGenerator.ViewType viewType) {
        drawingPane.setViewType(viewType);
        drawingPane.repaint();
    }

    /**
     * Creates and configures GUI elements.
     */
    private void initComponents() {
        this.drawingPane = new MiniDrawing();
        this.drawingPane.setPreferredSize(new Dimension((int)imageWidth, (int)imageHeight));
        this.getContentPane().add(this.drawingPane);
        pack();
    }

    private final int borderSize = 3;
    private final int rectThickness = 2;
    private double imageHeight = 128;
    private double imageWidth = 128;
    private MiniDrawing drawingPane;

}
