package etomica.potential;

import etomica.space.ISpace;

/**
 * Soft-spherical potential class that shifts both the potential energy and the
 * force such that both are 0 at the cutoff.  The potential is of the form
 * U = U_LJ + Ar + B
 * 
 * @author Andrew Schultz
 */
public class P2SoftSphericalTruncatedForceShifted extends
        P2SoftSphericalTruncatedShifted {

    public P2SoftSphericalTruncatedForceShifted(ISpace _space,
            Potential2SoftSpherical potential, double truncationRadius) {
        super(_space, potential, truncationRadius);
    }

    /**
     * Mutator method for the radial cutoff distance.
     */
    public void setTruncationRadius(double rCut) {
        super.setTruncationRadius(rCut);
        fShift = potential.du(r2Cutoff);
        shift = potential.u(r2Cutoff) + fShift*Math.sqrt(r2Cutoff);
    }
    
    public double u(double r2) {
        return (r2 < r2Cutoff) ? (potential.u(r2) - fShift*Math.sqrt(r2) - shift) : 0.0;
    }
    
    public double du(double r2) {
        return (r2 < r2Cutoff) ? (potential.du(r2) - fShift) : 0.0;
    }

    protected double fShift;
}
