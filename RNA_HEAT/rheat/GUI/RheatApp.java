package rheat.GUI;

import rheat.base.*;
import rheat.filter.*;
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
    
    // PRIVATE DATAMEMBERS RELEVANT TO RNAHEAT
    private AppMain appMain;
    private HelixImageGenerator helixImgGen;
    private JFileChooser fc = new JFileChooser();
    private BusyWaitDialog busyDialog;
    private int undoMax = 20;
    private int currentUndo = 0;
    private Image img;

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

    private void addUndo(){
        String undofile = appMain.getPrefUndoDir() + File.separator + "undo" + currentUndo;
        try {
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(undofile));
            ois.writeObject(this.appMain.rnaData);
        }
        catch (java.io.IOException ex){
            String s = "Cannot save undo information.  Please check undo directory is correct.";
            // do not display a dialog here; if the write fails then the
            // user sees a message EVERY time a filter action occurs!
            //JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(s);
        }
    }
    
    private void incrementUndo(){
        currentUndo = (currentUndo + 1) % undoMax;
    }
    
    private void addHistory(FilterInfo fi){
        @SuppressWarnings({"unchecked"}) DefaultListModel<FilterInfo> dlm =
                                         (DefaultListModel<FilterInfo>)this.historyList.getModel();
        dlm.addElement(fi);
    }
    
    private void clearHistory(){
        this.historyList.setModel(new DefaultListModel<FilterInfo>());
    }
    
    private void updateImage(){
        if (appMain.rnaData != null /*&& this.helixImgGen != null*/){
            busyDialog.setVisible(true);
            try {
                System.gc();
                float zoom = Float.parseFloat((String)this.zoomComboBox.getSelectedItem());
                helixImgGen.setZoomLevel(zoom);
                img = helixImgGen.drawImage(appMain.rnaData);
                Point p = this.DisplayScrollPane.getViewport().getViewPosition();
                img = helixImgGen.zoomImage(img);
                busyDialog.close();
                JLabel gpanel = new JLabel(new ImageIcon(img));
                gpanel.setBackground(Color.white);
                this.DisplayScrollPane.setViewportView(gpanel);
                this.DisplayScrollPane.getViewport().setViewPosition(p);
                if (appMain.rnaData.getHelices() != null){
                    this.helixNumField.setText("" + appMain.rnaData.getHelices().getCount());
                }
            }
            catch (NumberFormatException ex){
                
            }
            finally{
                busyDialog.close();
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
        DisplayScrollPane = new javax.swing.JScrollPane();
        ControlFrame = new javax.swing.JInternalFrame();
        jPanel1 = new javax.swing.JPanel();
        uidLabel = new javax.swing.JLabel();
        orgLabel = new javax.swing.JLabel();
        accNumLabel = new javax.swing.JLabel();
        lengthLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        zoomLevelLabel = new javax.swing.JLabel();
        zoomComboBox = new javax.swing.JComboBox<String>();
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
        historyList = new javax.swing.JList<FilterInfo>();
        jPanel5 = new javax.swing.JPanel();
        undoFilterBtn = new javax.swing.JButton();
        infoFilterBtn = new javax.swing.JButton();
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
        DisplayFrame.setNormalBounds(new java.awt.Rectangle(230, 20, 400, 400));
        DisplayFrame.setVisible(true);
        DisplayScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                DisplayScrollPaneMouseClicked(evt);
            }
        });

        DisplayFrame.getContentPane().add(DisplayScrollPane);

        DisplayFrame.setBounds(230, 10, 410, 440);
        desktopPane.add(DisplayFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        ControlFrame.getContentPane().setLayout(new java.awt.GridLayout(3, 0));

        ControlFrame.setIconifiable(true);
        ControlFrame.setTitle("Control Window");
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
        zoomLevelLabel.setText("Zoom Level");
        jPanel3.add(zoomLevelLabel);

        zoomComboBox.setEditable(true);
        zoomComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "0.01", "0.1", "0.5", "1", "1.5", "2", "5", "10", "15", "20" }));
        zoomComboBox.setSelectedIndex(3);
        zoomComboBox.setPreferredSize(new java.awt.Dimension(100, 26));
        zoomComboBox.setAutoscrolls(true);
        zoomComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zoomComboBoxItemStateChanged(evt);
            }
        });

        jPanel3.add(zoomComboBox);

        view2DBtn.setText("2D View");
        view2DBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view2DBtnActionPerformed(evt);
            }
        });

        jPanel3.add(view2DBtn);

        viewFlatBtn.setText("Flat View");
        viewFlatBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewFlatBtnActionPerformed(evt);
            }
        });

        jPanel3.add(viewFlatBtn);

        ControlFrame.getContentPane().add(jPanel3);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Current Helices");
        jLabel1.setPreferredSize(new java.awt.Dimension(90, 16));
        jPanel2.add(jLabel1);

        helixNumField.setColumns(7);
        helixNumField.setEnabled(false);
        jPanel2.add(helixNumField);

        jLabel3.setText("Total Helices");
        jLabel3.setPreferredSize(new java.awt.Dimension(90, 16));
        jPanel2.add(jLabel3);

        helixTotalField.setColumns(7);
        helixTotalField.setEnabled(false);
        jPanel2.add(helixTotalField);

        jLabel2.setText("Actual Helices");
        jLabel2.setPreferredSize(new java.awt.Dimension(90, 16));
        jPanel2.add(jLabel2);

        helixActualField.setColumns(7);
        helixActualField.setEnabled(false);
        jPanel2.add(helixActualField);

        ControlFrame.getContentPane().add(jPanel2);

        ControlFrame.setBounds(10, 10, 210, 330);
        desktopPane.add(ControlFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        HistoryFrame.getContentPane().setLayout(new java.awt.FlowLayout());

        HistoryFrame.setIconifiable(true);
        HistoryFrame.setTitle("Filter History");
        HistoryFrame.setVisible(true);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(180, 140));
        jScrollPane2.setAutoscrolls(true);
        historyList.setModel(new DefaultListModel<FilterInfo>());
        historyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        historyList.setPreferredSize(null);
        jScrollPane2.setViewportView(historyList);

        jPanel4.add(jScrollPane2);

        HistoryFrame.getContentPane().add(jPanel4);

        undoFilterBtn.setText("Undo");
        undoFilterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoFilterBtnActionPerformed(evt);
            }
        });

        jPanel5.add(undoFilterBtn);

        infoFilterBtn.setText("Info…");
        infoFilterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoFilterBtnActionPerformed(evt);
            }
        });

        jPanel5.add(infoFilterBtn);

        HistoryFrame.getContentPane().add(jPanel5);

        HistoryFrame.setBounds(10, 350, 210, 240);
        desktopPane.add(HistoryFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        InfoFrame.getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        InfoFrame.setIconifiable(true);
        InfoFrame.setResizable(true);
        InfoFrame.setTitle("Helix Info");
        InfoFrame.setVisible(true);
        infoTextPane.setEditable(false);
        jScrollPane1.setViewportView(infoTextPane);

        InfoFrame.getContentPane().add(jScrollPane1);

        InfoFrame.setBounds(230, 460, 410, 130);
        desktopPane.add(InfoFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);

        getContentPane().add(desktopPane, java.awt.BorderLayout.CENTER);

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
                basepairFilterItemActionPerformed(evt);
            }
        });

        filterMenu.add(basepairFilterItem);

        helixFilterItem.setMnemonic('H');
        setKey(helixFilterItem, KeyEvent.VK_2);
        helixFilterItem.setText("Helix Filter…");
        helixFilterItem.setEnabled(false);
        helixFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helixFilterItemActionPerformed(evt);
            }
        });

        filterMenu.add(helixFilterItem);

        diagonalFilterItem.setMnemonic('D');
        setKey(diagonalFilterItem, KeyEvent.VK_3);
        diagonalFilterItem.setText("Diagonal Filter…");
        diagonalFilterItem.setEnabled(false);
        diagonalFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diagonalFilterItemActionPerformed(evt);
            }
        });

        filterMenu.add(diagonalFilterItem);

        aa_agFilterItem.setMnemonic('A');
        setKey(aa_agFilterItem, KeyEvent.VK_4);
        aa_agFilterItem.setText("AA / AG Ends Filter…");
        aa_agFilterItem.setEnabled(false);
        aa_agFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aa_agFilterItemActionPerformed(evt);
            }
        });

        filterMenu.add(aa_agFilterItem);

        eLoopFilterItem.setMnemonic('E');
        setKey(eLoopFilterItem, KeyEvent.VK_5);
        eLoopFilterItem.setText("E-Loop Filter…");
        eLoopFilterItem.setEnabled(false);
        eLoopFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eLoopFilterItemActionPerformed(evt);
            }
        });

        filterMenu.add(eLoopFilterItem);

        energyFilterItem.setMnemonic('g');
        setKey(energyFilterItem, KeyEvent.VK_6);
        energyFilterItem.setText("Energy Filter…");
        energyFilterItem.setEnabled(false);
        energyFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                energyFilterItemActionPerformed(evt);
            }
        });

        filterMenu.add(energyFilterItem);

        complexFilterItem.setMnemonic('C');
        setKey(complexFilterItem, KeyEvent.VK_6);
        complexFilterItem.setText("Complex Distance Filter…");
        complexFilterItem.setEnabled(false);
        complexFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                complexFilterItemActionPerformed(evt);
            }
        });

        filterMenu.add(complexFilterItem);

        menuBar.add(filterMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");
        viewDisplayMenuItem.setMnemonic('D');
        viewDisplayMenuItem.setText("Display Window");
        viewDisplayMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDisplayMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(viewDisplayMenuItem);

        viewControlMenuItem.setMnemonic('C');
        viewControlMenuItem.setText("Control Window");
        viewControlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewControlMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(viewControlMenuItem);

        viewHistoryMenuItem.setMnemonic('H');
        viewHistoryMenuItem.setText("History Window");
        viewHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewHistoryMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(viewHistoryMenuItem);

        viewInfoMenuItem.setMnemonic('I');
        setKey(viewInfoMenuItem, KeyEvent.VK_I);
        viewInfoMenuItem.setText("Info Window");
        viewInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewInfoMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(viewInfoMenuItem);

        menuBar.add(viewMenu);

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

    private void complexFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_complexFilterItemActionPerformed
        addUndo();
        appMain.rnaData = FilterController.showFilterDialog(appMain.rnaData, this, FilterController.COMPLEX);
        if (FilterController.success){
            this.updateImage();
            addHistory(new FilterInfo("Complex Distance Filter", FilterController.description));
            this.incrementUndo();
        }
    }//GEN-LAST:event_complexFilterItemActionPerformed
    
    private void energyFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_energyFilterItemActionPerformed
        addUndo();
        appMain.rnaData = FilterController.showFilterDialog(appMain.rnaData, this, FilterController.ENERGY);
        if (FilterController.success){
            this.updateImage();
            addHistory(new FilterInfo("Energy Filter", FilterController.description));
            this.incrementUndo();
        }
    }//GEN-LAST:event_energyFilterItemActionPerformed
    
    private void infoFilterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoFilterBtnActionPerformed
        int select = this.historyList.getSelectedIndex();
        if (select != -1){
            DefaultListModel<FilterInfo> dlm = (DefaultListModel<FilterInfo>)this.historyList.getModel();
            FilterInfo info = dlm.get(select);
            JOptionPane.showMessageDialog(this, info.getDescription(), info.toString() + " Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_infoFilterBtnActionPerformed
    
    private void eLoopFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eLoopFilterItemActionPerformed
        addUndo();
        appMain.rnaData = FilterController.showFilterDialog(appMain.rnaData, this, FilterController.ELOOP);
        if (FilterController.success){
            this.updateImage();
            addHistory(new FilterInfo("E-Loop Filter", FilterController.description));
            this.incrementUndo();
        }
        
    }//GEN-LAST:event_eLoopFilterItemActionPerformed
    
    private void aa_agFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aa_agFilterItemActionPerformed
        addUndo();
        appMain.rnaData = FilterController.showFilterDialog(appMain.rnaData, this, FilterController.AA_AG);
        if (FilterController.success){
            this.updateImage();
            addHistory(new FilterInfo("AA/AG Filter", FilterController.description));
            this.incrementUndo();
        }
        
    }//GEN-LAST:event_aa_agFilterItemActionPerformed
    
    private void diagonalFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diagonalFilterItemActionPerformed
        addUndo();
        appMain.rnaData = FilterController.showFilterDialog(appMain.rnaData, this, FilterController.DIAGONAL);
        if (FilterController.success){
            this.updateImage();
            addHistory(new FilterInfo("Diagonal Filter", FilterController.description));
            this.incrementUndo();
        }
    }//GEN-LAST:event_diagonalFilterItemActionPerformed
    
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
                    javax.imageio.ImageIO.write((java.awt.image.RenderedImage)img, "png", outputImage);
                }
                else{
                    javax.imageio.ImageIO.write((java.awt.image.RenderedImage)img, "jpeg", outputImage);
                }
            }
            catch (java.io.IOException ex){
                JOptionPane.showMessageDialog(this, "Image IO error", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
    
    private void preferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesMenuItemActionPerformed
        javax.swing.JDialog d = new PreferenceDialog(this, true, appMain);
        java.awt.Point origin = getCenteredOrigin(d);
        d.setLocation(origin);
        d.setVisible(true);
    }//GEN-LAST:event_preferencesMenuItemActionPerformed
    
    private void viewInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewInfoMenuItemActionPerformed
        this.bringToFront(this.InfoFrame);
    }//GEN-LAST:event_viewInfoMenuItemActionPerformed
    
    private void DisplayScrollPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_DisplayScrollPaneMouseClicked
        java.awt.Point p = evt.getPoint();
        java.awt.Rectangle rect = this.DisplayScrollPane.getViewport().getViewRect();
        double x = p.getX() + rect.getX();
        double y = p.getY() + rect.getY();
        String s = "x: " + x + "; y: " + y + "\n";
        this.infoTextPane.setText(s);
        //helixClicked(x, y);
        if (this.helixImgGen != null){
            helixImgGen.clicked(x, y, this.infoTextPane);
            this.updateImage();
        }
    }//GEN-LAST:event_DisplayScrollPaneMouseClicked
    
    private void undoFilterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoFilterBtnActionPerformed
        if (this.historyList.getSelectedIndex() >= 0){
            int restore = this.historyList.getSelectedIndex();
            String s = appMain.getPrefUndoDir() + File.separator + "undo" + restore;
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
                RNA old = (RNA)ois.readObject();
                DefaultListModel<FilterInfo> dlm = new DefaultListModel<FilterInfo>();
                for (int i = 0; i < restore; i++){
                    dlm.addElement(this.historyList.getModel().getElementAt(i));
                }
                this.historyList.setModel(dlm);
                appMain.rnaData = old;
                this.updateImage(); 
                this.currentUndo = restore;
            }
            catch (java.io.IOException ex){
                String msg = "Cannot restore undo information.  Please check undo directory is correct";
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (ClassNotFoundException ex){
                String msg = "Cannot save undo information.  File is corrupt";
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_undoFilterBtnActionPerformed
    
    private void viewHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewHistoryMenuItemActionPerformed
        bringToFront(this.HistoryFrame);
    }//GEN-LAST:event_viewHistoryMenuItemActionPerformed
    
    private void contentMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentMenuItemActionPerformed
        HelpContentJFrame contents = new HelpContentJFrame();
        contents.setLocation(0,0);
        contents.setVisible(true);
    }//GEN-LAST:event_contentMenuItemActionPerformed
    
    private void helixFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helixFilterItemActionPerformed
        addUndo();
        appMain.rnaData = FilterController.showFilterDialog(appMain.rnaData, this, FilterController.HELIX);
        if (FilterController.success){
            this.updateImage();
            addHistory(new FilterInfo("Helix Filter", FilterController.description));
            this.incrementUndo();
        }
        
    }//GEN-LAST:event_helixFilterItemActionPerformed
    
    private void basepairFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_basepairFilterItemActionPerformed
        addUndo();
        appMain.rnaData = FilterController.showFilterDialog(appMain.rnaData, this, FilterController.BASEPAIR);
        if (FilterController.success){
            this.enableFilterMenuItems(true);
            addHistory(new FilterInfo("Basepair Filter", FilterController.description));
            this.helixTotalField.setText("" + appMain.rnaData.getHelices().getCount());
            this.incrementUndo();
        }
    }//GEN-LAST:event_basepairFilterItemActionPerformed
    
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        HelpAboutJFrame about = new HelpAboutJFrame();
        Point origin = getCenteredOrigin(about);
        about.setLocation(origin);
        about.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    private void zoomComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zoomComboBoxItemStateChanged
        if (helixImgGen != null){
            this.updateImage();
        }
    }//GEN-LAST:event_zoomComboBoxItemStateChanged
    
    private void viewFlatBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewFlatBtnActionPerformed
        if (helixImgGen != null && helixImgGen.setImageType(HelixImageGenerator.VIEW_FLAT)){
            this.updateImage();
        }
    }//GEN-LAST:event_viewFlatBtnActionPerformed
    
    private void view2DBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_view2DBtnActionPerformed
        if (helixImgGen != null && helixImgGen.setImageType(HelixImageGenerator.VIEW_2D)){
            this.updateImage();
        }
    }//GEN-LAST:event_view2DBtnActionPerformed
    
    /**
     * Private method to do clean up when closing the current session.
     */
    private void close(){
        setTitle("RNA HEAT");
        appMain.cleanUp();
        System.gc();
        this.DisplayScrollPane.getViewport().setView(null);
        this.enableFilterMenuItems(false);
        basepairFilterItem.setEnabled(false);
        setControlLabels();
        this.helixActualField.setText("");
        this.helixNumField.setText("");
        this.helixTotalField.setText("");
        this.clearHistory();
        this.currentUndo = 0;
        this.infoTextPane.setText("");
    }
    
    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        close();
    }//GEN-LAST:event_closeMenuItemActionPerformed
    
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
                rheat.base.Reader reader = new rheat.base.Reader(inputFile);
                appMain.rnaData = reader.readBPSEQ();
                this.helixActualField.setText("" + appMain.rnaData.getActual().getCount());
                this.setTitle(this.getTitle() + ": " + appMain.rnaData.getUID());
                helixImgGen = new HelixImageGenerator(appMain.rnaData.getLength());
                //this.helixGraphicsLabel = new HelixGraphicsLabel(appMain.rnaData.getLength());
                setControlLabels();
                basepairFilterItem.setEnabled(true);
                this.updateImage();
            }
            catch(RuntimeException ex){
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Opening File", JOptionPane.ERROR_MESSAGE);
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
        if (returnVal == fc.APPROVE_OPTION){
            inputFile = fc.getSelectedFile();
            try {
                appMain.runScript(inputFile.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error Running Script", JOptionPane.ERROR_MESSAGE);
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
        close();
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
    private javax.swing.JMenu viewMenu;
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
    private javax.swing.JButton infoFilterBtn;
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
    private javax.swing.JComboBox<String> zoomComboBox;
    private javax.swing.JList<FilterInfo> historyList;
    // End of variables declaration//GEN-END:variables
}
