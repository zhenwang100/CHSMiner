package org.biosino.CHS.ontology;

/**
 * This class describes the chromosomes.
 */
public class Chromosome implements Comparable<Chromosome> {
    /**
     * Taxonomy name.
     */
	public String tax;
    /**
     * Chromosome name.
     */
	public String chr;
    /**
     * Chromosome length.
     */
	public long length; 
    /**
     * Construct a new chromosome.
     * @param tax taxonomy name
     * @param chr chromosome name
     */
	public Chromosome (String tax, String chr) {
		this.tax = tax;
		this.chr = chr;
	}
    /**
     * Judge whether the chromosome is the same as another one.
     * @param other another chromosome object.
     * @return <CODE>true</CODE> if they are the same; <CODE>false</CODE> otherwise
     */
	public boolean equals (Chromosome other) {
		if (other == null) {
			return false;
		} else if (this.tax.equals(other.tax) && this.chr.equals(other.chr)) {
			return true;
		} else {
			return false;
		}
	}
    /**
     * Transform the information of the object to an object array.
     * @return an object array containing all fields of the object
     */
        public Object[] toArray() {
            Object[] array = {this.tax, this.chr};
            return array;
        }
    /**
     * Tansform the information of the object to a string.
     * @return a string joining all fields of the object by tabs
     */
        public String toString () {
                String string = "";
                Object[] array = this.toArray();
                for (int i = 0; i < array.length; i++) {
                    string += array[i];
                    if (i != array.length - 1) {
                        string += "\t";
                    }
                }
                return string;
	}
        
    /**
     * Descend from the Comparable interface and compare the Chromosome object with another one. 
     * If they do not belong to the same species, they are compared by their taxonomy name;
     * otherwise by their chromosome name.
     * @param other another Chromosome object
     * @return the difference according to the comparison rule
     */
        public int compareTo(Chromosome other) {
                if (!this.tax.equals(other.tax)) {
                    return this.tax.compareTo(other.tax);
                } else if (!this.chr.equals(other.chr)) {
                    return this.chr.compareTo(other.chr);
                } else {
                    return 0;
                }
        }
}

