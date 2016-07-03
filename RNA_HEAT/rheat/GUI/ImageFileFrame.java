package rheat.GUI;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A window for displaying images.
 *
 * @author Kevin Grant
 */
public class ImageFileFrame extends javax.swing.JInternalFrame {

    static int anchorX = 14; // shifts with each new window, to stagger
    static int anchorY = 36; // shifts with each new window, to stagger

    ImageFileFrame() {
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("untitled");
        setMinimumSize(new java.awt.Dimension(300, 300));
        setNormalBounds(new java.awt.Rectangle(anchorX, anchorY, 300, 300));
        setBounds(new java.awt.Rectangle(anchorX, anchorY, 300, 300));
        ImageFileFrame.anchorX += 20; // arbitrary
        ImageFileFrame.anchorY += 20; // arbitrary
    }

    /**
     * Displays the contents of the given file.
     */
    public void openFile(String filePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(filePath));
        this.imagePane.setIcon(new ImageIcon(image));
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

    /**
     * Creates and configures GUI elements.
     */
    private void initComponents() {
        this.setLayout(new BorderLayout());
        this.scrollPane = new JScrollPane();
        this.imagePane = new JLabel();
        this.scrollPane.setViewportView(this.imagePane);
        this.scrollPane.setBorder(new EmptyBorder(3, 3, 3, 3)); // arbitrary insets
        this.getContentPane().add(this.scrollPane, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    private JScrollPane scrollPane;
    private JLabel imagePane;

}
