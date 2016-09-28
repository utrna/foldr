package rheat.GUI;

import rheat.base.AppMain;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * A window for seeing log messages.
 *
 * @author Kevin Grant
 */
public class LogFrame
extends javax.swing.JInternalFrame
implements PropertyChangeListener {

    public enum MsgType {
        INFO,
        WARN,
        ERROR
    }

    LogFrame() {
        this.attrSet = new SimpleAttributeSet();
        this.doc = new DefaultStyledDocument();
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("Log");
        setMinimumSize(new java.awt.Dimension(400, 100));
        setNormalBounds(new java.awt.Rectangle(250, 550, 800, 150)); // arbitrary
        setBounds(new java.awt.Rectangle(250, 550, 800, 150));
        AppMain.addClassPropertyChangeListener(AppMain.PROPERTY_INFO_LOG_MESSAGE, this);
        AppMain.addClassPropertyChangeListener(AppMain.PROPERTY_WARN_LOG_MESSAGE, this);
        AppMain.addClassPropertyChangeListener(AppMain.PROPERTY_ERROR_LOG_MESSAGE, this);
    }

    /**
     * Implements PropertyChangeListener; used to update log display.
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(AppMain.PROPERTY_INFO_LOG_MESSAGE)) {
            addLineToLog(MsgType.INFO, (String)event.getNewValue());
        } else if (event.getPropertyName().equals(AppMain.PROPERTY_WARN_LOG_MESSAGE)) {
            addLineToLog(MsgType.WARN, (String)event.getNewValue());
        } else if (event.getPropertyName().equals(AppMain.PROPERTY_ERROR_LOG_MESSAGE)) {
            addLineToLog(MsgType.ERROR, (String)event.getNewValue());
        }
    }

    /**
     * Creates and configures GUI elements.
     */
    private void initComponents() {
        this.scrollLogPane = new JScrollPane();
        this.logPane = new JTextPane(this.doc);
        this.logPane.setForeground(Color.gray);
        this.logPane.setBackground(Color.black);
        this.logPane.setEditable(false);
        this.logPane.setFont(RheatApp.getMonospacedFont(this.logPane.getFont(), 12));
        DefaultCaret caret = (DefaultCaret)this.logPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // auto-scroll to bottom on new text
        this.logPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        this.scrollLogPane.setViewportView(this.logPane);
        this.scrollLogPane.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white)); // arbitrary insets
        this.setLayout(new BorderLayout());
        this.getContentPane().add(this.scrollLogPane, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        pack();
    }

    /**
     * Adds the specified type of message to the log and
     * scrolls to the bottom.
     */
    public void addLineToLog(MsgType messageType, String logMessage) {
        try {
            switch (messageType) {
            case ERROR:
                StyleConstants.setBold(this.attrSet, true);
                StyleConstants.setForeground(this.attrSet, Color.white);
                StyleConstants.setBackground(this.attrSet, Color.red);
                break;
            case WARN:
                StyleConstants.setBold(this.attrSet, true);
                StyleConstants.setForeground(this.attrSet, Color.yellow);
                StyleConstants.setBackground(this.attrSet, Color.black);
                break;
            case INFO:
            default:
                StyleConstants.setBold(this.attrSet, false);
                StyleConstants.setForeground(this.attrSet, Color.green);
                StyleConstants.setBackground(this.attrSet, Color.black);
                break;
            }
            this.doc.insertString(this.doc.getLength(), logMessage, this.attrSet);
            if (!this.logPane.getText().endsWith("\n")) {
                this.doc.insertString(this.doc.getLength(), "\n", this.attrSet);
            }
        } catch (javax.swing.text.BadLocationException e) {
            e.printStackTrace();
        }
        scrollToBottom();
    }

    /**
     * Scrolls the log to the bottom so new messages are visible.
     */
    private void scrollToBottom() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollLogPane.getVerticalScrollBar().setValue(scrollLogPane.getVerticalScrollBar().getMaximum());
            }
        });
    }

    private MutableAttributeSet attrSet;
    private StyledDocument doc;
    private JScrollPane scrollLogPane;
    private JTextPane logPane;

}
