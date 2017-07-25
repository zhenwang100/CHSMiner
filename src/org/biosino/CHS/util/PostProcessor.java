package org.biosino.CHS.util;

import java.util.*;

import org.biosino.CHS.ontology.*;

/**
 * This class is responsible for postprocess for resulted CHS.
 */
public class PostProcessor {
    
    /**
     * The map from chromosomes to ranked gene lists. Tandem repeated genes
     * are merged if <CODE>mergeTandem</CODE> is <CODE>true</CODE>.
     */
	private Map<Chromosome, List<Gene>> rankedChrMap;   // used for getting gene list for a CHS
    
    /**
     * The same as <CODE>rankedChrMap</CODE>, except that the genes with <CODE>family</CODE> id equivalent
     * to <CODE>nullFamID</CODE> are deleted from the map.
     */
        private Map<Chromosome, List<Gene>> filteredChrMap; // used for getting gene list for a CHS
    
    /**
     * Indicating whether the tandem repeated genes in <CODE>rankedChrMap</CODE> are merged.
     */
        private boolean mergeTandem;
    
    /**
     * The <CODE>family</CODE> id indicating a gene do not belong to any family (named null family).
     */
	private String nullFamID;
    
    /**
     * The maximum gap (gene number) allowed between neighboring genes linked in a CHS.
     */
        private int gapNum; // used for caculating p-value
        
    /**
     * The map from taxonomy name to total gene number.
     */
        private Map<String, Integer> geneNumMap;    // used for caculating p-value
        
    /**
     * The map from taxonomy name to <CODE>family</CODE> id to the number of genes belonging to the taxonomy and family,
     */
        private Map<String, Map<String, Integer>> famNumMap;   //  used for caculating p-value
	
    /**
     * The list containing all resulted CHS.
     */
        public List<CHS> CHSList;
    
    /**
     * The map organized as <chr1>--<chr2>--<CHS1, CHS2, ...>.
     */  
        public Map<Chromosome, Map<Chromosome, Set<CHS>>> CHSMap;
		
    /**
     * Create a new <CODE>PostProcessor</CODE> object and calculate statistics for each CHS.
     * @param prePro the <CODE>PreProcessor</CODE> object for corresponding CHS searching
     * @param CHSList a list containing all searched CHS
     * @param gapNum the maximum gap (gene number) allowed between neighboring genes
     */
	public PostProcessor (PreProcessor prePro, List<CHS> CHSList, int gapNum) {
		this.rankedChrMap = prePro.rankedChrMap;
		this.filteredChrMap = prePro.filteredChrMap;
		this.famNumMap = prePro.famNumMap;
		this.geneNumMap = prePro.geneNumMap;
		this.mergeTandem = prePro.mergeTandem;
		this.nullFamID = prePro.nullFamID;
		this.CHSList = CHSList;
                this.gapNum = gapNum;
		
		// construct CHSMap
		this.CHSMap = new HashMap<Chromosome, Map<Chromosome, Set<CHS>>>();
		for (CHS iCHS : this.CHSList) {
			if (!this.CHSMap.containsKey(iCHS.chrA))
				this.CHSMap.put(iCHS.chrA, new HashMap<Chromosome, Set<CHS>>());
			if (!this.CHSMap.get(iCHS.chrA).containsKey(iCHS.chrB))
				this.CHSMap.get(iCHS.chrA).put(iCHS.chrB, new HashSet<CHS>());
			this.CHSMap.get(iCHS.chrA).get(iCHS.chrB).add(iCHS);
			//
			if (!this.CHSMap.containsKey(iCHS.chrB))
				this.CHSMap.put(iCHS.chrB, new HashMap<Chromosome, Set<CHS>>());
			if (!this.CHSMap.get(iCHS.chrB).containsKey(iCHS.chrA))
				this.CHSMap.get(iCHS.chrB).put(iCHS.chrA, new HashSet<CHS>());
			this.CHSMap.get(iCHS.chrB).get(iCHS.chrA).add(iCHS);
		}

		// calculate size and p-value for each CHS
		for (CHS iCHS : this.CHSList) {
			this.setStat(iCHS);
		}
	}
	
    /**
     * Get gene lists linked in the CHS.
     * @param iCHS a CHS object
     * @return a two dimensional array with four rows. 
     * The first row contains all genes in GenoLoc A and the second row in GenoLoc B;
     * the third row contains matched genes in GenoLoc A and the fourth row in genoLoc B;
     * the genes in the third and fourth rows are matched.
     */
	public Gene[][] getGeneList (CHS iCHS) {
		List<Gene> listA = rankedChrMap.get(iCHS.chrA);
		List<Gene> listB = rankedChrMap.get(iCHS.chrB);
		
		if (listA == null || listB == null) {
			return null;
		}
		int indexA = Arrays.binarySearch(listA.toArray(), iCHS.locA);
		int indexB = Arrays.binarySearch(listB.toArray(), iCHS.locB);
		if (indexA < 0 || indexB <0) {
			return null;
		}
		
		// get all genes
		List<Gene> iListA = new ArrayList<Gene>();
		List<Gene> iListB = new ArrayList<Gene>();
		while (true) {
			Gene geneA = listA.get(indexA);
			if (geneA.start >= iCHS.locA.end)
				break;
			if (geneA.end <= iCHS.locA.end) {
				iListA.add(geneA);
			}
			indexA ++;
			if (indexA >= listA.size())
				break;
		}
		while (true) {
			Gene geneB = listB.get(indexB);
			if (geneB.start >= iCHS.locB.end)
				break;
			if (geneB.end <= iCHS.locB.end) {
				iListB.add(geneB);
			}
			indexB ++;
			if (indexB >= listB.size())
				break;
		}

		// get matched genes 
		List<Gene> matchedListA = new ArrayList<Gene>();
		List<Gene> matchedListB = new ArrayList<Gene>();
		Map<String, Set<Gene>> famMapB = new HashMap<String, Set<Gene>>();
		for (Gene geneB : iListB) {
			if (geneB.family.equals(this.nullFamID))
				continue;
			if (famMapB.get(geneB.family) == null) {
				famMapB.put(geneB.family, new HashSet<Gene>());
			}
			famMapB.get(geneB.family).add(geneB);
		}
		for (Gene geneA : iListA) {
			if (famMapB.get(geneA.family) != null) {
				for (Gene geneB : famMapB.get(geneA.family)) {
					matchedListA.add(geneA);
					matchedListB.add(geneB);
				}
			}
		}
		
		Gene[][] returnedList = new Gene[4][];
		returnedList[0] = new Gene[iListA.size()];
		returnedList[1] = new Gene[iListB.size()];
		returnedList[2] = new Gene[matchedListA.size()];
		returnedList[3] = new Gene[matchedListB.size()];

                //all genes lists
                iListA.toArray(returnedList[0]);
                iListB.toArray(returnedList[1]);
                //matched genes lists
                matchedListA.toArray(returnedList[2]);
                matchedListB.toArray(returnedList[3]);
		return returnedList;
	}
        
    /**
     * Caculate statistics (size and p-value) for a CHS.
     * @param iCHS a CHS object
     */
        private void setStat (CHS iCHS) {
		// calculate size
		Gene[] genes = (this.getGeneList(iCHS))[2];
		Set<String> famSet = new HashSet<String>();
		for (Gene gene : genes) {
			famSet.add(gene.family);
		}
		int size = famSet.size();
                iCHS.size = size;
		
                // calculate p-value
                double p = this.getProb(iCHS.chrA.tax, size, famSet) *
                        this.getProb(iCHS.chrB.tax, size, famSet);
		iCHS.pValue = p;
	}
        
    /**
     * Caculate the exact probability for a cluster with a given size 
     * to randomly distribute in the genome.
     * @param taxName taxonomy name of the genome
     * @param size size of the cluster (number of marked genes)
     * @param famSet a set contains all <CODE>family</CODE> id in the cluster
     * @return the exact probability
     */
        private double getProb (String taxName, int size, Set<String> famSet) {
                int n = 0;  // size of the genome
                int k = size;   // size of the cluster
		int d = this.gapNum;    // maximum gap size
                for (String tax : this.geneNumMap.keySet()) {
                    if (tax.equals(taxName)) {
                        n = this.geneNumMap.get(tax);
                    }
                }
                
                double p = Math.log(n - k + 1 - (k - 1) * d / 2) + (k - 1) * Math.log(d + 1) 
                    - this.LogCombination(n, k);
                
                // correct the probability when a family contains more than one gene
                Map<String, Integer> taxFamNum = null;
                for (String tax : this.famNumMap.keySet()) {
                    if (tax.equals(taxName)) {
                        taxFamNum = this.famNumMap.get(tax);
                    }
                }
                for (String famID : famSet) {
			p += Math.log(taxFamNum.get(famID));
		}
                
		p = Math.exp(p);
		if (p > 1) p = 1;
                return p;
        }
        
	private double LogCombination (int n, int k) {
		double rst = 0;
		for (int i = 0; i < k; i++) {
			rst += Math.log(n - i) - Math.log(k - i);
		}
		return rst;
        }
}

