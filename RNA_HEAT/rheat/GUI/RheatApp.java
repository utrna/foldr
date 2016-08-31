package rheat.GUI;

import rheat.base.*;
import rheat.filter.*;
import rheat.script.ConstraintInterpreter;
import static rheat.script.JSUtil.*;
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
        private boolean accepted = false;

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
        public boolean run(RheatApp parent) {
            parent.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    // no action (cannot "accept" this way)
                }
            });
            JInternalFrame internalFrame = parent.createDialogInternalFrame();
            internalFrame.setResizable(isResizable());
            int result = JOptionPane.showInternalOptionDialog
                         (internalFrame, this, title,
                          JOptionPane.DEFAULT_OPTION,
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
                this.accepted = true;
                actionPanelAccepted();
            } else {
                this.accepted = false;
            }
            return wasAccepted();
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

        /**
         * Call run() first, then check this value to see if the
         * user accepted the dialog (instead of closing or using
         * Cancel on the dialog).
         */
        final boolean wasAccepted() {
            return this.accepted;
        }
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
     * Used to implement menu items that run particular scripts.
     */
    static public class RunSpecificScriptAction
    extends AbstractAction {

        RunSpecificScriptAction(RheatApp app, File scriptFile, int indentLevel) {
            // for convenience; in the most basic case, make the menu item
            // match the file name exactly (if this is not desirable, use
            // the alternate version that customizes the title completely)
            this(app, scriptFile,
                 RheatApp.createIndentString(indentLevel) + scriptFile.getName());
        }

        RunSpecificScriptAction(RheatApp app, File scriptFile, String title) {
            super(title);
            this.app = app;
            this.scriptFile = scriptFile;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            try {
                app.runScript(this.scriptFile);
            } finally {
            }
        }

        private RheatApp app;
        private File scriptFile;

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
        // initialize checkboxes
        this.showGridCheckBox.setSelected(this.helixImgGen.isVisible(HelixImageGenerator.OptionalElement.GRID));
        this.showBinsCheckBox.setSelected(this.helixImgGen.isVisible(HelixImageGenerator.OptionalElement.HELIX_COLOR_SPECTRUM));
        this.showTagsCheckBox.setSelected(this.helixImgGen.isVisible(HelixImageGenerator.OptionalElement.HELIX_ANNOTATIONS));
        this.showUnconstrainedCheckBox.setSelected(this.helixImgGen.isVisible(HelixImageGenerator.OptionalElement.HELIX_NO_ANNOTATIONS));
        this.setBounds(0, 0 , 700, 700);
    }

    /**
     * Returns the low-level object given at construction time.
     * WARNING: Do not call this directly if possible.  It is
     * usually only correct to call an API that exists directly
     * in the RheatApp class (as the version from this class may
     * perform additional tasks, for important side effects).
     * When you call this method, consider if it wouldn’t be
     * cleaner to add an appropriate API to RheatApp instead.
     */
    public AppMain getAppMain() {
        return this.appMain;
    }

    /**
     * Presents an error message to the user in a large box that
     * can scroll.  Suitable for errors that might be very long,
     * such as scripting exceptions.
     * @param text the main text to display
     * @param title the title of the message dialog
     * @param optionPaneType JOptionPane.ERROR_MESSAGE or similar
     */
    public void showMessage(String text, String title,
                            int optionPaneType) {
        JInternalFrame internalFrame = createDialogInternalFrame();
        internalFrame.setResizable(true);
        Component content = createLongMessageComponent(text);
        JOptionPane.showInternalMessageDialog(internalFrame, content, title,
                                              JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Convenience version; see showMessage().
     */
    public boolean confirm(String text) {
        JInternalFrame internalFrame = createDialogInternalFrame();
        internalFrame.setResizable(true);
        Component content = createLongMessageComponent(text);
        int result = JOptionPane.showInternalOptionDialog
                     (internalFrame, content, "Confirm",
                      JOptionPane.DEFAULT_OPTION,
                      JOptionPane.QUESTION_MESSAGE,
                      null/* icon */,
                      new Object[] {
                          "OK",
                          "Cancel"
                      },
                      "OK");
        // NOTE: can return JOptionPane.CLOSE_OPTION (-1) or
        // OK_OPTION or CANCEL_OPTION
        if (result == JOptionPane.OK_OPTION) {
            return true;
        }
        return false;
    }

    /**
     * Convenience version; see showMessage().
     */
    public void showError(String text, String title) {
        showMessage(text, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Convenience version; see showMessage().
     */
    public void showScriptError(ScriptException e) {
        StringBuilder totalMsg = new StringBuilder();
        // NOTE: see AppMain.evaluateScriptCode() for this string
        if (!e.getFileName().equals("(inline script)")) {
            totalMsg.append("File: ");
            totalMsg.append(e.getFileName());
            totalMsg.append("\n");
        }
        if (e.getLineNumber() != 1) {
            totalMsg.append("Line: ");
            totalMsg.append("" + e.getLineNumber());
            totalMsg.append("\n");
        }
        String sanitizedMessage = e.getMessage();
        // try to remove stupid stuff from the JavaScript runtime that just
        // makes error messages longer without adding any useful information
        final String garbage[] = new String[] {
            "sun\\.org\\.mozilla\\.javascript\\.internal\\.EcmaError: ",
            "sun\\.org\\.mozilla\\.javascript\\.internal\\.EvaluatorException: ",
            "sun\\.org\\.mozilla\\.javascript\\.internal\\.WrappedException: ",
            "Wrapped javax\\.script\\.ScriptException: "
        };
        for (String regex : garbage) {
            sanitizedMessage = sanitizedMessage.replaceFirst(regex, "");
        }
        totalMsg.append(sanitizedMessage);
        showMessage(totalMsg.toString(), "Error Running Script",
                    JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Utility for creating the specified number of spaces.
     */
    static public String createIndentString(int indentLevel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentLevel; ++i) {
            sb.append(" ");
        }
        return sb.toString();
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
                    int binNumber = selectedHelix.getBinNumber();
                    if (this.appMain.rnaData != null) {
                        if (binNumber != Helix.NO_BIN) {
                            sb.append("Bin #: " + (binNumber + 1) + " of " + this.appMain.rnaData.getBinCount() + "\n");
                        } else {
                            sb.append("Bin #: None\n");
                        }
                    }
                    Map<String, String> helixTags = selectedHelix.getTags();
                    if (helixTags != null) {
                        for (String tag : helixTags.keySet()) {
                            sb.append("Tag: ");
                            sb.append(tag);
                            String value = helixTags.get(tag);
                            if (value != null) { 
                                sb.append("=");
                                sb.append(value);
                            }
                            sb.append("\n");
                        }
                    }
                }
                textArea.setText(sb.toString());
            }
        } else if (event.getPropertyName().equals(mainWindowFrame.IS_MAXIMUM_PROPERTY)) {
            // always size back to fit when the display is maximized or restored
            zoomToFit();
        }
    }

    /**
     * Requests that helices with the given tag be rendered using the
     * given color.  Since a helix can have more than one tag, and
     * tags may be hidden by the user, the exact rendering varies.
     * If the specified tag is in the hidden set or a higher-priority
     * tag is also on a helix, the given color may not be used even
     * when a helix has the tag.
     */
    public void setHelixTagColor(String tag, Color color) {
        this.helixImgGen.addColorForHelicesWithTag(tag, color);
        this.updateImage();
    }

    /**
     * Requests that helices with the given tag be rendered using the
     * given line thickness.  As with setHelixTagColor(), the exact
     * rendering depends on priority and visibility of tags.
     */
    public void setHelixTagLineWidth(String tag, Float width) {
        this.helixImgGen.addLineWidthForHelicesWithTag(tag, width);
        this.updateImage();
    }

    /**
     * Shows or hides the specified annotations.  Since a helix may
     * have multiple annotations and the list of tags is prioritized,
     * enabling or disabling a particular tag is not guaranteed to
     * change the appearance of a helix.  If all of the tags for a
     * helix are marked as hidden, its appearance may revert to use
     * a normal color (as if it had no tags at all).
     *
     * @param isVisible true only if helices with this tag should show annotations
     * @param tags if empty, ALL tags are changed; otherwise, only specified tags
     */
    public void setHelixTagsVisible(boolean isVisible, String... tags) {
        if (tags.length == 0) {
            this.helixImgGen.setAllTagsVisibility(isVisible);
        } else {
            for (String tag : tags) {
                this.helixImgGen.setTagVisibility(tag, isVisible);
            }
        }
        this.updateImage();
        // NOTE: must use paintImmediately() and not repaint() because
        // otherwise the runtime might combine multiple calls (e.g.
        // animation in a script may be squashed)
        displayPane.paintImmediately(displayPane.getBounds());
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
     * Notify that spectrum colors have changed externally.
     */
    public void updateSpectrumColors() {
        // NOTE: similar action taken in constructor (without the update)
        displayPane.setSpectrumColors(appMain.getPrefSpectrumStartColor(),
                                      appMain.getPrefSpectrum50PercentColor(),
                                      appMain.getPrefSpectrumEndColor());
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

    public double getZoomLevel() {
        return (zoomSlider.getValue() / 1000f);
    }

    private void zoomIn() {
        double currentZoom = getZoomLevel();
        runConsoleFunction("rheat.zoomTo", "" + (getZoomLevel() + 0.25f)); // should match zoom-out amount below
    }

    private void zoomOut() {
        double currentZoom = getZoomLevel();
        runConsoleFunction("rheat.zoomTo", "" + (getZoomLevel() - 0.25f)); // should match zoom-out amount below
    }

    /**
     * Zooms the display so that every part of the RNA is visible
     * and as large as possible.
     */
    public void zoomToFit() {
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
    public void scrollTo(int xy) {
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
    public void scrollTo(int x, int y) {
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
            addOrReuseComponent(imageFrame, javax.swing.JLayeredPane.PALETTE_LAYER);
            bringToFront(imageFrame);
        } else {
            TextFileFrame textFrame = new TextFileFrame();
            textFrame.openFile(filePath);
            addOrReuseComponent(textFrame, javax.swing.JLayeredPane.PALETTE_LAYER);
            bringToFront(textFrame);
        }
    }

    private void updateImage() {
        displayPane.setRNA(appMain.rnaData); // resets overlays
        for (int i = 0; i < appMain.overlayData.size(); ++i) {
            displayPane.addOverlayRNA(appMain.overlayData.get(i), appMain.overlayColors.get(i));
        }
        displayPane.setHelixImageGenerator(this.helixImgGen);
        updateSpectrumColors(); // read initial preferences, now that helix image generator exists
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
     * Creates or returns a no-longer-used internal frame for
     * use as a dialog.  The frame will already be added to the
     * modal layer, and positioned at a useful default location.
     */
    private JInternalFrame createDialogInternalFrame() {
        JInternalFrame internalFrame = new JInternalFrame();
        addOrReuseComponent(internalFrame, javax.swing.JLayeredPane.MODAL_LAYER);
        Dimension desktopSize = desktopPane.getSize();
        Dimension frameSize = internalFrame.getSize();
        internalFrame.setLocation((desktopSize.width - frameSize.width) / 2,
                                  (desktopSize.height- frameSize.height) / 2);
        return internalFrame;
    }

    /**
     * Creates a component suitable for displaying a large message
     * in an alert panel.
     */
    private Component createLongMessageComponent(String text) {
        JTextArea msg = new JTextArea(text);
        msg.setBackground(getBackground());
        msg.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        msg.setEditable(false);
        final int columns = 65;
        int rows = ((text.length() / columns) + 1);
        if (rows > 7/* arbitrary; do not make too long */) {
            rows = 7;
        }
        msg.setColumns((rows > 2) ? 65 : 30); // arbitrary
        msg.setRows(rows);
        msg.setLineWrap(true);
        msg.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(msg);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return scrollPane;
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

    private void setVisible(HelixImageGenerator.OptionalElement element, boolean isVisible) {
        displayPane.setVisible(element, isVisible);
        displayPane.repaint();
    }

    private void toggleVisible(HelixImageGenerator.OptionalElement element) {
        setVisible(element, (!displayPane.isVisible(element)));
    }

    /**
     * Removes any script-running items and adds new ones to
     * reflect the current recent-list.  This lets the user
     * run a script by selecting a menu item.
     */
    private void rebuildRecentScriptsMenu() {
        ArrayList<JMenuItem> itemsToRemove = new ArrayList<JMenuItem>();
        JMenu targetMenu = this.fileMenu;
        if (targetMenu == null) {
            return;
        }
        int insertionPointIndex = -1; // put new items below this point
        for (int i = 0; i < targetMenu.getItemCount(); ++i) {
            JMenuItem item = targetMenu.getItem(i);
            if (item == null) {
                // e.g. separators do not have items
                continue;
            } else if (item == this.runScriptMenuItem) {
                insertionPointIndex = (i + 1);
                continue;
            }
            Action action = item.getAction();
            if ((action != null) && (action instanceof RunSpecificScriptAction)) {
                itemsToRemove.add(item);
            }
        }
        for (JMenuItem toDelete : itemsToRemove) {
            targetMenu.remove(toDelete);
        }
        for (File recentScriptFile : this.recentScriptFiles) {
            if (recentScriptFile.exists()) {
                StringBuilder sb = new StringBuilder();
                sb.append(createIndentString(4));
                sb.append("Rerun “");
                // FIXME: it is possible that two files in completely different
                // directories will have the same name, adding confusion; for
                // now, use only the name for brevity
                sb.append(recentScriptFile.getName());
                sb.append("”");
                targetMenu.insert(new RunSpecificScriptAction(this, recentScriptFile,
                                                              sb.toString()),
                                  insertionPointIndex);
            }
        }
    }

    /**
     * Removes any window-selecting items and adds new ones to
     * reflect currently-opened windows.  This lets the user
     * bring any window to the front by selecting a menu item.
     */
    private void rebuildWindowMenu() {
        ArrayList<JMenuItem> itemsToRemove = new ArrayList<JMenuItem>();
        JMenu targetMenu = this.windowMenu;
        if (targetMenu == null) {
            return;
        }
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
        for (JInternalFrame frame : desktopPane.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER)) {
            if (!frame.getTitle().equals("")) {
                targetMenu.add(new FrameSelectAction(desktopPane, frame));
            }
        }
        for (JInternalFrame frame : desktopPane.getAllFramesInLayer(JLayeredPane.PALETTE_LAYER)) {
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

        helpFrame = new HelpFrame();
        aboutFrame = new AboutFrame();
        commandFrame = new ScriptEntryFrame(this);
        miniFrame = new MiniFrame();
        mainWindowFrame = new javax.swing.JInternalFrame();

        leftToolBar = new Box(BoxLayout.Y_AXIS);

        rightToolBar = new Box(BoxLayout.Y_AXIS);

        topToolBar = new Box(BoxLayout.X_AXIS);

        bottomToolBar = new Box(BoxLayout.X_AXIS);

        JPanel paneDisplay = new JPanel();
        paneDisplay.setLayout(new BorderLayout());
        paneDisplay.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
                zoomToFit();
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
                if (jumpFieldY.getText().isEmpty()) {
                    runConsoleFunction("rheat.scrollTo", jumpFieldX.getText(), jumpFieldX.getText());
                } else if (jumpFieldX.getText().isEmpty()) {
                    runConsoleFunction("rheat.scrollTo", jumpFieldY.getText(), jumpFieldY.getText());
                } else {
                    runConsoleFunction("rheat.scrollTo", jumpFieldX.getText(), jumpFieldY.getText());
                }
            }
        });
        jumpFieldX = new FocusingField.SingleLine();
        jumpFieldX.setColumns(4);
        jumpFieldX.setToolTipText("Enter a number from 1 to the number of base-pairs to jump to that X position.");
        jumpFieldX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpButton.doClick();
            }
        });
        jumpFieldY = new FocusingField.SingleLine();
        jumpFieldY.setColumns(4);
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
        displayScrollPane.getHorizontalScrollBar().setBlockIncrement(150); // arbitrary
        displayScrollPane.getHorizontalScrollBar().setUnitIncrement(50); // arbitrary
        displayScrollPane.getVerticalScrollBar().setBlockIncrement(150); // arbitrary
        displayScrollPane.getVerticalScrollBar().setUnitIncrement(50); // arbitrary
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
        paneDisplay.add(displayControlPanel, BorderLayout.NORTH);
        paneDisplay.add(displayScrollPane, BorderLayout.CENTER);
        JPanel padPaneDisplay = new JPanel();
        padPaneDisplay.setLayout(new BorderLayout());
        //padPaneDisplay.setBorder(BorderFactory.createTitledBorder("Main Display"));
        padPaneDisplay.setMinimumSize(new Dimension(200, 500)); // arbitrary
        padPaneDisplay.add(paneDisplay, BorderLayout.CENTER);

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
        scriptWindowMenuItem = new javax.swing.JMenuItem();
        runProgramMenuItem = new javax.swing.JMenuItem();
        closeRNAMenuItem = new javax.swing.JMenuItem();
        closeWindowMenuItem = new javax.swing.JMenuItem();
        minimizeWindowMenuItem = new javax.swing.JMenuItem();
        zoomWindowMenuItem = new javax.swing.JMenuItem();
        commandWindowMenuItem = new javax.swing.JMenuItem();
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
        //JSplitPane joinDisplayRightToolBar = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, LIVE_REPAINT, padPaneDisplay, rightToolBar);
        JPanel joinDisplayRightToolBar = new JPanel();
        joinDisplayRightToolBar.setLayout(new BorderLayout());
        joinDisplayRightToolBar.add(padPaneDisplay, BorderLayout.CENTER);
        joinDisplayRightToolBar.add(rightToolBar, BorderLayout.EAST);
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
        paneRNAInfoLabels.add(new JLabel(" Current Helices:   "));
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
        scrollConstraintHistory.setMinimumSize(new Dimension(200, 100));
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
        scrollSelectedHelixInfo.setMinimumSize(new Dimension(200, 100));
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

        JPanel paneShowHide = new JPanel();
        paneShowHide.setLayout(new GridLayout(20, 1)); // FIXME: adjust if there are more controls than this number (otherwise they may wrap)
        paneShowHide.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        showGridCheckBox = new JCheckBox("Grid");
        showGridCheckBox.setToolTipText("When checked, numbered grid lines will be drawn in the background.");
        showGridCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleVisible(HelixImageGenerator.OptionalElement.GRID);
            }
        });
        paneShowHide.add(showGridCheckBox);
        showBinsCheckBox = new JCheckBox("Spectrum");
        showBinsCheckBox.setToolTipText("When checked, helices that have been binned (according to energy, by default) will use spectrum colors from Preferences.");
        showBinsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleVisible(HelixImageGenerator.OptionalElement.HELIX_COLOR_SPECTRUM);
            }
        });
        paneShowHide.add(showBinsCheckBox);
        showTagsCheckBox = new JCheckBox("Annotations");
        showTagsCheckBox.setToolTipText("When checked, annotated helices will use assigned colors/lines instead of the default appearance.");
        showTagsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleVisible(HelixImageGenerator.OptionalElement.HELIX_ANNOTATIONS);
            }
        });
        paneShowHide.add(showTagsCheckBox);
        showUnconstrainedCheckBox = new JCheckBox("Untagged Helices");
        showUnconstrainedCheckBox.setToolTipText("When checked, helices that have no annotations at all will still be displayed.");
        showUnconstrainedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleVisible(HelixImageGenerator.OptionalElement.HELIX_NO_ANNOTATIONS);
            }
        });
        paneShowHide.add(showUnconstrainedCheckBox);
        JPanel padPaneShowHide = new JPanel();
        padPaneShowHide.setLayout(new BorderLayout());
        padPaneShowHide.setBorder(BorderFactory.createTitledBorder("Show"));
        padPaneShowHide.setMinimumSize(new Dimension(200, 500)); // arbitrary
        padPaneShowHide.add(paneShowHide, BorderLayout.NORTH);
        padPaneShowHide.add(new JPanel(), BorderLayout.CENTER);

        rightToolBar.add(padPaneShowHide);

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
        openTagsMenuItem.setToolTipText("Updates the display using a source of helix annotations (such as a '.helixcolor' file).");
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
        runScriptMenuItem.setToolTipText("Runs commands from a JavaScript ('.js') file, such as a series of constraints.");
        runScriptMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runScriptMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(runScriptMenuItem);

        setKey(scriptWindowMenuItem, KeyEvent.VK_L);
        scriptWindowMenuItem.setText("Scripting Console");
        scriptWindowMenuItem.setToolTipText("Shows a window for running scripting commands interactively, and displaying history.");
        scriptWindowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayScriptingWindow();
            }
        });

        fileMenu.add(scriptWindowMenuItem);

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
                zoomToFit();
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
     * Passes scripting commands to AppMain.  See also
     * runConsoleScript().
     */
    public void evaluateScriptCode(String scriptingCommands) throws ScriptException {
        this.appMain.evaluateScriptCode(scriptingCommands);
    }

    /**
     * Convenience function for when the goal is to run a single
     * function.  See runConsoleScript() and jsFunction().
     */
    public void runConsoleFunction(String funcName, String... args) {
        runConsoleScript(jsFunction(funcName, args));
    }

    /**
     * Runs script commands, automatically presenting any errors
     * or adding successful commands to the console history.
     * When generating code from data, it is strongly recommended
     * that JSUtil methods be used, such as jsEscape() for files.
     * See also evaluateScriptCode(), which runs scripts
     * without logging and raises exceptions.
     */
    public void runConsoleScript(String scriptingCommands) {
        this.commandFrame.runCommandLines(scriptingCommands);
    }

    /**
     * Adds a filter to the history list and applies its effects to
     * the currently-displayed RNA.
     */
    public void addConstraint(Filter filter) {
        try {
            // to guarantee the script command is correct, constraints are
            // applied by executing the equivalent script command instead of
            // by calling appMain.addConstraint() and updateImage() (see
            // "rheat/script/ScriptMain.java" implementations of each)
            String equivalentScriptCommand = ConstraintInterpreter.getScriptCommandFor(filter);
            runConsoleScript(equivalentScriptCommand);
            appMain.incrementUndo(); // success; next snapshot should use a new number
            // TODO: should the history-update portion be an option?
            addHistoryCommand(equivalentScriptCommand);
            if (filter instanceof EnergyMaxMinFilter) {
                // to reduce confusion, if an energy filter has been applied
                // then make sure that the “spectrum” is visible
                if (!showBinsCheckBox.isSelected()) {
                    showBinsCheckBox.doClick();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log(WARN, "Unable to produce script command for constraint history (see trace above).");
        }
    }

    private boolean constraintPreCheckError() {
        return constraintPreCheckError(false/* no helices OK */);
    }

    private boolean constraintPreCheckError(boolean noHelicesOK) {
        if (this.appMain.rnaData == null) {
            showError("Please open an RNA file first.", "No RNA Loaded");
            return true;
        }
        if ((!noHelicesOK) && this.appMain.rnaData.getHelices().isEmpty()) {
            showError("There are no helices defined; please apply a base-pair constraint first.", "No Base-Pairs Chosen");
            return true;
        }
        // no error
        return false;
    }

    private void showAA_AGEndsConstraintDialog() {
        if (constraintPreCheckError()) {
            return;
        }
        FilterDialog fd = new AAandAGFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showBasePairConstraintDialog() {
        if (constraintPreCheckError(true/* no helices OK */)) {
            return;
        }
        FilterDialog fd = new BasepairFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
            HelixStore helices = appMain.rnaData.getHelices();
            int count = ((helices != null) ? helices.getHelixCount() : 0);
            this.helixTotalField.setText("" + count);
        }
    }

    private void showComplexDistanceConstraintDialog() {
        if (constraintPreCheckError()) {
            return;
        }
        FilterDialog fd = new ComplexFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showDiagonalDistanceConstraintDialog() {
        if (constraintPreCheckError()) {
            return;
        }
        FilterDialog fd = new DiagonalFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showELoopConstraintDialog() {
        if (constraintPreCheckError()) {
            return;
        }
        FilterDialog fd = new ELoopFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showEnergyConstraintDialog() {
        if (constraintPreCheckError()) {
            return;
        }
        FilterDialog fd = new EnergyFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addConstraint(newFilter);
        }
    }

    private void showHelixLengthConstraintDialog() {
        if (constraintPreCheckError()) {
            return;
        }
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
            if (appMain.getHistoryCommands().isEmpty()) {
                // to reduce confusion, if all constraints have been removed
                // then make sure that “untagged helices” are made visible
                if (!showUnconstrainedCheckBox.isSelected()) {
                    showUnconstrainedCheckBox.doClick();
                }
            }
            updateImage();
            refreshHistoryTextPane();
            log(INFO, "Reverted to constraint #" + index + ".");
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
            } catch (java.io.IOException e) {
                e.printStackTrace();
                showError(e.getMessage(), "Save Error");
            } catch (java.lang.OutOfMemoryError e) {
                e.printStackTrace();
                showError("There is not enough memory to save the RNA image at this size.  Zoom out some more and try again.", "Memory Error");
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
        addOrReuseComponent(helpFrame, javax.swing.JLayeredPane.PALETTE_LAYER);
        bringToFront(helpFrame);
    }

    private void displayScriptingWindow() {
        addOrReuseComponent(commandFrame, javax.swing.JLayeredPane.PALETTE_LAYER);
        bringToFront(commandFrame);
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        addOrReuseComponent(aboutFrame, javax.swing.JLayeredPane.PALETTE_LAYER);
        bringToFront(aboutFrame);
    }

    /**
     * Changes the magnification level of the RNA display.
     * The value of 1.0 is actual size; anything less is zoomed out,
     * and anything larger is zoomed in.
     */
    public void setZoomLevel(double newLevel) {
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
     * Cleans up when closing the current session.
     */
    public void closeRNA() {
        runConsoleScript("rheat.closeRNA()");
    }

    /**
     * Convenience version of this method that resets everything.
     */
    public void refreshCurrentRNA() {
        this.updateImage();
        displayPane.repaint();
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
            int count = ((helices != null) ? helices.getHelixCount() : 0);
            this.helixActualField.setText("" + rna.getActual().getHelixCount());
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
        clearHistory();
        setControlLabels();
        this.updateImage(); // erases to blank if "appMain.rnaData" is null
        this.displayPane.repaint();
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
                    // uses the same base-pairs as the original
                    runConsoleFunction("rheat.openOverlayRNA",
                                       jsQuoteEscape(inputFile.getAbsolutePath()),
                                       jsQuoteColor(openOverlayColorPanel.getBackground())
                                       );
                } else {
                    runConsoleFunction("rheat.openRNA",
                                       jsQuoteEscape(inputFile.getAbsolutePath())
                                       );
                    zoomToFit();
                    // automatically request a base-pair set, since
                    // otherwise the default display is not very useful
                    showBasePairConstraintDialog();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError(e.getMessage(), "Error Opening File");
            }
        }
    }

    private void openTagsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        fc = new JFileChooser(appMain.getPrefHelixDataDir());
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Annotation Files", "helixcolor", "bpcolor"/* legacy name */, "txt"));
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == fc.APPROVE_OPTION) {
            for (File inputFile : fc.getSelectedFiles()) {
                try {
                    // openTags() will call refreshForNewRNA()
                    runConsoleFunction("rheat.openTags",
                                       jsQuoteEscape(inputFile.getAbsolutePath())
                                       );
                } catch (Exception e) {
                    e.printStackTrace();
                    showError(e.getMessage(), "Error Opening File");
                }
            }
        }
    }

    private void openDataMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        fc = new JFileChooser(appMain.getPrefHelixDataDir());
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(true);
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
                    showError(e.getMessage(), "Error Opening File");
                }
            }
        }
    }

    private void beginCursor(int cursorType) {
        final int finalType = cursorType;
        getGlassPane().setVisible(true);
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getGlassPane().setCursor(Cursor.getPredefinedCursor(finalType));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // call it anyway (may not happen until the long-running
            // operation completes though, hence this method)
            getGlassPane().setCursor(Cursor.getPredefinedCursor(finalType));
        }
    }

    private void endCursor() {
        getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        getGlassPane().setVisible(false);
    }

    /**
     * Runs the script in the specified file or displays an error.
     * The script is also added to the “recent” list.
     */
    private void runScript(File inputFile) {
        try {
            beginCursor(Cursor.WAIT_CURSOR);
            runConsoleFunction("rheat.runScript", jsQuoteEscape(inputFile.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
            showError(e.getMessage(), "Error Running Script");
        } finally {
            endCursor();
            // the list is extended even when there are errors (since the user
            // may fix the problem and want to try again)
            String canonPath = null;
            try {
                canonPath = inputFile.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // add each file only once (cannot use object equality test for this;
            // must compare something unique such as the canonical path)
            if ((canonPath != null) && (!this.recentScriptCanonPaths.contains(canonPath))) {
                this.recentScriptCanonPaths.add(canonPath);
                this.recentScriptFiles.add(inputFile);
                this.rebuildRecentScriptsMenu();
            }
        }
    }

    private void runScriptMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        File inputFile;
        fc = new JFileChooser(appMain.getPrefScriptDir());
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JavaScript Files", "js", "txt"));
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == fc.APPROVE_OPTION) {
            this.runScript(fc.getSelectedFile());
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
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                exitStatus = appMain.runProgram(inputFile.getAbsolutePath());
                if (exitStatus != 0) {
                    throw new RuntimeException("Exit status " + exitStatus + ".");
                }
                // in this case, “fake” the history entry (in the
                // future, it may make sense to bind the result to
                // a variable and call the JavaScript engine to
                // read the variable)
                this.commandFrame.addCommandToHistory("status = rheat.runProgram" + jsParams(jsQuoteEscape(inputFile.getAbsolutePath())));
            } catch (Exception e) {
                e.printStackTrace();
                showError(e.getMessage(), "Error Running Program");
            } finally {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
    public ScriptEntryFrame commandFrame;
    public HelpFrame helpFrame;
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
    private javax.swing.JMenuItem scriptWindowMenuItem;
    private javax.swing.JMenuItem runProgramMenuItem;
    private javax.swing.JMenuItem goBackMenuItem;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeRNAMenuItem;
    private javax.swing.JMenuItem closeWindowMenuItem;
    private javax.swing.JMenuItem minimizeWindowMenuItem;
    private javax.swing.JMenuItem selectNextWindowMenuItem;
    private javax.swing.JMenuItem selectPreviousWindowMenuItem;
    private javax.swing.JMenuItem zoomWindowMenuItem;
    private javax.swing.JMenuItem commandWindowMenuItem;
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
    private Box topToolBar;
    private Box bottomToolBar;
    private Box leftToolBar;
    private Box rightToolBar;
    private JPanel displayControlPanel;
    private JSlider zoomSlider;
    private JLabel zoomLabel;
    private FocusingField.SingleLine jumpFieldX;
    private FocusingField.SingleLine jumpFieldY;
    private JButton jumpButton;
    private JCheckBox showGridCheckBox;
    private JCheckBox showBinsCheckBox;
    private JCheckBox showTagsCheckBox;
    private JCheckBox showUnconstrainedCheckBox;
    private RNADisplay displayPane;
    private AboutFrame aboutFrame;
    private MiniFrame miniFrame;
    private ArrayList<File> recentScriptFiles = new ArrayList<File>();
    private ArrayList<String> recentScriptCanonPaths = new ArrayList<String>(); // same size

}
