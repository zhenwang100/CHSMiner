package org.biosino.CHS.image;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;

import org.biosino.CHS.ontology.*;

/**
 * This class renders a chromosome, which is used to compare CHS between chromosomes.
 */
public class ChrRendering {
    /**
     * The <CODE>Chromosome</CODE> object to be rendered.
     */
	private Chromosome chr;
	
    /**
     * A <CODE>Gaphics2D</CODE> object representing the graphic device.
     */
	private Graphics2D g2;
    /**
     * A <CODE>StringRendering</CODE> object to control the location of string drawing.
     */
        private StringRendering sRendering;
    /**
     * The x location of the middle point of the chromosome in the image.
     */
        private double midLoc;
    /**
     * The proportion of image coordinates relative to chromosome coordinates.
     */
	private double scale;
    /**
     * The height of the chromosome in the image.
     */
	private double height;
    /**
     * The y location of the chromosome.
     */
	private double y;
    /**
     * A value indicating which layer the chromosome should take up. Positive,
     * negative and 0 value indicates upper, lower, and middle layer, respectively.
     */
	private int location;
	
    /**
     * Create a new <CODE>ChrRendering</CODE> object.
     * @param chr the <CODE>Chromosome</CODE> object to be rendered
     * @param g2 a <CODE>Gaphics2D</CODE> object representing the graphic device
     * @param range the length of the chromosome in the image
     * @param midLoc the x location of the middle point of the chromosome in the image.
     * @param height the height of the chromosome
     * @param y the y location of the chromosome
     * @param location a value indicating which layer the chromosome should take up
     */
	public ChrRendering (Chromosome chr, Graphics2D g2, double range,
			double midLoc, double height, double y, int location) {
		this.chr = chr;
		this.g2 = g2;
                this.sRendering = new StringRendering(g2);
		this.scale = range / chr.length;
		this.midLoc = midLoc;
		this.height = height;
		this.y = y;
		this.location = location;
	}
	
    /**
     * Transform chromosome coordinates to image coordinates in horizontal (x) axes.
     * @param x a point in chromosome coordinates
     * @return corresponding x point in image coordinates
     */
	private double transX (double x) {
		double chrLocMid = (1 + this.chr.length) / 2;
		return (x - chrLocMid) * this.scale + this.midLoc;
	}

    /**
     * Draw the <CODE>Chromosome</CODE> object using a <CODE>RoundRectangle2D</CODE>.
     */
	public void drawChr() {
		double transStart = this.transX(1);
		double transEnd = this.transX(chr.length);
		this.g2.draw(new RoundRectangle2D.Double(transStart, this.y, transEnd - transStart, height,
					0.05 * (transEnd - transStart), height));
	}

    /**
     * Label the scales of the chromosome.
     */
	public void drawScale() {		
		double verticalLength = 0.125 * this.height;
		double labelDist = 0.6 * this.height;	
		int scaleNum = this.getScaleNum();
		double transStart = this.transX(1);
		double scaleUnit = this.chr.length * this.scale / scaleNum;
		for (int i = 1; i < scaleNum; i ++) {
			this.g2.draw(new Line2D.Double(transStart + i * scaleUnit, y,
						transStart + i * scaleUnit, y + verticalLength));
			this.sRendering.midDrawString((int)(i * scaleUnit / this.scale / 1e6) + "M",
					transStart + i * scaleUnit, y + labelDist);
		}
	}
        // The number of scales are caculated according to the width of the labels relative to the length of the chromosome. 
        private int getScaleNum() {
		Font font = this.g2.getFont();
		FontMetrics metrix = this.g2.getFontMetrics(font);
		int scaleWidth = 2 * metrix.stringWidth("xxxM");
		return (int)(this.chr.length * this.scale / scaleWidth);
	}
        
    /**
     * Draw a CHS on the chromosome and link it with another chromosome.
     * @param other another <CODE>ChrRendering</CODE> object
     * @param iCHS a CHS linking the two chromosomes
     * @param rectToCHS a map from rectangular to CHS
     */
	public void drawCHS(ChrRendering other, CHS iCHS, Map<Rectangle2D.Double, CHS> rectToCHS) {
                GenoLoc locA, locB;
		if (this.chr.equals(iCHS.chrA) && other.chr.equals(iCHS.chrB)) {
			locA = iCHS.locA;
			locB = iCHS.locB;
		} else if (this.chr.equals(iCHS.chrB) && other.chr.equals(iCHS.chrA)) {
			locA = iCHS.locB;
			locB = iCHS.locA;
		} else {
			// do not match error!
			return;
		}
		
                Rectangle2D.Double rectA = new Rectangle2D.Double(this.transX(locA.start), this.y,
					this.transX(locA.end) - this.transX(locA.start), this.height);
                Rectangle2D.Double rectB = new Rectangle2D.Double(other.transX(locB.start), other.y,
					other.transX(locB.end) - other.transX(locB.start), other.height);
                this.g2.fill(rectA);
                this.g2.fill(rectB);
	
		if (this.location > 0) {
			this.g2.draw(new Line2D.Double(0.5 * (this.transX(locA.start) + this.transX(locA.end)),
						this.y + this.height,
						0.5 * (other.transX(locB.start) + other.transX(locB.end)),
						other.y));
		} else if (this.location < 0) {
			this.g2.draw(new Line2D.Double(0.5 * (this.transX(locA.start) + this.transX(locA.end)),
						this.y,
						0.5 * (other.transX(locB.start) + other.transX(locB.end)),
						other.y + other.height));
		}
                
                rectToCHS.put(rectA, iCHS);
                rectToCHS.put(rectB, iCHS);
	}
	
    /**
     * Draw the name of the chromosome.
     */
	public void drawName() {
		double labelDist = 0.5 * this.height;
		if (this.location > 0 ) {
                        this.sRendering.midDrawString(this.chr.chr, this.midLoc, this.y - labelDist);
                        this.sRendering.midDrawString(this.chr.tax, this.midLoc, this.y - 2.5 * labelDist);
		} else if (this.location < 0) {
			this.sRendering.midDrawString(this.chr.chr, this.midLoc, this.y + this.height + 2 *labelDist);
                        this.sRendering.midDrawString(this.chr.tax, this.midLoc, this.y + this.height + 3.5 * labelDist);
		}
	}
}
