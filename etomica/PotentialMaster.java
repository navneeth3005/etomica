package etomica;

/**
 * Master potential that oversees all other potentials in the Hamiltonian.
 * Most calls to compute the energy or other potential calculations begin
 * with the calculate method of this class.  It then passes the calculation 
 * on to the contained potentials.
 *
 * @author David Kofke
 */
 
 /* History of changes
  * 08/13/02 (DAK) added removePotential method
  * 01/27/03 (DAK) many revisions as part of redesign of Potential
  * 06/15/03 (DAK) in makeBasis, for 2-species case, added check for whether the
  * two species are the same; if so uses one of them as basis, rather than
  * making a pair from them.
  */
public final class PotentialMaster extends PotentialGroup {
    
    public String getVersion() {return "PotentialMaster:02.08.13";}

    private SpeciesMaster speciesMaster;
    private PotentialGroupLrc lrcMaster;

    public PotentialMaster(Simulation sim) {
        super(sim);
    } 
    
	/**
	 * Returns the potential group that oversees the long-range
	 * correction zero-body potentials.
	 */
	 public PotentialGroupLrc lrcMaster() {
		if(lrcMaster == null) lrcMaster = new PotentialGroupLrc(this);
		return lrcMaster;
	 }

	public void calculate(Phase phase, IteratorDirective id, PotentialCalculation pc) {
		this.calculate(phase.speciesMaster, id, pc);
	}
    public void calculate(AtomSet basis, IteratorDirective id, PotentialCalculation pc) {
    	this.calculate((SpeciesMaster)basis, id, pc);
    }    
    
	//should build on this to do more filtering of potentials based on directive
    public void calculate(SpeciesMaster speciesMaster, IteratorDirective id, PotentialCalculation pc) {
    	if(!enabled) return;
        for(PotentialWrapper link=(PotentialWrapper)first; link!=null; link=(PotentialWrapper)link.next) {
            if(id.excludes(link.potential)) continue; //see if potential is ok with iterator directive
            link.potential.calculate(link.basis(speciesMaster), id, pc);
        }//end for
    }//end calculate
    
    public void setSpecies(Potential potential, Species[] species) {
    	for(PotentialLinker link=first; link!=null; link=link.next) {
    		if(link.potential == potential) {
    			((PotentialWrapper)link).setSpecies(species);
    			return;
    		}
    	}
    }
 
 	/**
 	 * Overrides to return a potential linker that holds information about the
 	 * potential's bases for iteration in each phase.
 	 * @see etomica.PotentialGroup#makeLinker(Potential, PotentialLinker)
 	 */
	protected PotentialLinker makeLinker(Potential p, PotentialLinker next) {
		return new PotentialWrapper(p, next);
	}
       
	/**
	 * Convenient reformulation of the calculate method, applicable if the
	 * potential calculation performs a sum.  The method returns the
	 * summable potential calculation object, so that the sum can be accessed
	 * in-line with the method call.
	 */
   public final PotentialCalculation.Summable calculate(Phase phase, IteratorDirective id, PotentialCalculation.Summable pa) {
	   this.calculate(phase.speciesMaster, id, (PotentialCalculation)pa);
	   return pa;
   }	    

	private static class PotentialWrapper extends PotentialLinker {
		private Species[] species;
		private AtomSet[] basisArray = new AtomSet[0];
		
		PotentialWrapper(Potential p, PotentialLinker next) {
			super(p, next);
		}
		
		void setSpecies(Species[] species) {
			this.species = new Species[species.length];
			System.arraycopy(species, 0, this.species, 0, species.length);
			basisArray = new AtomSet[0]; //(DAK) added 07/22/04 needed to ensure basis is updated if species are changed from values set first
		}
		
		public AtomSet basis(SpeciesMaster m) {
			if(potential.nBody == 0) return m;
//			if(potential instanceof PotentialGroupLrc) return m;
			try{
				AtomSet basis = basisArray[m.index];
				return (basis == null) ? makeBasis(m) : basis;
			} catch(ArrayIndexOutOfBoundsException ex) {
				return makeBasis(m);
			}
		}
		
		/**
		 * Makes a basis out of the SpeciesAgents of the species to which the wrapped potential applies,
		 * for the phase of the given SpeciesMaster.
		 * @param m SpeciesMaster in the phase for which the basis is being identified.
		 * @return an AtomSet that has one or more SpeciesAgents that are the basis for the potential
		 * in the given phase.
		 */
		private AtomSet makeBasis(SpeciesMaster m) {
			int index = m.index;
			if(index >= basisArray.length) {
				AtomSet[] newArray = new AtomSet[index+1];
				System.arraycopy(basisArray, 0, newArray, 0, basisArray.length);
				basisArray = newArray;
			}
			AtomSet newBasis = null;
			switch(species.length) {//if getting null pointer error here, make sure setSpecies is called by potential in main
				case 1: newBasis = species[0].getAgent(m); break; 
				case 2: 
					SpeciesAgent speciesA = species[0].getAgent(m);
					SpeciesAgent speciesB = species[1].getAgent(m);
					if(speciesA.equals(speciesB)) {newBasis = speciesA;}  //06/14/03 added
					else if(speciesA.seq.preceeds(speciesB)) newBasis = new AtomPair(speciesA, speciesB); 
					else newBasis = new AtomPair(speciesB, speciesA);
					break;
				default: throw new RuntimeException("problem in PotentialMaster.makeBasis");
			}
			basisArray[m.index] = newBasis;
			return newBasis;
		}//end of makeBasis
	}//end of PotentialWrapper
	
}//end of PotentialMaster
    