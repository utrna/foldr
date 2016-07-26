package rheat.GUI;

import javax.swing.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.net.URL;
import java.lang.*;
import java.io.IOException;
import java.util.*;

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
        setNormalBounds(new java.awt.Rectangle(14, 36, 720, 650));
        setBounds(new java.awt.Rectangle(14, 36, 720, 650));
    }

    private class BookInfo {
        public String bookName;
        public URL bookURL;

        public BookInfo(String book, String fileName) {
            bookName = book;
            bookURL = getClass().getResource("/help/" + fileName);
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

        TreeNode root = new JTree.DynamicUtilTreeNode("RNA Heat", new BookInfo[] {
          // specify title, and name of target HTML file (note that
          // the BookInfo class prefixes "help/" automatically)
          new BookInfo("Getting Started", "intro.html"),
          new BookInfo("Panels", "windows.html"),
          new BookInfo("Menus", "menus.html"),
          new BookInfo("Running Programs", "programs.html"),
          new BookInfo("Scripting", "scripting.html")
        });
        jTree1 = new javax.swing.JTree(root);
        jTree1.setRootVisible(false);
        expandAll(jTree1);

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        // detect when the user clicks an index entry, and update the page
        jTree1.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                // note: DefaultMutableTreeNode is a superclass of DynamicUtilTreeNode
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                              jTree1.getLastSelectedPathComponent();
                if (node != null) {
                    Object nodeInfo = node.getUserObject();
                    if (node.isLeaf()) {
                        BookInfo book = (BookInfo)nodeInfo;
                        // should match hyperlink listener behavior
                        pushHistory(book.bookURL);
                        displayURL(book.bookURL);
                    }
               }
            }
        });

        // detect when the user clicks a link, and update the page
        // (not automatic since this is not a normal web browser)
        jEditorPane1.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                // ignore roll-over and mouse-exit events
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    // should match tree selection listener behavior, except
                    // that the matching tree index must be found manually
                    URL url = e.getURL();
                    pushHistory(url);
                    displayURL(url);
                    selectIndexEntryForURL(url);
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

    /**
     * Automatically navigates back to the most-recently-viewed page, as
     * captured by the history stack.  The stack is reduced accordingly.
     */
    public void performGoBack() {
        try {
            popHistory();
            URL url = historyStack.peek();
            displayURL(url);
            selectIndexEntryForURL(url);
        } catch (EmptyStackException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Primitive method to add a URL to the history stack.  This should
     * probably only be directly called by event listeners.
     * @param url the target page to add to the history stack
     */
    private void pushHistory(URL url) {
        historyStack.push(url);
    }

    /**
     * Primitive method to remove the top element of the history stack.
     * The higher-level call is performGoBack().
     */
    private void popHistory() {
        try {
            historyStack.pop();
        } catch (EmptyStackException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Given a page, attempts to select its index tree entry.
     * @param url the URL to search for in the tree
     */
    private void selectIndexEntryForURL(URL url) {
        int rowIndex = 0;
        Object root = jTree1.getModel().getRoot();
        DefaultMutableTreeNode rootSub = (DefaultMutableTreeNode)root;
        Enumeration nodeEnum = rootSub.depthFirstEnumeration();
        while (nodeEnum.hasMoreElements()) {
            TreeNode tn = (TreeNode)nodeEnum.nextElement();
            if (tn.isLeaf()) {
                DefaultMutableTreeNode nodeSub = (DefaultMutableTreeNode)tn;
                BookInfo bkInfo = (BookInfo)nodeSub.getUserObject();
                if (bkInfo.bookURL.equals(url)) {
                    // found a matching URL; can select its row
                    break;
                }
            }
            ++rowIndex;
        }
        if (rowIndex < jTree1.getRowCount()) {
            jTree1.setSelectionRow(rowIndex);
            // a side effect of "selecting" above is that the target
            // is once again pushed onto the history stack; remove it
            // because the goal is to have the preceding entry on top
            popHistory();
        } else {
            jTree1.setSelectionRow(-1);
        }
    }

    /**
     * Reveals all nodes in a tree.
     */
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
    private Stack<URL> historyStack = new Stack<URL>();

}
