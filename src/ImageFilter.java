/*
 * ImageFilter.java
 *
 * Created on 1/5/2008
 *
 */

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * The class is used by jFileChooser to show the image file with the format this system support. 
 */
public class ImageFilter extends FileFilter {

    private String format;
    public ImageFilter(String format) {
        this.format = format.toLowerCase();
    }

    /** Show all directories and the image with the format this filter support */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = this.getExtension(f);
        if (extension != null && extension.equals(this.format)) {
            return true;
        } else {
            return false;
        }
    }

    /** The format this filter support */
    public String getDescription() {
        return this.format;
    }
    
    /** Get the extension name of a file */
    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
