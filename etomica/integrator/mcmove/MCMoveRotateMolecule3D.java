package etomica.integrator.mcmove;
import etomica.Atom;
import etomica.AtomIterator;
import etomica.Phase;
import etomica.PotentialMaster;
import etomica.Simulation;
import etomica.Space;
import etomica.action.AtomActionTransform;
import etomica.atom.iterator.AtomIteratorSinglet;
import etomica.atom.iterator.AtomIteratorTree;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.integrator.MCMove;
import etomica.space3d.RotationTensor;
import etomica.space3d.Vector3D;


public class MCMoveRotateMolecule3D extends MCMove {
    
    private final MeterPotentialEnergy energyMeter;
    protected final AtomIteratorSinglet affectedAtomIterator = new AtomIteratorSinglet();
    
    protected final AtomIteratorTree leafAtomIterator = new AtomIteratorTree();
    
    protected transient double uOld;
    protected transient double uNew = Double.NaN;
    protected transient Atom molecule;
    protected transient Vector3D r0;
    protected transient RotationTensor rotationTensor;
    public int count;
    public int count1;
    public boolean flag = false;
    public boolean flag1 = false;
    private double uOldSave;
    
    public MCMoveRotateMolecule3D(PotentialMaster potentialMaster, Space space) {
        super(potentialMaster, 1);
        energyMeter = new MeterPotentialEnergy(potentialMaster);
        rotationTensor = (RotationTensor)space.makeRotationTensor();
        r0 = (Vector3D)space.makeVector();
       
        setStepSizeMax(Math.PI);
        setStepSizeMin(0.0);
        setStepSize(Math.PI/2.0);
        perParticleFrequency = true;
        energyMeter.setIncludeLrc(false);
        //TODO enable this
        //set directive to exclude intramolecular contributions to the energy
//        iteratorDirective.addCriterion(new IteratorDirective.PotentialCriterion() {
//            public boolean excludes(Potential p) {return (p instanceof Potential1.Intramolecular);}
//        });
    }
     
    public boolean doTrial() {
        Phase phase = phases[0];
        if(phase.moleculeCount()==0) {molecule = null; return false;}
            
        molecule = phase.randomMolecule();
        energyMeter.setTarget(molecule);
        uOld = energyMeter.getDataAsScalar();
        if(uOld < Double.MAX_VALUE) uOldSave = uOld;
        
        double dTheta = (2*Simulation.random.nextDouble() - 1.0)*stepSize;
        rotationTensor.setAxial(dTheta);

        leafAtomIterator.setRoot(molecule);
        leafAtomIterator.reset();
        r0.E(molecule.node.firstLeafAtom().coord.position());
        AtomActionTransform.doAction(leafAtomIterator, r0, rotationTensor);
            
        uNew = Double.NaN;
        return true;
    }//end of doTrial
    
    public double lnTrialRatio() {return 0.0;}
    
    public double lnProbabilityRatio() {
        energyMeter.setTarget(molecule);
        uNew = energyMeter.getDataAsScalar();
        if(uOld > Double.MAX_VALUE) uOld = uOldSave;
        return -(uNew - uOld)/temperature;
    }
    
    public void acceptNotify() {  /* do nothing */
            
    }
    
    public void rejectNotify() {
        leafAtomIterator.reset();
        
        rotationTensor.invert();
        leafAtomIterator.reset();
        AtomActionTransform.doAction(leafAtomIterator, r0, rotationTensor);
        
    }//end of rejectNotify
    
    public double energyChange(Phase phase) {
        if(this.phases[0] != phase) return 0.0;
        return uNew - uOld;
    }

 
    public final AtomIterator affectedAtoms(Phase phase) {
        if(this.phases[0] != phase) return AtomIterator.NULL;
        affectedAtomIterator.setAtom(molecule);
        affectedAtomIterator.reset();
        return affectedAtomIterator;
    }


    /* (non-Javadoc)
     * @see etomica.integrator.MCMove#setPhase(etomica.Phase[])
     */
    public void setPhase(Phase[] p) {
        super.setPhase(p);
        energyMeter.setPhase(p[0]);
    }
}
