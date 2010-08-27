
package etomica.nbr.molecule;

import etomica.api.IMoleculeList;
import etomica.api.ISpecies;

/**
 * Filters molecule to match a given species.
 * 
 * @author Tai Boon Tan
 */
public class CriterionSpecies extends CriterionAdapterMolecular {

    public CriterionSpecies(NeighborCriterionMolecular criterion, 
            ISpecies species) {
        super(criterion);
        this.species = species;
    }
    
    /**
     * Returns true if the species of the molecule matches the Species given at 
     * construction and if the wrapped criterion accept also returns true.
     */
    public boolean accept(IMoleculeList molecule) {
        if (molecule.getMolecule(0).getType() == species) {
            return subCriterion.accept(molecule);
        }
        return false;
    }
    
    /**
     * Returns the Species accepted by this criterion.
     */
    public ISpecies getSpecies() {
        return species;
    }
    
    private static final long serialVersionUID = 1L;
    private final ISpecies species;
}