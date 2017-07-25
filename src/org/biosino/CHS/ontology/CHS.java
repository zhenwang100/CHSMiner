package org.biosino.CHS.ontology;

import java.util.*;

/**
 * This class describe a chromosomal homology segment(CHS), which
 * contains a pair of GenoLocs .
 */
public class CHS implements Comparable<CHS> {
    /**
     * Chromosome A.
     */
	public Chromosome chrA;
    /**
     * Chromosome B.
     */
	public Chromosome chrB;
    /**
     * GenoLoc A.
     */
	public GenoLoc locA;
    /**
     * GenoLoc B.
     */
	public GenoLoc locB;
	
    /**
     * Size of the CHS (number of gene pairs with distinct families).
     */
	public int size;
    /**
     * Statistical p-value of the CHS.
     */
	public double pValue;
	
    /**
     * Construct a new CHS object.
     * @param chrA chromosome A
     * @param chrB chromosome B
     * @param locA GenoLoc A
     * @param locB GenoLoc B
     */
	public CHS (Chromosome chrA, Chromosome chrB, GenoLoc locA, GenoLoc locB) {
		this.chrA = chrA;
		this.chrB = chrB;
		this.locA = locA;
		this.locB = locB;
	}

    /**
     * Judge if GenoLoc A overlap with GenoLoc B within a distance.
     * @param dist the given distance
     * @return <CODE>true</CODE> if it do; <CODE>false</CODE> otherwise 
     */
	public boolean overlap (int dist) {
		if (this.locA.mergable(this.locB, dist)) {
			return true;
		} else {
			return false;
		}
	}
        
    /**
     * Sort Genoloc A and B. If A is 'larger' compared with B, they are
     * interchanged with each other.
     */
	public void sortLocs () {
		if (locA.compareTo(locB) > 0) {
			Chromosome tempChr = this.chrA;
			this.chrA = this.chrB;
			this.chrB = tempChr;
			GenoLoc tempLoc = this.locA;
			this.locA = this.locB;
			this.locB = tempLoc;
		}
	}
        
    /**
     * Descend from the Comparable interface and compare the CHS object with another one.
     * If their GenoLocs are not located in the same chromosome, they are compared by their name;
     * otherwise by their start point.
     * @param other another CHS object
     * @return the difference according to the comparison rule
     */
	public int compareTo (CHS other) {
		if (!this.chrA.equals(other.chrA)) {
			return this.chrA.compareTo(other.chrA);
		} else if (!this.chrB.equals(other.chrB)) {
			return this.chrB.compareTo(other.chrB);
		} else {
			return this.locA.start - other.locA.start;
		}
	}

    /**
     * Judge if the CHS can merge another one within a distance.
     * @param other another CHS object
     * @return <CODE>true</CODE> if it can; <CODE>false</CODE> otherwise 
     */
	public boolean mergable (CHS other) {
		if (this.locA.mergable(other.locA) && 
				this.locB.mergable(other.locB)) {
			return true;
		} else {
			return false;
		}
	}

    /**
     * Judge if the CHS can merge another one within a distance.
     * @param other another CHS object
     * @param dist the limiting distance
     * @return <CODE>true</CODE> if it can; <CODE>false</CODE> otherwise
     */
	public boolean mergable (CHS other, int dist) {
		if (this.locA.mergable(other.locA, dist) &&
				this.locB.mergable(other.locB, dist)) {
			return true;
		} else {
			return false;
		}
	}

    /**
     * Merge the CHS object with another one.
     * @param other another CHS object
     * @return a new CHS object after merging; <CODE>null</CODE> if they can't be merged
     */
	public CHS merge (CHS other) {
		if (!this.mergable(other)) {
			System.out.println("The two CHSs for merging are not the same. A null value returns.");
			return null;
		} else {
			GenoLoc newLocA = this.locA.merge(other.locA);
			GenoLoc newLocB = this.locB.merge(other.locB);
			return new CHS(this.chrA, this.chrB, newLocA, newLocB);
		}
	}
        
    /**
     * Transform the information of the object to an object array.
     * @return  an object array containing all fields of the object
     */
        public Object[] toArray (){
                List<Object> temp = new ArrayList<Object>();
                temp.addAll(Arrays.asList(this.locA.toArray()));
                temp.addAll(Arrays.asList(this.locB.toArray()));
                temp.add(size);
                // p value is formated for output as text
                temp.add((new java.text.DecimalFormat("0.000E0")).format(pValue));
                return temp.toArray();
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

