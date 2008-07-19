package etomica.atom;

import java.io.Serializable;

import etomica.api.IAtom;

public class AtomFilterStatic implements AtomFilter, Serializable {

    //private to prevent instantiation
    private AtomFilterStatic(boolean accept) {
        rv = accept;
    }

    public boolean accept(IAtom a) {return rv;}

    /**
     * Static instance of a filter that accepts all atoms.
     * Returns true for null atom also.
     */
    public static final AtomFilterStatic ACCEPT_ALL = new AtomFilterStatic(true);

    /**
     * Static instance of a filter that rejects all atoms.
     * Returns false for null atom also.
     */
    public static final AtomFilterStatic ACCEPT_NONE = new AtomFilterStatic(false);

    /**
     * Required to guarantee singleton when deserializing.
     * @return the singleton INSTANCE
     */
    private Object readResolve() {
        if (this.accept(null)) {
            return ACCEPT_ALL;
        }
        return ACCEPT_NONE;
    }

    private static final long serialVersionUID = 1L;
    private final boolean rv;
}