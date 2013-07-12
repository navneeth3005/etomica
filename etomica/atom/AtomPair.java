package etomica.atom;

import etomica.api.IAtom;
import etomica.api.IAtomList;


/**
 * Data structure that contains two mutable atom instances.
 */
public class AtomPair implements IAtomList {

    public AtomPair() {
    }

    public AtomPair(IAtom atom0, IAtom atom1) {
        this.atom0 = atom0;
        this.atom1 = atom1;
    }

    public boolean equals(Object obj) {
        if (((AtomPair)obj).atom0 == atom0) {
            return ((AtomPair)obj).atom1 == atom1;
        }
        return (((AtomPair)obj).atom0 == atom1 && ((AtomPair)obj).atom1 == atom0);
    }

    public int hashCode() {
        return atom0.hashCode() + atom1.hashCode();
    }

    public final IAtom getAtom(int i) {
        if(i == 0) return atom0;
        if(i == 1) return atom1;
        throw new IllegalArgumentException();
    }

    public final int getAtomCount() {
        return 2;
    }

    public String toString() {
        return "["+atom0+","+atom1+"]";
    }

    public IAtom atom0, atom1;
}
