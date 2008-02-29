package etomica.modules.vle;
import etomica.EtomicaInfo;
import etomica.api.IBox;
import etomica.atom.iterator.IteratorDirective;
import etomica.box.Box;
import etomica.data.DataSourceScalar;
import etomica.potential.PotentialCalculationVirialSum;
import etomica.potential.PotentialMaster;
import etomica.space.Space;
import etomica.units.Pressure;

/**
 * Meter for evaluation of the soft-potential pressure in a box.
 * Requires that temperature be set in order to calculation ideal-gas
 * contribution to pressure; default is to use zero temperature, which
 * causes this contribution to be omitted.
 *
 * @author David Kofke
 */
 
public class MeterPressure2 extends DataSourceScalar {
    
    public MeterPressure2(Space space) {
    	super("Pressure",Pressure.dimension(space.D()));
    	dim = space.D();
        iteratorDirective = new IteratorDirective();
        iteratorDirective.includeLrc = true;
        virial = new PotentialCalculationVirialSum();
    }
      
    public static EtomicaInfo getEtomicaInfo() {
        EtomicaInfo info = new EtomicaInfo("Total pressure in a box (requires soft-potential model)");
        return info;
    }
    
    /**
     * Sets the integrator associated with this instance.  The pressure is 
     * calculated for the box the integrator acts on and integrator's 
     * temperature is used for the ideal gas contribution.
     */
    public void setPotentialMaster(PotentialMaster newPotentialMaster) {
        potentialMaster = newPotentialMaster;
    }
    
    /**
     * Returns the integrator associated with this instance.  The pressure is 
     * calculated for the box the integrator acts on and integrator's 
     * temperature is used for the ideal gas contribution.
     */
    public PotentialMaster getPotentialMaster() {
        return potentialMaster;
    }
    
    public void setBox(Box newBox) {
        box = newBox;
    }
    
    public IBox getBox() {
        return box;
    }

    /**
     * Sets flag indicating whether calculated energy should include
     * long-range correction for potential truncation (true) or not (false).
     */
    public void setIncludeLrc(boolean b) {
    	iteratorDirective.includeLrc = b;
    }
    
    /**
     * Indicates whether calculated energy should include
     * long-range correction for potential truncation (true) or not (false).
     */
    public boolean isIncludeLrc() {
    	return iteratorDirective.includeLrc;
    }

	 /**
	  * Computes total pressure in box by summing virial over all pairs, and adding
	  * ideal-gas contribution.
	  */
    public double getDataAsScalar() {
    	virial.zeroSum();
        potentialMaster.calculate(box, iteratorDirective, virial);
        //System.out.println("fac="+(1/(box.getBoundary().volume()*box.getSpace().D())));
        return - virial.getSum()/(box.getBoundary().volume()*dim);
    }

    private static final long serialVersionUID = 1L;
    protected PotentialMaster potentialMaster;
    protected Box box;
    private IteratorDirective iteratorDirective;
    private final PotentialCalculationVirialSum virial;
    private final int dim;
}
