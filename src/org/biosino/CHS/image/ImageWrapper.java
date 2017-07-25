package org.biosino.CHS.image;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

/**
 * Wrap an image using bitmap format.
 */
public class ImageWrapper implements Wrapper {

    /**
     * The image to be rendered.
     */
    private BufferedImage image;
    
    /**
     * The <CODE>Rendering</CODE> object to be rendered.
     */
    private Rendering iRendering;

    /**
     * Create a new <CODE>ImageWrapper</CODE> object.
     */
    public ImageWrapper() {
        this.image = new BufferedImage(800, 500, BufferedImage.TYPE_3BYTE_BGR);
    }

    public void Wrap(Rendering iRendering) {
        this.iRendering = iRendering;
        Graphics2D g2 = this.image.createGraphics();
        g2.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));

        iRendering.render(g2);
    }

    /**
     * Get the image.
     * @return the image
     */
    public BufferedImage getImage() {
        return this.image;
    }

    public Rendering getRendering() {
        return this.iRendering;
    }

    /**
     * Write the image to an output file in a given bitmap format.
     * @param format output file format
     * @param fileName output file name
     * @throws java.io.IOException IOException
     */
    public void saveRendering(String format, String fileName) throws IOException {
        File file = new File(fileName);
        ImageIO.write(this.image, format, file);
    }
}
