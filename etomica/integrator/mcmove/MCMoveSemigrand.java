package etomica.integrator.mcmove;

import etomica.action.AtomActionTranslateTo;
import etomica.atom.AtomArrayList;
import etomica.atom.AtomPositionCOM;
import etomica.atom.AtomPositionDefinition;
import etomica.atom.AtomSet;
import etomica.atom.IMolecule;
import etomica.atom.iterator.AtomIterator;
import etomica.atom.iterator.AtomIteratorArrayListSimple;
import etomica.box.Box;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.potential.PotentialMaster;
import etomica.species.Species;
import etomica.util.IRandom;

/**
 * Basic Monte Carlo move for semigrand-ensemble simulations.  Move consists
 * of selecting a molecule at random and changing its species identity.  More precisely,
 * the molecule is removed and another molecule of a different species replaces it.
 * An arbitrary number of species may be designated as subject to these exchange moves.
 * Acceptance is regulated by a set of fugacity fractions that are specified at design time.
 *
 * @author Jhumpa Adhikari
 * @author David Kofke
 */
public class MCMoveSemigrand extends MCMoveBox {
    
    private static final long serialVersionUID = 2L;
    private Species[] speciesSet;
    private AtomArrayList[] reservoirs;
    private double[] fugacityFraction;
    private int nSpecies;
    private final AtomArrayList affectedAtomList;
    private final AtomIteratorArrayListSimple affectedAtomIterator; 
    private final MeterPotentialEnergy energyMeter;
    private final AtomActionTranslateTo moleculeTranslator;
    private AtomPositionDefinition atomPositionDefinition;
    private final IRandom random;
    
    private transient IMolecule deleteMolecule, insertMolecule;
    private transient double uOld;
    private transient double uNew = Double.NaN;
    private transient int iInsert, iDelete;

    public MCMoveSemigrand(PotentialMaster potentialMaster, IRandom random) {
        super(potentialMaster);
        this.random = random;
        energyMeter = new MeterPotentialEnergy(potentialMaster);
        affectedAtomList = new AtomArrayList(2);
        affectedAtomIterator = new AtomIteratorArrayListSimple(affectedAtomList);
        affectedAtomIterator.setList(affectedAtomList);
        perParticleFrequency = true;
        energyMeter.setIncludeLrc(true);
        moleculeTranslator = new AtomActionTranslateTo(potentialMaster.getSpace());
        setAtomPositionDefinition(new AtomPositionCOM(potentialMaster.getSpace()));
    }
    
    /**
     * Extends the superclass method to initialize the exchange-set species agents for the box.
     */
    public void setBox(Box p) {
        super.setBox(p);
        energyMeter.setBox(box);
    }//end setBox
    
    /**
     * Mutator method for the set of species that can participate in an exchange move.
     */
    public void setSpecies(Species[] species) {
        nSpecies = species.length;
        if(nSpecies < 2) throw new IllegalArgumentException("Wrong size of species array in MCMoveSemigrand");
        speciesSet = new Species[nSpecies];
        fugacityFraction = new double[nSpecies];
        reservoirs = new AtomArrayList[nSpecies];
        for(int i=0; i<nSpecies; i++) {
            speciesSet[i] = species[i];
            fugacityFraction[i] = 1.0/nSpecies;
            reservoirs[i] = new AtomArrayList();
        }
    }
    
    /**
     * Accessor method for the set of species that can participate in an exchange move.
     */
    public Species[] getSpecies() {return speciesSet;}
    
    /**
     * Specifies the fugacity fractions for the set of species that can participate in
     * an exchange move.  The given array must have the same dimension as the array of
     * species that was previously set in a call to setSpecies.  If the given set of "fractions"
     * does not sum to unity, the values will be normalized (e.g., sending the set {1.0, 1.0} 
     * leads to fugacity fractions of {0.5, 0.5}).
     */
    public void setFugacityFraction(double[] f) {
        if(f.length != nSpecies || speciesSet == null) 
            throw new IllegalArgumentException("Wrong size of fugacity-fraction array in MCMoveSemigrand");
            
        double sum = 0.0;
        for(int i=0; i<nSpecies; i++) {
            fugacityFraction[i] = f[i]; 
            if(f[i] < 0.0) throw new IllegalArgumentException("Negative fugacity-fraction MCMoveSemigrand");
            sum += f[i];
        }
        for(int i=0; i<nSpecies; i++) {fugacityFraction[i] /= sum;}//normalize to unity
    }

    public double getFugacityFraction(int i) {
        if(i < 0 || i >= nSpecies) 
            throw new IllegalArgumentException("Illegal fugacity-fraction index in MCMoveSemigrand");
        return fugacityFraction[i];
    }

    /**
     * Accessor method for the set of fugacity fractions.
     */
    public double[] getFugacityFraction() {return fugacityFraction;}
    
    public boolean doTrial() {
        //select species for deletion
        iDelete = random.nextInt(nSpecies);//System.out.println("Random no. :"+randomNo);
        if(box.getNMolecules(speciesSet[iDelete]) == 0) {
            uNew = uOld = 0.0;
            return false;
        }

        //select species for insertion
        iInsert = iDelete;
        if(nSpecies == 2) iInsert = 1 - iDelete;
        else while(iInsert == iDelete) {iInsert = random.nextInt(nSpecies);}
  
        AtomSet moleculeList = box.getMoleculeList(speciesSet[iDelete]);
        deleteMolecule = (IMolecule)moleculeList.getAtom(random.nextInt(moleculeList.getAtomCount()));
        energyMeter.setTarget(deleteMolecule);
        uOld = energyMeter.getDataAsScalar();
        box.removeMolecule(deleteMolecule);
        
        int size = reservoirs[iInsert].getAtomCount();
        if(size>0) {
            insertMolecule = (IMolecule)reservoirs[iInsert].remove(size-1);
            box.addMolecule(insertMolecule);
        }
        else {
            insertMolecule = (IMolecule)box.addNewMolecule(speciesSet[iInsert]);
        }
        moleculeTranslator.setDestination(atomPositionDefinition.position(deleteMolecule));
        moleculeTranslator.actionPerformed(insertMolecule);
        //in general, should also randomize orintation and internal coordinates
        uNew = Double.NaN;
        return true;
    }//end of doTrial
    
    public double getA() {
        return (double)(box.getNMolecules(speciesSet[iDelete])+1)/(double)box.getNMolecules(speciesSet[iInsert])
                *(fugacityFraction[iInsert]/fugacityFraction[iDelete]);
    }
    
    public double getB() {
        energyMeter.setTarget(insertMolecule);
        uNew = energyMeter.getDataAsScalar();
        return -(uNew - uOld);
    }
    
    public void acceptNotify() {
        //put deleted molecule in reservoir
        reservoirs[iDelete].add(deleteMolecule);
    }

    public void rejectNotify() {
        //put deleted molecule back into box
        box.addMolecule(deleteMolecule);
        //remove inserted molecule and put in reservoir
        box.removeMolecule(insertMolecule);
        reservoirs[iInsert].add(insertMolecule);
    }
    
    

    public double energyChange() {return uNew - uOld;}
    
    public final AtomIterator affectedAtoms() {
        
        affectedAtomList.clear();
        affectedAtomList.add(insertMolecule);
        affectedAtomList.add(deleteMolecule);
        affectedAtomIterator.reset();
        return affectedAtomIterator;
    }

    /**
     * @return Returns the positionDefinition.
     */
    public AtomPositionDefinition geAtomPositionDefinition() {
        return atomPositionDefinition;
    }
    /**
     * @param positionDefinition The positionDefinition to set.
     */
    public void setAtomPositionDefinition(AtomPositionDefinition positionDefinition) {
        this.atomPositionDefinition = positionDefinition;
        moleculeTranslator.setAtomPositionDefinition(positionDefinition);
    }

}