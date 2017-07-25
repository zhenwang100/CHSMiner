package org.biosino.CHS.util;

import java.util.*;
import java.sql.*;

import org.biosino.CHS.ontology.*;

/**
 * This class manage Ensembl database connection.
 */
public class Ensembl {
    
    /**
     * The map from names to ids of species.
     */
    public Map<String, Integer> taxonMap;
    public Ensembl () {
        taxonMap = new HashMap<String, Integer>();
        taxonMap.put("Chimpanzee", 9598);
        taxonMap.put("Human", 9606);
        taxonMap.put("Rat", 10116);
        taxonMap.put("Mouse", 10090);
        taxonMap.put("Cow", 9913);
        taxonMap.put("Dog", 9615);
        taxonMap.put("Opossum", 13616);
        taxonMap.put("Chicken", 9031);
        taxonMap.put("Anole Lizard", 28377);
        taxonMap.put("X.tropicalis", 8364);
        taxonMap.put("Tetraodon", 99883);
        taxonMap.put("Zebrafish", 7955);
        taxonMap.put("Anopheles", 7165);
        taxonMap.put("C.elegans", 6239);
        taxonMap.put("Fruitfly", 7227);
        taxonMap.put("S.cerevisiae", 4932);
        taxonMap.put("Zebra finch", 59729);
    }
    
    /**
     * Fetch all genes belonging to some species into a map.
     * @param taxons a String array containing species names
     * @return a map from each chromosome to corresponding gene list
     * @throws java.lang.Exception SQL exception
     */    
    public Map<Chromosome, List<Gene>> getEnsemblGene (String[] taxons) throws Exception {
        String taxonSQL = "(";
        Map<Integer, String> reverseMap = new HashMap<Integer, String>();
        for (int i = 0; i < taxons.length; i++) {
            int taxon_id = taxonMap.get(taxons[i]);
            reverseMap.put(taxon_id, taxons[i]);
            taxonSQL += "taxon_id = " + taxon_id;
            if (i != taxons.length - 1) {
                taxonSQL += " OR ";
            } else {
                taxonSQL += ")";
            }
        }
        
        
        Map<Chromosome, List<Gene>> chromosomes = new HashMap<Chromosome, List<Gene>>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://ensembldb.ensembl.org:5306/ensembl_compara_64",
		"anonymous", null);
        Statement statement = connection.createStatement();
        ResultSet rowSet = statement.executeQuery(
		"SELECT stable_id, family_id, display_label, " +
                "taxon_id, chr_name, chr_strand, chr_start, chr_end " +
                "FROM member, family_member " + 
                "WHERE member.member_id = family_member.member_id " +
                "AND source_name = 'ENSEMBLGENE' " +
                "AND chr_name NOT LIKE '%random' " +
                "AND " + taxonSQL);
        	while (rowSet.next()) {
			String geneID = rowSet.getString(1);
			String famID = rowSet.getString(2);
                        String symbol = rowSet.getString(3);
                        if (symbol == null) {
                            symbol = geneID;
                        }
                        String tax = reverseMap.get(rowSet.getInt(4));
                        String contig = rowSet.getString(5);
                        String orient = rowSet.getInt(6) > 0 ? "+" : "-";
                        int start = rowSet.getInt(7);
                        int end = rowSet.getInt(8);

                        if (geneID == null || famID == null || tax == null ||
                                contig == null || orient == null || start == 0 || end ==0)
                            continue;

                        Chromosome tempChr = new Chromosome(tax, contig);
                        Gene gene = new Gene(geneID, famID, symbol, tax, contig, 
                                orient, start, end);
                        boolean existTempChr = false;
			
                        for (Chromosome chr : chromosomes.keySet()) {
                            if (tempChr.equals(chr)) {
                                chromosomes.get(chr).add(gene);
                                existTempChr = true;
                                break;
                            }
                        }

                        if (!existTempChr) {
                            chromosomes.put(tempChr, new ArrayList<Gene>());
                            chromosomes.get(tempChr).add(gene);
                        }
		}
                rowSet.close();
                statement.close();
                connection.close();
        
                return chromosomes;
    }
    
}
