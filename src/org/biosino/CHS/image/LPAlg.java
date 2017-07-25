package org.biosino.CHS.image;

import drasys.or.matrix.*;
import drasys.or.mp.*;
import drasys.or.mp.lp.*;

/**
 * This class is used to caculate the locations for gene labels given the locations of genes.
 */
public class LPAlg {
    /* used to test 
    public static void main(String[] args) throws Exception {
	double x[] = {-3, 1.5, 2, 3, 4, 5, 10, 15, 20, 21, 22};
	double y[] = getLabLoc(x, 2.0);
	for (int i = 0; i< y.length; i++) {
		System.out.println(y[i]);
	}
    }
    */
    
    
    /**
     * Given gene locations (x0, x1, x2, x3) et al (<CODE>double[] x</CODE>), 
     * calculate their corresponding label locations (y0, y1, y2, y3). Both types of 
     * locations can be negative.The distance between two neigboring lables must 
     * be greater than (<CODE>double dist</CODE>) to avoid overlap. 
     * Linear programming is used to achieve the goal.
     * @param x gene locations, which must be ascendent ordered.
     * @param dist the minimum distance between neigboring lables, which must be positive.
     * @return corresponding label locations; null if wrong parameters are provided or exception happens.
     */
    
    /* Linear Programming principle:
     * remember to sort x0, x1, x2, x3. first
     * 
     * The primary version:
     * Objection:
     * min |y0 - x0| + |y1 - x1| + |y2 - x2| + |y3 - x3|
     * Constrains:
     * -y0 + y1 >= dist
     * -y1 + y2 >= dist
     * -y2 + y3 >= dist
     * 
     *  
     * I take the above objection apart:
     *  let dp0 = 0.5 * (|x0 - y0| + x0 - y0)
     *      dn0 = 0.5 * (|x0 - y0| - x0 + y0)
     *  then dp0 + dn0 = |x0 - y0|
     *       dp0 - dn0 = x0 - y0
     *
     *  The second version becomes:
     *  Objection:
     *  min (dp0 + dn0) + (dp1 + dn1) + (dp2 + dn2) + (dp3 + dn3)
     *  Constrains:
     *    y constrains:
     *       -y0 + y1 >= dist;
     *       -y1 + y2 >= dist;
     *       -y2 + y3 >= dist;
     *    d constrains:
     *       dp0 - dn0 + y0 = x0
     *       dp1 - dn1 + y1 = x1
     *       dp2 - dn2 + y2 = x2
     *       dp3 - dn3 + y3 = x3
     *     dpp constrains:
     *       dp0, dp1, dp2, dp3 >= 0
     *     dnp constrains:
     *       dn0, dn1, dn2, dn3 >= 0
     *
     *  However, the package will constrain all parameters to be non-negative,
     *  which means that not only dp, dn, but also y will be non-negative 
     *  automatically. If we don't want y have this constrain,
     *     let y0 = zp0 - zn0
     *
     *	The final version becomes: 
     *  Objection:
     *  min (dp0 + dn0) + (dp1 + dn1) + (dp2 + dn2) + (dp3 + dn3)
     *  Constrains:
     *    z constrains:
     *       -zp0 + zn0 + zp1 -zn1 >= dist
     *       -zp1 + zn1 + zp2 -zn2 >= dist
     *       -zp2 + zn2 + zp3 -zn3 >= dist
     *    d constrains:
     *       dp0 - dn0 + zp0 - zn0 = x0
     *       dp1 - dn1 + zp1 - zn1 = x1
     *       dp2 - dn2 + zp2 - zn2 = x2
     *       dp3 - dn3 + zp3 - zn3 = x3
     *   and below constrains will be automatically added
     *     zpp constrains:
     *       zp0, zp1, zp2, zp3 >= 0
     *     znp constrains:
     *       zn0, zn1, zn2, zn3 >= 0
     *     dpp constrains:
     *       dp0, dp1, dp2, dp3 >= 0
     *     dnp constrains:
     *       dn0, dn1, dn2, dn3 >= 0
     */
    public static double[] getLabLoc (double[] x, double dist) {
	// check the parameter condition
	if (dist < 0) {
		System.err.println("Error: distance must >= 0");
		return null;
	}
	int xNum = x.length;
        if (xNum == 0) {
            return null;
        }
	for (int i = 0; i < xNum; i++) {
		if (i > 0 && x[i] < x[i - 1]) {
			System.out.println("Error: x must be in ascendent order");
			return null;
		}
	}
	
	// construct the symbols of variables
	String[] zp = new String[xNum];
	String[] zn = new String[xNum];
	String[] dp = new String[xNum];
	String[] dn = new String[xNum];
	for (int i = 0; i < xNum; i++) {
		zp[i] = "zp" + i;
		zn[i] = "zn" + i;
		dp[i] = "dp" + i;
		dn[i] = "dn" + i;
	}
	
	// construct the symbols of constrains
	String[] zCons = new String[xNum - 1];
	String[] dCons = new String[xNum];
	
	for (int i = 0; i < zCons.length; i++) {
		zCons[i] = "zCons" + i;
	}
	for (int i = 0; i < dCons.length; i++) {
		dCons[i] = "dCons" + i;
	}

	// construct linear prog problem
	int varNum = zp.length + zn.length + dp.length + dn.length;
	int consNum = zCons.length + dCons.length;
	
	SizableProblemI prob = new Problem(consNum, varNum);
	prob.getMetadata().put("lp.isMinimize", "true");
	
	try {
		// set variables and objection 
		for (int i = 0; i < xNum; i++) {
			prob.newVariable(zp[i]).setObjectiveCoefficient(0);
			prob.newVariable(zn[i]).setObjectiveCoefficient(0);
			prob.newVariable(dp[i]).setObjectiveCoefficient(1);
			prob.newVariable(dn[i]).setObjectiveCoefficient(1);
		}

		// construct constrains
        	byte GTE = Constraint.GREATER;
		byte EQU = Constraint.EQUAL;

		// z Constrains
		for (int i = 0; i < zCons.length; i++) {
			prob.newConstraint(zCons[i]).setType(GTE).setRightHandSide(dist);
			prob.setCoefficientAt(zCons[i], zp[i], -1);
			prob.setCoefficientAt(zCons[i], zn[i], 1);
			prob.setCoefficientAt(zCons[i], zp[i + 1], 1);
			prob.setCoefficientAt(zCons[i], zn[i + 1], -1);
		}
	
		// d Constrains
		for (int i = 0; i < dCons.length; i++) {
			prob.newConstraint(dCons[i]).setType(EQU).setRightHandSide(x[i]);
			prob.setCoefficientAt(dCons[i], dp[i], 1);
			prob.setCoefficientAt(dCons[i], dn[i], -1);
			prob.setCoefficientAt(dCons[i], zp[i], 1);
			prob.setCoefficientAt(dCons[i], zn[i], -1);
		}
	
		// solve the problem
		LinearProgrammingI lp = new DenseSimplex(prob);
        	double objValue = lp.solve();
		VectorI varVect = lp.getSolution();
        	// System.out.println("Solution = " + objValue);
		// System.out.println(varVect);
		
		// caculate y = zp - zn
		double[] y = new double[xNum];
		for (int i = 0; i < xNum; i++) {
			y[i] = varVect.elementAt(4 * i) - varVect.elementAt(4 * i + 1);
		}
		return y;
	} catch (Exception e) {
		System.err.println(e);
		return null;
	}
    }
} 

