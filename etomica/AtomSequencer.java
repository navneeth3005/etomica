package etomica;

/**
 * AtomSequencer is used to structure all the atoms in a phase into
 * a well defined order.  A single instance of this class is held by
 * the seq field of each atom, and it is the primary point of reference
 * for structuring lists of child atoms in each atom group.  Most of 
 * the iterators that loop through the atoms in the phase use the list
 * order set up using the sequencer.
 *
 * @author David Kofke
 * @version 02.03.09
 */

public abstract class AtomSequencer extends AtomLinker {
    
    public AtomSequencer(Atom a) {super(a);}
    
    /**
     * Notifies sequencer that atom has been moved to a new position
     * in the simulation volume.  Called most often by the translate
     * methods of Coordinate.
     */
    public abstract void moveNotify();
    
    /**
     * Notifies sequencer that the parent group of the atom has been
     * changed.  Called by the setParent method of AtomTreeNode.
     */
    public abstract void setParentNotify(AtomTreeNodeGroup newParent);
    
    public abstract boolean preceeds(Atom a);
    
    public interface Factory {
        public AtomSequencer makeSequencer(Atom atom);
    }
    
}