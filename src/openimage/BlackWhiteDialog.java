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

public class BlackWhiteDialog extends JDialog implements ChangeListener, ActionListener {

    private OpenImageWindow sc;
    private JSlider slider;
    private BufferedImage backup;

    public BlackWhiteDialog(final OpenImageWindow sc, BufferedImage backup, int startValue) {
        super(sc);
        this.sc = sc;
        ColorModel cm = backup.getColorModel();
        this.backup = new BufferedImage(cm, backup.copyData(null), cm.isAlphaPremultiplied(), null);
        setTitle("Schwarz-weiß");
        setModal(true);
        JPanel panel = new JPanel();
        slider = new JSlider();
        slider.setMaximum(256);
        slider.addChangeListener(this); // vor setValue!
        slider.setValue(startValue);
        panel.add(slider);
        JButton b = new JButton("Anwenden");
        b.addActionListener(this);
        panel.add(b);
        add(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ColorModel cm = BlackWhiteDialog.this.backup.getColorModel();
                sc.setImage(new BufferedImage(cm, BlackWhiteDialog.this.backup.copyData(null), cm.isAlphaPremultiplied(), null));
            }
        });
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public BlackWhiteDialog(OpenImageWindow sc, BufferedImage backup) {
        this(sc, backup, 128);
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        ColorModel cm = backup.getColorModel();
        sc.setImage(new BufferedImage(cm, backup.copyData(null), cm.isAlphaPremultiplied(), null));
        sc.blackWhiteWithoutShadesOfGray(slider.getValue());
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        setVisible(false);
    }
}