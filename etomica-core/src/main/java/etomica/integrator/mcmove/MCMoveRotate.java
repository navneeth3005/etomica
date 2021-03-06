/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.integrator.mcmove;

import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.atom.IAtomOriented;
import etomica.space.IOrientation;
import etomica.space.ISpace;
import etomica.space3d.Orientation3D;

/**
 * Performs a rotation of an atom (not a molecule) that has an orientation coordinate.
 */
public class MCMoveRotate extends MCMoveAtom {
    
    private IOrientation oldOrientation;

    private transient IOrientation iOrientation;

    public MCMoveRotate(IPotentialMaster potentialMaster, IRandom random,
    		            ISpace _space) {
        super(potentialMaster, random, _space, Math.PI/2, Math.PI, false);
    }
    
    public void setBox(IBox box) {
        super.setBox(box);
        if (oldOrientation != null) return;
        IAtomList atoms = box.getLeafList();
        if (atoms.getAtomCount() == 0) return;
        IAtomOriented atom0 = (IAtomOriented)atoms.getAtom(0);
        if (atom0.getOrientation() instanceof Orientation3D) {
            oldOrientation = new Orientation3D(space);
        }
        else {
            oldOrientation = space.makeOrientation();
        }
    }

    public boolean doTrial() {
        if(box.getMoleculeList().getMoleculeCount()==0) {return false;}
        atom = atomSource.getAtom();

        energyMeter.setTarget(atom);
        uOld = energyMeter.getDataAsScalar();
        iOrientation = ((IAtomOriented)atom).getOrientation(); 
        oldOrientation.E(iOrientation);  //save old orientation
        iOrientation.randomRotation(random, stepSize);
        uNew = energyMeter.getDataAsScalar();
        return true;
    }
    
    public void rejectNotify() {
        iOrientation.E(oldOrientation);
    }
}
