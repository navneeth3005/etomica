package etomica.integrator.mcmove;

import etomica.action.AtomActionTranslateTo;
import etomica.atom.AtomArrayList;
import etomica.atom.AtomFactory;
import etomica.atom.AtomSet;
import etomica.atom.IAtom;
import etomica.atom.SpeciesAgent;
import etomica.atom.iterator.AtomIterator;
import etomica.atom.iterator.AtomIteratorNull;
import etomica.atom.iterator.AtomIteratorSinglet;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.phase.Phase;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.species.Species;
import etomica.util.IRandom;

/**
 * Elementary Monte Carlo move in which a molecule of a specified species is
 * inserted into or removed from a phase.
 *
 * @author David Kofke
 */
public class MCMoveInsertDelete extends MCMovePhase {
    
    private static final long serialVersionUID = 2L;
    //chemical potential
    protected double mu;
    
    //directive must specify "BOTH" to get energy with all atom pairs
    protected final MeterPotentialEnergy energyMeter;
	protected Species species;
    protected SpeciesAgent speciesAgent;
	protected final AtomIteratorSinglet affectedAtomIterator = new AtomIteratorSinglet();
	protected IAtom testMolecule;
	protected double uOld;
	protected double uNew = Double.NaN;
	protected boolean insert;
	protected final AtomArrayList reservoir;
    protected final AtomActionTranslateTo atomTranslator;
    protected AtomFactory moleculeFactory;
    protected AtomSet moleculeList;
    protected IRandom random;

    public MCMoveInsertDelete(Simulation sim) {
        this(sim.getPotentialMaster(), sim.getRandom());
    }
    
    public MCMoveInsertDelete(PotentialMaster potentialMaster, IRandom random) {
        super(potentialMaster);
        energyMeter = new MeterPotentialEnergy(potentialMaster);
        setMu(0.0);
        energyMeter.setIncludeLrc(true);
        atomTranslator = new AtomActionTranslateTo(potentialMaster.getSpace());
        reservoir = new AtomArrayList();
        this.random = random;
    }
    
//perhaps should have a way to ensure that two instances of this class aren't assigned the same species
    public void setSpecies(Species s) {
        species = s;
        if(phase != null) {
            speciesAgent = species.getAgent(phase);
            moleculeList = speciesAgent.getChildList();
        }
        moleculeFactory = species.moleculeFactory();
    }
    public Species getSpecies() {return species;}
    
    public void setPhase(Phase p) {
        super.setPhase(p);
        energyMeter.setPhase(phase);
        if(species != null) {
            speciesAgent = species.getAgent(phase);
            moleculeList = speciesAgent.getChildList();
        }
    }
    
    /**
     * Chooses and performs with equal probability an elementary molecule insertion
     * or deletion.
     */
    public boolean doTrial() {
        insert = (random.nextInt(2) == 0);
        if(insert) {
            uOld = 0.0;
            
            if(!reservoir.isEmpty()) testMolecule = reservoir.remove(reservoir.getAtomCount()-1);
            else testMolecule = moleculeFactory.makeAtom();
            speciesAgent.addChildAtom(testMolecule);

            atomTranslator.setDestination(phase.getBoundary().randomPosition());
            atomTranslator.actionPerformed(testMolecule);
        } else {//delete
            if(speciesAgent.getNMolecules() == 0) {
                testMolecule = null;
                return false;
            }
            testMolecule = moleculeList.getAtom(random.nextInt(moleculeList.getAtomCount()));
            //delete molecule only upon accepting trial
            energyMeter.setTarget(testMolecule);
            uOld = energyMeter.getDataAsScalar();
        } 
        uNew = Double.NaN;
        return true;
    }//end of doTrial
    
    public double getA() {//note that moleculeCount() gives the number of molecules after the trial is attempted
        return insert ? phase.volume()/speciesAgent.getNMolecules() 
                      : (speciesAgent.getNMolecules()+1)/phase.volume();        
    }
    
    public double getB() {
        if(insert) {
            energyMeter.setTarget(testMolecule);
            uNew = energyMeter.getDataAsScalar();
            return (+mu - uNew);
        }
        uNew = 0.0;
        return (-mu + uOld);
    }
    
    public void acceptNotify() {
        if(!insert) {
            // accepted deletion - remove from phase and add to reservoir 
            speciesAgent.removeChildAtom(testMolecule);
            reservoir.add(testMolecule);
        }
    }
    
    public void rejectNotify() {
        if(insert) {
            // rejected insertion - remove from phase and return to reservoir
            speciesAgent.removeChildAtom(testMolecule);
            reservoir.add(testMolecule);
            // test molecule is no longer in the simulation and should not be 
            // returned by affectedAtoms
            testMolecule = null;
        }
    }
    
    public double energyChange() {return uNew - uOld;}

    /**
     * Returns an iterator giving molecule that is being added or deleted 
     * in the current or most recent trial.
     */
    public final AtomIterator affectedAtoms() {
        if(testMolecule == null) return AtomIteratorNull.INSTANCE;
        affectedAtomIterator.setAtom(testMolecule);
        return affectedAtomIterator;
    }

    /**
     * Mutator method for the chemical potential of the insertion/deletion species.
     */
    public final void setMu(double mu) {this.mu = mu;}
    
    /**
     * Accessor method for the chemical potential of th insertion/deletion species.
     */
    public final double getMu() {return mu;}
    
    /**
     * Indicates that chemical potential has dimensions of energy.
     */
    public final etomica.units.Dimension getMuDimension() {return etomica.units.Energy.DIMENSION;}
  
}//end of MCMoveInsertDelete