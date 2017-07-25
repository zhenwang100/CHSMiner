package org.biosino.CHS.image;

import java.awt.*;

/**
 * This class is used to control the location of string drawing.
 */
public class StringRendering {
    /**
     * A <CODE>Gaphics2D</CODE> object representing the graphic device.
     */
        private Graphics2D g2;
        
    /**
     * Create a new <CODE>StringRendering</CODE> object.
     * @param g2 a <CODE>Gaphics2D</CODE> object representing the graphic device
     */
        public StringRendering (Graphics2D g2) {
            this.g2 = g2;            
        }
        
    /**
     * Draw a string at the left of a point (x, y).
     * @param s a string
     * @param x x location
     * @param y y location
     */
	public void leftDrawString (String s, double x, double y) {
		this.g2.drawString(s, (float)x, (float)y);
	}
    /**
     * Draw a string with the middle point at (x, y).
     * @param s a string
     * @param x x location
     * @param y y location
     */
	public void midDrawString (String s, double x, double y) {
		Font font = this.g2.getFont();
		FontMetrics metrix = this.g2.getFontMetrics(font);
		x = x - metrix.stringWidth(s) / 2;
		this.g2.drawString(s, (float)x, (float)y);
	}
        
    /**
     * Draw a string at the right of a point (x, y).
     * @param s a string
     * @param x x location
     * @param y y location
     */
	public void rightDrawString (String s, double x, double y) {
		Font font = this.g2.getFont();
		FontMetrics metrix = this.g2.getFontMetrics(font);
		x = x - metrix.stringWidth(s);
		this.g2.drawString(s, (float)x, (float)y);
	}
}
