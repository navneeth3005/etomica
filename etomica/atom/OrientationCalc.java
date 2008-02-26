package etomica.atom;

import etomica.space3d.IOrientationFull3D;

/**
 * Interface for a class that can calculate the orientation of a molecule or
 * to set the orientation of a molecule.
 *
 * @author Andrew Schultz
 */
public interface OrientationCalc {

    /**
     * Calculates the orientation of the given molecule and stores that
     * in the given orientation.
     */
    public void calcOrientation(IMolecule molecule, IOrientationFull3D orientation);
    
    /**
     * Sets the orientation of the given molecule to be equal to the given
     * orientation.  This typically involves changing the position of the atoms
     * without changing the molecules position.
     */
    public void setOrientation(IMolecule molecule, IOrientationFull3D orientation);
}
