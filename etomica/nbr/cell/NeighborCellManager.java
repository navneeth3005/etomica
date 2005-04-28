/*
 * History
 * Created on Nov 21, 2004 by kofke
 */
package etomica.nbr.cell;

import etomica.Atom;
import etomica.IntegratorNonintervalEvent;
import etomica.IntegratorIntervalEvent;
import etomica.IntegratorIntervalListener;
import etomica.IntegratorNonintervalListener;
import etomica.Phase;
import etomica.PhaseEvent;
import etomica.PhaseListener;
import etomica.SimulationEvent;
import etomica.Space;
import etomica.SpeciesAgent;
import etomica.atom.AtomPositionDefinition;
import etomica.atom.iterator.AtomIteratorAllMolecules;
import etomica.atom.iterator.AtomIteratorPhaseDependent;
import etomica.lattice.CellLattice;
import etomica.space.Vector;

/**
 * Class that defines and manages construction and use of lattice of cells 
 * for cell-based neighbor listing.
 */

public class NeighborCellManager implements IntegratorIntervalListener, IntegratorNonintervalListener {

    private final CellLattice lattice;
    private final Space space;
    private final Phase phase;
    private int listCount;
    private final AtomIteratorPhaseDependent atomIterator;
    private int iieCount;
    private int updateInterval;
    private int priority;
    private final AtomPositionDefinition positionDefinition;
    
    /**
     * Constructs manager for neighbor cells in the given phase.  The number of
     * cells in each dimension is given by nCells. Position definition for each
     * atom is that given by its type (it is set to null in this class).
     */
    public NeighborCellManager(Phase phase, int nCells) {
        this(phase, nCells, null);
    }
    
    /**
     * Construct manager for neighbor cells in the given phase.  The number
     * of cells in each dimension is given by nCells.  Position definition is
     * used to determine the cell a given atom is in; if null, the position
     * definition given by the atom's type is used.  Position definition is
     * declared final.
     */
    public NeighborCellManager(Phase phase, int nCells, AtomPositionDefinition positionDefinition) {
        this.phase = phase;
        this.positionDefinition = positionDefinition;
        space = phase.space();
        atomIterator = new AtomIteratorAllMolecules(phase);
        setPriority(150);
        setUpdateInterval(1);

        lattice = new CellLattice(phase.boundary().dimensions(), NeighborCell.FACTORY);

        int[] size = new int[space.D()];
        for(int i=0; i<space.D(); i++) size[i] = nCells;
        lattice.setSize(size);
        addList(phase.speciesMaster.node.childAtomCount());//add occupant lists to cells for each species already present in phase

        //listener to phase to detect addition of new SpeciesAgent
        //or new atom
        phase.speciesMaster.addListener(new PhaseListener() {
            public void actionPerformed(SimulationEvent evt) {
                actionPerformed((PhaseEvent)evt);
            }
           public void actionPerformed(PhaseEvent evt) {
                if(evt.type() == PhaseEvent.ATOM_ADDED) {
                    Atom atom = evt.atom();
                    //new species agent requires another list in each cell
                    if(atom instanceof SpeciesAgent) {
                        addList();
                   //otherwise new atom placed in cell if at molecule level
                        //FIXME change to depth might make this wrong
                    } else if(atom.type.getDepth() == 2) {
                        assignCell(atom);
                    }
                }
            }
        });
    }

    /**
     * @return the number of atom lists held by each cell.
     */
    public int getListCount() {
        return listCount;
    }
    
    /**
     * @return the lattice of cells.
     */
    public CellLattice getCellLattice() {
        return lattice;
    }
    
    /**
     * Assigns cells to all molecules in the phase.
     */
    public void assignCellAll() {
        atomIterator.reset();
        while(atomIterator.hasNext()) {
            assignCell(atomIterator.nextAtom());
        }
    }
    
    /**
     * Assigns the cell for the given atom.
     * @param atom
     */
    public void assignCell(Atom atom) {
        AtomSequencerCell seq = (AtomSequencerCell)atom.seq;
        Vector position = (positionDefinition != null) ?
                positionDefinition.position(atom) :
                    atom.type.getPositionDefinition().position(atom);
        NeighborCell newCell = (NeighborCell)lattice.site(position);
        if(newCell != seq.cell) {assignCell(seq, newCell, atom.type.getSpeciesIndex()-1);}
    }
    
    /**
     * Assigns atom sequencer to given cell in the list of the given index.
     */
    public void assignCell(AtomSequencerCell seq, NeighborCell newCell, int listIndex) {
        if(seq.cell != null) seq.cell.occupants()[listIndex].remove(seq.nbrLink);
        seq.cell = newCell;
//        seq.nbrLink.remove();
        if(newCell != null) {
            newCell.occupants()[listIndex].add(seq.nbrLink);
        }
    }//end of assignCell
    
    private void addList(int n) {
        for(int i=0; i<n; i++) addList();
    }
    
    /**
     * Adds an AtomList to each cell of the lattice.  This is performed
     * when a new species is added to the simulation.  Each list associated
     * with a cell holds the molecules of a given species that are in that cell.
     */
    public void addList() {
        listCount++;
        Object[] sites = lattice.sites();
        for(int i=sites.length-1; i>=0; i--) {
            ((NeighborCell)sites[i]).addOccupantList();
        }
    }

    /**
     * Implementation of IntervalListener interface to cause all
     * atoms in phase to be assigned to their cells. 
     */
    public void nonintervalAction(IntegratorNonintervalEvent event) {
        if (event.type() == IntegratorNonintervalEvent.INITIALIZE) {
            assignCellAll();
        }
    }
    
    public void intervalAction(IntegratorIntervalEvent event) {
        if (--iieCount == 0) {
            assignCellAll();
            iieCount = updateInterval;
        }
    }
    
    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
    
    /**
     * @return the priority of this as an integrator interval-listenter (default is 150)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority of this as an integrator interval-listenter (default is 150)
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
}//end of NeighborCellManager
