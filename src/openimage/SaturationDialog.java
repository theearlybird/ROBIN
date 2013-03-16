package openimage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SaturationDialog extends JDialog implements ChangeListener, ActionListener {

    private OpenImageWindow sc;
    private JSlider slider;
    private BufferedImage backup;

    public SaturationDialog(final OpenImageWindow sc, BufferedImage backup) {
        super(sc);
        this.sc = sc;
        ColorModel cm = backup.getColorModel();
        this.backup = new BufferedImage(cm, backup.copyData(null), cm.isAlphaPremultiplied(), null);
        setTitle("Desaturieren");
        setModal(true);
        JPanel panel = new JPanel();
        slider = new JSlider();
        slider.setMaximum(100);
        slider.addChangeListener(this); // vor setValue!
        slider.setValue(100);
        panel.add(slider);
        JButton b = new JButton("Anwenden");
        b.addActionListener(this);
        panel.add(b);
        JButton b2 = new JButton("Abbrechen");
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                SaturationDialog.this.dispose();
            }
        });
        panel.add(b2);
        add(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ColorModel cm = SaturationDialog.this.backup.getColorModel();
                sc.setImage(new BufferedImage(cm, SaturationDialog.this.backup.copyData(null), cm.isAlphaPremultiplied(), null));
            }
        });
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        ColorModel cm = backup.getColorModel();
        sc.setImage(new BufferedImage(cm, backup.copyData(null), cm.isAlphaPremultiplied(), null));
        sc.desaturate(slider.getValue());
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        setVisible(false);
    }
}