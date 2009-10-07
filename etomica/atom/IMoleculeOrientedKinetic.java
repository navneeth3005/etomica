package etomica.atom;

import etomica.api.IVectorMutable;

/**
 * Interface for an Atom that has a position, orientation, velocity and angular
 * velocity.
 */
public interface IMoleculeOrientedKinetic extends IMoleculeKinetic, IMoleculeOriented {

    //XXX angular velocity is not a vector.  enjoy!
    public IVectorMutable getAngularVelocity(); //angular velocity vector in space-fixed frame
    
}