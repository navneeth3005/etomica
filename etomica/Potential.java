package etomica;

import etomica.Space.Tensor;
import etomica.Space.Vector;

/**
 * Superclass for all Potential classes, which define how the atoms in the
 * system interact with each other.
 *
 * @author David Kofke
 */
 
 /* History of changes
  * 06/16/03 (DAK) Revised to permit SimulationElement in constructor.
  * 01/27/03 (DAK) Large set of changes in revision of design of Potential
  * 08/14/02 (DAK) made parentPotential mutable, so that potential can be
  * added/removed from a potential group; added setParentPotential for this
  * purpose.
  */
public abstract class Potential extends SimulationElement {
    
    public static String VERSION = "Potential:03.01.27";
    
    private PotentialGroup parentPotential;
	public final PotentialTruncation potentialTruncation;
	protected boolean enabled = true;
	protected Species[] species;
	public final int nBody;
	private Potential0Lrc p0Lrc;
//	protected AtomSetIterator iterator;

	/**
	 * Constructor for use only by PotentialMaster subclass.
	 * @param sim Simulation instance in which potential is used.
	 */
	Potential(Simulation sim) {
		super(sim, Potential.class);
		nBody = 0;
		potentialTruncation = PotentialTruncation.NULL;
		if(!(this instanceof PotentialMaster)) throw new RuntimeException("Invalid attempt to instantiate potential");
	}
		
	/**
	 * Constructor with default potential truncation given
	 * as PotentialTruncation. NULL.
	 * @param nBody number of atoms to which potential is applied at a time
	 * @param parent simulation element (usually a PotentialGroup) in which this
	 * potential resides
	 */
    public Potential(int nBody, SimulationElement parent) {
    	this(nBody, parent, PotentialTruncation.NULL);
    }
    /**
     * General constructor for a potential instance
     * @param nBody number of atoms to which this potential applies at a time;
     * for example with a pair potential nBody = 2; for a single-body potential,
     * nBody = 1.
     * @param parent potential group in which this potential reside
     * @param truncation instance of a truncation class that specifies the
     * scheme for truncating the potential
     */
    public Potential(int nBody, SimulationElement parent, PotentialTruncation truncation) {
        super(parent, Potential.class);
        this.nBody = nBody;
        potentialTruncation = truncation;
        if(parent instanceof PotentialGroup) {
	        parentPotential = (PotentialGroup)parent;
	        parentPotential.addPotential(this);
        }
    }

    public final PotentialGroup parentPotential() {return parentPotential;}
     
    /**
     * Adds this potential to the given potential group.  No action is taken
     * if the new parent is the same as the current parent.  Otherwise, if
     * current parent is not null, this is removed from it.  Then this is
     * added via addPotential to the new parent.  If new parent is null, this
     * is removed from current parent and no new parent is set.
     */
    public void setParentPotential(PotentialGroup newParent) {
        if(newParent == parentPotential) return;
        if(parentPotential != null) parentPotential.removePotential(this);
        parentPotential = newParent;
        if(newParent != null) parentPotential.addPotential(this);
    }
    
    /**
     * Primary method to complete the action of the potential.  Takes a basis,
     * which is a set of atoms that define the basis for iteration, and
     * iterator directive, which specifies which of the candidate atoms are
     * generated upon iteration using the basis, and a PotentialCalculation,
     * which specifies what the potential is to do or calculate.
     * @param basis the basis atoms for the iteration
     * @param id directive specifying atoms generated upon iteration over the
     * basis
     * @param pc codes what potential is to calculate when method is invoked
     */
    public abstract void calculate(AtomSet basis, IteratorDirective id, PotentialCalculation pc);
            
//	public void calculate(AtomSet basis, IteratorDirective id, PotentialCalculation pc) {
//		if(!enabled) return;
//		iterator.all(basis, id, pc);
//	}
	
	/**
	 * Sets the species to which this potential applies, if it is being used for
	 * molecule-level interactions.  Subclasses may extend this method
	 * to perform actions to instantiate appropriate iterators; but overriding
	 * methods must at some point invoke this superclass method to make sure
	 * species is registered appropriately with potential master.  An exception
	 * is thrown if the parent of this potential is not the potential master
	 * (only child potentials of potential master apply to the molecule- level
	 * interactions).  Also sets the species of the potential used for the long-
	 * range correction, if it is not null.
	 */
    public void setSpecies(Species[] s) {
    	if(s == null || s.length == 0) throw new IllegalArgumentException("Error: setSpecies called without specifying any species instances");
    	if(s.length > nBody) throw new IllegalArgumentException("Error:  Attempting to associate potential with more species than can be defined for it");
    	if(parentPotential instanceof PotentialMaster) {
    		((PotentialMaster)parentPotential).setSpecies(this, s);
    	} else {
    		throw new RuntimeException("Error: Can set species only for potentials that apply at the molecule level.  Potential must have PotentialMaster as parent");
    	}
    	species = new Species[s.length];
    	System.arraycopy(s, 0, species, 0, s.length);
    	if(p0Lrc != null) p0Lrc.setSpecies(s);
    }
    /**
     * Returns the species to which this potential applies, if it is
     * defined for molecule-level interactions.  Returns null if no species has
     * been set, which is the case if the potential is not describing
     * interactions between molecule-level Atoms.
     */
    public Species[] getSpecies() {return species;}
    
	/**
	 * Returns the enabled flag, which if false will cause potential to not
	 * contribute to any potential calculations.
	 * @return boolean The current value of the enabled flag
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled flag, which if false will cause potential to not
	 * contribute to any potential calculations.
	 * @param enabled The enabled value to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Accessor method for potential cutoff implementation.
	 */
	public PotentialTruncation getTruncation() {return potentialTruncation;}

	/**
	 * Sets the iterator that defines the atoms to which this potential
	 * applies.  Iterator should be appropriate type for the concrete subclass
	 * of this class.
	 */
	public abstract void setIterator(AtomSetIterator iterator);
	
	/**
	 * Accessor method for the iterator that defines the atoms to which this
	 * potential applies.
	 */
	public abstract AtomSetIterator getIterator();
	
	/**
	 * Marker interface for Null potentials, which are defined to have no action.
	 */
	public interface Null {}
    
    /**
	 * Interface for hard potentials, having impulsive forces.
     */    
	public interface Hard {
    
    	/**
    	 * Value of the virial from the most recent collision.
    	 * @return double virial value
    	 */
		public double lastCollisionVirial();
    
    	/**
    	 * Value of the virial from the most recent collision, decomposed into
    	 * it tensoral elements.
    	 * @return Tensor
    	 */
		public Space.Tensor lastCollisionVirialTensor();
    
		/**
		 * Instance of hard pair potential corresponding to no interaction between atoms.
		 */
	}//end of interface Hard
	
	/**
	 * Returns the zero-body potential used to apply a long-range correction
	 * for truncation of this potential.
	 * @return Potential0Lrc
	 */
	public Potential0Lrc getLrc() {
		return p0Lrc;
	}

	/**
	 * Sets the zero-body potential used to apply a long-range correction (lrc)
	 * for truncation of this potential.  This is invoked in the constructor of
	 * Potential0Lrc, which itself is routinely invoked during the construction
	 * of this potential, so this method is declared final to guard against
	 * subclasses performing some action that is inappropriate while this class
	 * is being constructed.
	 * @param p0Lrc The lrc potential to set
	 */
	final void setLrc(Potential0Lrc p0Lrc) {
		this.p0Lrc = p0Lrc;
	}


	public static final Potential NullPotential(Simulation parent) {return new MyNull(parent);}
	
	private static final class MyNull extends Potential implements Potential.Hard, Potential1.Hard, Potential2.Hard, Potential2.Soft {
		
		private final Space.Vector zero;
		private final Space.Tensor zeroT;
		
		public MyNull(Simulation sim) {
			super(0, sim);
			zero = sim.space.makeVector();
			zeroT = sim.space.makeTensor();
		}

		public void bump(Atom atom) {}
		public double collisionTime(Atom atom) {return Double.MAX_VALUE;}
		public double energy(Atom atom) {return 0;}
		public void bump(AtomPair pair) {}
		public double collisionTime(AtomPair pair) {return Double.MAX_VALUE;}
		public double energy(AtomPair pair) {return 0;}
		public Vector gradient(AtomPair pair) {return zero;}
		public double hyperVirial(AtomPair pair) {return 0;}
		public double integral(double rC) {return 0;}
		public double virial(AtomPair pair) {return 0;}
		public double lastCollisionVirial() {return 0;}
		public Tensor lastCollisionVirialTensor() {return zeroT;}
		public void calculate(AtomSet basis,IteratorDirective id,PotentialCalculation pc) {}
		public AtomSetIterator getIterator() {return null;}
		public void setIterator(AtomSetIterator iterator) {}
	}
}//end of Potential

