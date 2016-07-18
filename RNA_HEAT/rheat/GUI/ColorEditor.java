package rheat.GUI;

import rheat.base.AppMain;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A set of controls for editing colors using text or a chooser.
 *
 * @author Kevin Grant
 */
public class ColorEditor extends javax.swing.JPanel {

    private enum UpdateType {
        ALL, COLOR_TEXT, COLOR_PANEL
    }

    ColorEditor() {
        initComponents();
    }

    /**
     * Returns the color string currently displayed in the field (and
     * rendered by the colored background).
     */
    public String getColorString() {
        return colorField.getText();
    }

    /**
     * Uses the given color name to update all necessary sub-components
     * to display the color.
     * @param colorName a name acceptable to Color.decode(), such as "#ab12ef"
     */
    public void setColorString(String colorName) {
        setColorInternal(colorName, UpdateType.ALL);
    }

    /**
     * Customizes the title displayed when picking a new color from a dialog.
     */
    public void setTitle(String s) {
        titleText = s;
    }

    /**
     * Customizes the help displayed for the color.
     */
    public void setToolTipText(String s) {
        colorDisplay.setToolTipText(s);
    }

    /**
     * Creates and configures GUI elements.
     */
    private void initComponents() {
        colorField = new JTextField();
        colorDisplay = new JPanel();
        chooseColorButton = new JButton();
        colorField.setColumns(8);
        colorField.setToolTipText("You can type a color in hex, like '#ab12ef', or click the 'Choose Color…' box.");
        colorField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                // try to keep the color in sync with typing
                setColorInternal(colorField.getText(), UpdateType.COLOR_PANEL);
            }
            public void changedUpdate(DocumentEvent evt) {
                // do nothing
            }
            public void removeUpdate(DocumentEvent evt) {
                // do nothing
            }
        });
        colorDisplay.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
        chooseColorButton.setText("Choose Color…");
        chooseColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setNewColorByAskingUser();
            }
        });
        final JComponent contentPane = this;
        contentPane.setLayout(new BorderLayout());
        contentPane.add(colorField, BorderLayout.WEST);
        contentPane.add(colorDisplay, BorderLayout.CENTER);
        contentPane.add(chooseColorButton, BorderLayout.EAST);
    }

    /**
     * Updates perhaps a subset of components to a new color.  This
     * is used by setColorString() to update all components at once,
     * and also by event listeners to update a subset of components.
     * @param colorName a name acceptable to Color.decode(), such as "#ab12ef"
     * @param updateWhat which subset of components should be updated (usually ALL)
     */
    private void setColorInternal(String colorName, UpdateType updateWhat) {
        try {
            // it must be possible to update components individually to handle
            // the case of responding to events in one of the updated components
            // (e.g. if user types new value in text field, field should not try
            // to update or an exception will be thrown; and yet, the rest of
            // the color display should still be updated)
            if ((updateWhat == UpdateType.ALL) ||
                (updateWhat == UpdateType.COLOR_PANEL)) {
                Color asColor = Color.decode(colorName);
                colorDisplay.setBackground(asColor);
            }
            if ((updateWhat == UpdateType.ALL) ||
                (updateWhat == UpdateType.COLOR_TEXT)) {
                colorField.setText(colorName);
            }
        } catch (NumberFormatException e) {
            // arbitrarily suppress warnings for shorter strings (since the
            // display is updated while the user types)
            if (colorName.length() >= 6) {
                AppMain.log(AppMain.ERROR, "Failed to decode color name '" + colorName + "'; ignoring.");
            }
	}
    }

    /**
     * Presents a color dialog that has been initialized to the current
     * color value, and updates the displayed value if the user accepts.
     */
    private void setNewColorByAskingUser() {
        Color selectedColor = colorDisplay.getBackground();
        if (selectedColor == null) {
            // just pick something
            selectedColor = Color.red;
        }
        selectedColor = JColorChooser.showDialog(this, this.titleText, selectedColor);
        if (selectedColor != null) {
            setColorString(String.format("#%02X%02X%02X", selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()));
        }
    }

    private JTextField colorField;
    private JPanel colorDisplay;
    private JButton chooseColorButton;
    private String titleText = "New Color"; // see setTitle()

}
