package etomica.virial;

import etomica.Atom;
import etomica.Phase;
import etomica.Space;
import etomica.potential.Potential0;

/**
 * @author David Kofke
 *
 * Pair potential given according to the Mayer bonds in a cluster integral.
 * Does not require that the value of the cluster is non-negative.
 */

/* History
 * 08/20/03 (DAK) small changes to energy method (check for g = 0; abs(g)->g in
 * log argument
 * 08/21/03 (DAK) invoke resetPairs for pairSet in pi method
 * 12/16/03 (DAK) added field to hold indication of whether cluster giving value
 * of potential is positive or negative
 */
public class P0Cluster extends Potential0 {

    private double temperature;
    private PhaseCluster phaseCluster;
	/**
	 * Constructor for P0Cluster.
	 */
	public P0Cluster(Space space) {
		super(space);
	}
	
	public double weight() {
		return phaseCluster.getSampleCluster().value(phaseCluster.getCPairSet(), 1/temperature);
	}

    public void setPhase(Phase phase) {
    	phaseCluster = (PhaseCluster)phase;
    }
    
    public void setTemperature(double aTemperature) {
        temperature = aTemperature;
    }

    /**
     * @deprecated use weight()
     */
    public double energy(Atom[] atoms) {
		double w = phaseCluster.getSampleCluster().value(phaseCluster.getCPairSet(), 1/temperature);
		if (w == 0) return Double.POSITIVE_INFINITY;
		return -Math.log(w)*temperature;
    }
    
}
