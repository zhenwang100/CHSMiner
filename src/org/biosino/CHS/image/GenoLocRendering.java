package org.biosino.CHS.image;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

import org.biosino.CHS.ontology.*;

/**
 * This class is used to render a <CODE>GenoLoc</CODE> object,which is an half part of a CHS rendering.
 */
public class GenoLocRendering {
    /**
     * The <CODE>GenoLoc</CODE> object to be rendered.
     */
	private GenoLoc genoLoc;
    /**
     * A <CODE>Gaphics2D</CODE> object representing the graphic device.
     */
	private Graphics2D g2;
    /**
     * A <CODE>StringRendering</CODE> object to control the location of string drawing.
     */
        private StringRendering sRendering;
    /**
     * The proportion of image coordinates relative to chromosome coordinates.
     */
        private double scale;
    /**
     * The image location of the middle point of the <CODE>GenoLoc</CODE>.
     */
	private double midLoc;
	
    /**
     * Create a new <CODE>GenoLocRendering</CODE> object.
     * @param genoLoc the <CODE>GenoLoc</CODE> object to be rendered
     * @param g2 a <CODE>Gaphics2D</CODE> object representing the graphic device
     * @param scale the proportion of image coordinates relative to chromosome coordinates
     * @param midLoc the image location of the middle point of the <CODE>GenoLoc</CODE>
     */
	public GenoLocRendering (GenoLoc genoLoc, Graphics2D g2, double scale, double midLoc) {
		this.genoLoc = genoLoc; 
		this.g2 = g2;
		this.sRendering = new StringRendering(g2);
                this.scale = scale;
		this.midLoc = midLoc; 
	}

    /**
     * Transform chromosome coordinates to image coordinates in horizontal (x) axes.
     * @param x a point in chromosome coordinates
     * @return corresponding x point in image coordinates
     */
	private double transX (double x) {
		double genoLocMid = (this.genoLoc.start + this.genoLoc.end) / 2;
		return (x - genoLocMid) * this.scale + this.midLoc;
	}

    /**
     * Draw the <CODE>GenoLoc</CODE> object using a horizontal line and label its name. See:
     * <PRE>    
     *          Chr label   ---------------------------
     * </PRE>
     * @param y the vertical location (in y axes) of the GenoLoc in the image
     * @param labelDist the x distance between the start ponit of the <CODE>GenoLoc</CODE> and its label
     */
	public void drawChr (double y, double labelDist) {
		double transStart = this.transX(this.genoLoc.start);
		double transEnd = this.transX(this.genoLoc.end);
		
                // draw chromosome
		this.g2.draw(new Line2D.Double(transStart, y, transEnd, y));
		
                // draw label
                Font oldFont = this.g2.getFont();
                Font newFont = new Font(oldFont.getFamily(), Font.BOLD, oldFont.getSize());
                this.g2.setFont(newFont);
                this.sRendering.rightDrawString(this.getChrLabel(), transStart - labelDist, y);
                this.g2.setFont(oldFont);      
		
	}
        
	private String getChrLabel () {
		return this.genoLoc.chromosome.tax + "   " + this.genoLoc.chromosome.chr;
	}

    /**
     * Draw the scale ruler of the <CODE>GenoLoc</CODE> and label it. See:
     * <PRE>
     *          50          51           52         53  Mb     (scale labels)
     *          |-----------|------------|----------|          (scale ruler and vertical lines)
     * 
     * </PRE>
     * @param y the y location of the scale in the image
     * @param labelDist the y distance between the scale and its labels
     * @param verticalLength the length of vertical lines on the scale ruler
     */
	public void drawScale (double y, double labelDist, double verticalLength) {
		int start = this.genoLoc.start;
		int end = this.genoLoc.end;
                double transStart = this.transX(start);
		double transEnd = this.transX(end);
		
		// draw the ruler and its both ends
                this.g2.draw(new Line2D.Double(transStart, y, transEnd, y));
		this.g2.draw(new Line2D.Double(transStart, y - verticalLength / 2, 
					transStart, y + verticalLength / 2));
		this.g2.draw(new Line2D.Double(transEnd, y - verticalLength / 2, 
					transEnd, y + verticalLength / 2));
		
                // draw the range labels (transformed to Mb unit)
		double scaleStart = start * 1e-6;
		double scaleEnd = end * 1e-6;
		java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		this.sRendering.rightDrawString(nf.format(scaleStart), transStart, y - labelDist);
		this.sRendering.leftDrawString(nf.format(scaleEnd) + "    Mb", transEnd, y - labelDist);
		
                // draw scale labels and their corresponding vertical lines
		for (int i = (int)scaleStart + 1; i <= (int)scaleEnd; i++) {
			double xi = this.transX(i * 1e6);
			this.g2.draw(new Line2D.Double(xi, y - verticalLength, xi, y + verticalLength));
			this.sRendering.midDrawString(Integer.toString(i), xi, y - labelDist);
		}		
	}

    /**
     * Draw genes located at the <CODE>GenoLoc</CODE> using rectangle.
     * @param geneList the gene list
     * @param y the y location of the genes (rectangles) in the image
     * @param height the height of the genes (rectangles) in the image
     * @param rectToGene a map from a rectangle to the gene it represents, may be null value
     * @return the x locations of the middle points of the rectangles (used to draw likage lines)
     */
	public double[] drawGenes (Gene[] geneList, double y, double height, 
                Map<Rectangle2D.Double, Gene> rectToGene) {
		double[] geneMid = new double[geneList.length];
		for (int i = 0; i < geneList.length; i++) {
			geneMid[i] = this.drawGene(geneList[i], y, height, rectToGene);
		}
		return geneMid;
	}
        
        private double drawGene (Gene gene, double y, double height,
                Map<Rectangle2D.Double, Gene> rectToGene) {
		double x = this.transX(gene.start);
		double width = this.transX(gene.end) - x;
                Rectangle2D.Double rect = new Rectangle2D.Double(x, y, width, height);
		this.g2.draw(rect);
		this.g2.fill(rect);
                if (rectToGene != null) {
                    rectToGene.put(rect, gene);
                }
		return x + width / 2;
	}

    /**
     * Draw gene labels. See:
     * <PRE>               gene label
     *                 |
     *          -------|-------------------------  (chromosome and genes)
     * 
     * </PRE>
     * @param geneList the gene list, which needs to be sorted according to their start points in ascendent order
     * @param labRange the horizontal range of the labels
     * @param geneY y location of genes
     * @param labelY y location of labels
     * @param theta the angle to rotate the labels
     */
	public void drawGeneLabels (Gene[] geneList, double labRange, double geneY, double labelY, double theta) {
                double geneLoc[] = new double[geneList.length];
                double geneMid[] = new double[geneList.length]; // Only used to draw likage lines but not to determine the order
		for (int i = 0; i < geneLoc.length; i++) {
			geneLoc[i] = this.transX(geneList[i].start);
                        geneMid[i] = this.transX((geneList[i].start + geneList[i].end)/2);
		}
		
                // labDist is the minimum distance between two neigboring labels
                double maxDist = labRange / geneList.length;
		Font font = this.g2.getFont();
		FontMetrics metrix = this.g2.getFontMetrics(font);
		double labDist = metrix.getHeight() < maxDist? metrix.getHeight() : maxDist;
                
                // Adjust the size of the font to make it suitable for labDist
                Font newFont = new Font(font.getFamily(), font.getStyle(), (int)(font.getSize() * labDist / metrix.getHeight()));
                this.g2.setFont(newFont);
                
                // labLoc is the x-locations of labels, which are caculated by linear programming.
		double labLoc[] = LPAlg.getLabLoc(geneLoc, labDist);
		if (labLoc == null)
			return;

		for (int i = 0; i < geneLoc.length; i++) {
			// draw linkage line
                        double loc1 = geneMid[i];
			double loc2 = labLoc[i];
			this.g2.draw(new Line2D.Double(loc1, geneY, loc2, labelY));
			
                        // draw the labels by rotating an angle
                        this.g2.rotate(theta, loc2, labelY);
                        this.sRendering.leftDrawString(this.getGeneLabel(geneList[i]), loc2, labelY);
                        this.g2.rotate(2* Math.PI - theta, loc2, labelY);
		}
                
                this.g2.setFont(font);
	}
	
	private String getGeneLabel (Gene gene) {
		return "(" + gene.orient + gene.rank + ")" + gene.symbol;
	}
}
