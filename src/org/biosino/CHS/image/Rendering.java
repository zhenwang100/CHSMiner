package org.biosino.CHS.image;

import java.awt.*;

/**
 * The interface represents objects to be rendered, regardless of the 
 * graphic device.
 */
public interface Rendering {

    /**
     * Render the object.
     * @param g2 a <CODE>Graphics2D</CODE> object
     */
    public void render(Graphics2D g2);
}