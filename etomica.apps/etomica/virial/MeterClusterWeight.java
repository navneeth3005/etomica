package etomica.virial;

import etomica.IteratorDirective;
import etomica.Phase;
import etomica.PotentialMaster;
import etomica.data.DataSourceScalar;
import etomica.units.Dimension;

/**
 * Meter to calculate the sampling weight of a cluster configuration.   
 */
 
public class MeterClusterWeight extends DataSourceScalar {
    
    public MeterClusterWeight(PotentialMaster potentialMaster) {
        setLabel("Cluster Weight");
        potential = potentialMaster;
    }
      
    public Dimension getDimension() {return Dimension.NULL;}
    
    public double getDataAsScalar(Phase p) {
    	weight.reset();
    	potential.calculate(p, up, weight);
    	return weight.sum();
    }

    private final PotentialMaster potential;
    private final PotentialCalculationClusterWeightSum weight = new PotentialCalculationClusterWeightSum();
    private final IteratorDirective up = new IteratorDirective();
}