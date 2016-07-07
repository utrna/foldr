package rheat.GUI;

import java.awt.BorderLayout;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.*;

/**
 * The application About window.
 *
 * @author Kevin Grant
 */
public class AboutFrame extends javax.swing.JInternalFrame {

    AboutFrame() {
        initComponents();
        setClosable(true);
        //setMaximizable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("About RNA Heat");
        setMinimumSize(new java.awt.Dimension(500, 300));
        setNormalBounds(new java.awt.Rectangle(40, 40, 500, 300));
        setBounds(new java.awt.Rectangle(40, 40, 500, 300));
    }

    /**
     * Creates and configures GUI elements.
     */
    private void initComponents() {
        this.setLayout(new BorderLayout());
        this.scrollPane = new JScrollPane();
        this.textPane = new JTextPane();
        this.textPane.setEditable(false);
        //this.textPane.setFont(RheatApp.getMonospacedFont(this.textPane.getFont(), 12));
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>\n");
        sb.append("<title>About RNA HEAT</title>\n");
        sb.append("</head><body>\n");
        sb.append("<h1>RNA HEAT v2.0</h1>\n");
        sb.append("<h2>RNA Helix Elimination and Acquisition Tool</h2>\n");
        sb.append("<p>Version 1.0 created in 2003 by Team MATRIX:\n");
        sb.append("<ul>\n");
        sb.append("<li>Gurushyam Hariharan (guru@ece.utexas.edu)\n");
        sb.append("<li>James Zhang (jyzhang@mail.utexas.edu)\n");
        sb.append("<li>Angie Li (aloysia@cs.utexas.edu)\n");
        sb.append("</ul>\n");
        sb.append("<p>Version 2.0 created in 2016 by:\n");
        sb.append("<ul>\n");
        sb.append("<li>Kevin M. Grant (kmgrant@utexas.edu)\n");
        sb.append("</ul>\n");
        sb.append("</body></html>\n");
        try {
            this.textPane.setEditorKit(new HTMLEditorKit());
            this.textPane.setText(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            this.textPane.setText("(warning: failed to load HTML)");
        }
        this.scrollPane.setViewportView(this.textPane);
        this.scrollPane.setBorder(new EmptyBorder(3, 3, 3, 3)); // arbitrary insets
        this.getContentPane().add(this.scrollPane, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    private JScrollPane scrollPane;
    private JTextPane textPane;

}
