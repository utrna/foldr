package rheat.GUI;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

public class FocusingField {

     static class BorderManager
     implements FocusListener, MouseListener {

         BorderManager(JComponent focusBorderTarget) {
             // for when field with border is the same as the one gaining/losing focus
             this(focusBorderTarget, focusBorderTarget);
         }

         BorderManager(JComponent focusTarget, JComponent borderedComponent) {
             this.mouseDown = false;
             this.mouseEvent = false;
             focusTarget.addFocusListener(this);
             focusTarget.addMouseListener(this);
             this.borderedComponent = borderedComponent;
             // arbitrary settings; try to make keyboard focus much more obvious and interesting (the Java default is too subtle and boring)
             Border innerPadBorder = BorderFactory.createMatteBorder(4, 2, 4, 2, focusTarget.getBackground()); // common to both states; interior space between text and outer edges
             Border focusedEdgeBorder = BorderFactory.createMatteBorder(3, 3, 3, 3, new Color(0, 136, 255)); // appearance of frame when focused
             Border inactiveFocusedEdgeBorder = BorderFactory.createMatteBorder(3, 3, 3, 3, new Color(130, 130, 130)); // e.g. when display of menu steals focus of active field
             this.focusBorder = BorderFactory.createCompoundBorder(focusedEdgeBorder, innerPadBorder);
             this.inactiveFocusBorder = BorderFactory.createCompoundBorder(inactiveFocusedEdgeBorder, innerPadBorder);
             Border emptySpaceOuterBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(240, 240, 240)); // space outside unfocused frame
             Border unfocusedEdgeBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black); // appearance of frame when not focused
             Border unfocusedOuterBorder = BorderFactory.createCompoundBorder(emptySpaceOuterBorder, unfocusedEdgeBorder); // edge and padding combined
             this.noFocusBorder = BorderFactory.createCompoundBorder(unfocusedOuterBorder, innerPadBorder); // inner space, edge and padding combined
             this.borderedComponent.setBorder(noFocusBorder);
         }

         public void setAutoSelectAll(boolean autoSelectAll) {
             this.autoSelectAll = autoSelectAll;
         }

         @Override
         public void focusGained(FocusEvent evt) {
             // when text can be entered, use a thick colored border
             this.borderedComponent.setBorder(this.focusBorder);
             if ((autoSelectAll) && ((!mouseEvent) || (!mouseDown))) {
                 if (evt.getSource() instanceof JTextComponent) {
                     // when tabbing to a field, select all the text; when
                     // clicking however, let the user pick a cursor location
                     JTextComponent asTC = (JTextComponent)evt.getSource();
                     asTC.selectAll();
                 }
             }
             // reset flags
             this.mouseEvent = false;
             this.mouseDown = false;
         }

         @Override
         public void focusLost(FocusEvent evt) {
             // when text cannot be entered, either restore a thin
             // plain border with padding (normal case) or use a
             // dimmed version of the focused border (e.g. for menu
             // temporarily stealing focus)
             this.borderedComponent.setBorder(evt.isTemporary() ? this.inactiveFocusBorder : this.noFocusBorder);
             // reset flags
             this.mouseEvent = false;
             this.mouseDown = false;
         }

         @Override
         public void mouseClicked(MouseEvent evt) {
             // detect mouse clicks to affect selection behavior
             this.mouseEvent = true;
             this.mouseDown = true;
         }

         @Override
         public void mousePressed(MouseEvent evt) {
             // detect mouse clicks to affect selection behavior
             this.mouseEvent = true;
             this.mouseDown = true;
         }

         @Override
         public void mouseReleased(MouseEvent evt) {
             // do nothing
             this.mouseEvent = true;
         }

         @Override
         public void mouseEntered(MouseEvent evt) {
             // do nothing
             this.mouseEvent = true;
         }

         @Override
         public void mouseExited(MouseEvent evt) {
             // do nothing
             this.mouseEvent = true;
         }

         private JComponent borderedComponent;
         private Border focusBorder;
         private Border inactiveFocusBorder;
         private Border noFocusBorder;
         private boolean mouseDown;
         private boolean mouseEvent;
         private boolean autoSelectAll;

     }

     /**
      * Use in place of regular JTextField for clearer focus rings
      * and more space for text.
      */
     static public class SingleLine
     extends JTextField {

         SingleLine() {
             super();
             borderMgr = new BorderManager(this);
             borderMgr.setAutoSelectAll(true);
         }

         private BorderManager borderMgr;

     }

     /**
      * Use in place of regular JTextArea for clearer focus rings
      * and more space for text.
      */
     static public class MultiLine
     extends JTextArea {

         MultiLine() {
             super();
             borderMgr = new BorderManager(this/* focus target and bordered component */);
         }

         MultiLine(JComponent borderedComponent) {
             // e.g. might want scroll pane to have border but text pane to monitor focus
             super();
             borderMgr = new BorderManager(this/* focus target */, borderedComponent);
         }

         private BorderManager borderMgr;

     }

}
