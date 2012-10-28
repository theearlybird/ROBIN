package openimage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ScaleDialog extends JDialog implements FocusListener, ActionListener {

    private OpenImageWindow sc;
    private JTextField wtf, htf;
    private int oldW, oldH;

    public ScaleDialog(OpenImageWindow sc, int w, int h) {
        super(sc);
        this.sc = sc;
        oldW = w;
        oldH = h;
        setTitle("Skalieren");
        setModal(true);
        JPanel panel = new JPanel();
        wtf = new JTextField(8);
        wtf.setHorizontalAlignment(JTextField.RIGHT);
        wtf.setText(String.valueOf(w));
        wtf.addFocusListener(this);
        panel.add(wtf);
        panel.add(new JLabel("x"));
        htf = new JTextField(8);
        htf.setHorizontalAlignment(JTextField.RIGHT);
        htf.setText(String.valueOf(h));
        htf.addFocusListener(this);
        panel.add(htf);
        JButton b = new JButton("Skalieren");
        b.addActionListener(this);
        panel.add(b);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void focusGained(FocusEvent fe) {
    }

    @Override
    public void focusLost(FocusEvent fe) {
        if (fe.getSource() == wtf) {
            try {
                int w = Integer.parseInt(wtf.getText());
                int h = oldH * w / oldW;
                htf.setText(String.valueOf(h));
                oldH = h;
                oldW = w;
            } catch (NumberFormatException ex) {
                wtf.setText(String.valueOf(oldW));
            }
        } else if (fe.getSource() == htf) {
            try {
                int h = Integer.parseInt(htf.getText());
                int w = oldW * h / oldH;
                wtf.setText(String.valueOf(w));
                oldW = w;
                oldH = h;
            } catch (NumberFormatException ex) {
                htf.setText(String.valueOf(oldH));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        setVisible(false);
        sc.scale(oldW, oldH);
    }
}