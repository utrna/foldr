package rheat.GUI;

import javax.swing.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.net.URL;
import java.lang.*;
import java.io.IOException;

/**
 * A window for accessing user documentation.
 *
 * @author  Li
 */
public class HelpContentJFrame extends javax.swing.JInternalFrame {

    public HelpContentJFrame() {
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("Help");
        setMinimumSize(new java.awt.Dimension(600, 400));
        setNormalBounds(new java.awt.Rectangle(14, 36, 600, 400));
        setBounds(new java.awt.Rectangle(14, 36, 600, 400));
    }

    private class BookInfo {
        public String bookName;
        public URL bookURL;
        public String prefix = "file://" + System.getProperty("user.dir") + "/help/";

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

    /**
     * Creates and configures the help GUI elements.
     */
    private void initComponents() {
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        DefaultMutableTreeNode root
        = new DefaultMutableTreeNode("RNA Heat");
        DefaultMutableTreeNode gettingstarted
        = new DefaultMutableTreeNode(new BookInfo("Getting Started",
            "intro.html"));
        root.add(gettingstarted);

        DefaultMutableTreeNode controlwindow
        = new DefaultMutableTreeNode(new BookInfo("Window: “Controls”",
            "windows.html#Controls"));
        root.add(controlwindow);

        DefaultMutableTreeNode displaywindow
        = new DefaultMutableTreeNode(new BookInfo("Window: “Display Window”",
            "windows.html#Display"));
        root.add(displaywindow);

        DefaultMutableTreeNode filterhistorywindow
        = new DefaultMutableTreeNode(new BookInfo("Window: “Filter History”",
            "windows.html#FilterHistory"));
        root.add(filterhistorywindow);

        DefaultMutableTreeNode helixinfowindow
        = new DefaultMutableTreeNode(new BookInfo("Window: “Helix Info”",
            "windows.html#HelixInfo"));
        root.add(helixinfowindow);

        DefaultMutableTreeNode menubarfile
        = new DefaultMutableTreeNode(new BookInfo("Menu: “File”",
            "menus.html#File"));
        root.add(menubarfile);

        DefaultMutableTreeNode menubaredit
        = new DefaultMutableTreeNode(new BookInfo("Menu: “Edit”",
            "menus.html#Edit"));
        root.add(menubaredit);

        DefaultMutableTreeNode menubarfilters
        = new DefaultMutableTreeNode(new BookInfo("Menu: “Filters”",
            "menus.html#Filters"));
        root.add(menubarfilters);

        DefaultMutableTreeNode menubarview
        = new DefaultMutableTreeNode(new BookInfo("Menu: “View”",
            "menus.html#View"));
        root.add(menubarview);

        DefaultMutableTreeNode menubarwindow
        = new DefaultMutableTreeNode(new BookInfo("Menu: “Window”",
            "menus.html#Window"));
        root.add(menubarwindow);

        DefaultMutableTreeNode menubarhelp
        = new DefaultMutableTreeNode(new BookInfo("Menu: “Help”",
            "menus.html#Help"));
        root.add(menubarhelp);

        jTree1 = new javax.swing.JTree(root);
        jTree1.setRootVisible(false);
        expandAll(jTree1);

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

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

        jSplitPane1.setPreferredSize(new java.awt.Dimension(800, 600));
        jSplitPane1.setAutoscrolls(true);
        jTree1.setPreferredSize(new java.awt.Dimension(200, 400));
        jTree1.setAutoscrolls(true);
        jScrollPane2.setViewportView(jTree1);

        jSplitPane1.setLeftComponent(jScrollPane2);

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html");
        jScrollPane1.setViewportView(jEditorPane1);

        jSplitPane1.setRightComponent(jScrollPane1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void expandAll(JTree tree) {
        int i = 0;
        int j = tree.getRowCount();
        while (i < j) {
            tree.expandRow(i);
            ++i;
            j = tree.getRowCount();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

}
