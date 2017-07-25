package org.biosino.CHS.ontology;

/**
 * This class descending from Chromosome describes a location in a genome.
 */
public class GenoLoc implements Comparable<GenoLoc> {
    
    /**
     * Chromosome.
     */
        public Chromosome chromosome; 
    /**
     * Start point.
     */
	public int start;
    /**
     * End point.
     */
	public int end;
    
    /**
     * Length.
     */   
        public int length;
    /**
     * Construct a new GenoLoc object.
     * @param tax taxonomy name
     * @param chr chromosome name
     * @param start start point
     * @param end end point
     */
	public GenoLoc (String tax, String chr, int start, int end) {
		this.chromosome = new Chromosome(tax, chr);
		if (start > end) {
			System.out.println("Note: 'start > end' exists and may cause caculation error!");
		}
		this.start = start;
		this.end = end;
		this.length = this.end - this.start + 1; //2007-12-14
	}
    /**
     * Get the distance between the GenoLoc object and another one.
     * @param other anothr GenoLoc object
     * @return the distance between the two chromosomes; 
     * -1 if they are not located at the same chromosome;
     * 0 if they overlap with each other
     */
	public int getDist (GenoLoc other) {
		if (!this.chromosome.equals(other.chromosome)) {
			return -1;
		} else if (this.end < other.start) {
			return other.start - this.end;
		} else if (this.start > other.end) {
			return this.start - other.end;
		} else {
			return 0;
		}
	}
    /**
     * Judge if the GenoLoc can merge with another one.
     * @param other another GenoLoc
     * @return <CODE>true</CODE> if it can; <CODE>false</CODE> otherwise
     */
	public boolean mergable (GenoLoc other) {
		if (!this.chromosome.equals(other.chromosome)) {
			return false;
		} else {
			return true;
		}
	}
    /**
     * Judge if the GenoLoc can merge another one within a distance.
     * @param other another GenoLoc object
     * @param dist the limiting distance
     * @return <CODE>true</CODE> if it can; <CODE>false</CODE> otherwise
     */
	public boolean mergable (GenoLoc other, int dist) {
		if (!this.chromosome.equals(other.chromosome)) {
			return false;
		} else if (this.getDist(other) <= dist) {
			return true;
		} else {
			return false;
		}
	}
    /**
     * Merge the GenoLoc object with another one.
     * @param other another GenoLoc object
     * @return a new GenoLoc object after merging; <CODE>null</CODE> if they can't be merged
     */
	public GenoLoc merge (GenoLoc other) {
		if (this.getDist(other) == -1) {
			System.out.println("The two GenoLocs for merging are not the same. A null value returns.");
			return null;
		} else {
			int newStart = this.start < other.start ? this.start : other.start;
			int newEnd = this.end > other.end ? this.end : other.end;
			return new GenoLoc(this.chromosome.tax, this.chromosome.chr, newStart, newEnd);
		}
	}
    /**
     * Descend from the Comparable interface and compare the GenoLoc object with another one. 
     * If they are not located in the same chromosome, they are compared by their name;
     * otherwise by their start point.
     * @param other another GenoLoc object
     * @return the difference according to the comparison rule
     */
	public int compareTo(GenoLoc other) {
		if (!this.chromosome.equals(other.chromosome)) {
			return this.chromosome.compareTo(other.chromosome);
		} else {
			return this.start - other.start;
		}
	}
    /**
     * Transform the information of the object to an object array.
     * @return an object array containing all fields of the object
     */
        public Object[] toArray() {
            Object[] array = {this.chromosome.tax, this.chromosome.chr, this.start, this.end};
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
}
