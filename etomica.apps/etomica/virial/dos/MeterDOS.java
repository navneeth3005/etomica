package etomica.virial.dos;

import etomica.Simulation;
import etomica.data.DataSourceFunction;
import etomica.units.Dimension;
import etomica.virial.Cluster;
import etomica.virial.P0Cluster;
import etomica.virial.PhaseCluster;

/**
 * @author kofke
 */

/* History
 * 08/21/03 (DAK) invoke resetPairs for pairSet in currentValue method
 */
public class MeterDOS extends DataSourceFunction {

	private P0Cluster p0;
	private Cluster cluster;
	private double dx;
	
	/**
	 * Constructor for MeterDOS.
	 * @param parent
	 * @param cluster
	 * @param temperature
	 */
	public MeterDOS(Simulation parent, Cluster cluster) {
		super(parent);
		setActive(true);
		setCluster(cluster);
		double xMax = Math.exp(3.0);
		setX(0.0, xMax, 100);
		dx = xMax/nPoints;
	}

	/**
	 * @see etomica.data.DataSourceScalar#getData()
	 */
	public double[] getData() {
		for(int i=0; i<nPoints; i++) y[i] = 0.0;
		int k = (int)(cluster.value(((PhaseCluster)phase).getPairSet().resetPairs(),1.0)/dx);
		if(k>=y.length) k = y.length-1;
		y[k] = 1.0;// /p0.pi(((PhaseCluster)phase));
		return y;
	}

	/**
	 * Returns the p0.
	 * @return P0Cluster
	 */
	public P0Cluster getP0() {
		return p0;
	}

	/**
	 * Sets the p0.
	 * @param p0 The p0 to set
	 */
	public void setP0(P0Cluster p0) {
		this.p0 = p0;
	}

	public Dimension getXDimension() {return Dimension.NULL;}
	
	public Dimension getDimension() {return Dimension.NULL;}

	/**
	 * Returns the cluster.
	 * @return Cluster
	 */
	public Cluster getCluster() {
		return cluster;
	}

	/**
	 * Sets the cluster.
	 * @param cluster The cluster to set
	 */
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

}
