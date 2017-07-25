import java.io.*;
import java.util.*;
import org.biosino.CHS.ontology.*;
import org.biosino.CHS.util.*;

/**
 * This class is responsible for checking the result of CHSMiner with other data source.
 * Note: Multiple test correction is not implemented here.
 */
public class TestResult {
    public static void main (String[] args) throws Exception {
        // synteny information download directly from ensembl 
        List<CHS> ensemblCHSList = IO.readCHSFile("data\\other\\ensembl_synteny.txt");
        // synteny generated from CHSMiner using the input of ensembl orthologs
        List<CHS> CHSMinerCHSList = IO.readCHSFile("data\\other\\CHSMiner_synteny_1.txt");
        // ortholog download from ensembl
        List<CHS> orthologPairs = readOrthologs("data\\other\\ensembl_orthologs.txt");
        
        // Positive set, homolog pairs linked in ensembl synteny.
        List<CHS> PSet = new ArrayList<CHS>();
        // Negative set, homolog pairs unlinked in ensembl synteny.
        List<CHS> NSet = new ArrayList<CHS>();
        // True postive set, homolog pairs in PSet and linked in CHSMiner synteny.
        List<CHS> TPSet = new ArrayList<CHS>();
        // False positive set, homolog pairs in NSet but linked in CHSMiner synteny.
        List<CHS> FPSet = new ArrayList<CHS>();
        // True negative set, homolog pairs in NSet and unlinked in CHSMiner synteny.
        List<CHS> TNSet = new ArrayList<CHS>();
        // False negative set, homolog pairs in PSet but unlinked in CHSMiner synteny.
        List<CHS> FNSet = new ArrayList<CHS>();
        
        intersect(ensemblCHSList, orthologPairs, PSet, NSet);
        System.out.println(orthologPairs.size() + "\t" +
                PSet.size() + "\t" + NSet.size());
        
        intersect(CHSMinerCHSList, PSet, TPSet, FNSet);
        System.out.println(PSet.size() + "\t" +
                TPSet.size() + "\t" + FNSet.size());
        
        intersect(CHSMinerCHSList, NSet, FPSet, TNSet);
        System.out.println(NSet.size() + "\t" +
                FPSet.size() + "\t" + TNSet.size());
    }

    /**
     * Every pair of orthologs in human and mouse is formed as an individual CHS,
     * and all thus CHS are returned as a list.  
     * @param fileName name of gene file
     * @return CHSList containing all ortholog pairs
     * @throws java.lang.Exception exception in reading gene file
     */
    private static List<CHS> readOrthologs (String fileName) throws Exception {
        // get all genes as a list
        Map<Chromosome, List<Gene>> chrMap = IO.readGeneFile(fileName);
        List<Gene> geneList = new ArrayList<Gene>();
                for (Chromosome chr : chrMap.keySet()) {
            geneList.addAll(chrMap.get(chr));
        }
        
        // genes with the same family are grouped together
        Map<String, Set<Gene>> famMap = new HashMap<String, Set<Gene>>();
        for (Gene gene : geneList) {
             if (!famMap.containsKey(gene.family)) {
                famMap.put(gene.family, new HashSet<Gene>());
             }
             famMap.get(gene.family).add(gene);
        }
        
        // genes in the same family but in different species are matched to form a CHS
        List<CHS> CHSList = new ArrayList<CHS>();
        for (String family : famMap.keySet()) {
            if (famMap.get(family).size() < 2)
                continue;
            Set<Gene> orgA = new HashSet<Gene>();
            Set<Gene> orgB = new HashSet<Gene>();
            for (Gene gene : famMap.get(family)) {
                if (gene.chromosome.tax.equals("Homo sapiens")) {
                    orgA.add(gene);
                } else if (gene.chromosome.tax.equals("Mus musculus")) {
                    orgB.add(gene);
                }
            }
            for (Gene geneA : orgA) {
                for (Gene geneB : orgB) {
                    CHSList.add(new CHS(null, null, geneA.toGenoLoc(), geneB.toGenoLoc()));
                }
            }
        }
        return CHSList;
    }
    
    /**
     * Classifying all homolog pairs to linked and unlinked ones in given CHS.
     * @param CHSList list containing all given CHS
     * @param genePairsList list containing all homolog gene pairs
     * @param linkedPairsList gene pairs linked in given CHS, as the result of the method 
     * @param unlinkedPairsList gene pairs unlinked in given CHS, as the result of the method
     */ 
    private static void intersect (List<CHS> CHSList, List<CHS> genePairsList, 
            List<CHS> linkedPairsList, List<CHS> unlinkedPairsList) {        
        for (CHS genePairs : genePairsList) {
            boolean linked = false;
            for (CHS iCHS : CHSList) {
                if (genePairs.mergable(iCHS, 0)) {
                    linkedPairsList.add(genePairs);
                    linked = true;
                    break;
                }
            }
            if (!linked) {
                unlinkedPairsList.add(genePairs);
            }
        }    
    }
}
