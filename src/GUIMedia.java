import java.util.*;
import java.awt.geom.*;
import javax.swing.*;

import org.w3c.dom.svg.*;

import org.biosino.CHS.ontology.*;
import org.biosino.CHS.util.*;
import org.biosino.CHS.algorithm.*;
import org.biosino.CHS.image.*;

/**
 * The class is responsible for data communication between GUI and core algorithm.
 */
public class GUIMedia {
    
    public Map<Chromosome, List<Gene>> chrMap;  // the map from chromosome to gene list
    public List<Chromosome> chrList; // chromosome list
    public boolean intraOrg;    // indicating whether comparison within a Species
    public PreProcessor iPrePro; // PreProcessor object for CHS search
    public List<CHS> CHSList; // resulted CHS list
    public PostProcessor iPostPro; // PostProcessor object for CHS
    public Ensembl iEnsembl;    // Object to manage Ensembl database
    public List<SVGWrapper> wrapperList; // SVGWrapper list, each contains a Rendering object
    public int index; // Current index in wrapperList 
    public boolean CHSRunning; // indicating whether the CHS search thread should be running
    
    /** Construct method */
    public GUIMedia () {
        this.wrapperList = new ArrayList<SVGWrapper>();
    }
    
    /** Read gene data from file and throw input file exception */
    public void readGeneFile(String fileName) throws Exception {    
        this.chrMap = IO.readGeneFile(fileName);
        this.setChrList();
        
    }
    
    /** Set chromosome list */
    private void setChrList () {
        this.chrList = new ArrayList<Chromosome>(this.chrMap.keySet());
        Collections.sort(this.chrList);
    }
    
    /** Fetch gene from Ensembl database */
    public Set<String> getEnsemblTaxons () {
        this.iEnsembl = new Ensembl();
        return iEnsembl.taxonMap.keySet();
    }
    
    /** Fetch gene from Ensembl database */
    public void getEnsemblGene (String[] taxons) throws Exception {
        this.chrMap = iEnsembl.getEnsemblGene(taxons);
        this.setChrList();
    }
    
    /** Return all genes as a table */
    public List<Object[]> getGeneTable() {
        List<Object[]> geneTable = new ArrayList<Object[]>();
        for (Chromosome chr : this.chrList) {
		List<Gene> geneList = this.chrMap.get(chr);
                for (Gene gene : geneList) {
                    Object[] row = gene.toArray();
                    geneTable.add(row);
                }
        }
        return geneTable;
    }
    
    /** Carry on preprocess for CHS search */
    public void preProcess(List<Integer> indexList, boolean mergeTandem,
            String nullFamID) {
        // filter chromosomes indexed in indexList
        //List<Chromosome> chrList = new ArrayList<Chromosome>(this.chrSet);
        List<Chromosome> filteredChrs = new ArrayList<Chromosome>();
        for (int index : indexList) {
            if (index < this.chrList.size()) {
                filteredChrs.add(this.chrList.get(index));
            }
        }
        this.chrList.removeAll(filteredChrs);
        
        // carry on preprocess
        this.iPrePro = new PreProcessor(this.chrMap, mergeTandem, nullFamID);
        this.chrMap = this.iPrePro.rankedChrMap;
    }
    
    /** Generate another thread to carry on CHS finding */
    public void findCHS(int size, int gapNum, JProgressBar bar, JComponent component) {
        this.CHSRunning = true;
        (new findCHSThread(this, size, gapNum * this.iPrePro.geneMeanLen, 
                this.intraOrg, bar, component)).start();
    }
    
    /** Return all CHS results as a table */
    public List<Object[]> getCHSTable() {
        Collections.sort(this.CHSList);     // You may change the sort method here!
        
        List<Object[]> CHSTable = new ArrayList<Object[]>();
        for (CHS iCHS : this.CHSList) {
            Object[] row = iCHS.toArray();
            CHSTable.add(row);
        }
        return CHSTable;
    }
    
    /** Carry on postprocess for resulted CHS */
    public void postProcess (int gapNum) {
        this.iPostPro = new PostProcessor(this.iPrePro, this.CHSList, gapNum); 
    }
    
    /** Write CHS data into file and throw file writing exception */
    public void writeCHSFile (String fileName) throws Exception {
        IO.writeCHSFile(this.iPostPro, fileName);
    }
    
    /** Add a new Wrapper to the list, which contains a new Rendering object */
    private SVGDocument addRendering (Rendering iRendering) {
        SVGWrapper iWrapper = new SVGWrapper();
        iWrapper.Wrap(iRendering);
        SVGDocument doc = iWrapper.getSVGDoc();
        this.wrapperList.add(iWrapper);
        this.index = this.wrapperList.size() - 1;   // Point to the last rendering
        return doc;     
    }
    
    /** Render the indexed CHS in CHSList */ 
    public SVGDocument getCHSRendering (int index) {
        CHS iCHS = this.CHSList.get(index);
        Gene[][] geneList = this.iPostPro.getGeneList(iCHS);
        
        // Render 
        Rendering iRendering = new CHSRendering(iCHS, geneList);
        return this.addRendering(iRendering);    
    }
    
    /** Render the comparison between center chromosome (indicated by index) and its surrounding chromosomes (indicated by indexList) */
    public SVGDocument getChrCHSRendering (int index, List<Integer> indexList, int size, double pValue) {
        //List<Chromosome> chrList = new ArrayList<Chromosome>(this.chrSet);
        Chromosome centerChr = this.chrList.get(index);
        if (!this.iPostPro.CHSMap.containsKey(centerChr)) { //no CHS found associated with the chromosome
            return null;
        } 
        List<Chromosome> surChrs = new ArrayList<Chromosome>();
        for (int i : indexList) {
            surChrs.add(this.chrList.get(i));
        }
        
        Map<Chromosome, Set<CHS>> iCHSMap = new HashMap<Chromosome, Set<CHS>>();
        for (Chromosome chr : surChrs) {
            if (iPostPro.CHSMap.get(centerChr).containsKey(chr)) {
                iCHSMap.put(chr, iPostPro.CHSMap.get(centerChr).get(chr));
            }
        }
        
        // Render
        Rendering iRendering = new ChrCHSRendering(centerChr, iCHSMap, size, pValue);
        return this.addRendering(iRendering);
    }
    
    /** Render the CHS p pointing in ChrCHSRendering or CHSRendering */ 
    public SVGDocument getCHSRendering (Point2D p) {
        if (this.wrapperList.size() == 0) {
            return null;
        }
        Rendering iRendering = this.wrapperList.get(this.index).getRendering();
        String className = iRendering.getClass().getName();
        if (className.equals("org.biosino.CHS.image.ChrCHSRendering")) {
            ChrCHSRendering iChrCHSRendering = (ChrCHSRendering)iRendering;
            CHS iCHS = iChrCHSRendering.getCHSClicked(p); // return null?
            Gene[][] geneList = this.iPostPro.getGeneList(iCHS);
        
            // Re-render
            return this.addRendering(new CHSRendering(iCHS, geneList));
        } else if (className.equals("org.biosino.CHS.image.CHSRendering")){
            CHSRendering iCHSRendering = (CHSRendering)iRendering;
            iCHSRendering.drawGeneClicked(p);
            return this.wrapperList.get(this.index).getSVGDoc();
        } else {
            return null;
        }
    }
    
    /** Indicate if there is a link at position p in ChrCHSRendering or CHSRendering */
    public boolean pointCHS (Point2D p) {
        if (this.wrapperList.size() == 0) {
            return false;
        }
        Rendering iRendering = this.wrapperList.get(this.index).getRendering();
        String className = iRendering.getClass().getName();
        if (className.equals("org.biosino.CHS.image.ChrCHSRendering")) {
            ChrCHSRendering iChrCHSRendering = (ChrCHSRendering)iRendering;
            CHS iCHS = iChrCHSRendering.getCHSClicked(p);
            if (iCHS != null) {
                return true;
            } else {
                return false;
            }
        } else if (className.equals("org.biosino.CHS.image.CHSRendering")) {
            CHSRendering iCHSRendering = (CHSRendering)iRendering;
            Gene iGene = iCHSRendering.getGeneClicked(p);
            if (iGene != null) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /** Save current Rendering */
    public void writeCHSRendering (String format, String fileName) throws Exception {
        SVGWrapper iWrapper = this.wrapperList.get(this.index);
        if (format.equals("ps")) {  //for .ps format
            PsWrapper iPsWrapper = new PsWrapper(fileName);
            iPsWrapper.Wrap(iWrapper.getRendering()); // rewrap current image using .ps wrapper 
        } else if (format.equals("svg")){    //for .svg format
            iWrapper.saveRendering(fileName);
        }
    }
    
    /** Nevigate Rendering objects in Wrapper list, backward and forward */
    public SVGDocument nevigateRendering (int orient) {
        if (this.wrapperList.size() == 0) {
            return null;
        }
        if (orient < 0 && this.index - 1 >= 0) {
            index--;  
        } else if (orient > 0 && this.index + 1 <= this.wrapperList.size() - 1) {
            index++;
        }
        return this.wrapperList.get(index).getSVGDoc();
    }
    
    /** Delete current Rendering objects */
    public SVGDocument deleteRendering () {
        if (this.wrapperList.size() == 0) {
            return null;
        }
        this.wrapperList.remove(index);
        if (this.wrapperList.size() == 0) {
            return null;
        }
        if (index > 0) {
            index--;
        } 
        return this.wrapperList.get(index).getSVGDoc();
    }
    
    /** Judge if local CHS can be shown. This is true only when CHSRendering is shown. */
    public boolean localCHSEnabled () {
        if (this.wrapperList.size() == 0) {
            return false;
        }
        Rendering iRendering = this.wrapperList.get(this.index).getRendering();
        if (!iRendering.getClass().getName().equals("org.biosino.CHS.image.CHSRendering")) {
            return false;
        }
        return true;
    }
    
    /** Get local CHSRendering by indicated gene ranks */
    public SVGDocument getLocalCHS (int startA, int endA, int startB, int endB) {
        if (startA > endA || startB > endB) {
            return null;
        }
        
        CHSRendering iCHSRendering = (CHSRendering)this.wrapperList.get(this.index).getRendering();
        CHS iCHS = iCHSRendering.iCHS;  // current CHS
        Gene[][] geneList = this.iPostPro.getGeneList(iCHS);
        Chromosome chrA = iCHS.chrA, chrB = iCHS.chrB;
        int minStartA = geneList[0][0].rank;
        int maxEndA = geneList[0][geneList[0].length - 1].rank;
        int minStartB = geneList[1][0].rank;
        int maxEndB = geneList[1][geneList[1].length - 1].rank;
        
        // The new CHS must be within the range of the current one
        if (startA < minStartA || endA > maxEndA || startB < minStartB || endB > maxEndB) { 
            return null;
        }
         
        // Create new CHS
        GenoLoc locA = new GenoLoc(chrA.tax, chrA.chr, 
                this.chrMap.get(chrA).get(--startA).start, this.chrMap.get(chrA).get(--endA).end); 
        GenoLoc locB = new GenoLoc(chrB.tax, chrB.chr, 
                this.chrMap.get(chrB).get(--startB).start, this.chrMap.get(chrB).get(--endB).end); 
        CHS newCHS = new CHS(chrA, chrB, locA, locB);
        geneList = this.iPostPro.getGeneList(newCHS);  
        return this.addRendering(new CHSRendering(newCHS, geneList));    
    }
}

/**
 * A seperate thread to carry on CHS finding
 */
class findCHSThread extends Thread {
    private GUIMedia iMedia;
    private int size;
    private int gap;
    private boolean intraOrg;
    private JProgressBar progressBar;
    private JComponent component;
    public findCHSThread(GUIMedia iMedia, int size, int gap, boolean intraOrg, 
            JProgressBar progressBar, JComponent component) {
        super();
        this.setDaemon(true);
        this.iMedia = iMedia;
        this.size = size;
        this.gap = gap;
        this.intraOrg = intraOrg;
        this.progressBar = progressBar;
        this.component = component;
    }
    public void run() {
        (new JoinThread(this, this.component)).start();
        GreedyAlg iAlg = new GreedyAlg(size, gap);
        List<Chromosome> chrList = this.iMedia.chrList;
        iMedia.CHSList = new ArrayList<CHS>();
        
        // Caculate the length of the progress, depending on comparison inter- or intra- species
        int progressLength = 0;
        int currentLength = 0;
	for (int i = 0; i < chrList.size(); i++) {
            for (int j = 0; j <= i; j++) {
                Chromosome chr1 = chrList.get(i);
		Chromosome chr2 = chrList.get(j);
                if ((this.intraOrg && chr1.tax.equals(chr2.tax)) ||
                    (!this.intraOrg && !chr1.tax.equals(chr2.tax))) {
                            progressLength++;
                }
            }
        }
        
        // Carry on searching
        for (int i = 0; i < chrList.size(); i++) {
            for (int j = 0; j <= i; j++) {
                if (!this.iMedia.CHSRunning) {  // indicating the thread should be stopped 
                    break;
                }
                
                Chromosome chr1 = chrList.get(i);
		Chromosome chr2 = chrList.get(j);
                if ((this.intraOrg && chr1.tax.equals(chr2.tax)) ||
                    (!this.intraOrg && !chr1.tax.equals(chr2.tax))) {
                    List<Gene> list1 = iMedia.iPrePro.filteredChrMap.get(chr1);
                    List<Gene> list2 = iMedia.iPrePro.filteredChrMap.get(chr2);
                    iMedia.CHSList.addAll(iAlg.search(chr1, chr2, list1, list2));
                    progressBar.setValue(++currentLength * 100 / progressLength);
                }
            }
	}
    }
}

/**
 *  Monitor the CHS finding thread. If the thread is over, the information is sent to a jComponent.
 */
class JoinThread extends Thread {
    private Thread parentThread;
    private JComponent component;
    public JoinThread(Thread parentThread, JComponent component) {
        super();
        this.setDaemon(true);
        this.parentThread = parentThread;
        this.component = component;
        this.component.setEnabled(false);
    }
    public void run() {
        try {
            this.parentThread.join();
            this.component.setEnabled(true);
        } catch (Exception e) {}
    } 
}
