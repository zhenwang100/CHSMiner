package org.biosino.CHS.util;

import java.io.*;
import java.util.*;

import org.biosino.CHS.ontology.*;

/**
 * This class manage IO.
 */
public class IO {
	
    /**
     * Read gene file into a map. 
     * The format for input file is
     * <PRE>
     * -----------------------
     *   Field         Type  
     * -----------------------
     *   geneID       String
     *   familyID     String
     *   Symbol       String  
     *   Species    String  
     *   Chromosome   String  
     *   Orient       String  
     *   Start        int     
     *   End          int     
     * -----------------------
     * </PRE>
     * Fields are separated by tabs. A line starting with "#" will be omitted.
     * InputFileFormatException will be thrown if data type is not correct or a record is not complete.
     * A gene record with null value for type String or 0 value for type int will be omitted.
     * @param fileName gene file name (including path)
     * @return a map from each chromosome to corresponding gene list
     * @throws java.lang.Exception IOException or InputFileFormatException
     */
	public static Map<Chromosome, List<Gene>> readGeneFile (String fileName) 
            throws Exception {
		String row;
		String[] fields;
		Map<Chromosome, List<Gene>> chromosomes = new HashMap<Chromosome, List<Gene>>();
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		int i = 0; //line number
                while ((row = in.readLine()) != null) {
			i++;
                        if (row.startsWith("#"))
				continue;
			
			fields = row.split("\t");
			
			String geneID = null;
                        String famID = null;
                        String symbol = null;
                        String tax = null;
                        String contig = null;
                        String orient = null;
                        int start = 0;
                        int end = 0;                        
                        try {
                            geneID = fields[0];
                            famID = fields[1];
                            symbol = fields[2];
                            tax = fields[3];
                            contig = fields[4];
                            orient = fields[5];
                            start = Integer.parseInt(fields[6]);
                            end = Integer.parseInt(fields[7]);
                        } catch (Exception e) {
                            throw new InputFileFormatException(i, e);
                        }

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
		in.close();
		return chromosomes;
	}

    /**
     * Read CHS file into a list. 
     * The format for input file is
     * <PRE>
     * -----------------------
     *   Field          Type  
     * -----------------------
     *   SpeciesA     String     
     *   ChromosomeA   String     
     *   StartA        int  
     *   EndA          int  
     *   SpeciesB     String
     *   ChromosomeB   String  
     *   StartB        int  
     *   EndB          int     
     *   Size          int
     *   P value       double
     * -----------------------
     * </PRE>
     * Size and P value are not necessary in the file.  
     * Fields are separated by tabs. A line starting with "#" will be omitted.
     * InputFileFormatException will be thrown if data type is not correct or a record is not complete.
     * @param fileName CHS file name (including path)
     * @return a list containing all CHS
     * @throws java.lang.Exception IOException or InputFileFormatException
     */
        public static List<CHS> readCHSFile (String fileName) throws Exception {
                List<CHS> CHSList = new ArrayList<CHS>();
                BufferedReader in = new BufferedReader(new FileReader(fileName));
                String line;
                int i = 0; //line number
                while ((line = in.readLine()) != null) {
                    i++;
                    if (line.startsWith("#"))
                        continue;
                    String[] fields = line.split("\t");
                    try {
                        String taxA = fields[0];
                        String chrA = fields[1];
                        int startA = Integer.parseInt(fields[2]);
                        int endA = Integer.parseInt(fields[3]);
                        String taxB = fields[4];
                        String chrB = fields[5];
                        int startB = Integer.parseInt(fields[6]);
                        int endB = Integer.parseInt(fields[7]);
                        
                        GenoLoc locA = new GenoLoc(taxA, chrA, startA, endA);
                        GenoLoc locB = new GenoLoc(taxB, chrB, startB, endB);
                        // the chromosome parameter is null for the time being...
                        CHS iCHS = new CHS(null, null, locA, locB);
                        
                        int size = 0;
                        double pValue = 0;
                        if (fields.length >= 9) {
                            size = Integer.parseInt(fields[8]);
                        }
                        if (fields.length >= 10) {
                            pValue = Double.parseDouble(fields[9]);
                        }
                        iCHS.size = size;
                        iCHS.pValue = pValue;
                        
                        CHSList.add(iCHS);
                    } catch (Exception e) {
                        throw new InputFileFormatException(i, e);
                    }
                }
                in.close();
                return CHSList;
        }

    /**
     * Write all CHS in a list to an output file in text format.
     * The format for output file is
     * <PRE>
     * -----------------------
     *   Field           Type  
     * -----------------------
     *   Species A     String     
     *   Chromosome A   String     
     *   Start A        int  
     *   End A          int
     *   Species B     String  
     *   Chromosome B   String  
     *   Start B        int  
     *   End B          int     
     *   Size           int
     *   P value        double
     * -----------------------
     * </PRE>
     * @param CHSList CHS list
     * @param fileName output file name
     * @throws java.io.IOException IOException
     */
	public static void writeCHSFile (List<CHS> CHSList, String fileName)
		throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(fileName)));
                out.println(">Species A\tChromosome A\tStart A\tEnd A\t" +
                        "Species B\tChromosome B\tStart B\tEnd B\tSize\tP value");
		for (CHS iCHS : CHSList) {
			out.println(iCHS);
		}
		out.close();
	}

    /**
     * Write all CHS in a list to an output file in text format.
     * The file contains both CHS and matched genes.
     * The format for CHS line (starting with ">") is
     * <PRE>
     * -----------------------
     *   Field           Type
     * -----------------------
     *   Species A     String
     *   Chromosome A   String
     *   Start A        int
     *   End A          int
     *   Species B     String
     *   Chromosome B   String
     *   Start B        int
     *   End B          int
     *   Size           int
     *   P value        double
     * -----------------------
     * </PRE>
     * The format for gene line (starting with " ")is
     * <PRE>
     * -----------------------
     *   Field           Type
     * -----------------------
     *   GeneID A       String
     *   Symbol A       String
     *   GeneID B       String
     *   Symbol B       String
     * -----------------------
     * </PRE>
     * @param iPostPro a post processor object for CHS result
     * @param fileName output file name
     * @throws java.io.IOException IOException
     */
        public static void writeCHSFile(PostProcessor iPostPro, String fileName)
                throws IOException {
                List<CHS> CHSList = iPostPro.CHSList;
                PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(fileName)));
                out.println("#Species A\tChromosome A\tStart A\tEnd A\t" +
                        "Species B\tChromosome B\tStart B\tEnd B\tSize\tP value");
                out.println("#GeneID A\tSymbol A\tGeneID B\tSymbol B");
		for (CHS iCHS : CHSList) {
			out.println(">" + iCHS);
                        Gene[][] geneAry = iPostPro.getGeneList(iCHS);
                        for (int i = 0; i < geneAry[2].length; i++) {
                            out.println(" " + geneAry[2][i].id + "\t" + geneAry[2][i].symbol + "\t" +
                                    geneAry[3][i].id + "\t" + geneAry[3][i].symbol);
                        }
		}
		out.close();
        }
}


/**
 * Exception will be thrown when the format of input file is not correct.
 * For every gene record, both completeness for all fields and the data type 
 * for each field will be checked.
 */
class InputFileFormatException extends Exception {
    
    /** Message of the exception */
    private String msg;
    
    /**
     * Constructs a new instance with the specified low level exception 
     * and the row at which it is thrown.
     * @param i the row at which the exception is thrown
     * @param e a low level exception
     */
    public InputFileFormatException (int i, Exception e) {
        super(e);
        String oriMsg = e.toString();
        if (oriMsg.startsWith("java.lang.NumberFormatException")) {
            this.msg = "Value is not a valid number at line " + i
                    + ": " + (oriMsg.split(":"))[2];
        } else if (oriMsg.startsWith("java.lang.ArrayIndexOutOfBoundsException")) {
            this.msg = "Data is not complete at line " + i;
        } else {
            this.msg = oriMsg;
        }
    }
    
    /**
     * Gets the message of the exception
     * @return the message of the exception
     */
    public String getMessage () {
        return this.msg;
    }
    /**
     * Gets the description of the exception
     * @return the description of the exception
     */
    public String toString () {
        return "InputFileFormatException: " + this.msg;
    }
}
