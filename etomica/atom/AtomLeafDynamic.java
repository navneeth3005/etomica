package etomica.atom;

import etomica.api.IVector;
import etomica.space.Space;

public class AtomLeafDynamic extends AtomLeaf implements IAtomKinetic {

    public AtomLeafDynamic(Space space, AtomTypeLeaf type) {
        super(space, type);
        velocity = space.makeVector();
    }
    
    public IVector getVelocity() {
        return velocity;
    }
    
    private static final long serialVersionUID = 1L;
    protected final IVector velocity;
}
