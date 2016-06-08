package rheat.GUI;

import rheat.base.*;
import rheat.filter.*;
import rheat.script.ScriptFilterInterpreter;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.Math;
import java.util.*;
import javax.imageio.ImageIO;
import javax.script.*;
import javax.swing.*;

/**
 * Main window of RNA HEAT application.
 *
 * @author  jyzhang
 */
public class RheatApp extends javax.swing.JFrame {

    static public abstract class RheatActionPanel extends javax.swing.JComponent {

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
            int result = JOptionPane.showOptionDialog(parent, this, title,
                                                      JOptionPane.OK_CANCEL_OPTION,
                                                      JOptionPane.PLAIN_MESSAGE, null/* icon */,
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
    };

    /**
     * For the log() method.
     */
    public static final int INFO = AppMain.INFO;
    public static final int WARN = AppMain.WARN;
    public static final int ERROR = AppMain.ERROR;

    // PRIVATE DATAMEMBERS RELEVANT TO RNAHEAT
    private AppMain appMain;
    private HelixImageGenerator helixImgGen;
    private JFileChooser fc = new JFileChooser();
    private BusyWaitDialog busyDialog;
    private BufferedImage img = null;
    private float zoomLevelAtLastUpdate = 1.0f;

    /**
     * Main window, using an AppMain object to manage
     * state that is not GUI-specific.
     */
    public RheatApp(AppMain appMain) {
        this.appMain = appMain;
        initComponents();
        this.setBounds(0, 0 , 700, 700);
        busyDialog = new BusyWaitDialog(this, false);
        Point origin = getCenteredOrigin(busyDialog);
        busyDialog.setLocation(origin);
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

    private void addHistoryCommand(String scriptCommandLines) {
        @SuppressWarnings({"unchecked"}) DefaultListModel<String> dlm =
                                         (DefaultListModel<String>)this.historyList.getModel();
        dlm.addElement(scriptCommandLines);
        this.appMain.addHistoryCommand(scriptCommandLines);
    }

    public void clearHistory() {
        this.historyList.setModel(new DefaultListModel<String>());
        this.appMain.clearHistoryCommands();
    }

    private float getZoomAt(int index) {
        float result = 1.0f;
        if (index >= 0) {
            String itemText = (String)this.zoomComboBox.getItemAt(index);
            result = Float.parseFloat(itemText);
        }
        return result;
    }

    private float getZoomLevel() {
        float result = 1.0f;
        int selectedIndex = this.zoomComboBox.getSelectedIndex();
        if (selectedIndex >= 0) {
            result = this.getZoomAt(selectedIndex);
        }
        return result;
    }

    private void zoomIn() {
        float currentZoom = getZoomLevel();
        int numLevels = this.zoomComboBox.getItemCount();
        for (int i = 0; i < numLevels; ++i) {
            if (getZoomAt(i) > currentZoom) {
                this.zoomComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void zoomOut() {
        float currentZoom = getZoomLevel();
        int numLevels = this.zoomComboBox.getItemCount();
        for (int i = numLevels - 1; i >= 0; --i) {
            if (getZoomAt(i) < currentZoom) {
                this.zoomComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Returns a fixed-width font, if one is available.
     * @param fallback a font to return if the monospaced lookup fails
     * @param size the desired point size of the font (like "12")
     * @return a fixed-width font of the given size, or the "fallback"
     */
    static private Font getMonospacedFont(Font fallback, int size) {
        Font result = fallback;
        Font fixedWidthFont = new Font("Courier", Font.PLAIN, size);
        if (fixedWidthFont != null) {
            result = fixedWidthFont;
        }
        return result;
    }

    private void updateImage() {
        if (this.helixImgGen != null) {
            busyDialog.setVisible(true);
            try {
                System.gc();
                img = helixImgGen.drawImage(appMain.rnaData);
                JViewport viewPort = this.DisplayScrollPane.getViewport();
                Dimension oldVisibleSize = viewPort.getExtentSize();
                double oldViewCenterX = (viewPort.getViewPosition().getX() + (oldVisibleSize.getWidth() / 2.0));
                double oldViewCenterY = (viewPort.getViewPosition().getY() + (oldVisibleSize.getHeight() / 2.0));
                Dimension oldMaxSize = viewPort.getView().getSize();
                // update the display (TEMPORARY; this ought to be
                // achieved by a custom component whose implementation
                // of paintComponent() redraws as needed)
                oldDisplayPane.setIcon(new ImageIcon(img));
                oldDisplayPane.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                oldDisplayPane.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
                oldDisplayPane.setSize(helixImgGen.getSize());
                oldDisplayPane.setBackground(Color.white);
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
                busyDialog.close();
                this.zoomLevelAtLastUpdate = helixImgGen.getZoomLevel();
            }
        }
    }
    
    private void setControlLabels(){
        if (appMain.rnaData != null){
            this.uidLabel.setText(appMain.rnaData.getUID());
            this.orgLabel.setText(appMain.rnaData.getOrganism());
            this.accNumLabel.setText(appMain.rnaData.getAccession());
            this.lengthLabel.setText(appMain.rnaData.getSize());
        }
        else {
            this.uidLabel.setText("");
            this.orgLabel.setText("");
            this.accNumLabel.setText("");
            this.lengthLabel.setText("");
        }
    }
    
    /**
     * A private method useful for resuing a closed JComponent.
     * Most useful for JInternalFrames that has been closed, but
     * may be reopened using a call to this method.
     */
    private void reuseComponent(javax.swing.JComponent f){
        f.setVisible(true);
        desktopPane.add(f, javax.swing.JLayeredPane.DEFAULT_LAYER);
        repaint();
    }
    
    /**
     * A private method for bringing a JInternalFrame to the front
     * of the JDesktopPane.  A call to this method will unminimize
     * the internal frame (if minimized) and bring it to the front
     * and select it.
     *
     */
    private void bringToFront(javax.swing.JInternalFrame f){
        try {
            if (f.isIcon()){ // if control window is minimized,
                f.setIcon(false); // unminimize it.
            }
            f.setSelected(true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        f.toFront();
    }
    
    private void enableFilterMenuItems(boolean b){
        if (b){
            this.helixFilterItem.setEnabled(true);
            this.diagonalFilterItem.setEnabled(true);
            this.aa_agFilterItem.setEnabled(true);
            this.eLoopFilterItem.setEnabled(true);
            this.energyFilterItem.setEnabled(true);
            this.complexFilterItem.setEnabled(true);
        }
        else {
            this.helixFilterItem.setEnabled(false);
            this.diagonalFilterItem.setEnabled(false);
            this.aa_agFilterItem.setEnabled(false);
            this.eLoopFilterItem.setEnabled(false);
            this.energyFilterItem.setEnabled(false);
            this.complexFilterItem.setEnabled(false);
        }
    }
    
    /**
     * A public method that returns the coordinates that will center
     * a newly opened JFrame against the application JFrame
     * (Using this does not guarantee window will be onscreen)
     */
    public Point getCenteredOrigin(java.awt.Window f){
        int frontWidth, frontHeight;    //Width and Height of newly opened
        int backHeight, backWidth;      //Width and height of current
        int backX, backY;               //Origin of current
        int frontX = 0, frontY = 0;             //Origin of new (to be determined)
        
        frontWidth = f.getWidth();
        frontHeight = f.getHeight();
        
        backWidth = this.getWidth();
        backHeight = this.getWidth();
        
        backX = this.getX();
        backY = this.getY();
        
        //Find difference in height between the two windows
        int diffHeight = Math.abs(backHeight/2 - frontHeight/2);
        //Find difference in width between the two windows
        int diffWidth = Math.abs(backWidth/2 - frontWidth/2);
        
        // Compare to see if the new JFrame is larger/smaller in height/width
        // and calculate new coordinates accordingly
        if (backWidth >= frontWidth){
            frontX = (backX + diffWidth);
        }
        else if (backWidth < frontWidth){
            frontX = Math.max(0, backX - diffWidth);
        }
        
        if (backHeight >= backHeight){
            frontY = backY + diffHeight;
        }
        else if (backWidth < backWidth){
            frontY = Math.max(0, backY - diffHeight);
        }
        return new Point(frontX, frontY);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        desktopPane = new javax.swing.JDesktopPane();
        DisplayFrame = new javax.swing.JInternalFrame();
        oldDisplayPane = new javax.swing.JLabel();
        DisplayScrollPane = new javax.swing.JScrollPane();
        DisplayScrollPane.setViewportView(oldDisplayPane);
        ControlFrame = new javax.swing.JInternalFrame();
        jPanel1 = new javax.swing.JPanel();
        uidLabel = new javax.swing.JLabel();
        orgLabel = new javax.swing.JLabel();
        accNumLabel = new javax.swing.JLabel();
        lengthLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        zoomLevelLabel = new javax.swing.JLabel();
        zoomComboBox = new javax.swing.JComboBox<String>();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        view2DBtn = new javax.swing.JButton();
        viewFlatBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        helixNumField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        helixTotalField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        helixActualField = new javax.swing.JTextField();
        HistoryFrame = new javax.swing.JInternalFrame();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        historyList = new javax.swing.JList<String>();
        jPanel5 = new javax.swing.JPanel();
        undoFilterBtn = new javax.swing.JButton();
        InfoFrame = new javax.swing.JInternalFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        infoTextPane = new javax.swing.JTextPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        runMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        preferencesMenuItem = new javax.swing.JMenuItem();
        filterMenu = new javax.swing.JMenu();
        basepairFilterItem = new javax.swing.JMenuItem();
        helixFilterItem = new javax.swing.JMenuItem();
        diagonalFilterItem = new javax.swing.JMenuItem();
        aa_agFilterItem = new javax.swing.JMenuItem();
        eLoopFilterItem = new javax.swing.JMenuItem();
        energyFilterItem = new javax.swing.JMenuItem();
        complexFilterItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewType2DMenuItem = new javax.swing.JCheckBoxMenuItem();
        viewTypeFlatMenuItem = new javax.swing.JCheckBoxMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        zoomInMenuItem = new javax.swing.JMenuItem();
        windowMenu = new javax.swing.JMenu();
        viewDisplayMenuItem = new javax.swing.JMenuItem();
        viewControlMenuItem = new javax.swing.JMenuItem();
        viewHistoryMenuItem = new javax.swing.JMenuItem();
        viewInfoMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setTitle("RNA HEAT");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        DisplayFrame.getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        DisplayFrame.setMaximizable(true);
        DisplayFrame.setResizable(true);
        DisplayFrame.setTitle("Display Window");
        DisplayFrame.setMinimumSize(new java.awt.Dimension(100, 100));
        DisplayFrame.setNormalBounds(new java.awt.Rectangle(270, 20, 400, 400));
        DisplayFrame.setVisible(true);
        DisplayScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                DisplayScrollPaneMouseClicked(evt);
            }
        });

        DisplayFrame.getContentPane().add(DisplayScrollPane);

        DisplayFrame.setBounds(270, 10, 500, 500);
        desktopPane.add(DisplayFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        ControlFrame.getContentPane().setLayout(new java.awt.GridLayout(3, 0));

        ControlFrame.setIconifiable(true);
        ControlFrame.setTitle("Controls");
        ControlFrame.setVisible(true);
        jPanel1.setLayout(new java.awt.GridLayout(5, 0));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        uidLabel.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.add(uidLabel);

        jPanel1.add(orgLabel);

        jPanel1.add(accNumLabel);

        jPanel1.add(lengthLabel);

        ControlFrame.getContentPane().add(jPanel1);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        zoomLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        zoomLevelLabel.setText("Zoom:");
        jPanel3.add(zoomLevelLabel);

        zoomComboBox.setEditable(true);
        zoomComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "0.01", "0.1", "0.5", "1", "1.5", "2", "5", "10", "15", "20" }));
        zoomComboBox.setSelectedIndex(3);
        zoomComboBox.setPreferredSize(new java.awt.Dimension(50, 26));
        zoomComboBox.setAutoscrolls(true);
        zoomComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zoomComboBoxItemStateChanged(evt);
            }
        });

        jPanel3.add(zoomComboBox);

        zoomOutButton.setText("-");
        zoomOutButton.setMnemonic(KeyEvent.VK_MINUS);
        zoomOutButton.setDisplayedMnemonicIndex(-1);
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOut();
            }
        });
        jPanel3.add(zoomOutButton);

        zoomInButton.setText("+");
        zoomInButton.setMnemonic(KeyEvent.VK_EQUALS);
        zoomInButton.setDisplayedMnemonicIndex(-1);
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomIn();
            }
        });
        jPanel3.add(zoomInButton);

        view2DBtn.setText("2D View");
        view2DBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setViewType2D();
            }
        });

        jPanel3.add(view2DBtn);

        viewFlatBtn.setText("Flat View");
        viewFlatBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setViewTypeFlat();
            }
        });

        jPanel3.add(viewFlatBtn);

        ControlFrame.getContentPane().add(jPanel3);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Current Helices");
        jLabel1.setPreferredSize(new java.awt.Dimension(120, 16));
        jPanel2.add(jLabel1);

        helixNumField.setColumns(7);
        helixNumField.setEnabled(false);
        jPanel2.add(helixNumField);

        jLabel3.setText("Total Helices");
        jLabel3.setPreferredSize(new java.awt.Dimension(120, 16));
        jPanel2.add(jLabel3);

        helixTotalField.setColumns(7);
        helixTotalField.setEnabled(false);
        jPanel2.add(helixTotalField);

        jLabel2.setText("Actual Helices");
        jLabel2.setPreferredSize(new java.awt.Dimension(120, 16));
        jPanel2.add(jLabel2);

        helixActualField.setColumns(7);
        helixActualField.setEnabled(false);
        jPanel2.add(helixActualField);

        ControlFrame.getContentPane().add(jPanel2);

        ControlFrame.setBounds(10, 10, 250, 330);
        desktopPane.add(ControlFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        BorderLayout historyLayout = new java.awt.BorderLayout();
        HistoryFrame.getContentPane().setLayout(historyLayout);
        HistoryFrame.setIconifiable(true);
        HistoryFrame.setResizable(true);
        HistoryFrame.setTitle("Filter History");
        HistoryFrame.setVisible(true);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(420, 100));
        jScrollPane2.setAutoscrolls(true);
        historyList.setModel(new DefaultListModel<String>());
        historyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        historyList.setPreferredSize(null);
        historyList.setFont(getMonospacedFont(historyList.getFont(), 12)); // monospaced font so pairs are easier to see
        jScrollPane2.setViewportView(historyList);

        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel4.add(jScrollPane2, BorderLayout.CENTER);

        HistoryFrame.getContentPane().add(jPanel4, BorderLayout.CENTER);

        undoFilterBtn.setText("Undo Last");
        undoFilterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    int index = appMain.getUndoIndex() - 1;
                    appMain.revertToPreviousRNA(index);
                    updateImage();
                    DefaultListModel<String> dlm = new DefaultListModel<String>();
                    for (int i = 0; i < index; i++) {
                        dlm.addElement(historyList.getModel().getElementAt(i));
                    }
                    historyList.setModel(dlm);
                    log(INFO, "Reverted to snapshot #" + index + ".");
                } catch (Exception e) {
                    e.printStackTrace();
                    log(WARN, "Unable to undo (see trace above).");
                }
            }
        });

        jPanel5.add(undoFilterBtn);

        HistoryFrame.getContentPane().add(jPanel5, BorderLayout.SOUTH);

        HistoryFrame.setBounds(270, 520, 500, 190);
        desktopPane.add(HistoryFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        InfoFrame.getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        InfoFrame.setIconifiable(true);
        InfoFrame.setTitle("Helix Info");
        InfoFrame.setVisible(true);
        infoTextPane.setEditable(false);
        infoTextPane.setFont(getMonospacedFont(infoTextPane.getFont(), 12)); // monospaced font so pairs are easier to see
        jScrollPane1.setViewportView(infoTextPane);

        InfoFrame.getContentPane().add(jScrollPane1);

        InfoFrame.setBounds(10, 350, 250, 360);
        desktopPane.add(InfoFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        getContentPane().add(desktopPane, java.awt.BorderLayout.CENTER);
        setExtendedState(MAXIMIZED_BOTH);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        openMenuItem.setMnemonic('o');
        setKey(openMenuItem, KeyEvent.VK_O);
        openMenuItem.setText("Open Helices File…");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        runMenuItem.setMnemonic('r');
        setKey(runMenuItem, KeyEvent.VK_R);
        runMenuItem.setText("Run Script…");
        runMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(runMenuItem);
        fileMenu.addSeparator();

        closeMenuItem.setMnemonic('C');
        setKey(closeMenuItem, KeyEvent.VK_W);
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(closeMenuItem);

        saveAsMenuItem.setMnemonic('A');
        setKey(saveAsMenuItem, KeyEvent.VK_S);
        saveAsMenuItem.setText("Save As…");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();

        exitMenuItem.setMnemonic('x');
        setKey(exitMenuItem, KeyEvent.VK_Q);
        exitMenuItem.setText((appMain.isMac()) ? "Quit" : "Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("Edit");

        //preferencesMenuItem.setMnemonic('p');
        setKey(preferencesMenuItem, KeyEvent.VK_SEMICOLON);
        preferencesMenuItem.setText("Preferences…");
        preferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(preferencesMenuItem);

        menuBar.add(editMenu);

        filterMenu.setMnemonic('t');
        filterMenu.setText("Filters");

        basepairFilterItem.setMnemonic('B');
        setKey(basepairFilterItem, KeyEvent.VK_1);
        basepairFilterItem.setText("Basepair Filter…");
        basepairFilterItem.setEnabled(false);
        basepairFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBasePairFilterDialog();
            }
        });

        filterMenu.add(basepairFilterItem);

        helixFilterItem.setMnemonic('H');
        setKey(helixFilterItem, KeyEvent.VK_2);
        helixFilterItem.setText("Helix Length Filter…");
        helixFilterItem.setEnabled(false);
        helixFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHelixLengthFilterDialog();
            }
        });

        filterMenu.add(helixFilterItem);

        diagonalFilterItem.setMnemonic('D');
        setKey(diagonalFilterItem, KeyEvent.VK_3);
        diagonalFilterItem.setText("Diagonal Filter…");
        diagonalFilterItem.setEnabled(false);
        diagonalFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showDiagonalFilterDialog();
            }
        });

        filterMenu.add(diagonalFilterItem);

        aa_agFilterItem.setMnemonic('A');
        setKey(aa_agFilterItem, KeyEvent.VK_4);
        aa_agFilterItem.setText("AA / AG Ends Filter…");
        aa_agFilterItem.setEnabled(false);
        aa_agFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAA_AGFilterDialog();
            }
        });

        filterMenu.add(aa_agFilterItem);

        eLoopFilterItem.setMnemonic('E');
        setKey(eLoopFilterItem, KeyEvent.VK_5);
        eLoopFilterItem.setText("E-Loop Filter…");
        eLoopFilterItem.setEnabled(false);
        eLoopFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showELoopFilterDialog();
            }
        });

        filterMenu.add(eLoopFilterItem);

        energyFilterItem.setMnemonic('g');
        setKey(energyFilterItem, KeyEvent.VK_6);
        energyFilterItem.setText("Energy Filter…");
        energyFilterItem.setEnabled(false);
        energyFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showEnergyFilterDialog();
            }
        });

        filterMenu.add(energyFilterItem);

        complexFilterItem.setMnemonic('C');
        setKey(complexFilterItem, KeyEvent.VK_6);
        complexFilterItem.setText("Complex Distance Filter…");
        complexFilterItem.setEnabled(false);
        complexFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showComplexFilterDialog();
            }
        });

        filterMenu.add(complexFilterItem);

        menuBar.add(filterMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");

        viewType2DMenuItem.setMnemonic('2');
        viewType2DMenuItem.setState(true); // initially...
        viewType2DMenuItem.setText("2D");
        viewType2DMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setViewType2D();
            }
        });

        viewMenu.add(viewType2DMenuItem);

        viewTypeFlatMenuItem.setMnemonic('F');
        viewTypeFlatMenuItem.setText("Flat");
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
        zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOut();
            }
        });

        viewMenu.add(zoomOutMenuItem);

        zoomInMenuItem.setMnemonic('I');
        setKey(zoomInMenuItem, KeyEvent.VK_PERIOD);
        zoomInMenuItem.setText("Zoom In");
        zoomInMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomIn();
            }
        });

        viewMenu.add(zoomInMenuItem);

        menuBar.add(viewMenu);

        windowMenu.setMnemonic('W');
        windowMenu.setText("Window");

        viewDisplayMenuItem.setMnemonic('D');
        viewDisplayMenuItem.setText("Display Window");
        viewDisplayMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDisplayMenuItemActionPerformed(evt);
            }
        });

        windowMenu.add(viewDisplayMenuItem);

        viewControlMenuItem.setMnemonic('C');
        viewControlMenuItem.setText("Controls");
        viewControlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewControlMenuItemActionPerformed(evt);
            }
        });

        windowMenu.add(viewControlMenuItem);

        viewHistoryMenuItem.setMnemonic('H');
        viewHistoryMenuItem.setText("Filter History");
        viewHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewHistoryMenuItemActionPerformed(evt);
            }
        });

        windowMenu.add(viewHistoryMenuItem);

        viewInfoMenuItem.setMnemonic('I');
        setKey(viewInfoMenuItem, KeyEvent.VK_I);
        viewInfoMenuItem.setText("Helix Info");
        viewInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewInfoMenuItemActionPerformed(evt);
            }
        });

        windowMenu.add(viewInfoMenuItem);

        menuBar.add(windowMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

        contentMenuItem.setText("Contents");
        setKey(contentMenuItem, KeyEvent.VK_SLASH);
        contentMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contentMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(contentMenuItem);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }//GEN-END:initComponents

    /**
     * Adds a filter to the history list and applies its effects to
     * the currently-displayed RNA.
     */
    public void addFilter(Filter filter) {
        try {
            appMain.snapshotRNAData();
            // to guarantee that the script command is correct, the filter is
            // applied by executing the equivalent script command instead of
            // by directly calling appMain.addFilter() and updateImage() (see
            // "rheat/script/ScriptMain.java" implementations of each filter)
            String equivalentScriptCommand = ScriptFilterInterpreter.getScriptCommandForFilter(filter);
            appMain.evaluateScriptCode(equivalentScriptCommand);
            appMain.incrementUndo(); // success; next snapshot should use a new number
            // TODO: should the history-update portion be an option?
            addHistoryCommand(equivalentScriptCommand);
        } catch (Exception e) {
            e.printStackTrace();
            log(WARN, "Unable to produce script command for filter history (see trace above).");
        }
    }

    private void showAA_AGFilterDialog() {
        FilterDialog fd = new AAandAGFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addFilter(newFilter);
        }
    }

    private void showBasePairFilterDialog() {
        FilterDialog fd = new BasepairFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addFilter(newFilter);
            this.enableFilterMenuItems(true);
            HelixStore helices = appMain.rnaData.getHelices();
            int count = ((helices != null) ? helices.getCount() : 0);
            this.helixTotalField.setText("" + count);
        }
    }

    private void showComplexFilterDialog() {
        FilterDialog fd = new ComplexFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addFilter(newFilter);
        }
    }

    private void showDiagonalFilterDialog() {
        FilterDialog fd = new DiagonalFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addFilter(newFilter);
        }
    }

    private void showELoopFilterDialog() {
        FilterDialog fd = new ELoopFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addFilter(newFilter);
        }
    }

    private void showEnergyFilterDialog() {
        FilterDialog fd = new EnergyFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addFilter(newFilter);
        }
    }

    private void showHelixLengthFilterDialog() {
        FilterDialog fd = new HelixFilterDialog();
        fd.run(this);
        Filter newFilter = fd.getNewFilter();
        if (newFilter != null) {
            addFilter(newFilter);
        }
    }

    private void preferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        new PreferenceDialog(appMain).run(this);
    }

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        File outputImage;
        fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Files", "png"));
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JPEG Files", "jpg"));
        if (fc.showSaveDialog(this) == fc.APPROVE_OPTION){
            outputImage = fc.getSelectedFile();
            try {
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
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
    
    private void viewInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewInfoMenuItemActionPerformed
        this.bringToFront(this.InfoFrame);
    }//GEN-LAST:event_viewInfoMenuItemActionPerformed
    
    private void DisplayScrollPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_DisplayScrollPaneMouseClicked
        java.awt.Point p = evt.getPoint();
        java.awt.Rectangle rect = this.DisplayScrollPane.getViewport().getViewRect();
        double x = p.getX() + rect.getX();
        double y = p.getY() + rect.getY();
        String s = "x: " + x + "; y: " + y + "\n";
        x = helixImgGen.getUnzoomedX(x);
        y = helixImgGen.getUnzoomedY(y);
        this.infoTextPane.setText(s);
        if (this.helixImgGen != null) {
            helixImgGen.clicked(x, y, this.infoTextPane);
            this.updateImage();
        }
    }//GEN-LAST:event_DisplayScrollPaneMouseClicked
    
    private void viewHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewHistoryMenuItemActionPerformed
        bringToFront(this.HistoryFrame);
    }//GEN-LAST:event_viewHistoryMenuItemActionPerformed
    
    private void contentMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentMenuItemActionPerformed
        HelpContentJFrame contents = new HelpContentJFrame();
        contents.setLocation(0,0);
        contents.setVisible(true);
    }//GEN-LAST:event_contentMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        HelpAboutJFrame about = new HelpAboutJFrame();
        Point origin = getCenteredOrigin(about);
        about.setLocation(origin);
        about.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    private void zoomComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zoomComboBoxItemStateChanged
        if (helixImgGen != null){
            helixImgGen.setZoomLevel(this.getZoomLevel());
            this.updateImage();
        }
    }//GEN-LAST:event_zoomComboBoxItemStateChanged
    
    private void setViewTypeFlat() {
        viewType2DMenuItem.setState(false);
        viewTypeFlatMenuItem.setState(true);
        if (helixImgGen != null && helixImgGen.setImageType(HelixImageGenerator.VIEW_FLAT)){
            this.updateImage();
        }
    }
    
    private void setViewType2D() {
        viewType2DMenuItem.setState(true);
        viewTypeFlatMenuItem.setState(false);
        if (helixImgGen != null && helixImgGen.setImageType(HelixImageGenerator.VIEW_2D)){
            this.updateImage();
        }
    }

    /**
     * Private method to do clean up when closing the current session.
     */
    private void closeRNA() {
        appMain.cleanUp();
        refreshForNewRNA();
        this.enableFilterMenuItems(false);
        this.basepairFilterItem.setEnabled(false);
        this.clearHistory();
    }

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        closeRNA();
    }//GEN-LAST:event_closeMenuItemActionPerformed

    /**
     * Updates the display to reflect current RNA data.
     * Used after opening helix files.
     */
    public void refreshForNewRNA() {
        RNA rna = appMain.rnaData;
        if (rna != null) {
            this.helixActualField.setText("" + rna.getActual().getCount());
            this.setTitle("RNA HEAT: " + rna.getUID());
            this.helixImgGen = new HelixImageGenerator(rna.getLength());
            //this.helixGraphicsLabel = new HelixGraphicsLabel(rna.getLength());
            HelixStore helices = rna.getHelices();
            int count = ((helices != null) ? helices.getCount() : 0);
            this.helixNumField.setText("" + count);
            this.helixTotalField.setText("");
        } else {
            this.helixActualField.setText("");
            this.setTitle("RNA HEAT");
            this.helixImgGen = new HelixImageGenerator(0);
            //this.helixGraphicsLabel = new HelixGraphicsLabel(0);
            this.helixNumField.setText("");
            this.helixTotalField.setText("");
        }
        this.infoTextPane.setText("");
        setControlLabels();
        basepairFilterItem.setEnabled(true);
        this.updateImage(); // erases to blank if "appMain.rnaData" is null
    }

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        File inputFile;
        fc = new JFileChooser(appMain.getPrefHelixDataDir());
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("BPSEQ Files", "bpseq", "txt"));
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == fc.APPROVE_OPTION){
            inputFile = fc.getSelectedFile();
            try {
                // openRNA() will call refreshForNewRNA()
                appMain.openRNA(inputFile.getAbsolutePath());
                // automatically request a base-pair filter, since
                // otherwise the default display is not very useful
                showBasePairFilterDialog();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error Opening File", JOptionPane.ERROR_MESSAGE);
            }
            updateImage();
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void runMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
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

    private void viewControlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewControlMenuItemActionPerformed
        bringToFront(this.ControlFrame);
    }//GEN-LAST:event_viewControlMenuItemActionPerformed

    private void viewDisplayMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDisplayMenuItemActionPerformed
        bringToFront(this.DisplayFrame);
    }//GEN-LAST:event_viewDisplayMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        closeRNA();
        System.exit(0);
    }//GEN-LAST:event_exitForm

    /**
     * @param item the menu item to set a keyboard short-cut for
     * @param keyCode e.g. "KeyEvent.VK_C" for the letter "C"
     */
    private void setKey(JMenuItem item, int keyCode) {
        item.setAccelerator(KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JInternalFrame DisplayFrame;
    private javax.swing.JMenu windowMenu;
    private javax.swing.JTextField helixNumField;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem viewControlMenuItem;
    private javax.swing.JMenuItem contentMenuItem;
    private javax.swing.JButton undoFilterBtn;
    private javax.swing.JMenuItem diagonalFilterItem;
    private javax.swing.JMenuItem helixFilterItem;
    private javax.swing.JMenuItem viewInfoMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem viewHistoryMenuItem;
    private javax.swing.JTextField helixTotalField;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JScrollPane DisplayScrollPane;
    private javax.swing.JInternalFrame ControlFrame;
    private javax.swing.JMenu editMenu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuItem viewDisplayMenuItem;
    private javax.swing.JTextPane infoTextPane;
    private javax.swing.JInternalFrame InfoFrame;
    private javax.swing.JMenuItem aa_agFilterItem;
    private javax.swing.JMenuItem energyFilterItem;
    private javax.swing.JButton viewFlatBtn;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JLabel zoomLevelLabel;
    private javax.swing.JTextField helixActualField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuItem basepairFilterItem;
    private javax.swing.JDesktopPane desktopPane;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuItem eLoopFilterItem;
    private javax.swing.JInternalFrame HistoryFrame;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel uidLabel;
    private javax.swing.JLabel accNumLabel;
    private javax.swing.JButton view2DBtn;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem runMenuItem;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel orgLabel;
    private javax.swing.JMenu filterMenu;
    private javax.swing.JMenuItem complexFilterItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JCheckBoxMenuItem viewType2DMenuItem;
    private javax.swing.JCheckBoxMenuItem viewTypeFlatMenuItem;
    private javax.swing.JMenuItem zoomOutMenuItem;
    private javax.swing.JMenuItem zoomInMenuItem;
    private javax.swing.JComboBox<String> zoomComboBox;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    private javax.swing.JList<String> historyList;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JLabel oldDisplayPane; // TEMPORARY (want to replace with custom-painted view)
}
