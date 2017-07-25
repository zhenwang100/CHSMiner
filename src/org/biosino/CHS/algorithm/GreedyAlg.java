package org.biosino.CHS.algorithm;

import java.util.*;

import org.biosino.CHS.ontology.*;

/**
 * This class uses greedy algorithm to detect CHS between two chromosomes (gene lists).
 */
public class GreedyAlg {
    /**
     * Minimum size of each CHS (number of distinct families).
     */
	private int sm;
    /**
     * Maximum gap size(bp) allowed between neighboring genes linked in a CHS in a gene list.
     */
	private int gapSize;
	
    /**
     * Chromosome A.
     */
	private Chromosome chrA;
    /**
     * Chromosome B.
     */
	private Chromosome chrB;
	
    /**
     * Gene list A located on chromosome A.
     */
	private List<Gene> geneListA;
    /**
     * Gene list B located on chromosome B.
     */
	private List<Gene> geneListB;

    /**
     * CHS list containing all CHS between the two chromosomes.
     */
	private List<CHS> CHSList;
	
    /**
     * The map between family id to genes, which is used to speeding up querying.
     */
	private Map<String, Set<Gene>> famMapB;
	
    /**
     * Create a new GreedyAlg object.
     * @param num minimum size of each CHS (number of distinct families).
     * @param gapSize maximum gap size between two neighboring genes linked in a CHS
     */
	public GreedyAlg (int num, int gapSize) {
            this.sm = num;
            this.gapSize = gapSize;
        }
        
    /**
     * Search all CHS for two chromosomes using greedy algorithm.
     * The two chromosomes will be searched by exchange with each other and then redundancy is removed.
     * @param chrA chromosome A
     * @param chrB chromosome B
     * @param geneListA gene list A for chromosome A
     * @param geneListB gene list B for chromosome B
     * @return a CHS list containing all CHS
     */
	public List<CHS> search (Chromosome chrA, Chromosome chrB, 
			List<Gene> geneListA, List<Gene> geneListB) {
		
		this.CHSList = new ArrayList<CHS>();
                
                // Search by exchange A and B
		this._search(chrA, chrB, geneListA, geneListB);
		this._search(chrB, chrA, geneListB, geneListA);
		
                // Remove redundancy
		this.removeRedund();
		return this.CHSList;
	}
	
    /**
     * Search two chromosomes by exchange A and B.
     * @param chrA chromosome A
     * @param chrB chromosome B
     * @param geneListA gene list A
     * @param geneListB gene list B
     */
	private void _search (Chromosome chrA, Chromosome chrB,
			List<Gene> geneListA, List<Gene> geneListB) {
		this.chrA = chrA;
		this.chrB = chrB;
		this.geneListA = geneListA;
		this.geneListB = geneListB;
		
		this.famMapB = new HashMap<String, Set<Gene>>();
		for (Gene gene : geneListB) {
			if(this.famMapB.get(gene.family) == null) {
				this.famMapB.put(gene.family, new HashSet<Gene>());
			}
			this.famMapB.get(gene.family).add(gene);
		}
		
		for (Gene geneA : geneListA) {
			this.startsWith(geneA);
		}
	}

    /**
     * Start to search for a CHS from a given gene in gene list A.
     * @param geneA the given gene
     */
	private void startsWith (Gene geneA) {
	
		String famA = geneA.family;
		int indexA = this.geneListA.indexOf(geneA);
		
		if (this.famMapB.get(famA) != null)
			for(Gene geneB : this.famMapB.get(famA)) {
				if (!geneA.id.equals(geneB.id)) {
					Set<String> famSet = new HashSet<String>();
					CHS tempCHS = new CHS(this.chrA, this.chrB, 
                                                geneA.toGenoLoc(), geneB.toGenoLoc());
					famSet.add(famA);
					this.extend(indexA, tempCHS, famSet);
				}
			}
	}

    /**
     * Search by extending a current CHS.
     * @param indexA current index in gene list A
     * @param tempCHS current CHS
     * @param famSet current families contained in <CODE>tempCHS</CODE>
     */
	private void extend (int indexA, CHS tempCHS, Set<String> famSet) {
		while (true) {
			indexA ++;
			if (indexA >= this.geneListA.size()) {
				if (famSet.size() >= this.sm && 
						!tempCHS.overlap(this.gapSize)) {
					tempCHS.sortLocs();
					this.CHSList.add(tempCHS);
				}
				break;
			}
	
			Gene nextA = this.geneListA.get(indexA);
			if (tempCHS.locA.getDist(nextA) > this.gapSize) {
				if (famSet.size() >= this.sm && 
						!tempCHS.overlap(this.gapSize)) {
					tempCHS.sortLocs();
					this.CHSList.add(tempCHS);
				}
				break;
			}

			String nextFamA = nextA.family;
			if (this.famMapB.get(nextFamA) != null) {
				for (Gene nextB : this.famMapB.get(nextFamA)) {
					if (nextA.id.equals(nextB.id))
						continue;
					if (nextB.getDist(tempCHS.locB) > this.gapSize)
						continue;
					famSet.add(nextFamA);
					tempCHS = tempCHS.merge(new CHS(this.chrA, this.chrB, nextA, nextB));
				}
			}
		}
	}
	
	/*
	 *  The resursive version of greedy algorithm
	 *  This function will give the same result as the above iterative one
	 *  So it has not been used
	 *
	private void extend (int indexA1, CHS tempCHS, Set<Integer> famSet) {
		int indexA2 = indexA1 + 1;
		if (indexA2 >= this.geneListA.size()) {
			if (famSet.size() >= this.sm && !tempCHS.overlap(this.gapSize)) {
				tempCHS.sortLocs();
				this.CHSList.add(tempCHS);
			}
			return;
		}
	
		Gene geneA2 = this.geneListA.get(indexA2);
		if (tempCHS.locA.getDist(geneA2) > this.gapSize) {
			if (famSet.size() >= this.sm && !tempCHS.overlap(this.gapSize)) {
				tempCHS.sortLocs();
				this.CHSList.add(tempCHS);
			}
			return;
		}

		int famA2 = geneA2.family;
		if (this.famMapB.get(famA2) != null) {
			for (Gene geneB2 : this.famMapB.get(famA2)) {
				if (geneA2.id == geneB2.id)
					continue;
				if (geneB2.getDist(tempCHS.locB) > this.gapSize)
					continue;
				famSet.add(famA2);
				tempCHS = tempCHS.merge(new CHS(geneA2, geneB2));
			}
		}

		this.extend(indexA2, tempCHS, famSet);
	}
	*/

	
	// the remove redundancy algorithm may not be perfect
    /**
     * Remove redundancy in CHS list. Two CHS are merged if they can.
     */
	private void removeRedund () {
		Collections.sort(this.CHSList);
		for (int i = 1; i < this.CHSList.size(); i++) {
			CHS iCHS = this.CHSList.get(i);
			for (int j = i - 1; j >= 0; j --) {
				CHS lastCHS = this.CHSList.get(j);
				if (lastCHS.mergable(iCHS, this.gapSize)) {
					lastCHS = lastCHS.merge(iCHS);
					this.CHSList.remove(iCHS);
					i --;
					break;
				}
			}
		}
	}
}

