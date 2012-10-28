package openimage.io;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class AlertDialog {

    public AlertDialog(JFrame parent, String msg) {
        final JOptionPane optionPane = new JOptionPane(new String[]{msg}, JOptionPane.ERROR_MESSAGE, JOptionPane.OK_OPTION, null, new String[]{"OK"}, "OK");
        JDialog dialog = optionPane.createDialog(parent, "Fehler");
        dialog.pack();
        dialog.setVisible(true);
    }

    public AlertDialog(JFrame parent, Exception ex) {
        this(parent, ex.getMessage());
    }
}