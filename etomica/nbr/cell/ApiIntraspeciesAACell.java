/*
 * History
 * Created on Aug 30, 2004 by kofke
 */
package etomica.nbr.cell;

import etomica.Atom;
import etomica.AtomsetIterator;
import etomica.IteratorDirective;
import etomica.NearestImageVectorSource;
import etomica.Phase;
import etomica.Space;
import etomica.Species;
import etomica.action.AtomsetAction;
import etomica.action.AtomsetCount;
import etomica.action.AtomsetDetect;
import etomica.atom.AtomList;
import etomica.atom.iterator.ApiBuilder;
import etomica.atom.iterator.ApiInnerFixed;
import etomica.atom.iterator.ApiListSimple;
import etomica.atom.iterator.AtomIteratorListSimple;
import etomica.atom.iterator.AtomsetIteratorPhaseDependent;
import etomica.lattice.CellLattice;
import etomica.lattice.RectangularLattice;

/**
 * Returns iterates formed from all molecule pairs of a single species.
 */

public class ApiIntraspeciesAACell implements AtomsetIteratorPhaseDependent, 
                        AtomsetIteratorCellular, NearestImageVectorSource {

    /**
     * @param D the dimension of the space of the simulation
     * @param species the species whose molecules form the pair iterates
     */
    public ApiIntraspeciesAACell(int D, Species species) {
        this(D, new Species[] {species, species});
    }
    
	/**
     * Constructor makes iterator that must have phase specified and then be
     * reset() before iteration.
     * 
     * @param D
     *            the dimension of the space of the simulation (used to
     *            construct cell iterators)
     * @param species
     *            length > 0 array with the (single) species whose molecules
     *            are interacting.  Only the first element of array is relevant.
     */
	public ApiIntraspeciesAACell(int D, Species[] species) {
        cellIterator = new RectangularLattice.Iterator(D);
        neighborIterator = new CellLattice.NeighborIterator(D);
        neighborIterator.setDirection(IteratorDirective.UP);
        interListIterator = ApiBuilder.makeInterlistIterator();
        aiInner = ((AtomIteratorListSimple)interListIterator.getInnerIterator());
        aiOuter = ((AtomIteratorListSimple)interListIterator.getOuterIterator());
        intraListIterator = new ApiListSimple();
        listIterator = intraListIterator;
        index = species[0].getIndex();
        nearestImageVector = Space.makeVector(D);
	}

	public void setPhase(Phase phase) {
        cellIterator.setLattice(phase.getLattice());
		neighborIterator.setLattice(phase.getLattice());
        unset();
	}
    
    public Space.Vector getNearestImageVector() {
        return nearestImageVector;
    }
    
    /**
     * Performs action on all iterates.
     */
    public void allAtoms(AtomsetAction action) {
        cellIterator.reset();
        neighborIterator.checkDimensions();
        while(cellIterator.hasNext()) {//outer loop over all cells
            //get cell without advancing -- advance is done via nextIndex,
            // below
            NeighborCell cell = (NeighborCell)cellIterator.peek();
            AtomList list = cell.occupants()[index];
            
            //no molecules of species in cell
            if(list.isEmpty()) {
                cellIterator.nextIndex();
                continue;
            }
            
            //consider pairs formed from molecules in cell
            intraListIterator.setList(list);
            intraListIterator.allAtoms(action);

            //loop over neighbor cells
            aiOuter.setList(list);
            neighborIterator.setSite(cellIterator.nextIndex());
            neighborIterator.reset();
            while(neighborIterator.hasNext()) {
                NeighborCell neighborCell = (NeighborCell)neighborIterator.next(); 
                aiInner.setList(neighborCell.occupants()[index]);
                if(aiInner.size() > 0) interListIterator.allAtoms(action);
            }
        }//end of outer loop over cells
    }//end of allAtoms
    
	/**
     * Returns the number of atom pairs the iterator will return if reset and
     * iterated in its present state.
     */
	public int size() {
        AtomsetCount counter = new AtomsetCount();
        allAtoms(counter);
        return counter.callCount();
	}
	
	/**
     * Indicates whether the given atom pair will be among the iterates given by
     * the iterator if reset in its present state. True only if an iterated pair
     * would match the atoms as ordered in the given array.
     */
	public boolean contains(Atom[] atoms) {
        if(atoms==null || atoms[0]==null || atoms[1]==null || atoms[0]==atoms[1]) return false;
        AtomsetDetect detector = new AtomsetDetect(atoms);
        allAtoms(detector);
        return detector.detectedAtom();
	}

    public boolean hasNext() {
        return listIterator.hasNext();
    }
    
    public Atom[] next() {
        if(!hasNext()) return null;
        Atom[] nextPair = listIterator.next();
        pair[0] = nextPair[0];
        pair[1] = nextPair[1];
        nearestImageVector = neighborIterator.getNearestImageVector();
        if(!listIterator.hasNext()) {
            advanceLists();
        }
        return pair;
    }
    
    public Atom[] peek() {
        return listIterator.peek();
    }
    
    public void unset() {
        listIterator.unset();
    }

    /**
     * Returns 2, indicating that this is a pair iterator.
     */
    public int nBody() {
        return 2;
    }
    
    public void reset() {
        cellIterator.reset();
        neighborIterator.checkDimensions();
        neighborIterator.unset();
        listIterator.unset();
        advanceLists();

    }//end of reset
    
    // Moves to next pair of lists that can provide an iterate
    // This should be invoked only if listIterator.hasNext is false
    private void advanceLists() {
        do {
              //advance neighbor cell
            if(neighborIterator.hasNext()) {
                aiInner.setList(((NeighborCell)neighborIterator.next()).occupants()[index]);
                listIterator = interListIterator;
                interListIterator.reset();

                //advance central cell and set up neighbor cell iterator if
                // central cell has some molecules
            } else if(cellIterator.hasNext()) {
                AtomList list = ((NeighborCell)cellIterator.peek()).occupants()[index];
                neighborIterator.setSite(cellIterator.nextIndex());

                if(!list.isEmpty()) {//central cell has molecules
                    aiOuter.setList(list); //for neighbor-cell looping
                    intraListIterator.setList(list);//for intra-cell looping
                    neighborIterator.reset();

                    listIterator = intraListIterator;
                    intraListIterator.reset();
                        
                } else {//no molecules in central cell
                    neighborIterator.unset();
                    listIterator.unset();
                }
            } else {//no more cells at all
                break;
            }
        } while(!listIterator.hasNext());
    }//end of advanceCell
    
    /**
     * @return Returns the cellIterator.
     */
    public CellLattice.NeighborIterator getNbrCellIterator() {
        return neighborIterator;
    }
   
    private AtomsetIterator listIterator;
    private final ApiListSimple intraListIterator;
    private final AtomIteratorListSimple aiInner, aiOuter;
    private final ApiInnerFixed interListIterator;
    private final CellLattice.NeighborIterator neighborIterator;
    private final RectangularLattice.Iterator cellIterator;
    private final int index;
    private Space.Vector nearestImageVector;
    
    private final Atom[] pair = new Atom[2];
}
