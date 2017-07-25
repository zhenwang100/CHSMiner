package org.biosino.CHS.ontology;

/**
 * This class descending from GenoLoc describes a gene.
 */
public class Gene extends GenoLoc {
    /**
     * Gene id.
     */
	public String id;
    /**
     * Family id.
     */
	public String family;
    /**
     * Rank in a chromosome.
     */
	public int rank;
    /**
     * Orient ('+' or '-').
     */
	public String orient;
    /**
     * Gene symbol.
     */
	public String symbol;
	
    /**
     * Construct a new Gene object
     * @param id gene id
     * @param family family id
     * @param symbol gene symbol
     * @param tax taxonomy name
     * @param chr chromosome name
     * @param orient orient
     * @param start start point
     * @param end end point
     */
	public Gene (String id, String family, String symbol, String tax, String chr,
			String orient, int start, int end) {
		super(tax, chr, start, end);
		this.id = id;
		this.family = family;
		this.symbol = symbol;
		this.orient = orient; 
	}
	
    /**
     * Transform the information of the object to an object array.
     * @return an object array containing all fields of the object
     */
        public Object[] toArray() {
                Object[] array = {this.id, this.family, this.symbol, this.chromosome.tax,
                    this.chromosome.chr, this.orient, this.start, this.end, this.rank};
                return array;
        } 
        
    /**
     * Get a new <CODE>GenoLoc</CODE> object the gene represents.
     * @return a <CODE>GenoLoc</CODE> object
     */
        public GenoLoc toGenoLoc() {
                return new GenoLoc(this.chromosome.tax, this.chromosome.chr, this.start, this.end); 
        }
}
