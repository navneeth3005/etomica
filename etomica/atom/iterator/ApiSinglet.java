package etomica.atom.iterator;

import etomica.Atom;
import etomica.AtomPair;
import etomica.AtomPairIterator;
import etomica.AtomSet;
import etomica.action.AtomsetAction;

/**
 * Iterator that expires after returning a single atom pair, which is
 * specified by a call to the setPair methods, or via the constructor.
 * Subsequent calls to reset() and next() will return the specified atoms,
 * until another is specified via setPair.
 *
 * @author David Kofke
 */
 
public final class ApiSinglet implements AtomPairIterator {
    
    /**
     * Constructs iterator without defining atoms in pair.
     */
    public ApiSinglet() {
        pair = new AtomPair();
        expired = true;
    }
    
    /**
     * Defines atoms returned by iterator and leaves iterator unset.
     * Call to reset() must be performed before beginning iteration.
     */
    public void setPair(Atom a0, Atom a1) {
    	pair.atom0 = a0;
        pair.atom1 = a1;
    	unset();
    }

    /**
     * @return the pair given by this iterator as its single iterate
     */
    public AtomPair getPair() {
        return pair;
    }
    
    /**
     * returns 1, the number of iterates that would be returned on reset.
     */
    public int size() {return (pair.atom0 == null) || (pair.atom1 == null)  ? 0 : 1;}

	public void allAtoms(AtomsetAction action) {
		if(pair.atom0 != null && pair.atom1 != null) action.actionPerformed(pair);
	}
        
    /**
     * Returns true if the given atom equals the atom passed to the last call to setAtom(Atom).
     */
    public boolean contains(AtomSet a) {
    	return (a != null && a.equals(pair));
    }
    
    /**
     * Returns true if the an atom has been set and a call to reset() has been
     * performed, without any subsequent calls to next().
     */
    public boolean hasNext() {
        return !expired && (pair.atom0 != null) && (pair.atom1 != null); 
    }
    
    /**
     * Sets iterator to a state where hasNext() returns false.
     */
    public void unset() {
        expired = true;
    }
    
    /**
     * Resets iterator to a state where hasNext is true.
     */
    public void reset() {
        expired = false; 
    }
    
    /**
     * Returns the iterator's pair and unsets iterator.
     */
    public AtomPair nextPair() {
    	if (!hasNext()) return null;
    	expired = true;
    	return pair;
    }
    
    public AtomSet next() {
        return nextPair();
    }
    
    /**
     * Returns the atom last specified via setAtom.  Does
     * not advance iterator.
     */
    public AtomSet peek() {
    	return hasNext() ? pair : null;
    }
    
    /**
     * Returns 2, indicating that this is a pair iterator.
     */
    public final int nBody() {return 2;}
    
    private boolean expired;
    private final AtomPair pair;

}//end of ApiSinglet
