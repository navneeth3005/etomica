package etomica.normalmode;
import etomica.EtomicaInfo;
import etomica.api.IBox;
import etomica.atom.iterator.IteratorDirective;
import etomica.data.DataSourceScalar;
import etomica.integrator.IntegratorBox;
import etomica.potential.PotentialCalculationVirialSum;
import etomica.space.ISpace;
import etomica.units.Pressure;

/**
 * Meter for evaluation of the soft-potential pressure in a box.
 * Requires that temperature be set in order to calculation ideal-gas
 * contribution to pressure; default is to use zero temperature, which
 * causes this contribution to be omitted.
 *
 * @author David Kofke
 */
 
public class MeterPressureSuperBox extends DataSourceScalar {
    
    public MeterPressureSuperBox(ISpace space) {
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
    public void setIntegrator(IntegratorBox newIntegrator) {
        integrator = newIntegrator;
    }
    
    /**
     * Returns the integrator associated with this instance.  The pressure is 
     * calculated for the box the integrator acts on and integrator's 
     * temperature is used for the ideal gas contribution.
     */
    public IntegratorBox getIntegrator() {
        return integrator;
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
        if (integrator == null) {
            throw new IllegalStateException("You must call setIntegrator before using this class");
        }
    	virial.zeroSum();
        IBox box = integrator.getBox();
        integrator.getPotentialMaster().calculate(box, iteratorDirective, virial);
        //System.out.println("fac="+(1/(box.getBoundary().volume()*box.getSpace().D())));
        return (box.getMoleculeList().getMoleculeCount() / box.getBoundary().volume())*integrator.getTemperature() - 27*virial.getSum()/(box.getBoundary().volume()*dim);
    }

    private static final long serialVersionUID = 1L;
    private IntegratorBox integrator;
    private IteratorDirective iteratorDirective;
    private final PotentialCalculationVirialSum virial;
    private final int dim;
}