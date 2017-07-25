package org.biosino.CHS.image;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

import org.biosino.CHS.ontology.*;

/**
 * This class is used to render a <CODE>CHS</CODE> object, 
 * which contains two <CODE>GenoLocRendering</CODE> objects.
 */
public class CHSRendering implements Rendering{
    /**
     * A <CODE>Gaphics2D</CODE> object representing the graphic device of the image.
     */
	private Graphics2D g2;
        
    /**
     * The <CODE>CHS</CODE> object to be rendered.
     */
        public CHS iCHS;
    /**
     * The gene list belonging to the CHS. 
     * (see {@link getGeneList} in class {@link PostProcessor}).
     */
        private Gene[][] geneLists;
        
    /**
     * The <CODE>GenoLocRendering</CODE> object for GenoLocA.
     */
	private GenoLocRendering genoLocA;
    
    /**
     * The <CODE>GenoLocRendering</CODE> object for GenoLocB.
     */
	private GenoLocRendering genoLocB;
    
    /**
     * A map from a rectangular to the gene it represents.
     */   
        private Map<Rectangle2D.Double, Gene> rectToGene;
	
    /**
     * Create a new <CODE>CHSRendering</CODE> object for the given CHS and carry on all drawings.
     * @param iCHS the <CODE>CHS</CODE> object to be rendered
     * @param geneLists the gene list belonging to the CHS (see {@link getGeneList} in class {@link PostProcessor})
     */
	public CHSRendering (CHS iCHS, Gene[][] geneLists) {
		this.iCHS = iCHS;
		this.geneLists = geneLists; 
                this.rectToGene = new HashMap<Rectangle2D.Double, Gene>();
	}
        
    /**
     * Render the object. Note all parameters for drawing could be set in the method.
     * They are specified with the size of image to be 1000 * 600. 
     * @param g2 a <CODE>Graphics2D</CODE> object
     */
        public void render (Graphics2D g2) {
                this.g2 = g2;
                
                // draw background
		this.g2.setColor(Color.white);
		this.g2.fillRect(0, 0, 1000, 600);
		
		this.createGenoLocRendering(600, 500);
		
		// draw chromosomes and their scales and labels
                this.g2.setColor(Color.black);
		this.drawChrs(250, 350, 20);
		this.drawScales(100, 500, 4, -15, 2);
		
                // draw all genes
		this.g2.setColor(Color.gray);
		this.drawGenes(this.geneLists[0], this.geneLists[1], 240, 340, 20);
		
		// draw matched genes and their labels
                this.g2.setColor(Color.blue);
		this.drawMatchedGenes(this.geneLists[2], this.geneLists[3], 240, 340, 20);
		this.g2.setColor(Color.black);
		this.drawGeneLabels(this.geneLists[2], this.geneLists[3], 900, 240, 360, 220, 380);
        }
	
    /**
     * Create two <CODE>GenoLocRendering</CODE> objects for CHS.
     * @param chrRange the length of the longest GenoLoc in the image
     * @param chrMid the middle point of the GenoLoc in the image
     */
	private void createGenoLocRendering (double chrRange, double chrMid) {
		GenoLoc locA = this.iCHS.locA;
		GenoLoc locB = this.iCHS.locB;
		int lengthA = locA.end - locA.start;
		int lengthB = locB.end - locB.start;
		double scale = lengthA > lengthB ? lengthA : lengthB;
		scale = chrRange / scale;
		this.genoLocA = new GenoLocRendering(locA, g2, scale, chrMid);
		this.genoLocB = new GenoLocRendering(locB, g2, scale, chrMid);
	}
	
    /**
     * Draw the two chromosomes.
     * @param yA y location for GenoLoc A
     * @param yB y location for GenoLoc B
     * @param labelDist  the x distance between chromosome lines and labels
     */
	private void drawChrs (double yA, double yB, double labelDist) {
		this.genoLocA.drawChr(yA, labelDist);
		this.genoLocB.drawChr(yB, labelDist);
	}

    /**
     * Draw the two scales.
     * @param yA y location for scale A
     * @param yB y location for scale B
     * @param labelDistA y distance between scale A and its labels
     * @param labelDistB y distance between scale B and its labels
     * @param verticalLength the length of vertical lines on the scale ruler
     */
	private void drawScales (double yA, double yB, double labelDistA, 
			double labelDistB, double verticalLength) {
		this.genoLocA.drawScale(yA, labelDistA, verticalLength);
		this.genoLocB.drawScale(yB, labelDistB, verticalLength);
	}

    /**
     * Draw all genes for the CHS.
     * @param listA the gene list for GenoLoc A
     * @param listB the gene list for GenoLoc B
     * @param yA the y location of gene list A in the image
     * @param yB the y location of gene list B in the image
     * @param height the height of the genes in the image
     */
	private void drawGenes (Gene[] listA, Gene[] listB, double yA, double yB, double height) {
		this.genoLocA.drawGenes(listA, yA, height, null);
		this.genoLocB.drawGenes(listB, yB, height, null);
	}

    /**
     * Draw matched genes and their linking lines for the CHS.
     * @param listA the matched gene list for GenoLoc A
     * @param listB the matched gene list for GenoLoc B
     * @param yA the y location of gene list A in the image
     * @param yB the y location of gene list B in the image
     * @param height the height of the genes in the image
     */
	private void drawMatchedGenes (Gene[] listA, Gene[] listB, double yA, double yB, double height) {
		// draw matched genes
                double[] geneXA = this.genoLocA.drawGenes(listA, yA, height, this.rectToGene);
		double[] geneXB = this.genoLocB.drawGenes(listB, yB, height, this.rectToGene);
		// draw linking lines
                double geneYA = yA + height;
		double geneYB = yB;
		if (geneXA.length != geneXB.length)  //not matched genes
			return;
		for(int i = 0; i < geneXA.length; i++) {
			g2.draw(new Line2D.Double(geneXA[i], geneYA, geneXB[i], geneYB));
		}
	}
	
    /**
     * Draw labels for matched genes.
     * @param listA the matched gene list for GenoLoc A
     * @param listB the matched gene list for GenoLoc B
     * @param labRange the horizontal range of labels 
     * @param geneYA y location of genes for GenoLoc A
     * @param geneYB y location of genes for GenoLoc B
     * @param labelYA y location of labels for GenoLoc A
     * @param labelYB y location of labels for GenoLoc B
     */
	private void drawGeneLabels (Gene[] listA, Gene[] listB, double labRange,
			double geneYA, double geneYB, double labelYA, double labelYB) {
		this.genoLocA.drawGeneLabels(this.prepareDrawGeneLabels(listA), labRange,
				geneYA, labelYA, Math.PI * 1.5);
		this.genoLocB.drawGeneLabels(this.prepareDrawGeneLabels(listB), labRange,
				geneYB, labelYB, Math.PI * 0.5);
	}

	private Gene[] prepareDrawGeneLabels (Gene[] geneList) {
		// use a new gene list to sort the genes and avoid permuting the original matched genes
                ArrayList<Gene> temp = new ArrayList<Gene>(Arrays.asList(geneList));
		Collections.sort(temp);
                // remove redundancy in the list
		for (int i = 1; i < temp.size(); i++) {
			if (temp.get(i) == temp.get(i - 1)) {
				temp.remove(i);
				i --;
			}
		}
		Gene[] newGeneList = new Gene[temp.size()];
		temp.toArray(newGeneList);
		return newGeneList;
	}
    
    /**
     * Get the gene a point located in. The method is used for user interaction from the graphics.
     * @param p a point
     * @return the gene the point located in
     */
        public Gene getGeneClicked (Point2D p) {
                Set<Rectangle2D.Double> rectSet = this.rectToGene.keySet();
                for (Rectangle2D.Double rect : rectSet) {
                    if (rect.contains(p)) {
                        return this.rectToGene.get(rect);
                    }
                }
                return null;
        }
        
     /**
     * Mark matched genes a point located in by red color. The method is used for user interaction from the graphics.
     * @param p a point
     */       
        public void drawGeneClicked (Point2D p) {
                Gene gene = this.getGeneClicked(p);
                
                // One gene may match many genes, so a list is needed
                ArrayList<Gene> listA = new ArrayList<Gene>();
                ArrayList<Gene> listB = new ArrayList<Gene>();
                Gene[] arrayA = null, arrayB = null;
                if (gene == null) {
                    return;
                }
                
                this.g2.setColor(Color.red);               
                
                // The gene may be in genoLocA
                for (int i = 0; i < this.geneLists[2].length; i++) {
                    if (gene == this.geneLists[2][i]) {
                        listA.add(this.geneLists[2][i]);
                        listB.add(this.geneLists[3][i]);
                    }
                }
                if (listA.size() != 0) {
                    arrayA = new Gene[listA.size()];
                    arrayB = new Gene[listB.size()];
                    this.drawMatchedGenes(listA.toArray(arrayA), listB.toArray(arrayB), 240, 340, 20);
                } else {    // or in genoLocB
                    for (int i = 0; i < this.geneLists[3].length; i++) {
                        if (gene == this.geneLists[3][i]) {
                            listA.add(this.geneLists[3][i]);
                            listB.add(this.geneLists[2][i]);
                        }
                    }
                    if (listA.size() != 0) {
                        arrayA = new Gene[listA.size()];
                        arrayB = new Gene[listB.size()];
                        this.drawMatchedGenes(listB.toArray(arrayB), listA.toArray(arrayA), 240, 340, 20);
                    }
                }
        }
}

