package openimage.io;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class PictureFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        return Utils.isImageFile(f);
    }

    @Override
    public String getDescription() {
        return "Bilddateien (*.gif, *.jpg, *.jpeg, *.png)";
    }
}