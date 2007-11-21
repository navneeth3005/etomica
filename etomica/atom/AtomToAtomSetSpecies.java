package etomica.atom;

import java.io.Serializable;

import etomica.box.Box;
import etomica.species.Species;

public class AtomToAtomSetSpecies implements AtomToAtomSet, AtomToIndex, Serializable {

    private static final long serialVersionUID = 1L;

    public AtomToAtomSetSpecies(Species species) {
        this.species = species;
    }
    
    public AtomSet getAtomSet(IAtom atom) {
        return moleculeList;
    }
    
    public int getIndex(IAtom atom) {
        return atom.getIndex();
    }
    
    public void setBox(Box box) {
        moleculeList = box.getMoleculeList(species);
    }

    protected AtomSet moleculeList;
    protected final Species species;
}
