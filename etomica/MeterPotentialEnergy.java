package etomica;
import etomica.units.Dimension;

/**
 * Meter for evaluation of the potential energy in a phase
 * Includes several related methods for computing the potential energy of a single
 * atom or molecule with all neighboring atoms
 *
 * @author David Kofke
 */
 
public class MeterPotentialEnergy extends Meter implements EtomicaElement {
    
    private IteratorDirective iteratorDirective;
    private final PotentialCalculation.EnergySum energy = new PotentialCalculation.EnergySum();
    
    public MeterPotentialEnergy() {
        this(Simulation.instance);
    }
    public MeterPotentialEnergy(Simulation sim) {
        super(sim);
        setLabel("Potential Energy");
        iteratorDirective = new IteratorDirective();
    }
      
    public static EtomicaInfo getEtomicaInfo() {
        EtomicaInfo info = new EtomicaInfo("Total intermolecular potential energy in a phase");
        return info;
    }

    public Dimension getDimension() {return Dimension.ENERGY;}
      
 /**
  * Computes total potential energy for all atom pairs in phase
  * Returns infinity (MAX_VALUE) as soon as overlap is detected
  * Currently, does not include long-range correction to truncation of energy
  */
    public final double currentValue() {
        energy.reset();
        phase.potential().calculate(iteratorDirective, energy);
        return energy.sum();
    }
    
}
