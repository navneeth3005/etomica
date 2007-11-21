package etomica.atom.iterator;

import etomica.action.AtomAction;
import etomica.action.AtomsetAction;
import etomica.action.AtomsetCount;
import etomica.atom.AtomSet;
import etomica.atom.AtomSetSinglet;
import etomica.atom.IAtom;
import etomica.atom.IMolecule;

/**
 * Atom iterator that traverses all atoms at or to a specified depth below a
 * specified root atom in the atom tree hierarchy. The iterator is conditioned
 * using the following parameters:
 * <ul>
 * <li>root: atom that provides the point of reference for the iteration.  Atoms
 * below the iteration-root atom are subject to iteration.  The iteration-root
 * does not have to be the root of the species tree; any atom in the species tree
 * can serve as the root of iteration.
 * <li>depth: any non-negative integer, indicating how many levels below the root
 * can iterates be taken.  The value 0 indicates iteration of the root atom only, 
 * 1 is children of the root atom, etc. If the bottom of the hierarchy is reached 
 * before the specified depth, the leaf atoms encountered there are the iterates.
 * If the tree has branches that are deeper in some parts than in others, the deepest
 * iterates of each branch are taken up to the specified depth.  A very large value
 * of depth causes all leaf atoms below the root to be iterated.
 * <li>doAllNodes: flag indicating whether iteration is inclusive of all atoms
 * between the root and those at the iteration depth. If false, the atoms only 
 * at the specified depth (or leaf atoms if depth exceeds depth of tree) are iterated;
 * if true, all atoms from (and including) the root atom to those at the iteration
 * depth are iterated.  For example, if depth is 1, then doAllNodes == true indicates
 * iteration of both the root and its child atoms; doAllNodes == false indicates
 * only the child atoms are iterated. 
 * <ul>
 * 
 * @author David Kofke and Andrew Schultz
 */
   
public abstract class AtomIteratorTree implements AtomIterator, java.io.Serializable {
    
    /**
     * Constructor permitting specification of all conditions.  Requires
     * reset before beginning iteration.
     * @param root          iteration root 
     * @param depth         nominal depth of iteration
     * @param doAllNodes    flag for iteration of all nodes between root and depth, inclusive
     */
    protected AtomIteratorTree(IAtom root, int depth, boolean doAllNodes) {
        setIterationDepth(depth);
        if (root != null) {
            setRootAtom(root);
        }
        setDoAllNodes(doAllNodes);
        counter = new AtomsetCount();
        atomSetSinglet = new AtomSetSinglet();
    }
    
    /**
     * Performs action on all iterates in current condition.  Unaffected
     * by reset status. Clobbers iteration state.
     */
    public void allAtoms(AtomsetAction act) {
        AtomSet list = ((IMolecule)rootAtom).getChildList();
        int nAtoms = list.getAtomCount();
        for (int iAtom=0; iAtom<nAtoms; iAtom++) {
            IAtom atom = list.getAtom(iAtom);
            if (!(atom instanceof IMolecule) || iterationDepth == 1) {
                atomSetSinglet.atom = atom;
                act.actionPerformed(atomSetSinglet);
                continue;
            }
            
            if (doAllNodes) {
                atomSetSinglet.atom = atom;
                act.actionPerformed(atomSetSinglet);
            }

            AtomSet childList = ((IMolecule)atom).getChildList();
            for (int iChild=0; iChild<childList.getAtomCount(); iChild++) {
                atomSetSinglet.atom = childList.getAtom(iChild);
                act.actionPerformed(atomSetSinglet);
            }
		}
    	unset();
    }
    
    /**
     * Performs action on all iterates in current condition.  Unaffected
     * by reset status. Clobbers iteration state.
     */
    public void allAtoms(AtomAction act) {
        AtomSet list = ((IMolecule)rootAtom).getChildList();
        int nAtoms = list.getAtomCount();
        for (int iAtom=0; iAtom<nAtoms; iAtom++) {
            IAtom atom = list.getAtom(iAtom);
            if (!(atom instanceof IMolecule) || iterationDepth == 1) {
                act.actionPerformed(atom);
                continue;
            }
            
            if (doAllNodes) {
                act.actionPerformed(atom);
            }

            AtomSet childList = ((IMolecule)atom).getChildList();
            for (int iChild=0; iChild<childList.getAtomCount(); iChild++) {
                act.actionPerformed(childList.getAtom(iChild));
            }
        }
        unset();
    }
    /**
     * Puts iterator in state in which hasNext is false.
     */
    public void unset() {
        listIterator.unset();
        if (childListIterator != null) childListIterator.unset();
    }
    
    /**
     * Reinitializes the iterator according to the most recently specified basis,
     * iteration depth, and doAllNodes flag.
     */
    public void reset() {
        listIterator.setList(((IMolecule)rootAtom).getChildList());
        listIterator.reset();

        if (childListIterator != null) childListIterator.unset();
    }

    /**
     * Returns the next atom in the iteration sequence.
     */
    public IAtom nextAtom() {
        if (childListIterator != null) {
            // If we're in the middle of returning iterates from the tree
            // then continue doing so until the sub-tree iterator runs out.
            IAtom nextAtom = childListIterator.nextAtom();
            if (nextAtom != null) {
                return nextAtom;
            }
        }
        for (IAtom atom = listIterator.nextAtom(); atom != null;
             atom = listIterator.nextAtom()) {
            if (!(atom instanceof IMolecule) || iterationDepth == 1) {
                return atom;
            }
            if (childListIterator == null) {
                childListIterator = new AtomIteratorArrayListSimple();
            }
            childListIterator.setList(((IMolecule)atom).getChildList());
            childListIterator.reset();

            if (doAllNodes) {
                // tree iterator won't return its root, so return that now
                // we'll return the tree iterator's iterates next call
                return atom;
            }

            // we're only interested in iterates below our own level
            IAtom nextAtom = childListIterator.nextAtom();
            if(nextAtom != null) {
                return nextAtom;
            }
        }
        return null;
    }

    /**
     * Returns the next atom in the iteration sequence.  Same as nextAtom().
     */
    public AtomSet next() {
        atomSetSinglet.atom = nextAtom();
        if (atomSetSinglet.atom == null) {
            return null;
        }
        return atomSetSinglet;
    }

    /**
     * Defines the root of the tree under which the iteration is performed.
     * User must perform a subsequent call to reset() before beginning iteration.
     */
    protected void setRootAtom(IAtom newRootAtom) {
        rootAtom = newRootAtom;
    }
        
    /**
     * Returns the number of iterates given by a full cycle of this iterator
     * in its current condition (independent of current iteration state).
     */
    public int size() {
    	unset();
        counter.reset();
        allAtoms(counter);
    	return counter.callCount();
    }
    
    /**
     * Returns 1, indicating that this is an Atom iterator.
     */
    public final int nBody() {return 1;}
    
    /**
     * Sets the depth below the current root for which the iteration will occur.
     * Any non-negative value is permitted.  A value of zero causes singlet iteration
     * returning just the root atom. A value of 1 returns all children of the root
     * atom, a value of 2 returns all children of all children of the root, etc.
     * If doAllNodes is false, iterator returns only atoms at the specified level, and not those above it.
     * Returns atoms at bottom of hierarchy (i.e., leafs) if specified depth
     * exceeds depth of hierarchy in a particular branch.  
     * Default is Integer.MAX_VALUE, which causes all leaf atoms to be iterated.
     */
    public void setIterationDepth(int depth) {
        if (iterationDepth == depth) return;
        if(depth < 1) throw new IllegalArgumentException("Error: iteration depth must be positive");
        iterationDepth = depth;
        if(childListIterator != null && depth == 1) {
            // drop our childList iterator.  this isn't efficient if someone
            // keeps toggling between depth=1 and depth>1, but that's kinda dumb.
            childListIterator = null;
        }
        unset();
    }

    /**
     * Returns the currently set value of iteration depth.
     */
    public int getIterationDepth() {return iterationDepth;}
    
    /**
     * Convenience method that sets iterator to iterate over all leaf atoms 
     * below the root atom.
     * Equivalent to setDoAllNodes(false) and setIterationDepth(Integer.MAX_VALUE)
     */
    public void setAsLeafIterator() {
        setDoAllNodes(false);
        setIterationDepth(Integer.MAX_VALUE);
    }
    
    /**
     * Accessor method for doAllNodes flag.
     */
	public boolean isDoAllNodes() {
		return doAllNodes;
	}
    
    /**
     * Flag indicating whether iterates are taken only at the iteration depth (false),
     * or whether all atoms between (and including) the root and iteration depth
     * are given (true).
     */
	public void setDoAllNodes(boolean doAllNodes) {
		this.doAllNodes = doAllNodes;
		unset();
	}

    private static final long serialVersionUID = 2L;
    protected IAtom rootAtom;
    protected final AtomIteratorArrayListSimple listIterator = new AtomIteratorArrayListSimple();
    protected AtomIteratorArrayListSimple childListIterator;
    protected int iterationDepth = Integer.MAX_VALUE;
    protected IAtom next;
    protected boolean doAllNodes = false;
    protected final AtomsetCount counter;
    protected final AtomSetSinglet atomSetSinglet;
        
}//end of AtomIteratorTree
