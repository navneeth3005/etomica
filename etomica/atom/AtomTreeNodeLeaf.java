package etomica.atom;


/**
 * Leaf node in the tree of atoms.  Differs from group node in having firstChild, lastChild,
 * firstLeafAtom, lastLeafAtom, all given as this node's atom.  Having firstChild
 * and lastChild point to itself is useful in looping through interactions between a leaf
 * and a group.
 */

public final class AtomTreeNodeLeaf extends AtomTreeNode {
    
    /**
     * Linker used to form a list of all leaf atoms in the phase.
     * List is maintained by the speciesMaster node.
     */
    public final AtomLinker leafLinker;
    private int leafIndex;
    
    public AtomTreeNodeLeaf(Atom atom) {
        super(atom);
        leafLinker = new AtomLinker(atom);
    }
    
    public boolean isLeaf() {return true;}
    
    /**
     * Returns this node's atom.
     */
    public AtomLeaf firstLeafAtom() {return (AtomLeaf)atom;}
    
    /**
     * Returns this node's atom.
     */
    public AtomLeaf lastLeafAtom() {return (AtomLeaf)atom;}
    
    /**
     * Returns 1.
     */
    public int leafAtomCount() {return 1;}
    
    /**
     * Returns 0.
     */
    public int childAtomCount() {return 0;}

    public final void setLeafIndex(int newLeafIndex) {
        leafIndex = newLeafIndex;
    }
    
    public final int getLeafIndex() {
        return leafIndex;
    }
    
    public static final AtomTreeNodeFactory FACTORY = new AtomTreeNodeLeaf.Factory();
    
    private static final class Factory implements AtomTreeNodeFactory, java.io.Serializable {
        public AtomTreeNode makeNode(Atom atom) {
            return new AtomTreeNodeLeaf(atom);
        }
    }
}