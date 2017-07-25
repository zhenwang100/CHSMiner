package org.biosino.CHS.util;

import java.util.*;

import org.biosino.CHS.ontology.*;

/**
 * This class is responsible for preprocess for genes.
 */
public class PreProcessor {

    /**
     * The map from chromosomes to ranked gene lists. Tandem repeated genes
     * are merged if <CODE>mergeTandem</CODE> is <CODE>true</CODE>.
     */
	public Map<Chromosome, List<Gene>> rankedChrMap; 
    /**
     * The same as <CODE>rankedChrMap</CODE>, except that the genes with <CODE>family</CODE> id equivalent
     * to <CODE>nullFamID</CODE> are deleted from the map.
     */
	public Map<Chromosome, List<Gene>> filteredChrMap;
    /**
     * The map from taxonomy name to <CODE>family</CODE> id to the number of genes belonging to the taxonomy and family.
     */
	public Map<String, Map<String, Integer>> famNumMap;
    /**
     * The map from taxonomy name to total gene number.
     */
	public Map<String, Integer> geneNumMap;
    /**
     * Average gene length.
     */
        public int geneMeanLen;
	
    /**
     * Indicating whether the tandem repeated genes in <CODE>rankedChrMap</CODE> are merged.
     */
	public boolean mergeTandem;
    /**
     * The <CODE>family</CODE> id indicating a gene do not belong to any family (named null family).
     */
	public String nullFamID;
	
    /**
     * Create a new <CODE>PreProcessor</CODE> object and carry on all necessary preprocesses.
     * @param chrMap original chromosome map with genes not ranked
     * @param mergeTandem indicating whether tandem repeated genes should be merged
     * @param nullFamID null family id
     */
	public PreProcessor (Map<Chromosome, List<Gene>> chrMap,
			boolean mergeTandem, String nullFamID) {

		this.mergeTandem = mergeTandem;
		this.nullFamID = nullFamID;
		
                // Set ranks for each gene list and merge tandem repeated genes.
		Set<Chromosome> chrSet = chrMap.keySet();
		for (Chromosome chr : chrSet) {
			List<Gene> genes = chrMap.get(chr);
			this.setRanks(genes);
		}
		this.rankedChrMap = chrMap;
                
                // Other preprocesses based on ranked gene list.
		this.filteredChrMap = new HashMap<Chromosome, List<Gene>>();
                this.geneNumMap = new HashMap<String, Integer>();
                
		for (Chromosome chr : chrSet) {
			
                        // Filter null family.
                        List<Gene> genes = this.rankedChrMap.get(chr);
                        this.filteredChrMap.put(chr, this.filterNullFam(genes));
			
                        // For each taxonomy, caculate total gene number
                        this.countGenes(chr, genes);
                        
                        // the length of a chromosome
                        chr.length = genes.get(genes.size() - 1).end; 		
		}       
                
                // Count the number of genes belonging to each taxonomy and family.
		this.famNumMap = new HashMap<String, Map<String, Integer>>();
		for (Chromosome chr : chrSet) {
			List<Gene> genes = this.filteredChrMap.get(chr);
                        this.countFamNum(chr, genes);
		}
                this.setGeneMeanLen();
	}
	
    /**
     * Set ranks for a gene list. If <CODE>mergeTandm</CODE> is true,
     * tandem repeated genes are merged.
     * @param genes a gene list not ranked
     */
	private void setRanks (List<Gene> genes) {
		Collections.sort(genes);
		
                // If tandem repeated genes are merged, the ranks should be modified. 
		if (!this.mergeTandem) {
			for (int i = 0; i < genes.size(); i++) {
				genes.get(i).rank = i + 1;
			}
		} else {
			Gene lastGene = null;
			for (int i = 0; i < genes.size(); i ++) {
				Gene gene = genes.get(i);
				if (lastGene == null) {
					gene.rank = 1;
					lastGene = gene;
				} else if (gene.family.equals(this.nullFamID) ||
						!gene.family.equals(lastGene.family)) {
					gene.rank = lastGene.rank + 1;
					lastGene = gene;
				} else {
					lastGene.end = gene.end > lastGene.end ? 
						gene.end : lastGene.end;
					if (!lastGene.symbol.endsWith("_cluster"))
						lastGene.symbol += "_cluster";
					genes.remove(i);
					i --;
				}
			}
		}
	}

    /**
     * Create a new gene list with genes with null family deleted.
     * @param genes a ranked gene list
     * @return a gene list with null family deleted
     */
	private List<Gene> filterNullFam (List<Gene> genes) {
		List<Gene> newGenes = new ArrayList<Gene>();
		for (Gene gene : genes) {
			if (!gene.family.equals(this.nullFamID)) {
				newGenes.add(gene);
			}
		}
		return newGenes;
	}
    
    /**
     * For each taxonomy, caculate total gene number.
     * @param chr chromosome of the gene list.
     * @param genes a gene list
     */
        private void countGenes (Chromosome chr, List<Gene> genes) {
                int n = genes.size();
                for (String tax : this.geneNumMap.keySet()) {
                        if (tax.equals(chr.tax)) {
                            n += this.geneNumMap.get(tax);
                            this.geneNumMap.put(tax, n);
                            return;
                        }
                }
                this.geneNumMap.put(chr.tax, n);
        }

    /**
     * Set the averge gene length.
     */
        private void setGeneMeanLen () {
                long geneTotalLen = 0;
                int geneTotalNum = 0;
                
                // Caculate average gene length for all taxonomy and genes.
                Set<Chromosome> chrSet = this.rankedChrMap.keySet();
                for (Chromosome chr : chrSet) {
                    List<Gene> genes = this.rankedChrMap.get(chr); // May be filteredChrMap for 2R
                    geneTotalNum += genes.size();
                    for (Gene gene : genes) {
                        geneTotalLen += gene.end - gene.start + 1;
                    }
                    
                    //May be a better way in next version, but the length will be too long (and for 2R is tricky) 
                    //geneTotalLen += chr.length; 
                }
                this.geneMeanLen = (int)(geneTotalLen / geneTotalNum);
        }
        
    /**
     * Count the the number of genes belonging to each taxonomy and family.
     * @param genes a gene list
     */
	private void countFamNum (Chromosome chr, List<Gene> genes) {
		Map<String, Integer> temp = null;
                for (String tax : this.famNumMap.keySet()) {
                        if (tax.equals(chr.tax)) {
                            temp = this.famNumMap.get(tax);
                            break;
                         }
                }
                if (temp == null) {
                        temp = new HashMap<String, Integer>();
                        this.famNumMap.put(chr.tax, temp);
                }
                for (Gene gene : genes) {
                        String famID = gene.family;
                        if (temp.containsKey(famID)) {
                            temp.put(famID, temp.get(famID) + 1);
                        } else {
                            temp.put(famID, 1);
                        }
                }
        }
}

