import java.io.*;
import java.util.*;

import org.biosino.CHS.ontology.*;
import org.biosino.CHS.util.*;
import org.biosino.CHS.algorithm.*;
import org.biosino.CHS.image.*;

/**
 * The class is rsponsible for test CHS package in command line.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        int gap = 30;        
        
        /* Test input */
        Map<Chromosome, List<Gene>> chrMap =
                IO.readGeneFile("data\\input\\9606_15.txt");
        List<Chromosome> chrList =
                new ArrayList<Chromosome>(chrMap.keySet());

        /* Check input: correct
        PrintWriter out = new PrintWriter(new BufferedWriter(
        new FileWriter("test.txt")));
        for (Chromosome chr : chrList) {
        List<Gene> genes = chrMap.get(chr);
        for (Gene gene : genes) {
        System.out.println(gene);
        out.println(gene + "\t" + gene.rank);
        }
        }
        out.close();
         */

        /*Test searching for paralogon */
        PreProcessor iPrePro = new PreProcessor(chrMap, true, "0");
        GreedyAlg iAlg = new GreedyAlg(2, gap * iPrePro.geneMeanLen);
        List<CHS> CHSList = new ArrayList<CHS>();
        for (int i = 0; i < chrList.size(); i++) {
            for (int j = 0; j <= i; j++) {
                Chromosome chr1 = chrList.get(i);
                Chromosome chr2 = chrList.get(j);
                if (!chr1.tax.equals(chr2.tax))
                    continue;
                List<Gene> list1 = iPrePro.filteredChrMap.get(chr1);
                List<Gene> list2 = iPrePro.filteredChrMap.get(chr2);

                CHSList.addAll(iAlg.search(chr1, chr2, list1, list2));
            }
        }
        Collections.sort(CHSList);  // sort all CHS 
        
        PostProcessor iPostPro = new PostProcessor(iPrePro, CHSList, gap);

        /* Check CHSList: correct
        CHSList.clear();
        Map<Chromosome, Map<Chromosome, Set<CHS>>> CHSMap = iPostPro.CHSMap;
        CHSList.addAll(CHSMap.get(chrList.get(1)).get(chrList.get(1)));
         */

        /* Test output */
        IO.writeCHSFile(CHSList, "data\\output\\out1.txt");
        CHS iCHS = CHSList.get(0);
        Gene[][] geneAry = iPostPro.getGeneList(iCHS);
        for (int i = 0; i < geneAry[3].length; i++) {
            System.out.println(geneAry[3][i]);
        }
        IO.writeCHSFile(iPostPro, "data\\output\\out2.txt");
    }
}
