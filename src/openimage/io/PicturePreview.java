package openimage.io;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

public class PicturePreview extends JComponent implements PropertyChangeListener {
    
    private BufferedImage bi;
    
    public PicturePreview(JFileChooser chooser) {
        chooser.addPropertyChangeListener(this);
        setPreferredSize(new Dimension(180, 180));
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName())) {
            try {
                bi = ImageIO.read((File) e.getNewValue());
                repaint();
            } catch (IOException ex) {
            }
        }
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (bi != null) {
            int width, height;
            if (bi.getWidth() > bi.getHeight()) {
                width = 150;
                height = 150 * bi.getHeight() / bi.getWidth();
            } else {
                width = 150 * bi.getWidth() / bi.getHeight();
                height = 150;
            }
            g.drawImage(bi, 15, 15, width, height, getBackground(), null);
        }
    }
}