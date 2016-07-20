package rheat.GUI;

import rheat.base.*;
import rheat.filter.*;
import rheat.script.ConstraintInterpreter;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.Math;
import java.util.*;
import javax.imageio.ImageIO;
import javax.script.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalSliderUI;
import javax.swing.text.DefaultEditorKit;

/**
 * Main window of RNA HEAT application.
 *
 * @author  jyzhang
 * @author  Kevin Grant
 */
public class RheatApp
extends javax.swing.JFrame
implements PropertyChangeListener {

    public enum RNADisplayFeature {
        ZOOM_LEVEL // current zoom level of RNA display
    }

    static public abstract class RheatActionPanel
    extends javax.swing.JComponent {

        private String title = null;
        private String okTitle = "OK";
        private String cancelTitle = "Cancel";

        RheatActionPanel(String title) {
            this.title = title;
        }

        /**
         * For convenience, to allow code to look more like how it would
         * appear when creating components inside a window.
         * @return This object.
         */
        protected Container getContentPane() { return this; }

        protected String getOKTitle() { return okTitle; }

        protected String getCancelTitle() { return cancelTitle; }

        protected boolean isResizable() { return false; }

        /**
         * Primitive runner; blocks until user selects a button.
         * Calls actionPanelAccepted() only if appropriate.  The
         * dialog is then closed regardless.
         */
        public void run(RheatApp parent) {
            parent.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    // no action (cannot "accept" this way)
                }
            });
            JInternalFrame internalFrame = new JInternalFrame();
            internalFrame.setResizable(isResizable());
            parent.addOrReuseComponent(internalFrame);
            Dimension desktopSize = parent.desktopPane.getSize();
            Dimension frameSize = internalFrame.getSize();
            internalFrame.setLocation((desktopSize.width - frameSize.width) / 2,
                                      (desktopSize.height- frameSize.height) / 2);
            int result = JOptionPane.showInternalOptionDialog
                         (internalFrame, this, title,
                          JOptionPane.OK_CANCEL_OPTION,
                          JOptionPane.PLAIN_MESSAGE,
                          null/* icon */,
                          new Object[] {
                              getOKTitle(),
                              getCancelTitle()
                          },
                          getOKTitle());
            // NOTE: can return JOptionPane.CLOSE_OPTION (-1) or
            // OK_OPTION or CANCEL_OPTION
            if (result == JOptionPane.OK_OPTION) {
                actionPanelAccepted();
            }
        }

        /**
         * Called in response to an OK button action, before the
         * dialog is destroyed (allowing various components to
         * be queried, etc.).  Regardless of which button is
         * chosen, the dialog is closed automatically after a
         * run-method finishes and returns a value so this call
         * should NOT try to change the window state.
         */
        abstract void actionPanelAccepted();
    }

    /**
     * Used to implement menu items that bring windows to the front.
     */
    static public class FrameSelectAction
    extends AbstractAction {

        FrameSelectAction(JDesktopPane desktop, JInternalFrame f) {
            super(f.getTitle());
            this.desktop = desktop;
            this.frame = f;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            try {
                frame.setVisible(true);
                if (frame.isIcon()) {
                    frame.setIcon(false);
                }
                frame.setSelected(true);
                desktop.moveToFront(frame);
            } catch (java.beans.PropertyVetoException e) {
                e.printStackTrace();
            }
        }

        private JDesktopPane desktop;
        private JInternalFrame frame;

    }

    /**
     * For the log() method.
     */
    public static final int INFO = AppMain.INFO;
    public static final int WARN = AppMain.WARN;
    public static final int ERROR = AppMain.ERROR;

    // PRIVATE DATAMEMBERS RELEVANT TO RNAHEAT
    private AppMain appMain;
    private HelixImageGenerator helixImgGen = new HelixImageGenerator();
    private JFileChooser fc = new JFileChooser();
    private BufferedImage img = null;
    private Rectangle tmpRect = new Rectangle(); // reused as needed to repaint

    /**
     * Main window, using an AppMain object to manage
     * state that is not GUI-specific.
     */
    public RheatApp(AppMain appMain) {
        this.appMain = appMain;
        initComponents();
        this.displayPane.setDefaultHelixColor(appMain.getPrefDefaultHelixColor()); // see also updateDefaultHelixColor()
        this.displayPane.setDefaultAnnotatedHelixColor(appMain.getPrefDefaultHelixAnnotationColor()); // see also updateDefaultHelixColor()
        this.helixImgGen.setGridFraction(appMain.getPrefGridFraction()); // see also updateGrid()
        this.helixImgGen.addPropertyChangeListener(HelixImageGenerator.PROPERTY_SELECTED_HELIX, this); // updates info pane for selected helix
        this.setBounds(0, 0 , 700, 700);
    }

    /**
     * Logs message with implicit StringBuilder.
     * Currently an alias for AppMain.log() but this could evolve to
     * use a GUI log window.  (Use lower-level methods to force the
     * output to use the console.)
     * @param messageType use INFO, WARN or ERROR
     * @param parts strings to join to form the message
     */
    static public void log(int messageType, String[] parts) {
        AppMain.log(messageType, parts);
    }

    /**
     * Logs simple messages.
     * Currently an alias for AppMain.log() but this could evolve to
     * use a GUI log window.  (Use lower-level methods to force the
     * output to use the console.)
     * @param messageType use INFO, WARN or ERROR
     * @param text the content of the log message
     */
    static public void log(int messageType, String text) {
        AppMain.log(messageType, text);
    }

    /**
     * Implements PropertyChangeListener; used to update info display.
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(HelixImageGenerator.PROPERTY_SELECTED_HELIX)) {
            JTextArea textArea = this.helixInfoTextPane;
            if (textArea != null) {
                StringBuilder sb = new StringBuilder();
                Helix selectedHelix = getSelectedHelix();
                if (selectedHelix == null) {
                    sb.append("You did not select a helix.\n");
                } else {
                    HelixInfo info = new HelixInfo(selectedHelix, appMain.rnaData);
                    sb.append("Helix Length: " + info.getLength() + "\n");
                    sb.append("Helix Energy: " + String.format("%.5f", info.getEnergy()) + "\n");
                    sb.append("5'...." + info.get5PrimeSequence() + "....3'\n");
                    sb.append("3'...." + info.get3PrimeSequence() + "....5'\n");
                    sb.append("5' Start: " + (info.get5PrimeStart() + 1) + "\n");
                    sb.append("5' End: " + (info.get5PrimeEnd() + 1) + "\n");
                    sb.append("3' Start: " + (info.get3PrimeStart() + 1) + "\n");
                    sb.append("3' End: " + (info.get3PrimeEnd() + 1) + "\n");
                    Set<String> helixTags = selectedHelix.getTags();
                    if (helixTags != null) {
                        sb.append("Annotations:");
                        for (String tag : helixTags) {
                            sb.append(" ");
                            sb.append(tag);
                        }
                    }
                }
                textArea.setText(sb.toString());
            }
        } else if (event.getPropertyName().equals(mainWindowFrame.IS_MAXIMUM_PROPERTY)) {
            // always size back to fit when the display is maximized or restored
            zoomFit();
        }
    }

    /**
     * Returns the selected helix, or null.
     * @return a Helix object
     */
    public Helix getSelectedHelix() {
        return helixImgGen.getSelectedHelix();
    }

    private void addHistoryCommand(String scriptCommandLines) {
        this.appMain.addHistoryCommand(scriptCommandLines);
        refreshHistoryTextPane();
    }

    private void refreshHistoryTextPane() {
        ArrayList<String> commands = this.appMain.getHistoryCommands();
        StringBuilder sb = new StringBuilder();
        for (String s : commands) {
            sb.append(s);
            sb.append("\n");
        }
        historyTextPane.setText(sb.toString());
    }

    /**
     * Notify that helix color has changed externally.
     */
    public void updateDefaultHelixColor() {
        // NOTE: similar action taken in constructor (without the update)
        displayPane.setDefaultHelixColor(appMain.getPrefDefaultHelixColor());
        displayPane.setDefaultAnnotatedHelixColor(appMain.getPrefDefaultHelixAnnotationColor());
        displayPane.repaint();
    }

    /**
     * Notify that grid setting has changed externally.
     */
    public void updateGrid() {
        // NOTE: similar action taken in constructor (without the update)
        helixImgGen.setGridFraction(appMain.getPrefGridFraction());
        updateImage();
    }

    public void clearHistory() {
        this.appMain.clearHistoryCommands();
        historyTextPane.setText("");
    }

    private double getZoomLevel() {
        return (zoomSlider.getValue() / 1000f);
    }

    private void zoomIn() {
        double currentZoom = getZoomLevel();
        setZoomLevel(getZoomLevel() + 0.25f); // should match zoom-out amount below
    }

    private void zoomOut() {
        double currentZoom = getZoomLevel();
        setZoomLevel(getZoomLevel() - 0.25f); // should match zoom-in amount above
    }

    private void zoomFit() {
        Dimension availableSize = displayScrollPane.getViewport().getSize();
        Dimension imageSize = helixImgGen.getSize();
        if (imageSize.getWidth() <= 0.001) {
            // special case: if image is zoomed way out, its width
            // will be tiny and the fit size will blow up; force
            // a reset to a baseline zoom before adjusting further
            setZoomLevel(1); // baseline
            imageSize = helixImgGen.getSize();
        }
        double availableRange = ((availableSize.getWidth() < availableSize.getHeight())
                                 ? availableSize.getWidth()
                                 : availableSize.getHeight());
        double imageRange = imageSize.getWidth(); // image is square
        setZoomLevel(getZoomLevel() * (availableRange / imageRange));
    }

    private void scrollUpLeft() {
        // activate two scroll bars at once
        JScrollBar bar = displayScrollPane.getHorizontalScrollBar();
        bar.setValue(bar.getValue() - bar.getBlockIncrement());
        bar = displayScrollPane.getVerticalScrollBar();
        bar.setValue(bar.getValue() - bar.getBlockIncrement());
    }

    private void scrollDownRight() {
        // activate two scroll bars at once
        JScrollBar bar = displayScrollPane.getHorizontalScrollBar();
        bar.setValue(bar.getValue() + bar.getBlockIncrement());
        bar = displayScrollPane.getVerticalScrollBar();
        bar.setValue(bar.getValue() + bar.getBlockIncrement());
    }

    /**
     * Scrolls to a particular location (same X and Y).
     * @param xy data-relative value (i.e. maximum is base-pair length of RNA)
     */
    private void scrollTo(int xy) {
        if (appMain.rnaData == null) {
            return;
        }
        this.scrollTo(xy, xy);
    }

    /**
     * Scrolls to a particular location, attempting to center the
     * display on that point.
     * @param x data-relative value (i.e. maximum is base-pair length of RNA)
     * @param y data-relative value (i.e. maximum is base-pair length of RNA)
     */
    private void scrollTo(int x, int y) {
        if (appMain.rnaData == null) {
            return;
        }
        // activate two scroll bars at once
        final int maxBP = appMain.rnaData.getLength();
        JScrollBar bar = displayScrollPane.getHorizontalScrollBar();
        int maximum = bar.getMaximum();
        int visible = bar.getVisibleAmount();
        int scrollValue = (int)((double)maximum / (double)maxBP * (double)x - (visible / 2));
        bar.setValue(scrollValue);
        bar = displayScrollPane.getVerticalScrollBar();
        maximum = bar.getMaximum();
        visible = bar.getVisibleAmount();
        scrollValue = (int)((double)maximum / (double)maxBP * (double)y - (visible / 2));
        bar.setValue(scrollValue);
    }

    /**
     * Returns a fixed-width font, if one is available.
     * @param fallback a font to return if the monospaced lookup fails
     * @param size the desired point size of the font (like "12")
     * @return a fixed-width font of the given size, or the "fallback"
     */
    static public Font getMonospacedFont(Font fallback, int size) {
        Font result = fallback;
        Font fixedWidthFont = new Font("Courier", Font.PLAIN, size);
        if (fixedWidthFont != null) {
            result = fixedWidthFont;
        }
        return result;
    }

    /**
     * Turns wait-state on or off; used for long-running operations
     * so that the user knows something is happening.
     *
     * NOTE: Operations that update the GUI over time should probably
     * use SwingWorker so that responsiveness is preserved.
     *
     * @param statusMsg status information for the user, or null to end waiting
     * @param statusPercent if nonzero, specifies new percent completion
     */
    public void setStatus(String statusMsg, int statusPercent) {
        // TODO: maybe this should be a stack, to support multiple operations
        int newCursorType = ((statusMsg != null)
                             ? Cursor.WAIT_CURSOR
                             : Cursor.DEFAULT_CURSOR);
        setCursor(Cursor.getPredefinedCursor(newCursorType));
        if (statusMsg != null) {
            // TODO: add a GUI panel for status
            //log(INFO, statusMsg);
        }
        // TODO: statusPercent is not displayed
    }

    /**
     * Displays the contents of the given file in a new window.
     */
    public void openDataFile(String filePath) throws IOException {
        if (filePath.endsWith(".jpeg") ||
            filePath.endsWith(".jpg") ||
            filePath.endsWith(".png")) {
            ImageFileFrame imageFrame = new ImageFileFrame();
            imageFrame.openFile(filePath);
            addOrReuseComponent(imageFrame);
            bringToFront(imageFrame);
        } else {
            TextFileFrame textFrame = new TextFileFrame();
            textFrame.openFile(filePath);
            addOrReuseComponent(textFrame);
            bringToFront(textFrame);
        }
    }

    private void updateImage() {
        displayPane.setRNA(appMain.rnaData); // resets overlays
        for (int i = 0; i < appMain.overlayData.size(); ++i) {
            displayPane.addOverlayRNA(appMain.overlayData.get(i), appMain.overlayColors.get(i));
        }
        displayPane.setHelixImageGenerator(this.helixImgGen);
        if (this.helixImgGen != null) {
            setStatus("Updating image…", 0);
            try {
                System.gc();
                //img = helixImgGen.drawImage(appMain.rnaData);
                displayPane.repaint(displayPane.getBounds(this.tmpRect));
                JViewport viewPort = this.displayScrollPane.getViewport();
                Dimension oldVisibleSize = viewPort.getExtentSize();
                double oldViewCenterX = (viewPort.getViewPosition().getX() + (oldVisibleSize.getWidth() / 2.0));
                double oldViewCenterY = (viewPort.getViewPosition().getY() + (oldVisibleSize.getHeight() / 2.0));
                Dimension oldMaxSize = viewPort.getView().getSize();
                // update the display
                // disabled old method: constructing a new image was extremely
                // costly (e.g. zooming a giant RNA could run out of memory)
                // so the new method is to use a custom component
                //oldDisplayPane.setIcon(new ImageIcon(img));
                //oldDisplayPane.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                //oldDisplayPane.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
                //oldDisplayPane.setSize(helixImgGen.getSize());
                //oldDisplayPane.setBackground(Color.white);
                displayPane.setSize(helixImgGen.getSize());
                displayPane.setBackground(Color.white);
                Dimension newVisibleSize = viewPort.getExtentSize();
                Dimension newMaxSize = viewPort.getView().getSize();
                double viewScaleX = (newMaxSize.getWidth() / oldMaxSize.getWidth());
                double viewScaleY = (newMaxSize.getHeight() / oldMaxSize.getHeight());
                if ((viewScaleX != 1.0) || (viewScaleY != 1.0)) {
                    // the previous scroll information referred to an image
                    // of a different size; in order to keep focus on the
                    // same area, the new scroll position must be scaled to
                    // account for the change in size (it is kept centered)
                    double newViewCenterX = (oldViewCenterX * viewScaleX);
                    double newViewCenterY = (oldViewCenterY * viewScaleY);
                    double newViewX = (newViewCenterX - (newVisibleSize.getWidth() / 2.0));
                    double newViewY = (newViewCenterY - (newVisibleSize.getHeight() / 2.0));
                    viewPort.setViewPosition(new Point((int)newViewX, (int)newViewY));
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            } finally {
                setStatus(null, 0);
            }
        }
    }

    private void setControlLabels() {
        if (appMain.rnaData != null) {
            this.uidValue.setText(appMain.rnaData.getUID());
            this.organismValue.setText(appMain.rnaData.getOrganism());
            this.accNumValue.setText(appMain.rnaData.getAccession());
            this.lengthValue.setText(appMain.rnaData.getLength() + " bp");
        } else {
            this.uidValue.setText("");
            this.organismValue.setText("");
            this.accNumValue.setText("");
            this.lengthValue.setText("");
        }
    }

    /**
     * Convenience method that assumes DEFAULT_LAYER (appropriate
     * for normal windows).
     */
    private void addOrReuseComponent(Component f) {
        this.addOrReuseComponent(f, javax.swing.JLayeredPane.DEFAULT_LAYER);
    }

    /**
     * A private method useful for reusing a closed JComponent.
     * Most useful for JInternalFrames that has been closed, but
     * may be reopened using a call to this method.
     * @param f the frame to reuse
     * @param layer typically javax.swing.JLayeredPane.DEFAULT_LAYER or
     * javax.swing.JLayeredPane.PALETTE_LAYER
     */
    private void addOrReuseComponent(Component f, int layer) {
        f.setVisible(true);
        if (f.getParent() == null) {
            desktopPane.setLayer(f, layer);
            desktopPane.add(f);
        }
        repaint();
    }

    /**
     * A private method for bringing a JInternalFrame to the front
     * of the JDesktopPane.  A call to this method will unminimize
     * the internal frame (if minimized) and bring it to the front
     * and select it.
     *
     */
    private void bringToFront(javax.swing.JInternalFrame f) {
        try {
            if (f.isIcon()){ // if control window is minimized,
                f.setIcon(false); // unminimize it.
            }
            f.setSelected(true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        desktopPane.moveToFront(f);
    }

    /**
     * Updates the mini-frame to match current scroll proportions.
     * Call this when the display view port changes.
     */
    private void updateMiniFrame() {
        Rectangle viewRect = displayScrollPane.getViewport().getViewRect();
        Dimension fullSize = displayScrollPane.getViewport().getView().getSize();
        // express location and view frame as fractions out of 1.0;
        // e.g. width 0.3 means 30% of the total width is visible;
        // and an X coordinate of 0.1 means the left edge is at 10%
        final double normX = (double)viewRect.getX() / fullSize.getWidth();
        final double normY = (double)viewRect.getY() / fullSize.getHeight();
        final double normW = (double)viewRect.getWidth() / fullSize.getWidth();
        final double normH = (double)viewRect.getHeight() / fullSize.getHeight();
        miniFrame.setNormalizedViewRect(normX, normY, normW, normH);
        // it is not always necessary to show the frame; if the
        // view frame shows everything, hide the mini-frame
        if ((normW >= 1) && (normH >= 1)) {
            miniFrame.setVisible(false);
        } else {
            addOrReuseComponent(miniFrame, javax.swing.JLayeredPane.PALETTE_LAYER);
            // new frame will already float due to layering; do not want
            // to change active state of display frame
            //bringToFront(miniFrame);
        }
    }

    private void enableConstraintMenuItems(boolean b){
        if (b){
            this.helixConstraintItem.setEnabled(true);
            this.diagonalConstraintItem.setEnabled(true);
            this.aa_agConstraintItem.setEnabled(true);
            this.eLoopConstraintItem.setEnabled(true);
            this.energyConstraintItem.setEnabled(true);
            this.complexConstraintItem.setEnabled(true);
        }
        else {
            this.helixConstraintItem.setEnabled(false);
            this.diagonalConstraintItem.setEnabled(false);
            this.aa_agConstraintItem.setEnabled(false);
            this.eLoopConstraintItem.setEnabled(false);
            this.energyConstraintItem.setEnabled(false);
            this.complexConstraintItem.setEnabled(false);
        }
    }

    /**
     * Removes any window-selecting items and adds new ones to
     * reflect currently-opened windows.  This lets the user
     * bring any window to the front by selecting a menu item.
     */
    private void rebuildWindowMenu() {
        JInternalFrame[] windows = desktopPane.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER);
        ArrayList<JMenuItem> itemsToRemove = new ArrayList<JMenuItem>();
        JMenu targetMenu = this.windowMenu;
        for (int i = 0; i < targetMenu.getItemCount(); ++i) {
            JMenuItem item = targetMenu.getItem(i);
            if (item == null) {
                // e.g. separators do not have items
                continue;
            }
            Action action = item.getAction();
            if ((action != null) && (action instanceof FrameSelectAction)) {
                itemsToRemove.add(item);
            }
        }
        for (JMenuItem toDelete : itemsToRemove) {
            targetMenu.remove(toDelete);
        }
        for (JInternalFrame frame : windows) {
            if (!frame.getTitle().equals("")) {
                targetMenu.add(new FrameSelectAction(desktopPane, frame));
            }
        }
    }

    private void initComponents() {
        final RheatApp app = this;

        desktopPane = new javax.swing.JDesktopPane();
        desktopPane.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                Component added = evt.getChild();
                if (added instanceof JInternalFrame) {
                    app.rebuildWindowMenu();
                }
            }
            public void componentRemoved(java.awt.event.ContainerEvent evt) {
                Component added = evt.getChild();
                if (added instanceof JInternalFrame) {
                    app.rebuildWindowMenu();
                }
            }
        });

        customOpenPane = new JPanel();
        openOverlayColorPanel = new JPanel();
        openOverlayColorPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
        openOverlayColorPanel.setBackground(Color.black);
        openOverlayColorPanel.setVisible(false); // see listener below
        openOverlayColorButton = new JButton("Color…");
        openOverlayColorButton.setEnabled(false); // see listener below
        openOverlayColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Color newColor = JColorChooser.showDialog(app, "RNA Overlay Color", openOverlayColorPanel.getBackground());
                if (newColor != null) {
                    openOverlayColorPanel.setBackground(newColor);
                }
            }
        });
        openOverlayCheckBox = new JCheckBox("Overlay (keep existing RNA)");
        openOverlayCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                final boolean enabled = openOverlayCheckBox.isSelected();
                openOverlayColorButton.setEnabled(enabled);
                openOverlayColorPanel.setEnabled(enabled);
                openOverlayColorPanel.setVisible(enabled);
            }
        });
        customOpenPane.setLayout(new GridLayout(6, 1));
        customOpenPane.add(openOverlayCheckBox);
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new BorderLayout());
        subPanel.add(openOverlayColorPanel, BorderLayout.CENTER);
        subPanel.add(openOverlayColorButton, BorderLayout.EAST);
        customOpenPane.add(subPanel);

        helpFrame = new HelpContentJFrame();
        aboutFrame = new AboutFrame();
        miniFrame = new MiniFrame();
        mainWindowFrame = new javax.swing.JInternalFrame();

        leftToolBar = new JToolBar();
        leftToolBar.setFloatable(false);
        leftToolBar.setOrientation(JToolBar.VERTICAL);

        rightToolBar = new JToolBar();
        rightToolBar.setFloatable(false);
        rightToolBar.setOrientation(JToolBar.VERTICAL);

        topToolBar = new JToolBar();
        topToolBar.setFloatable(false);
        topToolBar.setOrientation(JToolBar.HORIZONTAL);

        bottomToolBar = new JToolBar();
        bottomToolBar.setFloatable(false);
        bottomToolBar.setOrientation(JToolBar.HORIZONTAL);

        displayControlPanel = new JPanel();
        JPanel paneZoomControls = new JPanel();
        paneZoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
        zoomOutButton = new javax.swing.JButton();
        zoomOutButton.setText("-");
        zoomOutButton.setMnemonic(KeyEvent.VK_MINUS);
        zoomOutButton.setDisplayedMnemonicIndex(-1);
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOut();
            }
        });
        zoomInButton = new javax.swing.JButton();
        zoomInButton.setText("+");
        zoomInButton.setMnemonic(KeyEvent.VK_EQUALS);
        zoomInButton.setDisplayedMnemonicIndex(-1);
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomIn();
            }
        });
        zoomFitButton = new javax.swing.JButton();
        zoomFitButton.setText("Fit");
        zoomFitButton.setMnemonic(KeyEvent.VK_F);
        zoomFitButton.setDisplayedMnemonicIndex(-1);
        zoomFitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomFit();
            }
        });
        zoomSlider = new JSlider(10, 20000);
        zoomSlider.setMajorTickSpacing(2000);
        zoomSlider.setMinorTickSpacing(1000);
        zoomSlider.setPaintTicks(true);
        //zoomSlider.setPaintTrack(false);
        zoomSlider.putClientProperty("JSlider.isFilled", Boolean.FALSE);
        zoomSlider.setUI(new MetalSliderUI() {
            // oddly, there is no way to set the amount shifted after
            // clicking in the slider; it is controlled by look-and-feel
            // so customize the look-and-feel accordingly
            protected void scrollDueToClickInTrack(int direction) {
                if (false) {
                    // option 1: set value to point that is clicked:
                    int value = zoomSlider.getValue();
                    value = this.valueForXPosition(zoomSlider.getMousePosition().x);
                    zoomSlider.setValue(value);
                } else {
                    // option 2: increment or decrement by fixed amount
                    scrollByBlock(direction);
                }
            }
        });
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zoomLevelChanged();
            }
        });
        zoomLabel = new JLabel();
        paneZoomControls.add(zoomOutButton);
        paneZoomControls.add(zoomInButton);
        paneZoomControls.add(zoomFitButton);
        paneZoomControls.add(zoomSlider);
        paneZoomControls.add(zoomLabel);
        JPanel paneJump = new JPanel();
        paneJump.setBorder(BorderFactory.createTitledBorder("Jump to X and Y"));
        jumpButton = new JButton("Go");
        jumpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    if (jumpFieldY.getText().isEmpty()) {
                        int xInt = Integer.parseInt(jumpFieldX.getText());
                        scrollTo(xInt, xInt);
                    } else if (jumpFieldX.getText().isEmpty()) {
                        int yInt = Integer.parseInt(jumpFieldY.getText());
                        scrollTo(yInt, yInt);
                    } else {
                        int xInt = Integer.parseInt(jumpFieldX.getText());
                        int yInt = Integer.parseInt(jumpFieldY.getText());
                        scrollTo(xInt, yInt);
                    }
                } catch (NumberFormatException e) {
                    log(ERROR, "Expected a number.");
                    //e.printStackTrace();
                }
            }
        });
        jumpFieldX = new JTextField();
        jumpFieldX.setColumns(6);
        jumpFieldX.setToolTipText("Enter a number from 1 to the number of base-pairs to jump to that X position.");
        jumpFieldX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpButton.doClick();
            }
        });
        jumpFieldY = new JTextField();
        jumpFieldY.setColumns(6);
        jumpFieldY.setToolTipText("Enter a number from 1 to the number of base-pairs to jump to that Y position.  Or, leave blank to copy the X value.");
        jumpFieldY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpButton.doClick();
            }
        });
        paneJump.add(jumpFieldX);
        paneJump.add(jumpFieldY);
        paneJump.add(jumpButton);
        displayControlPanel.setLayout(new FlowLayout(java.awt.FlowLayout.LEFT));
        displayControlPanel.add(paneZoomControls);
        displayControlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        displayControlPanel.add(paneJump);
        displayScrollPane = new javax.swing.JScrollPane();
        displayPane = new RNADisplay();
        displayPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                displayPaneMouseClicked(evt);
            }
        });
        displayScrollPane.setViewportView(displayPane);
        displayScrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateMiniFrame();
            }
        });
        displayScrollPane.setMinimumSize(new Dimension(420, 200));
        displayScrollPane.setPreferredSize(new Dimension(700, 500));
        displayScrollPane.getHorizontalScrollBar().setUnitIncrement(10); // arbitrary
        displayScrollPane.getVerticalScrollBar().setUnitIncrement(10); // arbitrary
        JPanel paneDiagonalScroll = new JPanel();
        paneDiagonalScroll.setLayout(new GridLayout(2, 2));
        scrollUpLeftBtn = new JButton();
        scrollUpLeftBtn.setText("");
        scrollUpLeftBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollUpLeft();
            }
        });
        scrollDownRightBtn = new JButton();
        scrollDownRightBtn.setText("");
        scrollDownRightBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollDownRight();
            }
        });
        paneDiagonalScroll.add(scrollUpLeftBtn);
        paneDiagonalScroll.add(Box.createRigidArea(new Dimension(10, 10)));
        paneDiagonalScroll.add(Box.createRigidArea(new Dimension(10, 10)));
        paneDiagonalScroll.add(scrollDownRightBtn);
        displayScrollPane.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, paneDiagonalScroll);
        JPanel paneDisplay = new JPanel();
        paneDisplay.setLayout(new BorderLayout());
        paneDisplay.add(displayControlPanel, BorderLayout.NORTH);
        paneDisplay.add(displayScrollPane, BorderLayout.CENTER);

        uidValue = new javax.swing.JLabel();
        organismValue = new javax.swing.JLabel();
        accNumValue = new javax.swing.JLabel();
        lengthValue = new javax.swing.JLabel();
        helixNumField = new javax.swing.JLabel();
        helixTotalField = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        helixActualField = new javax.swing.JLabel();
        historyTextPane = new javax.swing.JTextArea();
        undoConstraintBtn = new javax.swing.JButton();
        helixInfoTextPane = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openRNAMenuItem = new javax.swing.JMenuItem();
        openTagsMenuItem = new javax.swing.JMenuItem();
        openDataMenuItem = new javax.swing.JMenuItem();
        runScriptMenuItem = new javax.swing.JMenuItem();
        runProgramMenuItem = new javax.swing.JMenuItem();
        closeRNAMenuItem = new javax.swing.JMenuItem();
        closeWindowMenuItem = new javax.swing.JMenuItem();
        minimizeWindowMenuItem = new javax.swing.JMenuItem();
        zoomWindowMenuItem = new javax.swing.JMenuItem();
        selectNextWindowMenuItem = new javax.swing.JMenuItem();
        selectPreviousWindowMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        preferencesMenuItem = new javax.swing.JMenuItem();
        filterMenu = new javax.swing.JMenu();
        basepairConstraintItem = new javax.swing.JMenuItem();
        helixConstraintItem = new javax.swing.JMenuItem();
        diagonalConstraintItem = new javax.swing.JMenuItem();
        aa_agConstraintItem = new javax.swing.JMenuItem();
        eLoopConstraintItem = new javax.swing.JMenuItem();
        energyConstraintItem = new javax.swing.JMenuItem();
        complexConstraintItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewType2DMenuItem = new javax.swing.JCheckBoxMenuItem();
        viewTypeFlatMenuItem = new javax.swing.JCheckBoxMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomFitMenuItem = new javax.swing.JMenuItem();
        windowMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        contentMenuItem = new javax.swing.JMenuItem();
        goBackMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setTitle("RNA HEAT");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitProgram();
            }
        });

        // IMPORTANT: in order for floatable toolbars to work correctly,
        // a BorderLayout can only have ONE non-CENTER element that is
        // used; therefore, to allow “multiple” toolbars, the illusion
        // of a single frame must be created by nesting panels that use
        // BorderLayout, where each CENTER component holds one of the
        // other panels and only ONE non-CENTER element is a toolbar in
        // each case
        final boolean LIVE_REPAINT = true; // see JSplitPane constructor
        JSplitPane joinDisplayRightToolBar = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, LIVE_REPAINT, paneDisplay, rightToolBar);
        JSplitPane joinDisplayLeftRightToolBars = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, LIVE_REPAINT, leftToolBar, joinDisplayRightToolBar);
        JPanel joinDisplayLeftRightBottomToolBars = new JPanel();
        joinDisplayLeftRightBottomToolBars.setLayout(new BorderLayout());
        joinDisplayLeftRightBottomToolBars.add(bottomToolBar, BorderLayout.SOUTH);
        joinDisplayLeftRightBottomToolBars.add(joinDisplayLeftRightToolBars, BorderLayout.CENTER);
        JPanel paneMainWindowContent = new JPanel();
        paneMainWindowContent.setLayout(new BorderLayout());
        //paneMainWindowContent.add(topToolBar, BorderLayout.NORTH); // FIXME: add this when there are controls present
        paneMainWindowContent.add(joinDisplayLeftRightBottomToolBars, BorderLayout.CENTER);
        //mainWindowFrame.getContentPane().setLayout(new BorderLayout());
        mainWindowFrame.getContentPane().add(paneMainWindowContent);
        mainWindowFrame.setMaximizable(true);
        mainWindowFrame.setResizable(true);
        mainWindowFrame.setTitle("Main Window");
        mainWindowFrame.setMinimumSize(new java.awt.Dimension(100, 100));
        mainWindowFrame.setNormalBounds(new java.awt.Rectangle(270, 20, 400, 400));
        mainWindowFrame.setVisible(true);
        mainWindowFrame.setBounds(10, 10, 600, 500);
        mainWindowFrame.addPropertyChangeListener(mainWindowFrame.IS_MAXIMUM_PROPERTY, this);

        JPanel paneCurrentRNAInfo = new JPanel();
        paneCurrentRNAInfo.setLayout(new java.awt.GridLayout(4, 1));
        paneCurrentRNAInfo.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JPanel paneRNAInfoLabels = new JPanel();
        JPanel paneRNAInfoValues = new JPanel();
        paneRNAInfoLabels.setLayout(new GridLayout(7, 2));
        paneRNAInfoValues.setLayout(new GridLayout(7, 2));
        paneRNAInfoLabels.add(new JLabel(" Name: "));
        paneRNAInfoValues.add(uidValue);
        paneRNAInfoLabels.add(new JLabel(" Organism: "));
        paneRNAInfoValues.add(organismValue);
        paneRNAInfoLabels.add(new JLabel(" Accession #: "));
        paneRNAInfoValues.add(accNumValue);
        paneRNAInfoLabels.add(new JLabel(" Length: "));
        paneRNAInfoValues.add(lengthValue);
        paneRNAInfoLabels.add(new JLabel(" Current Helices: "));
        helixNumField.setMinimumSize(new Dimension(100, (int)helixNumField.getMinimumSize().getHeight()));
        paneRNAInfoValues.add(helixNumField);
        paneRNAInfoLabels.add(new JLabel(" Total Helices: "));
        helixTotalField.setMinimumSize(new Dimension(100, (int)helixTotalField.getMinimumSize().getHeight()));
        paneRNAInfoValues.add(helixTotalField);
        paneRNAInfoLabels.add(new JLabel(" Actual Helices: "));
        helixActualField.setMinimumSize(new Dimension(100, (int)helixActualField.getMinimumSize().getHeight()));
        paneRNAInfoValues.add(helixActualField);
        JPanel paneHelixCounts = new JPanel();
        paneHelixCounts.setLayout(new BorderLayout());
        paneHelixCounts.add(paneRNAInfoLabels, BorderLayout.WEST);
        paneHelixCounts.add(paneRNAInfoValues, BorderLayout.CENTER);
        Box paneFileInfo = new Box(BoxLayout.Y_AXIS);
        paneFileInfo.setBorder(BorderFactory.createTitledBorder("RNA File Info"));
        paneFileInfo.add(paneCurrentRNAInfo);
        paneFileInfo.add(paneHelixCounts);

        historyTextPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        historyTextPane.setEditable(false);
        historyTextPane.setFont(getMonospacedFont(historyTextPane.getFont(), 12)); // monospaced font so pairs are easier to see
        JScrollPane scrollConstraintHistory = new JScrollPane();
        scrollConstraintHistory.setViewportView(historyTextPane);
        scrollConstraintHistory.setAutoscrolls(true);
        scrollConstraintHistory.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white));
        scrollConstraintHistory.setMinimumSize(new Dimension(250, 100));
        undoConstraintBtn.setText("Undo Last");
        undoConstraintBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performUndo();
            }
        });
        JPanel historyButtonPanel = new JPanel();
        historyButtonPanel.add(undoConstraintBtn);
        JPanel paneHistory = new JPanel();
        paneHistory.setLayout(new BorderLayout());
        paneHistory.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        paneHistory.add(scrollConstraintHistory, BorderLayout.CENTER);
        paneHistory.add(historyButtonPanel, BorderLayout.SOUTH);
        JPanel padPaneConstraintHistory = new JPanel();
        padPaneConstraintHistory.setLayout(new BorderLayout());
        padPaneConstraintHistory.setBorder(BorderFactory.createTitledBorder("Constraint History"));
        padPaneConstraintHistory.add(paneHistory, BorderLayout.CENTER);

        helixInfoTextPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        helixInfoTextPane.setEditable(false);
        helixInfoTextPane.setFont(getMonospacedFont(helixInfoTextPane.getFont(), 12)); // monospaced font so pairs are easier to see
        JScrollPane scrollSelectedHelixInfo = new JScrollPane();
        scrollSelectedHelixInfo.setViewportView(helixInfoTextPane);
        scrollSelectedHelixInfo.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white));
        scrollSelectedHelixInfo.setMinimumSize(new Dimension(250, 100));
        JPanel paneSelectedHelix = new JPanel();
        paneSelectedHelix.setLayout(new BorderLayout());
        paneSelectedHelix.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        paneSelectedHelix.add(scrollSelectedHelixInfo, BorderLayout.CENTER);
        JPanel padPaneSelectedHelix = new JPanel();
        padPaneSelectedHelix.setLayout(new BorderLayout());
        padPaneSelectedHelix.setBorder(BorderFactory.createTitledBorder("Selected Helix"));
        padPaneSelectedHelix.add(paneSelectedHelix, BorderLayout.CENTER);

        getContentPane().add(desktopPane, java.awt.BorderLayout.CENTER);
        setExtendedState(MAXIMIZED_BOTH);

        leftToolBar.add(paneFileInfo);
        leftToolBar.add(Box.createVerticalStrut(10));
        leftToolBar.add(padPaneSelectedHelix);
        leftToolBar.add(Box.createVerticalStrut(10));
        leftToolBar.add(padPaneConstraintHistory);

        JPanel paneExperimentInfo = new JPanel();
        JPanel paneExperiments = new JPanel();
        paneExperiments.setLayout(new BorderLayout());
        paneExperiments.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        paneExperiments.add(paneExperimentInfo, BorderLayout.CENTER);
        JPanel padPaneExperiments = new JPanel();
        padPaneExperiments.setLayout(new BorderLayout());
        padPaneExperiments.setBorder(BorderFactory.createTitledBorder("Experiments"));
        padPaneExperiments.add(paneExperiments, BorderLayout.CENTER);

        rightToolBar.add(padPaneExperiments);
        // TODO: one option for the right-hand side is an interface
        // for managing the files in the experiment tree; for now,
        // just hide this region of the window
        rightToolBar.setVisible(false);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        openRNAMenuItem.setMnemonic('O');
        setKey(openRNAMenuItem, KeyEvent.VK_O);
        openRNAMenuItem.setText("Open RNA File…");
        openRNAMenuItem.setToolTipText("Updates the display with the contents of an RNA data source (such as a '.bpseq' file).");
        openRNAMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openRNAMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openRNAMenuItem);

        openTagsMenuItem.setMnemonic('A');
        //setKey(openTagsMenuItem, KeyEvent.VK_O);
        openTagsMenuItem.setText("Open Annotation File…");
        openTagsMenuItem.setToolTipText("Updates the display using a source of helix annotations (such as a '.bpcolor' file).");
        openTagsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTagsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openTagsMenuItem);

        openDataMenuItem.setMnemonic('T');
        //setKey(openDataMenuItem, KeyEvent.VK_O);
        openDataMenuItem.setText("Open Text or Image File…");
        openDataMenuItem.setToolTipText("Opens a data file in a separate display, keeping the RNA display untouched.");
        openDataMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDataMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openDataMenuItem);

        runScriptMenuItem.setMnemonic('R');
        setKey(runScriptMenuItem, KeyEvent.VK_R);
        runScriptMenuItem.setText("Run Script…");
        runScriptMenuItem.setToolTipText("Runs commands from a JavaScript ('.js') file, such as a series of filters.");
        runScriptMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runScriptMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(runScriptMenuItem);

        runProgramMenuItem.setMnemonic('P');
        //setKey(runProgramMenuItem, KeyEvent.VK_...);
        runProgramMenuItem.setText("Run Program…");
        runProgramMenuItem.setToolTipText("Runs external programs, sending output to the current experiment tree.");
        runProgramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgramMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(runProgramMenuItem);
        fileMenu.addSeparator();

        closeWindowMenuItem.setMnemonic('W');
        setKey(closeWindowMenuItem, KeyEvent.VK_W);
        closeWindowMenuItem.setText("Close Window");
        closeWindowMenuItem.setToolTipText("Hides the frontmost window that has a close box.");
        closeWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWindowMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(closeWindowMenuItem);

        closeRNAMenuItem.setMnemonic('C');
        //setKey(closeRNAMenuItem, KeyEvent.VK_W);
        closeRNAMenuItem.setText("Close RNA");
        closeRNAMenuItem.setToolTipText("Clears the display and all other windows.");
        closeRNAMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeRNA();
            }
        });

        fileMenu.add(closeRNAMenuItem);

        saveAsMenuItem.setMnemonic('S');
        setKey(saveAsMenuItem, KeyEvent.VK_S);
        saveAsMenuItem.setText("Save As…");
        saveAsMenuItem.setToolTipText("Exports a picture of the current display as an image file (such as '.png').");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();

        exitMenuItem.setMnemonic((appMain.isMac()) ? 'Q' : 'E');
        setKey(exitMenuItem, KeyEvent.VK_Q);
        exitMenuItem.setText((appMain.isMac()) ? "Quit" : "Exit");
        exitMenuItem.setToolTipText("Ends the program.");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitProgram();
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("Edit");

        undoMenuItem.setMnemonic('U');
        setKey(undoMenuItem, KeyEvent.VK_Z);
        undoMenuItem.setText("Undo");
        undoMenuItem.setToolTipText("Reverses the effects of the most recent action, if possible.");
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performUndo();
            }
        });

        editMenu.add(undoMenuItem);
        editMenu.addSeparator();

        cutMenuItem.setAction(new DefaultEditorKit.CutAction()); // do this first, as it may change other things
        cutMenuItem.setMnemonic('t');
        setKey(cutMenuItem, KeyEvent.VK_X);
        cutMenuItem.setText("Cut");
        cutMenuItem.setToolTipText("Clears any selection, if possible, and copies it to the Clipboard.");

        editMenu.add(cutMenuItem);

        copyMenuItem.setAction(new DefaultEditorKit.CopyAction()); // do this first, as it may change other things
        copyMenuItem.setMnemonic('C');
        setKey(copyMenuItem, KeyEvent.VK_C);
        copyMenuItem.setText("Copy");
        copyMenuItem.setToolTipText("Copies any selection to the Clipboard, if possible.");

        editMenu.add(copyMenuItem);

        pasteMenuItem.setAction(new DefaultEditorKit.PasteAction()); // do this first, as it may change other things
        pasteMenuItem.setMnemonic('P');
        setKey(pasteMenuItem, KeyEvent.VK_V);
        pasteMenuItem.setText("Paste");
        pasteMenuItem.setToolTipText("Inserts anything that has been put on the Clipboard with a Copy command.");

        editMenu.add(pasteMenuItem);
        editMenu.addSeparator();

        preferencesMenuItem.setMnemonic('r');
        setKey(preferencesMenuItem, KeyEvent.VK_SEMICOLON);
        preferencesMenuItem.setText("Preferences…");
        preferencesMenuItem.setToolTipText("Allows customization of behavior, such as the directory to start from when opening files.");
        preferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(preferencesMenuItem);

        menuBar.add(editMenu);

        filterMenu.setMnemonic('C');
        filterMenu.setText("Constraints");

        basepairConstraintItem.setMnemonic('B');
        setKey(basepairConstraintItem, KeyEvent.VK_1);
        basepairConstraintItem.setText("Basepairs…");
        basepairConstraintItem.setToolTipText("Excludes helices that do not match selected base-pair values, such as 'C-G'.");
        basepairConstraintItem.setEnabled(false);
        basepairConstraintItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBasePairConstraintDialog();
            }
        });

        filterMenu.add(basepairConstraintItem);

        helixConstraintItem.setMnemonic('H');
        setKey(helixConstraintItem, KeyEvent.VK_2);
        helixConstraintItem.setText("Helix Length…");
        helixConstraintItem.setToolTipText("Excludes helices with a span more or less than the specified range.");
        helixConstraintItem.setEnabled(false);
        helixConstraintItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHelixLengthConstraintDialog();
            }
        });

        filterMenu.add(helixConstraintItem);

        diagonalConstraintItem.setMnemonic('D');
        setKey(diagonalConstraintItem, KeyEvent.VK_3);
        diagonalConstraintItem.setText("Diagonal Distance…");
        diagonalConstraintItem.setToolTipText("Excludes helices whose distance from the diagonal line (2D view) is outside the given range.");
        diagonalConstraintItem.setEnabled(false);
        diagonalConstraintItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showDiagonalDistanceConstraintDialog();
            }
        });

        filterMenu.add(diagonalConstraintItem);

        aa_agConstraintItem.setMnemonic('A');
        setKey(aa_agConstraintItem, KeyEvent.VK_4);
        aa_agConstraintItem.setText("AA / AG Ends…");
        aa_agConstraintItem.setToolTipText("Excludes helices that do not have AA or AG 'just past' their ends.");
        aa_agConstraintItem.setEnabled(false);
        aa_agConstraintItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAA_AGEndsConstraintDialog();
            }
        });

        filterMenu.add(aa_agConstraintItem);

        eLoopConstraintItem.setMnemonic('E');
        setKey(eLoopConstraintItem, KeyEvent.VK_5);
        eLoopConstraintItem.setText("E-Loop…");
        eLoopConstraintItem.setToolTipText("Excludes helices unless they have AAG at 3' start, AUG at 5' start, GAA at 3' end, and AUG at 5' end.");
        eLoopConstraintItem.setEnabled(false);
        eLoopConstraintItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showELoopConstraintDialog();
            }
        });

        filterMenu.add(eLoopConstraintItem);

        energyConstraintItem.setMnemonic('g');
        setKey(energyConstraintItem, KeyEvent.VK_6);
        energyConstraintItem.setText("Helix Energy…");
        energyConstraintItem.setToolTipText("Excludes helices that have an energy value outside the given range.");
        energyConstraintItem.setEnabled(false);
        energyConstraintItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showEnergyConstraintDialog();
            }
        });

        filterMenu.add(energyConstraintItem);

        complexConstraintItem.setMnemonic('C');
        setKey(complexConstraintItem, KeyEvent.VK_7);
        complexConstraintItem.setText("Complex Distance…");
        complexConstraintItem.setToolTipText("Excludes helices with simple or complex distances that are greater than the specified maximums.");
        complexConstraintItem.setEnabled(false);
        complexConstraintItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showComplexDistanceConstraintDialog();
            }
        });

        filterMenu.add(complexConstraintItem);

        menuBar.add(filterMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");

        viewType2DMenuItem.setMnemonic('2');
        viewType2DMenuItem.setState(true); // initially...
        viewType2DMenuItem.setText("2D");
        viewType2DMenuItem.setToolTipText("Changes to a two-axis display mode, with a diagonal line.");
        viewType2DMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setViewType2D();
            }
        });

        viewMenu.add(viewType2DMenuItem);

        viewTypeFlatMenuItem.setMnemonic('l');
        viewTypeFlatMenuItem.setText("Flat");
        viewTypeFlatMenuItem.setToolTipText("Changes to a single-axis display mode.");
        viewTypeFlatMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setViewTypeFlat();
            }
        });

        viewMenu.add(viewTypeFlatMenuItem);
        viewMenu.addSeparator();

        zoomOutMenuItem.setMnemonic('O');
        setKey(zoomOutMenuItem, KeyEvent.VK_COMMA);
        zoomOutMenuItem.setText("Zoom Out");
        zoomOutMenuItem.setToolTipText("Shows more of the display, at a reduced size.");
        zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOut();
            }
        });

        viewMenu.add(zoomOutMenuItem);

        zoomInMenuItem.setMnemonic('I');
        setKey(zoomInMenuItem, KeyEvent.VK_PERIOD);
        zoomInMenuItem.setText("Zoom In");
        zoomInMenuItem.setToolTipText("Shows less of the display, at an increased size.");
        zoomInMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomIn();
            }
        });

        viewMenu.add(zoomInMenuItem);

        zoomFitMenuItem.setMnemonic('F');
        setKey(zoomFitMenuItem, KeyEvent.VK_EQUALS);
        zoomFitMenuItem.setText("Zoom to Fit");
        zoomFitMenuItem.setToolTipText("Shows the entire display, at a size that fills the window.");
        zoomFitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomFit();
            }
        });

        viewMenu.add(zoomFitMenuItem);

        menuBar.add(viewMenu);

        windowMenu.setMnemonic('W');
        windowMenu.setText("Window");

        minimizeWindowMenuItem.setMnemonic('M');
        setKey(minimizeWindowMenuItem, KeyEvent.VK_M);
        minimizeWindowMenuItem.setText("Minimize Window");
        minimizeWindowMenuItem.setToolTipText("Reduces the frontmost window to a miniature form.");
        minimizeWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeWindowMenuItemActionPerformed(evt);
            }
        });

        windowMenu.add(minimizeWindowMenuItem);

        zoomWindowMenuItem.setMnemonic((appMain.isMac()) ? 'Z' : 'x');
        setKey(zoomWindowMenuItem, KeyEvent.VK_QUOTE);
        zoomWindowMenuItem.setText((appMain.isMac()) ? "Zoom Window" : "Maximize/Restore Window");
        zoomWindowMenuItem.setToolTipText("Toggles the frontmost window between its two main sizes.");
        zoomWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomWindowMenuItemActionPerformed(evt);
            }
        });

        windowMenu.add(zoomWindowMenuItem);

        selectNextWindowMenuItem.setMnemonic('N');
        setKey(selectNextWindowMenuItem, KeyEvent.VK_BACK_QUOTE);
        selectNextWindowMenuItem.setText("Select Next Window");
        selectNextWindowMenuItem.setToolTipText("Brings another window to the front.");
        selectNextWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNextWindowMenuItemActionPerformed(evt);
            }
        });

        windowMenu.add(selectNextWindowMenuItem);

        selectPreviousWindowMenuItem.setMnemonic('P');
        setShiftedKey(selectPreviousWindowMenuItem, KeyEvent.VK_BACK_QUOTE);
        selectPreviousWindowMenuItem.setText("Select Previous Window");
        selectPreviousWindowMenuItem.setToolTipText("Brings another window to the front (reverse order).");
        selectPreviousWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPreviousWindowMenuItemActionPerformed(evt);
            }
        });

        windowMenu.add(selectPreviousWindowMenuItem);
        windowMenu.addSeparator();

        menuBar.add(windowMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

        setKey(contentMenuItem, KeyEvent.VK_SLASH);
        contentMenuItem.setText("Contents");
        contentMenuItem.setToolTipText("Shows a window with user documentation on RNA HEAT.");
        contentMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayHelp();
            }
        });

        helpMenu.add(contentMenuItem);

        setKey(goBackMenuItem, KeyEvent.VK_LEFT);
        goBackMenuItem.setText("Go Back");
        goBackMenuItem.setToolTipText("Displays the previously-visited help page, if a link was followed.");
        goBackMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpFrame.performGoBack();
            }
        });

        helpMenu.add(goBackMenuItem);
        helpMenu.addSeparator();

        aboutMenuItem.setText("About");
        aboutMenuItem.setToolTipText("Shows a window with information on the authors of RNA HEAT.");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        addOrReuseComponent(mainWindowFrame);
        try {
            mainWindowFrame.setMaximum(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
        bringToFront(mainWindowFrame);

        pack();
    }

    /**
     * Adds a filter to the history list and applies its effects to
     * the currently-displayed RNA.
     */
    public void addConstraint(Filter filter) {
        try {
            try {
                appMain.snapshotRNAData();
            } catch (IOException e) {
                e.printStackTrace();
                log(WARN, "Unable to take a snapshot of previous state (see trace above); Undo will not work.");
            }
            // to guarantee the script command is correct, constraints are
            // applied by executing the equivalent script command instead of
            // by calling appMain.addConstraint() and updateImage() (see
            // "rheat/script/ScriptMain.java" implementations of each)
            String equivalentScriptCommand = ConstraintInterpreter.getScriptCommandFor(filter);
            appMain.evaluateScriptCode(equivalentScriptCommand);
            appMain.incrementUndo(); // success; next snapshot should use a new number
            // TODO: should the history-update portion be an option?
            addHistoryCommand(equivalentScriptCommand);
        } catch (Exception e) {
            e.printStackTrace();
            log(WARN, "Unable to produce script command for filter history (see trace above).");
        }
    }

    private void showAA_AGEndsConstraintDialog() {
        FilterDialog fd = new AAandAGFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showBasePairConstraintDialog() {
        FilterDialog fd = new BasepairFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
            this.enableConstraintMenuItems(true);
            HelixStore helices = appMain.rnaData.getHelices();
            int count = ((helices != null) ? helices.getCount() : 0);
            this.helixTotalField.setText("" + count);
        }
    }

    private void showComplexDistanceConstraintDialog() {
        FilterDialog fd = new ComplexFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showDiagonalDistanceConstraintDialog() {
        FilterDialog fd = new DiagonalFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showELoopConstraintDialog() {
        FilterDialog fd = new ELoopFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showEnergyConstraintDialog() {
        FilterDialog fd = new EnergyFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showHelixLengthConstraintDialog() {
        FilterDialog fd = new HelixFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void performUndo() {
        try {
            int index = appMain.getUndoIndex() - 1;
            appMain.revertToPreviousRNA(index);
            updateImage();
            refreshHistoryTextPane();
            log(INFO, "Reverted to snapshot #" + index + ".");
        } catch (Exception e) {
            e.printStackTrace();
            log(WARN, "Unable to undo (see trace above).");
        }
    }

    private void performCutSelection() {
        log(ERROR, "not implemented: Cut"); // FIXME
    }

    private void performCopySelection() {
        log(ERROR, "not implemented: Copy"); // FIXME
    }

    private void performPaste() {
        log(ERROR, "not implemented: Paste"); // FIXME
    }

    private void performDeleteSelection() {
        log(ERROR, "not implemented: Delete"); // FIXME
    }

    private void preferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        new PreferenceDialog(appMain).run(this);
    }

    private void closeWindowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        JInternalFrame activeWindow = this.desktopPane.getSelectedFrame();
        if ((activeWindow != null) && activeWindow.isClosable()) {
            try {
                activeWindow.setClosed(true);
                activeWindow.setVisible(false); // seems necessary too, in some cases...
                desktopPane.selectFrame(true/* forward */); // arbitrary; keep something focused
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void minimizeWindowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        JInternalFrame activeWindow = this.desktopPane.getSelectedFrame();
        if ((activeWindow != null) && activeWindow.isIconifiable()) {
            try {
                activeWindow.setIcon(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void zoomWindowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        JInternalFrame activeWindow = this.desktopPane.getSelectedFrame();
        if ((activeWindow != null) && activeWindow.isMaximizable()) {
            try {
                activeWindow.setMaximum(!activeWindow.isMaximum());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void selectNextWindowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        this.desktopPane.selectFrame(false/* forward */);
    }

    private void selectPreviousWindowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        this.desktopPane.selectFrame(true/* forward */);
    }

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        File outputImage;
        fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Files", "png"));
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JPEG Files", "jpg"));
        if (fc.showSaveDialog(this) == fc.APPROVE_OPTION){
            outputImage = fc.getSelectedFile();
            try {
                BufferedImage img = helixImgGen.drawImage(appMain.rnaData);
                if (fc.getFileFilter().getDescription().equals("PNG Files")){
                    javax.imageio.ImageIO.write(img, "png", outputImage);
                }
                else{
                    javax.imageio.ImageIO.write(img, "jpeg", outputImage);
                }
            }
            catch (java.io.IOException ex){
                JOptionPane.showMessageDialog(this, "Image IO error", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Responds to a mouse click by updating the display (to
     * select a helix for example).  Information about the
     * selected helix will also be updated accordingly.
     */
    private void displayPaneMouseClicked(java.awt.event.MouseEvent evt) {
        java.awt.Point p = evt.getPoint();
        java.awt.Rectangle rect = this.displayScrollPane.getViewport().getViewRect();
        double x = p.getX();
        double y = p.getY();
        helixImgGen.setPrimarySelectionLocation(x, y, rect.getSize());
        this.updateImage();
        // for an unknown reason, normal updates do not seem to
        // take place in the case of a click; perhaps it is due
        // to interference from processing events but in any
        // case, an explicit repaint will work
        displayPane.repaint();
        // IMPORTANT: current helix is not up-to-date until after
        // the image is updated; the propertyChange() method is
        // used to respond slightly later, after an update
    }

    private void displayHelp() {
        addOrReuseComponent(helpFrame);
        bringToFront(helpFrame);
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        addOrReuseComponent(aboutFrame);
        bringToFront(aboutFrame);
    }

    private void setZoomLevel(double newLevel) {
        zoomSlider.setValue((int)(1000 * newLevel));
        //zoomLevelChanged(); // implicit, from events in slider
    }

    private void zoomLevelChanged() {
        double zoom = this.getZoomLevel();
        String floatFormat = ((zoom < 10)
                              ? String.format("%.2f", zoom)
                              : String.format("%.1f", zoom));
        zoomLabel.setText(floatFormat + "x");
        helixImgGen.setZoomLevel(this.getZoomLevel());
        this.updateImage();
    }

    private void setViewTypeFlat() {
        viewType2DMenuItem.setState(false);
        viewTypeFlatMenuItem.setState(true);
        if (helixImgGen.setImageType(HelixImageGenerator.ViewType.VIEW_FLAT)) {
            this.updateImage();
        }
        miniFrame.setViewType(HelixImageGenerator.ViewType.VIEW_FLAT);
    }
    
    private void setViewType2D() {
        viewType2DMenuItem.setState(true);
        viewTypeFlatMenuItem.setState(false);
        if (helixImgGen.setImageType(HelixImageGenerator.ViewType.VIEW_2D)) {
            this.updateImage();
        }
        miniFrame.setViewType(HelixImageGenerator.ViewType.VIEW_2D);
    }

    /**
     * Private method to do clean up when closing the current session.
     */
    private void closeRNA() {
        appMain.cleanUp();
        refreshForNewRNA();
        this.enableConstraintMenuItems(false);
        this.basepairConstraintItem.setEnabled(false);
        this.clearHistory();
    }

    /**
     * Convenience version of this method that resets everything.
     */
    public void refreshForNewRNA() {
        refreshForNewRNA(EnumSet.noneOf(RNADisplayFeature.class));
    }

    /**
     * Updates the display to reflect current RNA data.
     * Used after opening helix files.
     */
    public void refreshForNewRNA(EnumSet<RNADisplayFeature> thingsToKeep) {
        RNA rna = appMain.rnaData;
        if (rna != null) {
            this.setTitle("RNA HEAT: " + rna.getUID());
            this.helixImgGen.setBaseWidth(rna.getLength());
            this.helixImgGen.setBaseHeight(rna.getLength());
            HelixStore helices = rna.getHelices();
            int count = ((helices != null) ? helices.getCount() : 0);
            this.helixActualField.setText("" + rna.getActual().getCount());
            this.helixNumField.setText("" + count);
            this.helixTotalField.setText("");
        } else {
            this.setTitle("RNA HEAT");
            this.helixImgGen.setBaseWidth(100); // arbitrary (will be blank)
            this.helixImgGen.setBaseHeight(100);
            this.helixActualField.setText("");
            this.helixNumField.setText("");
            this.helixTotalField.setText("");
        }
        if (!thingsToKeep.contains(RNADisplayFeature.ZOOM_LEVEL)) {
            setZoomLevel(1);
        }
        this.helixInfoTextPane.setText("");
        setControlLabels();
        basepairConstraintItem.setEnabled(true);
        this.updateImage(); // erases to blank if "appMain.rnaData" is null
    }

    private void openRNAMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        File inputFile;
        fc = new JFileChooser(appMain.getPrefHelixDataDir());
        this.openOverlayCheckBox.setSelected(false);
        fc.setAccessory(this.customOpenPane); // contains "openOverlayCheckBox"
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("BPSEQ Files", "bpseq", "txt"));
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == fc.APPROVE_OPTION) {
            final boolean isOverlay = openOverlayCheckBox.isSelected();
            inputFile = fc.getSelectedFile();
            try {
                // openRNA()/openOverlayRNA() will call refreshForNewRNA()
                if (isOverlay) {
                    // in AppMain class, overlay data automatically
                    // uses the same base-pair filter as the original
                    appMain.openOverlayRNA(inputFile.getAbsolutePath(), openOverlayColorPanel.getBackground());
                } else {
                    appMain.openRNA(inputFile.getAbsolutePath());
                    zoomFit();
                    // automatically request a base-pair set, since
                    // otherwise the default display is not very useful
                    showBasePairConstraintDialog();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error Opening File", JOptionPane.ERROR_MESSAGE);
            }
            updateImage();
        }
    }

    private void openTagsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        fc = new JFileChooser(appMain.getPrefHelixDataDir());
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Annotation Files", "bpcolor", "txt"));
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == fc.APPROVE_OPTION) {
            for (File inputFile : fc.getSelectedFiles()) {
                try {
                    // openTags() will call refreshForNewRNA()
                    appMain.openTags(inputFile.getAbsolutePath());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error Opening File", JOptionPane.ERROR_MESSAGE);
                }
            }
            updateImage();
        }
    }

    private void openDataMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        fc = new JFileChooser(appMain.getPrefHelixDataDir());
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text or Image Files", "txt", "jpeg", "jpg", "png"));
        fc.setAcceptAllFileFilterUsed(true); // try to open anything as text
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == fc.APPROVE_OPTION) {
            for (File inputFile : fc.getSelectedFiles()) {
                try {
                    String filePath = inputFile.getAbsolutePath();
                    this.openDataFile(filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error Opening File", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void runScriptMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        File inputFile;
        fc = new JFileChooser(appMain.getPrefScriptDir());
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JavaScript Files", "js", "txt"));
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == fc.APPROVE_OPTION) {
            inputFile = fc.getSelectedFile();
            try {
                appMain.runScript(inputFile.getAbsolutePath());
            } catch (Exception e) {
                JTextArea msg = new JTextArea(e.getMessage());
                msg.setColumns(65);
                msg.setRows(7);
                msg.setLineWrap(true);
                msg.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(msg);
                JOptionPane.showMessageDialog(this, scrollPane, "Error Running Script", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void runProgramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        File inputFile;
        fc = new JFileChooser(appMain.getPrefProgramsDir());
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Python or JAR Files", "jar", "py"));
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == fc.APPROVE_OPTION) {
            inputFile = fc.getSelectedFile();
            int exitStatus = -1;
            try {
                exitStatus = appMain.runProgram(inputFile.getAbsolutePath());
                if (exitStatus != 0) {
                    throw new RuntimeException("Exit status " + exitStatus + ".");
                }
            } catch (Exception e) {
                JTextArea msg = new JTextArea(e.getMessage());
                msg.setColumns(65);
                msg.setRows(7);
                msg.setLineWrap(true);
                msg.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(msg);
                JOptionPane.showMessageDialog(this, scrollPane, "Error Running Program", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exitProgram() {
        closeRNA();
        System.exit(0);
    }

    /**
     * @param item the menu item to set a keyboard short-cut for
     * @param keyCode e.g. "KeyEvent.VK_C" for the letter "C"
     */
    private void setKey(JMenuItem item, int keyCode) {
        item.setAccelerator(KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * Same as setKey() but Shift key is also added.
     * @param item the menu item to set a keyboard short-cut for
     * @param keyCode e.g. "KeyEvent.VK_C" for the letter "C"
     */
    private void setShiftedKey(JMenuItem item, int keyCode) {
        item.setAccelerator(KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
    }

    private javax.swing.JInternalFrame mainWindowFrame;
    private javax.swing.JMenu windowMenu;
    private javax.swing.JLabel helixNumField;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem contentMenuItem;
    private javax.swing.JButton undoConstraintBtn;
    private javax.swing.JButton scrollUpLeftBtn;
    private javax.swing.JButton scrollDownRightBtn;
    private javax.swing.JMenuItem diagonalConstraintItem;
    private javax.swing.JMenuItem helixConstraintItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JLabel helixTotalField;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JScrollPane displayScrollPane;
    private javax.swing.JMenu editMenu;
    private javax.swing.JTextArea helixInfoTextPane;
    private javax.swing.JMenuItem aa_agConstraintItem;
    private javax.swing.JMenuItem energyConstraintItem;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JLabel helixActualField;
    private javax.swing.JMenuItem basepairConstraintItem;
    private javax.swing.JDesktopPane desktopPane;
    public HelpContentJFrame helpFrame;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuItem eLoopConstraintItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JLabel uidValue;
    private javax.swing.JLabel organismValue;
    private javax.swing.JLabel accNumValue;
    private javax.swing.JLabel lengthValue;
    private javax.swing.JMenuItem openRNAMenuItem;
    private javax.swing.JMenuItem openTagsMenuItem;
    private javax.swing.JMenuItem openDataMenuItem;
    private javax.swing.JMenuItem runScriptMenuItem;
    private javax.swing.JMenuItem runProgramMenuItem;
    private javax.swing.JMenuItem goBackMenuItem;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeRNAMenuItem;
    private javax.swing.JMenuItem closeWindowMenuItem;
    private javax.swing.JMenuItem minimizeWindowMenuItem;
    private javax.swing.JMenuItem selectNextWindowMenuItem;
    private javax.swing.JMenuItem selectPreviousWindowMenuItem;
    private javax.swing.JMenuItem zoomWindowMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu filterMenu;
    private javax.swing.JMenuItem complexConstraintItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JCheckBoxMenuItem viewType2DMenuItem;
    private javax.swing.JCheckBoxMenuItem viewTypeFlatMenuItem;
    private javax.swing.JMenuItem zoomOutMenuItem;
    private javax.swing.JMenuItem zoomInMenuItem;
    private javax.swing.JMenuItem zoomFitMenuItem;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    private javax.swing.JButton zoomFitButton;
    private javax.swing.JTextArea historyTextPane;
    private JPanel customOpenPane;
    private JCheckBox openOverlayCheckBox;
    private JPanel openOverlayColorPanel;
    private JButton openOverlayColorButton;
    private JToolBar topToolBar;
    private JToolBar bottomToolBar;
    private JToolBar leftToolBar;
    private JToolBar rightToolBar;
    private JPanel displayControlPanel;
    private JSlider zoomSlider;
    private JLabel zoomLabel;
    private JTextField jumpFieldX;
    private JTextField jumpFieldY;
    private JButton jumpButton;
    private RNADisplay displayPane;
    private AboutFrame aboutFrame;
    private MiniFrame miniFrame;

}
