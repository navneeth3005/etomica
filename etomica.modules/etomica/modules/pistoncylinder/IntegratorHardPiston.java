package etomica.modules.pistoncylinder;

import etomica.Debug;
import etomica.atom.Atom;
import etomica.integrator.IntegratorHard;
import etomica.phase.Phase;
import etomica.potential.P1HardMovingBoundary;
import etomica.potential.PotentialHard;
import etomica.potential.PotentialMaster;

/**
 * Integrator for DMD with a piston (P1HardMovingBoundary)
 */
public class IntegratorHardPiston extends IntegratorHard {

    /**
     * @param potentialMaster
     * @param potential Potential between piston and every atom in the phase
     */
    public IntegratorHardPiston(PotentialMaster potentialMaster, P1HardMovingBoundary potential) {
        super(potentialMaster);
        pistonPotential = potential;
    }

    public void setup() {
        pistonPotential.setWallPosition(0.0);
        super.setup();
    }
    
    public void doStep() {
        if (pistonUpdateRequested) {
            pistonUpdateRequested = false;
            updatePiston();
        }
        super.doStep();
    }
    
    public void updateAtom(Atom a) {
        PotentialHard collisionPotential = colliderAgent == null ? null : colliderAgent.collisionPotential;
        // actually updates the atom
        super.updateAtom(a);
        // check if the atom hit the piston.  if so, then update every atom with the piston
        if (collisionPotential == pistonPotential) {
            updatePiston();
        }
    }
    
    /**
     * recalculate collision times for all atoms with the wall/piston
     */
    public void updatePiston() {
        atomIterator.reset();
        listToUpdate.clear();
        while (atomIterator.hasNext()) {
            // look for atoms that wanted to collide with the wall and queue up an uplist recalculation for them.
            Atom atom1 = (Atom)atomIterator.next();
            PotentialHard atom1Potential = ((Agent)atom1.ia).collisionPotential;
            if (Debug.ON && Debug.DEBUG_NOW && ((Debug.allAtoms(atom1) && Debug.LEVEL > 1) || (Debug.anyAtom(atom1) && Debug.LEVEL > 2))) {
                System.out.println(atom1+" thought it would collide with the piston");
            }
            if(atom1Potential == pistonPotential) {
                if (Debug.ON && Debug.DEBUG_NOW && (Debug.allAtoms(atom1) || Debug.LEVEL > 1)) {
                    System.out.println("Will update "+atom1+" because it wanted to collide with the piston");
                }
                listToUpdate.add(atom1);
            }

            
            // recalculate collision time for every atom with the wall
            double collisionTime = pistonPotential.collisionTime(atom1,collisionTimeStep);
            if (Debug.ON && Debug.DEBUG_NOW && (Debug.LEVEL > 2 || (Debug.LEVEL > 1 && Debug.anyAtom(atom1)))) {
                System.out.println("collision down time "+collisionTime+" for atom "+atom1+" with null "+pistonPotential.getClass());
            }
            if(collisionTime < Double.POSITIVE_INFINITY) {
                Agent aia = (Agent)atom1.ia;
                if(collisionTime < aia.collisionTime()) {
                    if (Debug.ON && Debug.DEBUG_NOW && (Debug.LEVEL > 2 || Debug.anyAtom(atom1))) {
                        System.out.println("setting down time "+collisionTime+" for atom "+atom1+" with null");
                    }
                    if (aia.collisionPotential != null) {
                        aia.eventLinker.remove();
                    }
                    aia.setCollision(collisionTime, null, pistonPotential);
                    eventList.add(aia.eventLinker);
                }//end if
            }//end if
        }
        
        processReverseList();
    }
    
    public void advanceAcrossTimeStep(double tStep) {
        super.advanceAcrossTimeStep(tStep);
        pistonPotential.advanceAcrossTimeStep(tStep);
    }
    
    public synchronized void pistonUpdateRequested() {
        pistonUpdateRequested = true;
    }
    
    protected double scaleMomenta(Phase aPhase) {
        // Force a piston update after scaling momenta since the collision
        // time correction won't work (piston velocity/acceleration are unchanged)
        pistonUpdateRequested = true;
        return super.scaleMomenta(aPhase);
    }
    
    private final P1HardMovingBoundary pistonPotential;
    private boolean pistonUpdateRequested = false;
}
