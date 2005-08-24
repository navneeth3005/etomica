/*
 * History
 * Created on Aug 30, 2004 by kofke
 */
package etomica.nbr.cell;

import etomica.action.AtomsetAction;
import etomica.action.AtomsetCount;
import etomica.action.AtomsetDetect;
import etomica.atom.Atom;
import etomica.atom.AtomLinker;
import etomica.atom.AtomPair;
import etomica.atom.AtomPairVector;
import etomica.atom.AtomSet;
import etomica.atom.iterator.ApiInnerFixed;
import etomica.atom.iterator.AtomIterator;
import etomica.atom.iterator.AtomIteratorSequence;
import etomica.atom.iterator.AtomIteratorSequenceDirectable;
import etomica.atom.iterator.AtomIteratorSinglet;
import etomica.atom.iterator.AtomPairIterator;
import etomica.atom.iterator.AtomsetIteratorMolecule;
import etomica.atom.iterator.IteratorDirective;
import etomica.atom.iterator.IteratorDirective.Direction;
import etomica.lattice.CellLattice;
import etomica.phase.Phase;
import etomica.space.BoundaryPeriodic;

/**
 * Bad comments follow.
 * 
 * Gives pairs formed from the molecules of a species in a phase, taking one
 * molecule the species with all of its other neighboring molecules. Species is
 * specified at construction and cannot be changed afterwards. Iteration is
 * performed using cell lists, which defines the neighboring molecules.
 * Direction is related to ordering of cells and, within a cell, ordering of
 * molecules in cell's occupant list.
 */

public class Api1ACell implements AtomsetIteratorMolecule, AtomsetIteratorCellular, 
        AtomPairIterator, java.io.Serializable {
    
	/**
	 * Constructor makes iterator that must have phase specified and then be 
	 * reset() before iteration.
     * 
     * @param D the dimension of the space of the simulation (used to construct cell iterators)
     * @param species length = 2 array with the (different) species whose molecules are interacting 
     */
	public Api1ACell(int D) {
        neighborIterator = new CellLattice.NeighborIterator(D);
        aiOuter = new AtomIteratorSinglet();
        aiSeq = new AtomIteratorSequence(IteratorDirective.UP);
        //this iterator is used to loop through list of occupants of atoms's cell;
        //construct with AtomToLinker that gives appropriate linker
        aiSeqDirectable = new AtomIteratorSequenceDirectable(new AtomIteratorSequence.AtomToLinker() {
            public AtomLinker getLinker(Atom atom) {return ((AtomSequencerCell)atom.seq).nbrLink;}
        });
        nbrCellListIterator = new ApiInnerFixed(aiOuter, aiSeq);//used only by allAtoms
        latticeIndex = new int[D];

        aiSeqDirectable.setDirection(null);
        aiSeqDirectable.setNumToSkip(1);
        neighborIterator.setDirection(null);
        setPhase(null);
	}

	public void setPhase(Phase phase) {
        if(phase != null) {
            lattice = (CellLattice)phase.getLattice();
            neighborIterator.setLattice(lattice);
            neighborIterator.setPeriod(phase.boundary().dimensions());
            neighborIterator.setPeriodicity(((BoundaryPeriodic)phase.boundary()).getPeriodicity());
        }
	}

    /**
     * Performs action on all iterates.
     */
    public void allAtoms(AtomsetAction action) {
        if(pair.atom0 == null) return;
        aiOuter.setAtom(pair.atom0);
        neighborIterator.checkDimensions();
        Cell cell = ((AtomSequencerCell)pair.atom0.seq).cell;
        lattice.latticeIndex(cell.latticeArrayIndex,latticeIndex);
        
        //get pairs in targetMolecule's cell
        aiSeqDirectable.setAtom(pair.atom0);
        nbrCellListIterator.allAtoms(action);

        //loop over neighbor cells
        neighborIterator.setSite(latticeIndex);
        neighborIterator.reset();
        while(neighborIterator.hasNext()) {
            Cell neighborCell = (Cell)neighborIterator.next(); 
            aiSeq.setFirst(neighborCell.occupants().header.next);
            if(neighborCell.occupants().size() > 0) nbrCellListIterator.allAtoms(action);
        }
    }//end of allAtoms
    
	/**
	 * Returns the number of atom pairs the iterator will return if
	 * reset and iterated in its present state.
	 */
	public int size() {
        AtomsetCount counter = new AtomsetCount();
        allAtoms(counter);
        return counter.callCount();
	}
	
	/**
	 * Indicates whether the given atom pair will be among the iterates
	 * given by the iterator if reset in its present state.  True only
	 * if an iterated pair would match the atoms as ordered in the given
	 * array.
	 */
	public boolean contains(AtomSet atoms) {
        if(!(atoms instanceof AtomPair) || ((AtomPair)atoms).atom0 == ((AtomPair)atoms).atom1) return false;
        AtomsetDetect detector = new AtomsetDetect(atoms);
        allAtoms(detector);
        return detector.detectedAtom();
	}

    public boolean hasNext() {
        return aiInner.hasNext();
    }
    
    public AtomSet next() {
        return nextPair();
    }
    
    public AtomPair nextPair() {
        if(!hasNext()) return null;
        pair.atom1 = aiInner.nextAtom();
        pair.nearestImageVector = neighborIterator.getNearestImageVector();
        if(!aiInner.hasNext()) {
            advanceLists();
        }
        return pair;
    }
    
    public AtomSet peek() {
        pair.atom1 = (Atom)aiInner.peek();
        return pair;
    }
    
    public void unset() {
        aiInner.unset();
    }

    /**
     * Returns 2, indicating that this is a pair iterator.
     */
    public int nBody() {
        return 2;
    }
    
    public void reset() {
        if(pair.atom0 == null) {
            unset();
            return;
        }
        neighborIterator.checkDimensions();
        Cell cell = ((AtomSequencerCell)pair.atom0.seq).cell;
        lattice.latticeIndex(cell.latticeArrayIndex,latticeIndex);
        neighborIterator.setSite(latticeIndex);
        neighborIterator.reset();
        
        //start with targetMolecule's cell
        aiSeqDirectable.setAtom(pair.atom0);
        aiSeqDirectable.reset();

        if(!aiSeqDirectable.hasNext()) { 
            advanceLists();
        } else {
            aiInner = aiSeqDirectable;
        }
    }
    
    /**
     * Indicates allowed direction for iteration, relative to specified target
     * atom. Specification of a null direction indicates iteration in both directions
     * relative to the target. Direction is determined by ordering within occupant
     * list of cell of target atom, and then by the cell ordering of neighboring cells.
     */
    public void setDirection(Direction direction) {
        aiSeqDirectable.setDirection(direction);
        neighborIterator.setDirection(direction);
    }

    /**
     * Sets the target molecule with which all pairs are formed.  Molecule
     * is determined from the first atom of the array, which may be the molecule
     * itself or an atom that is part of it.  If the atom is null or is not 
     * in one of the species given at construction, no iterates will be returned.
     * @throws NullPointerException
     *          if targetAtoms is null; use AtomSet.NULL instead
     * @throws IllegalArgumentException
     *          if targetAtoms.count() is not 0 or 1
     */
    public void setTarget(AtomSet targetAtoms) {
        switch(targetAtoms.count()) {
        case 0: 
            pair.atom0 = null;
            break;
        case 1:
            pair.atom0 = targetAtoms.getAtom(0);
            break;
        default:
            throw new IllegalArgumentException("Can specify at most one target atom to iterator");
        }
    }

    
    // Moves to next neighbor-cell list that can provide an iterate
    // This should be invoked only if aiInner.hasNext is false
    private void advanceLists() {
        aiInner = aiSeq;//need to switch from aiSeqDirectable on first call
        do {
              //advance neighbor cell 
            if(neighborIterator.hasNext()) {
                aiSeq.setFirst(((Cell)neighborIterator.next()).occupants().header.next);
                aiSeq.reset();
            } else {//no more cells
                break;
            }
        } while(!aiInner.hasNext());
    }//end of advanceCell

    /**
     * @return Returns the cellIterator.
     */
    public CellLattice.NeighborIterator getNbrCellIterator() {
        return neighborIterator;
    }
   
    private final ApiInnerFixed nbrCellListIterator;//used only by allAtoms
    private final CellLattice.NeighborIterator neighborIterator;
    private final AtomIteratorSequenceDirectable aiSeqDirectable;
    private final AtomIteratorSequence aiSeq;
    private final AtomIteratorSinglet aiOuter;
    private final AtomPairVector pair = new AtomPairVector();
    private final int[] latticeIndex;
    
    private CellLattice lattice;
    
    private AtomIterator aiInner;

}
