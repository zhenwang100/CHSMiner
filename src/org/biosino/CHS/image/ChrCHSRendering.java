package org.biosino.CHS.image;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

import org.biosino.CHS.ontology.*;

/**
 * The class renders all CHS between a center chromosome and other surrounding chromosomes.
 */
public class ChrCHSRendering implements Rendering {
    /**
     * The chromosome to be rendered on the center layer.
     */
	private Chromosome centerChr;             
    /**
     * The map between a surrounding chromosome to its CHS list.
     */
	private Map<Chromosome, Set<CHS>> CHSMap;
    /**
     * Chromosomes to be rendered on the upper layer.
     */
	private ArrayList<Chromosome> chrListA;
    /**
     * Chromosomes to be rendered on the lower layer.
     */
	private ArrayList<Chromosome> chrListB;
        
    /**
     * A map from a rectangular to the CHS it represents.
     */ 
        private Map<Rectangle2D.Double, CHS> rectToCHS;
	
    /**
     * The minimum size of CHS to be rendered.
     */
        private int CHSSize;
    /**
     * The maximum p value of CHS to be rendered.
     */
        private double CHSPValue;
        
    /**
     * A <CODE>Gaphics2D</CODE> object representing the graphic device of the image.
     */
	private Graphics2D g2;
    /**
     * A <CODE>StringRendering</CODE> object to control the location of string drawing.
     */
        private StringRendering sRendering;

    /**
     * The <CODE>ChrRendering</CODE> object for center chromosome.
     */
	private ChrRendering centerChrRendering;

    /**
     * The color list to be recursively used for each surrounding chromosome. 
     */
	private Color[] colors = {Color.red, Color.orange, Color.green, Color.blue, Color.magenta};
        
    /**
     * Create a new <CODE>ChrCHSRendering</CODE> object for the given center chromosomes and all of its CHS 
     * and carry on all drawings.
     * @param centerChr the chromosome to be rendered on the center layer
     * @param CHSMap the map between a surrounding chromosome to its CHS list
     * @param CHSSize the minimum size of CHS to be rendered
     * @param CHSPValue the maximum p value of CHS to be rendered
     */
	public ChrCHSRendering(Chromosome centerChr, Map<Chromosome, Set<CHS>> CHSMap,
                    int CHSSize, double CHSPValue) {
		this.centerChr = centerChr;
		this.CHSMap = CHSMap;
                this.CHSSize = CHSSize;
                this.CHSPValue = CHSPValue;
                this.rectToCHS = new HashMap<Rectangle2D.Double, CHS>();
                
                // assign chromosomes to upper and lower layers
		this.chrListA = new ArrayList<Chromosome>();
		this.chrListB = new ArrayList<Chromosome>();
		int i = 0;
		for (Chromosome chr : CHSMap.keySet()) {
			if (i % 2 == 0) {
				this.chrListA.add(chr);
			} else {
				this.chrListB.add(chr);
			}
			i++;
		}         
	}
        
    /**
     * Render the object. Note all parameters for drawing could be set in the method.
     * They are specified with the size of image to be 1000 * 600. 
     * @param g2 a <CODE>Graphics2D</CODE> object
     */
        public void render(Graphics2D g2) {
                this.g2 = g2;
                this.sRendering = new StringRendering(this.g2);
                
                // draw backgroud
                this.g2.setColor(Color.white);
		this.g2.fillRect(0, 0, 1000, 600);

		// draw the center chromosome
                this.g2.setColor(Color.black);
		this.drawCenterChr(centerChr, 700, 500, 40, 260, 0);
		
                // draw surrounding chromosomes and link all CHS
		this.drawSurChrs(this.chrListA, 900, 500, 30, 130, 10, 1);
		this.drawSurChrs(this.chrListB, 900, 500, 30, 390, 10, -1);
                
                // draw titles
                Font oldFont = this.g2.getFont();
                Font newFont = new Font(oldFont.getFamily(), 
                        Font.BOLD, (int)(1.5 * oldFont.getSize()));
                this.g2.setFont(newFont);
                this.sRendering.midDrawString("Homologous Segments for " +
                        this.centerChr.tax + " Chromosome " + this.centerChr.chr , 500, 550);
                this.g2.setFont(oldFont);                  
        }

    /**
     * Draw the center chomosome.
     * @param chr the center chromosome
     * @param chrRange the length of the chromosome in the image
     * @param chrMid the x location of the middle point of the chromosome in the image
     * @param chrHeight the height of the chromosome
     * @param y the y location of the chromosome
     * @param location a value indicating which layer the chromosome should take up; 0 for center layer.
     */
	private void drawCenterChr(Chromosome chr, 
			double chrRange, double chrMid, double chrHeight, double y, int  location) {
		this.centerChrRendering = new ChrRendering(
				chr, this.g2, chrRange, chrMid, chrHeight, y, location);
		this.centerChrRendering.drawChr();
		this.centerChrRendering.drawScale();
	}
	
    /**
     * Draw surrounding chromosomes.
     * @param chrList a list containing surrounding chromosomes
     * @param range the totle range of those chromosomes in the image
     * @param mid the x location of the middle point in the image
     * @param chrHeight the height of the chromosomes
     * @param y the y location of the chromosomes
     * @param margin the gap between chromosomes in the image
     * @param location a value indicating which layer the chromosome should take up; 1 for upper and -1 for lower layer.
     */
	private void drawSurChrs (ArrayList<Chromosome> chrList, 
			double range, double mid, double chrHeight, double y, double margin, int location) {
		// caculate range and middle point for each chromosome
                double start = mid - 0.5 * range;
		int chrNum = chrList.size();
		if (chrNum == 0)
			return;
		double chrRange = range / chrNum;
		double[] chrMid = new double[chrNum];
		for (int i = 0; i < chrNum; i++) {
			chrMid[i] = start + (0.5 + i) * chrRange;
		}
		chrRange -= 2 * margin;
                
                // draw each chromosome and all CHS between it and center chomosome
		for (int i = 0; i < chrNum; i++) {
			ChrRendering rendering = new ChrRendering(chrList.get(i), this.g2,
					chrRange, chrMid[i], chrHeight, y, location);
			rendering.drawChr();
			rendering.drawName();

			if (location == 1) {
				this.g2.setColor(this.colors[i % 5]);
			} else if (location == -1) {
				this.g2.setColor(this.colors[4 - i % 5]);
			}
			for (CHS iCHS : this.CHSMap.get(chrList.get(i))) {
                                if (iCHS.size >= this.CHSSize && iCHS.pValue <= this.CHSPValue) {
                                    rendering.drawCHS(this.centerChrRendering, iCHS, this.rectToCHS);
                                }
			}
			this.g2.setColor(Color.BLACK);
			rendering.drawScale();
		}
		this.centerChrRendering.drawScale();
	}
        
    /**
     * Get the CHS a point located in. The method is used for user interaction from the graphics.
     * @param p a point
     * @return the CHS the point located in
     */
        public CHS getCHSClicked (Point2D p) {
                Set<Rectangle2D.Double> rectSet = this.rectToCHS.keySet();
                for (Rectangle2D.Double rect : rectSet) {
                    if (rect.contains(p)) {
                        return this.rectToCHS.get(rect);
                    }
                }
                return null;
        }
}
