package etomica.virial;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.ISimulation;
import etomica.api.IVectorMutable;
import etomica.integrator.mcmove.MCMoveMolecule;
import etomica.space.ISpace;

/**
 * Monte Carlo molecule-displacement from 1 layer to the other layer trial move for cluster integrals.
 * 
 * copied and modified from MCMoveClusterMolecule class
 * for virial coefficients calculation of phenanthrene and anthracene 
 * for n = 4 , the molecules tend to form either 2 layers or 3 layers structures 
 * add this monte carlo move, force one molecule to move out the existing layer to the other layer to explore more configurations
 * @author shu
 * date : april 27 2011
 */
public class MCMoveClusterMoleculeLayerMove extends MCMoveClusterMolecule {
    private static final long serialVersionUID = 1L;
    protected final IVectorMutable vector1 , vector2 , crossVector; //crossVector = vector1 * vector2
    public MCMoveClusterMoleculeLayerMove(ISimulation sim, ISpace _space) {
    	super (sim.getRandom(), _space, 1.0); //superclass parameter stepsize
    	
    	vector1     =  space.makeVector(); // Initialize 
    	vector2     =  space.makeVector();// Initialize 
    	crossVector =  space.makeVector(); // Initialize 
    }
      
    public boolean doTrial() {
    	if(box.getMoleculeList().getMoleculeCount()==1) return false;
        
        molecule = moleculeSource.getMolecule();
        while (molecule.getIndex() == 0) {
            molecule = moleculeSource.getMolecule();// keep on picking up molecules randomly if picking up the molecule in the origin
        }
        
        uOld = ((BoxCluster)box).getSampleCluster().value((BoxCluster)box); // calculate integrand (-1/2) * f 12 , i.e. B2 
       
         // generate a random integer, 0 or 1 call nextInt in IRandom interfce
        int direction = random.nextInt(2);/////tai :cannot be the same 
       // now the  _random value is either 0 or 1
       // if is 0, then the chosen molecule is moved toward the adjacent plane 
        IAtomList atoms = molecule.getChildList(); // get atoms
     // public IAtom getAtom(int i);
        IAtom atom0 = atoms.getAtom(0);
        IAtom atom1 = atoms.getAtom(1);
        IAtom atom2 = atoms.getAtom(2);
      //public IVectorMutable getPosition();
        IVectorMutable position0 = atom0.getPosition();
        IVectorMutable position1 = atom1.getPosition();
        IVectorMutable position2 = atom2.getPosition();
        vector1.Ev1Mv2(position1, position0);
        vector2.Ev1Mv2(position2, position1);
        crossVector.E(vector1);
        crossVector.XE(vector2);
        crossVector.normalize();
        crossVector.TE(3.85 * (1 + random.nextInt(2)));
        groupTranslationVector.E(crossVector);
        if (direction == 0 ){
        	groupTranslationVector.TE(-1);
        }
        moveMoleculeAction.actionPerformed(molecule);
        //groupTranslationVector.setRandomCube(random); //  random unit vector
      //  groupTranslationVector.TE(stepSize);  // multiply by a number, this will be tuned to get a 50 % trail move acceptance
      //  moveMoleculeAction.actionPerformed(molecule); // execute???????????
       
        
        ((BoxCluster)box).trialNotify();
        uNew = ((BoxCluster)box).getSampleCluster().value((BoxCluster)box);
        return true;
    }
    
    
        
}