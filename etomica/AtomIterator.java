/* History
 * Created on Aug 4, 2004
 */
package etomica;

/**
 * Interface for classes that loop over a set of atoms. Permits
 * iteration via a hasNext()-next() while loop (iterator returns
 * atoms to client) or via a call to allAtoms(AtomActive) (client gives
 * action to iterator).
 */

public interface AtomIterator {
    
	/**
	 * Indicates whether the atom is among those returned by the iterator.
	 * @param atom the atom in question
	 * @return true if the atom is among the iterates; false if
	 * otherwise, or if atom is null.
	 */
    public boolean contains(Atom atom);
    
    /**
     * Indicates whether the iterator has another atom.
     */
    public boolean hasNext();
    
    /**
     * Resets the iterator to loop through its iterates again.
     */
    public void reset();
    
	/**
	 * Puts iterator in a state in which next() returns null.
	 */
    public void unset();
    
	/**
	 * Returns the next atom in the iteration sequence.
	 * No specific behavior is guaranteed if hasNext() == false 
	 * at the time method is called, except that calling next()
	 * will not cause hasNext() to become true.
	 */
    public Atom next();
    
    /**
     * Returns the next iterate without advancing the iterator.
     * No specific behavior is guaranteed if hasNext() == false 
     * at the time method is called.
     */
    public Atom peek();
    
    /**
     * Performs given actions over all the iterates of this iterator in its
     * current state.  The state of hasNext() is not relevant to this method,
     * and no call to reset() is needed beforehand. The set of atoms encountered 
     * in the allAtoms call should be the same set that would be returned by 
     * looping using hasNext/next.
     */
    public void allAtoms(AtomActive action);

    /**
     * The number of iterates returned by this iterator in its current state.
     */
    public int size(); 

    /**
     * Static iterator that returns no atoms.
     * @author kofke
     */
    public static AtomIterator NULL = new AtomIterator() {
    	public void allAtoms(AtomActive action) {}
    	public boolean contains(Atom atom) {return false;}
    	public boolean hasNext() {return false;}
    	public Atom next() {return null;}
    	public void reset() {}
    	public int size() {return 0;}
    	public Atom peek() {return null;}
    	public void unset() {}
    };
}
