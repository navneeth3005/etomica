package etomica.util;

import etomica.atom.AtomPair;
import etomica.atom.AtomSet;
import etomica.atom.IAtom;
import etomica.atom.IAtomLeaf;
import etomica.atom.iterator.AtomIteratorTreeBox;
import etomica.box.Box;

/**
 * Class holding static fields that determine whether debugging is on, how
 * much, what types and what (if anything) should be looked at specifically.
 * @author andrew
 */

public final class Debug {

    protected static AtomPair debugPair = null;
    
	/**
	 * true if any debugging should be done
	 */
	public static final boolean ON = false;
        
	/**
	 * what step of the integrator debugging should start from.
	 * to get debugging to start before Integrator.run, explicitly
	 * set DEBUG_NOW to true. 
	 */
	public static int START = 0;
	
	/**
	 * what step of the integrator should the simulation bail
	 * set this to prevent Etomica from running for a long time while 
	 * outputting debugging information (potentially filling up the disk)  
	 */
	public static int STOP = -1;
	
	/**
	 * debugging level.  A higher level means more debugging
	 * performance should suffer as the level goes up, and
	 * finding useful information might get difficult.
	 */
	public static int LEVEL = 1;
	
    /**
     * true if debugging is currently enabled (when the integrator reaches step START) 
     */
    public static boolean DEBUG_NOW = false;

	/**
	 * Global index of first atom of interest.  More debugging information will be
	 * printed out about this particular atom.  -1 indicates no particular atom.
	 */
	public static final int ATOM1_INDEX = -1;
	
	/**
	 * Global index of second atom of interest.  This is often used in conjunction with 
	 * ATOM1_INDEX to collect information about a pair of atoms.  -1 indicates no
	 * particular atom.  
	 */
	public static final int ATOM2_INDEX = -1;
	
    /**
     * Global index of a molecule of interest.  More debugging information will be
     * printed out about atoms within this molecule.  -1 indicates no
     * particular atom.  
     */
    public static final int MOLECULE1_INDEX = -1;

    /**
     * Global index of a molecule of interest.  This is often used in conjunction with 
     * MOLECULE2_INDEX to collect information about a pair of molecules.  -1 indicates no
     * particular atom.  
     */
    public static final int MOLECULE2_INDEX = -1;
    
    /**
     * index of box of interest.  -1 indicates no particular box.
     */
    public static final int BOX_INDEX = 0;
    
    /**
     * The minimum allowable distance between Atoms.
     */
    public static final double ATOM_SIZE = 1.0;
    
	/**
	 * determines whether any of the atoms in the given array are set to be debugged
	 * (via ATOMx_NUM and setAtoms(box)).
	 * @param atoms array of atoms to be checked for debugging status
	 * @return true if any of the atoms in the atoms array should be debugged
	 */
	public static boolean anyAtom(AtomSet atoms) {
		for (int i=0; i<atoms.getAtomCount(); i++) {
            int globalIndex = atoms.getAtom(i).getGlobalIndex();
			if ((ATOM1_INDEX > -1 && globalIndex == ATOM1_INDEX) || (ATOM2_INDEX > -1 && globalIndex == ATOM2_INDEX)) return true;
            IAtom molecule = atoms.getAtom(i);
            if (molecule instanceof IAtomLeaf) {
                molecule = ((IAtomLeaf)molecule).getParentGroup();
            }
            globalIndex = molecule.getGlobalIndex();
            if ((MOLECULE1_INDEX > -1 && globalIndex == MOLECULE1_INDEX) || (MOLECULE2_INDEX > -1 && globalIndex == MOLECULE2_INDEX)) return true;
		}
		return false;
	}

	/**
	 * determines if all of the atoms in the given array are set to be debugged
	 * (via ATOMx_NUM and setAtoms(box)).
	 * @param atoms array of atoms to be checked for debugging status
	 * @return true if all of the atoms in the atoms array should be debugged
	 */
	public static boolean allAtoms(AtomSet atoms) {
		for (int i=0; i<atoms.getAtomCount(); i++) {
            int globalIndex = atoms.getAtom(i).getGlobalIndex();
			if (globalIndex == ATOM1_INDEX || globalIndex == ATOM2_INDEX) {
			    continue;
			}
            IAtom molecule = atoms.getAtom(i);
            if (molecule instanceof IAtomLeaf) {
                molecule = ((IAtomLeaf)molecule).getParentGroup();
            }
            globalIndex = molecule.getGlobalIndex();
            if (globalIndex == MOLECULE1_INDEX && globalIndex != MOLECULE2_INDEX) {
                continue;
            }
            return false;
		}
		return true;
	}

    /**
     * Checks whether the given box is of debugging interest
     * @param box to be checked
     * @return true if the box is of interest
     */
    public static boolean thisBox(Box box) {
         return box.getIndex() == BOX_INDEX;
    }
    
    /**
     * Returns an AtomPair containing the two atoms with global indices
     * ATOM1_INDEX and ATOM2_INDEX within the given box.  The atom in
     * the AtomPair will be null if the box does not contain an Atom 
     * with the proper global index.
     */
    public static AtomPair getAtoms(Box box) {
        if (debugPair == null) {
            debugPair = new AtomPair();
        }
        if (ATOM1_INDEX > -1 || ATOM2_INDEX > -1) {
            AtomIteratorTreeBox iterator = new AtomIteratorTreeBox(box,Integer.MAX_VALUE,true);
            iterator.reset();
            for (IAtom atom = iterator.nextAtom(); atom != null;
                 atom = iterator.nextAtom()) {
                if (atom.getGlobalIndex() == ATOM1_INDEX) {
                    debugPair.atom0 = atom;
                    if (debugPair.atom1 != null || ATOM2_INDEX < 0) {
                        break;
                    }
                }
                if (atom.getGlobalIndex() == ATOM2_INDEX) {
                    debugPair.atom1 = atom;
                    if (debugPair.atom0 != null || ATOM1_INDEX < 0) {
                        break;
                    }
                }
            }
        }
        if (debugPair.atom0 == null || debugPair.atom1 == null) return null;
        return debugPair;
    }
    
    /**
     * Returns an AtomPair containing the two atoms with global indices
     * ATOM1_INDEX and ATOM2_INDEX within the given box.  The atom in
     * the AtomPair will be null if the box does not contain an Atom 
     * with the proper global index.
     */
    public static IAtom getAtom1(Box box) {
        if (ATOM1_INDEX > -1) {
            AtomIteratorTreeBox iterator = new AtomIteratorTreeBox(box,Integer.MAX_VALUE,true);
            iterator.reset();
            for (IAtom atom = iterator.nextAtom(); atom != null;
                 atom = iterator.nextAtom()) {
                if (atom.getGlobalIndex() == ATOM1_INDEX) {
                    return atom;
                }
            }
        }
        return null;
    }

}
