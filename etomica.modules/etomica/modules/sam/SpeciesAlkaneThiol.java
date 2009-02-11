package etomica.modules.sam;

import etomica.api.IAtomType;
import etomica.api.ISimulation;
import etomica.chem.elements.Element;
import etomica.chem.elements.ElementSimple;
import etomica.chem.elements.Sulfur;
import etomica.space.ISpace;
import etomica.species.SpeciesSpheresHetero;

public class SpeciesAlkaneThiol extends SpeciesSpheresHetero {

    public SpeciesAlkaneThiol(ISimulation sim, ISpace _space, int numCarbons) {
        super(_space, sim.isDynamic(), makeAtomTypeSpheres(new Element[]{Sulfur.INSTANCE, new ElementSimple("CH3", 15), new ElementSimple("CH2", 14)}));
        setTotalChildren(numCarbons+1);
    }

    public IAtomType getSulfurType() {
        return childTypes[0];
    }
    
    public IAtomType getCH2Type() {
        return childTypes[1];
    }
    
    public IAtomType getCH3Type() {
        return childTypes[2];
    }
    
    public void setTotalChildren(int newTotalChildren) {
        if (newTotalChildren < 2) {
            throw new RuntimeException("we need at least 2 children (1 carbon)");
        }
        childCount[0] = 1;
        childCount[1] = newTotalChildren-2;
        childCount[2] = 1;
    }

    private static final long serialVersionUID = 1L;
}
