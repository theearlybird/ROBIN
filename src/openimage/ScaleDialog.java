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
    private JButton b;
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
        wtf.addActionListener(this);
        panel.add(wtf);
        panel.add(new JLabel("x"));
        htf = new JTextField(8);
        htf.setHorizontalAlignment(JTextField.RIGHT);
        htf.setText(String.valueOf(h));
        htf.addFocusListener(this);
        htf.addActionListener(this);
        panel.add(htf);
        b = new JButton("Skalieren");
        b.addActionListener(this);
        panel.add(b);
        add(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void focusGained(FocusEvent fe) {
        JTextField tf = (JTextField) fe.getComponent();
        tf.setSelectionStart(0);
        tf.setSelectionEnd(tf.getText().length());
    }

    @Override
    public void focusLost(FocusEvent fe) {
        leave((JTextField) fe.getSource());
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        // WICHTIG: umrechnung zum Beibehalten des Verhältnisses ausführen (in 'focusLost')
        if (ae.getSource() instanceof JTextField) {
            leave((JTextField) ae.getSource());
        }
        setVisible(false);
        sc.scale(oldW, oldH);
    }

    public void leave(JTextField tf) {
        if (tf == wtf) {
            try {
                int w = Integer.parseInt(wtf.getText());
                int h = oldH * w / oldW;
                htf.setText(String.valueOf(h));
                oldH = h;
                oldW = w;
            } catch (NumberFormatException ex) {
                wtf.setText(String.valueOf(oldW));
            }
        } else if (tf == htf) {
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
}