package etomica.atom;

import etomica.phase.Phase;


/**
 * Abstract node in the atom tree. Atoms are arranged in a tree structure to
 * define collections of molecules, individual molecules, atom groups and atoms.
 * All of these elements are represented by the Atom class, and the structure
 * that forms the larger elements from the smaller ones is maintained by the
 * node class associated with each Atom. The node for each atom holds
 * information about its parent and its children (if any). <br>
 * 
 * (The node for) a SpeciesRoot instance (held by the Simulation) is the root of
 * the atom tree. Directly below it are instances of SpeciesMaster, one of which
 * is associated with each Phase instance. Below this are instances of the
 * SpeciesAgent class, which are constructed for each Phase by each Species.
 * Below the SpeciesAgent are instances of Atoms that represent the physical
 * molecules of the system. The molecules may be leaf atoms, ending the tree, or
 * they may contain child atoms (or other groups of atoms) used to form the
 * molecule. The integer returned by the index method is a code that can be
 * interpreted by the AtomIndexManager (held by the atom's type, and accessed
 * also via some of the methods of Atom) to determine the location and ancestry
 * of an atom in the tree.<br>
 * 
 * To summarize, the depth levels are as follows
 * <ol>
 * <li>SpeciesRoot
 * <li>SpeciesMaster, representing phases
 * <li>SpeciesAgent, representing species in phases
 * <li>Molecules
 * <li>(optional, 0 or more additional depths) atom groups and atoms forming molecules
 * </ol>
 * 
 * @see AtomAddressManager
 */
 
public abstract class AtomTreeNode implements Comparable, java.io.Serializable {
    
    public AtomTreeNode(Atom atom) {
        this.atom = atom;
    }
    
    public abstract boolean isLeaf();
    public abstract AtomLeaf firstLeafAtom();
    
    /**
     * Returns the last leaf atom descended from this group.
     */
    public abstract AtomLeaf lastLeafAtom();
    
    public abstract int leafAtomCount();
    public abstract int childAtomCount();
    
    public void dispose() {
    	setParent((AtomTreeNodeGroup)null);
    }

    public void setParent(Atom parent) {
        setParent(parent==null ? (AtomTreeNodeGroup)null : (AtomTreeNodeGroup)parent.node);
    }
    
    public void setParent(AtomTreeNodeGroup parent) {
    	
        //old parent is not null; remove from it
        if(parentNode != null) {
            int ordinal = atom.node.getOrdinal();
            parentNode.childList.removeAndReplace(ordinal-1);
            if (parentNode.childList.size() > ordinal-1) {
                parentNode.childList.get(ordinal-1).node.setOrdinal(ordinal);
            }
            parentNode.removeAtomNotify(atom);
        }
        
        parentNode = parent;

        if(parentNode == null) {//new parent is null
            return;
        }

        setOrdinal(parentNode.childList.size()+1);
        
        parentNode.childList.add(atom);
        parentNode.addAtomNotify(atom);
    }//end of addAtom

    public Atom atom() {return atom;}
        
    public Atom parentGroup() {
        return (parentNode != null) ? parentNode.atom : null;
    }
    
    public AtomTreeNodeGroup parentNode() {
        return parentNode;
    }

    /**
     * Returns the molecule in which this atom resides.  A "molecule" is an atomgroup
     * that is one step below a species agent in the hierarchy of atomgroups.
     */
    public Atom parentMolecule() {
        if(parentNode == null) return null;
        return (parentNode instanceof SpeciesAgent.AgentAtomTreeNode) ? this.atom : parentNode.parentMolecule();
    }
                
    /**
     * Phase in which this atom resides
     */
    public Phase parentPhase() {
        return (parentNode != null) ? parentNode.parentPhase() : null;
    }
    
    public SpeciesAgent parentSpeciesAgent() {
        return (parentNode != null) ? parentNode.parentSpeciesAgent() : null;
    }
    
    /**
     * Integer assigned to this atom by its parent molecule.
     */
    public final int index() {return atomTreeAddress;}

    public void setOrdinal(int ordinal) {
        setOrdinal((parentNode != null) ? parentNode.index() : 0, ordinal);
    }
    
    public void setOrdinal(int parentIndex, int ordinal) {
        atomTreeAddress = parentIndex + atom.type.getAddressManager().shiftOrdinal(ordinal);
        if (atom.type.getAddressManager().getOrdinal(atomTreeAddress) != ordinal) {
            atom.type.getAddressManager().getOrdinal(atomTreeAddress);
            throw new RuntimeException(atomTreeAddress+" "+ordinal+" "+atom.type.getAddressManager().getOrdinal(atomTreeAddress));
        }
    }
    
    public int getOrdinal() {
//        System.out.println(atom.type.getDepth()+" "+atom.type.getIndexManager().getOrdinal(atomIndex));
//        if(atom.type.getDepth()==4 && atom.type.getIndexManager().getOrdinal(atomIndex)==6) {
//        }
        return atom.type.getAddressManager().getOrdinal(atomTreeAddress);
    }
    
    public int getMoleculeOrdinal() {
        return atom.type.getAddressManager().getMoleculeOrdinal(atomTreeAddress);
    }

    public int getPhaseOrdinal() {
        return atom.type.getAddressManager().getPhaseOrdinal(atomTreeAddress);
    }

    public int getSpeciesOrdinal() {
        return atom.type.getAddressManager().getSpeciesOrdinal(atomTreeAddress);
    }

    public boolean inSimulation() {
        return atomTreeAddress < 0;
    }
    
    /**
     * Returns true if the given atom is in the hierarchy of parents of this atom,
     * or if the given atom is this atom.  Returns true, for example, if the given
     * atom is this atom's parent, or its parent's parent, etc.
     */ 
    public boolean isDescendedFrom(Atom group) {
    	if(group == null) return false;
        return group.type.getAddressManager().sameAncestry(group.node.atomTreeAddress,this.atomTreeAddress);
    }
    
    /**
     * Returns the child of the given node through which this node is derived.
     * If given node is parent node of this, returns this.
     * If this node is not descended from the given node, returns null.
     */
    public AtomTreeNode childWhereDescendedFrom(AtomTreeNode node) {
        if(parentNode == null) return null;
        return (parentNode == node) ? this : parentNode.childWhereDescendedFrom(node);
    }
    
    /**
     * Implementation of Comparable interface.  Returns -1, 0, 1 if given atomTreeNode
     * is less, equal, or greater, respectively, than this node.  Order is determined
     * by comparison of (absolute value of) atomIndex values.
     */
    public int compareTo(Object atomTreeNode) {
        int otherIndex = ((AtomTreeNode)atomTreeNode).atomTreeAddress;
        //use "<" for ">" because atomIndex is negative
        return otherIndex < atomTreeAddress ? 1 : (otherIndex == atomTreeAddress ? 0 : -1);
    }
                
    protected final Atom atom;
    protected int atomTreeAddress;
    private AtomTreeNodeGroup parentNode;
    
}//end of AtomTreeNode
