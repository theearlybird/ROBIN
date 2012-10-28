package openimage.io;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class PictureFilter extends FileFilter {

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        }
        String path = pathname.getName().toLowerCase();
        return path.endsWith(".gif") || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg");
    }

    @Override
    public String getDescription() {
        return "Bilddateien (*.gif, *.jpg, *.jpeg, *.png)";
    }
}