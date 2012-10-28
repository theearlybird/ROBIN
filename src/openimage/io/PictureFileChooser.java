package openimage.io;

import java.io.File;
import javax.swing.JFileChooser;

public class PictureFileChooser extends JFileChooser {

    public PictureFileChooser() {
        super();
        removeChoosableFileFilter(getChoosableFileFilters()[0]);
        setFileFilter(new PictureFilter());
        setAccessory(new PicturePreview(this));
        setCurrentDirectory(new File(System.getProperty("user.home")));
    }

    @Override
    public File getSelectedFile() {
        File f = super.getSelectedFile();
        if (f != null) { // keine Exception mehr bei nicht-ubuntu LookAndFeel
            String s = f.toString().toLowerCase();
            if (!s.endsWith(".gif") && !s.endsWith(".jpeg") && !s.endsWith(".jpg") && !s.endsWith(".png")) {
                return new File(f.toString() + ".jpg"); // jpg ist default-format
            }
        }
        return f;
    }
}