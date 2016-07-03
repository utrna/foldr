package rheat.GUI;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A window for displaying plain text.
 *
 * @author Kevin Grant
 */
public class TextFileFrame extends javax.swing.JInternalFrame {

    static int anchorX = 14; // shifts with each new window, to stagger
    static int anchorY = 36; // shifts with each new window, to stagger

    TextFileFrame() {
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("untitled");
        setMinimumSize(new java.awt.Dimension(500, 400));
        setNormalBounds(new java.awt.Rectangle(anchorX, anchorY, 500, 700));
        setBounds(new java.awt.Rectangle(anchorX, anchorY, 500, 700));
        TextFileFrame.anchorX += 20; // arbitrary
        TextFileFrame.anchorY += 20; // arbitrary
    }

    /**
     * Displays the contents of the given file.
     */
    public void openFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            this.textPane.read(reader, filePath/* description object */);
            Path asPath = Paths.get(filePath);
            Path parent = ((asPath != null) ? asPath.getParent() : null);
            StringBuilder titleSB = new StringBuilder();
            titleSB.append(asPath.getFileName());
            if (parent != null) {
                titleSB.append(" - ");
                titleSB.append(parent.toString());
            }
            setTitle(titleSB.toString());
        }
    }

    /**
     * Creates and configures GUI elements.
     */
    private void initComponents() {
        this.setLayout(new BorderLayout());
        this.scrollPane = new JScrollPane();
        this.textPane = new JTextArea();
        this.textPane.setEditable(false);
        this.textPane.setFont(RheatApp.getMonospacedFont(this.textPane.getFont(), 12));
        this.scrollPane.setViewportView(this.textPane);
        this.scrollPane.setBorder(new EmptyBorder(3, 3, 3, 3)); // arbitrary insets
        this.getContentPane().add(this.scrollPane, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    private JScrollPane scrollPane;
    private JTextArea textPane;

}
