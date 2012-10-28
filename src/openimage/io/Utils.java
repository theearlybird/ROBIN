package openimage.io;

import java.io.File;

public class Utils {

    public static boolean isImageFile(File f) {
        return f.getName().toLowerCase().endsWith(".png") || f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg") || f.getName().toLowerCase().endsWith(".gif");
    }
}