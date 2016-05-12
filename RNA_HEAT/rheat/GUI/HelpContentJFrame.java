package rheat.GUI;

import javax.swing.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.net.URL;
import java.lang.*;
import java.io.IOException;
/**
 *
 * @author  Li
 */
public class HelpContentJFrame extends javax.swing.JFrame {
    
    /** Creates new form JFrame */
    public HelpContentJFrame() {
        initComponents();
     }
    
    private class BookInfo {
    public String bookName;
    public URL bookURL;
    //public String prefix = "http://www.rna.icmb.utexas.edu/CLASS/2003-BIO384K/projects/angie/HelpContents/";
    public String prefix = System.getProperty("user.dir") + java.io.File.separator + "help" + java.io.File.separator;
    public BookInfo(String book, String filename) {
        bookName = book;
        try {
             bookURL = new URL(prefix + filename);
        } catch (java.net.MalformedURLException exc) {
            System.err.println("Attempted to create a BookInfo "
                               + "with a bad URL: " + bookURL);
            bookURL = null;
        }
    }

        public String toString() {
            return bookName;
        }
    }

    private void displayURL(URL url) {
    try {
         jEditorPane1.setPage(url);
       } catch (IOException e) {
         System.err.println("Attempted to read a bad URL: " + url);
       }
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        DefaultMutableTreeNode root
        = new DefaultMutableTreeNode("RNA Heat");
        DefaultMutableTreeNode gettingstarted
        = new DefaultMutableTreeNode(new BookInfo("Getting Started",
            "GettingStarted.html"));
        root.add(gettingstarted);

        DefaultMutableTreeNode desktopwindows
        = new DefaultMutableTreeNode("Desktop Windows");
        root.add(desktopwindows);

        DefaultMutableTreeNode controlwindow
        = new DefaultMutableTreeNode(new BookInfo("The Control Window",
            "ControlWindow.html"));
        desktopwindows.add(controlwindow);

        DefaultMutableTreeNode displaywindow
        = new DefaultMutableTreeNode(new BookInfo("The Display Window",
            "DisplayWindow.html"));
        desktopwindows.add(displaywindow);

        DefaultMutableTreeNode filterhistorywindow
        = new DefaultMutableTreeNode(new BookInfo("The Filter History Window",
            "FilterHistory.html"));
        desktopwindows.add(filterhistorywindow);

        DefaultMutableTreeNode helixinfowindow
        = new DefaultMutableTreeNode(new BookInfo("The Helix Info Window",
            "HelixInfo.html"));
        desktopwindows.add(helixinfowindow);

        DefaultMutableTreeNode menubar
        = new DefaultMutableTreeNode("The Menu Bar");
        root.add(menubar);

        DefaultMutableTreeNode menubarfile
        = new DefaultMutableTreeNode(new BookInfo("The Menu Bar: File",
            "MenuBarFile.html"));
        menubar.add(menubarfile);

        DefaultMutableTreeNode menubaredit
        = new DefaultMutableTreeNode(new BookInfo("The Menu Bar: Edit",
            "MenuBarEdit.html"));
        menubar.add(menubaredit);

        DefaultMutableTreeNode menubarfilters
        = new DefaultMutableTreeNode(new BookInfo("The Menu Bar: Filters",
            "MenuBarFilters.html"));
        menubar.add(menubarfilters);

        DefaultMutableTreeNode menubarview
        = new DefaultMutableTreeNode(new BookInfo("The Menu Bar: View",
            "MenuBarView.html"));
        menubar.add(menubarview);

        DefaultMutableTreeNode menubarhelp
        = new DefaultMutableTreeNode(new BookInfo("The Menu Bar: Help",
            "MenuBarHelp.html"));
        menubar.add(menubarhelp);
        /*DefaultMutableTreeNode filters
        = new DefaultMutableTreeNode("Filters");
        root.add(filters);
        */
        jTree1 = new javax.swing.JTree(root);

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        //Listen for when the selection changes.
        jTree1.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                jTree1.getLastSelectedPathComponent();

                if (node == null) return;
                Object nodeInfo = node.getUserObject();
                if (node.isLeaf()) {
                    BookInfo book = (BookInfo)nodeInfo;
                    displayURL(book.bookURL);
                }
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Contents");
        setName("HelpContentJFrame1");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jSplitPane1.setPreferredSize(new java.awt.Dimension(800, 600));
        jSplitPane1.setAutoscrolls(true);
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(200, 400));
        jTree1.setAutoscrolls(true);
        jScrollPane2.setViewportView(jTree1);

        jTabbedPane1.addTab("Contents", new javax.swing.ImageIcon(""), jScrollPane2);

        jSplitPane1.setLeftComponent(jTabbedPane1);

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html");
        jScrollPane1.setViewportView(jEditorPane1);

        jSplitPane1.setRightComponent(jScrollPane1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();
    }//GEN-LAST:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
         
        new HelpContentJFrame().setVisible(true);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree jTree1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
 
}

