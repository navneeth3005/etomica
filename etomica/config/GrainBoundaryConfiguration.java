/*
 * Created on Aug 2, 2006
 *
 * Adapted from ConfigurationLattice for the specific case of a two-lattice system,
 * separated by a 2-D grain boundary. latticeA is above the grain boundary, and
 * latticeB is below.  Because the lattice types may be differect, PBC exist only
 * in x- and y-directions.  Along the boundaries perpendicular to the z-direction 
 * are two levels of unit cells filled with fixed atoms (atoms with infinite mass).
 */
package etomica.config;

import etomica.action.AtomActionTranslateTo;
import etomica.atom.AtomPositionDefinitionSimple;
import etomica.atom.AtomSet;
import etomica.atom.AtomTypeMolecule;
import etomica.atom.IAtom;
import etomica.atom.IMolecule;
import etomica.box.Box;
import etomica.lattice.BravaisLatticeCrystal;
import etomica.lattice.IndexIteratorRectangular;
import etomica.lattice.IndexIteratorSizable;
import etomica.lattice.SpaceLattice;
import etomica.space.IVector;
import etomica.space.Space;
import etomica.space3d.Vector3D;
import etomica.species.Species;

/**
 * @author K.R. Schadel with help from A. Schultz
 */
public class GrainBoundaryConfiguration implements Configuration {

    /**
     * Construct class that will place atoms on sites of the given lattices,
     * proceeding in the order resulting from iteration through the given index
     * iterator.
     */
    public GrainBoundaryConfiguration(BravaisLatticeCrystal latticeA, BravaisLatticeCrystal latticeB) {
        super(); 
        /** Lattices A + B share same space.  Only need to getSpace() for one 
         *  of the two.
         */
        this.latticeA = latticeA; 
        this.latticeB = latticeB;
        this.indexIteratorA = new IndexIteratorRectangular(latticeA.D()); 
        this.indexIteratorB = new IndexIteratorRectangular(latticeB.D());
        if(indexIteratorA.getD() != latticeA.D()) {
            throw new IllegalArgumentException(
            		"Dimension of index iterator and lattice are incompatible");
        }
        if(indexIteratorB.getD() != latticeB.D()) {
            throw new IllegalArgumentException(
            		"Dimension of index iterator and lattice are incompatible");
        }
        atomActionTranslateTo = new AtomActionTranslateTo(latticeA.getSpace());
        atomActionTranslateTo.setAtomPositionDefinition(new AtomPositionDefinitionSimple());
    }

    public void setDimensions (int nCellsAx, int nCellsAy, int nCellsAz, 
    		int nCellsBx, int nCellsBy, int nCellsBz, double aA, double bA,
			double cA, double aB, double bB, double cB) {
    	
    	this.nCellsAx = nCellsAx; 
    	this.nCellsAy = nCellsAy; 
    	this.nCellsAz = nCellsAz; 
    	this.nCellsBx = nCellsBx;
    	this.nCellsBy = nCellsBy;
    	this.nCellsBz = nCellsBz;
    	
    	latticeDimensionsA[0] = nCellsAx * aA;
    	latticeDimensionsA[1] = nCellsAy * bA;
    	latticeDimensionsA[2] = nCellsAz * cA;
    	latticeDimensionsB[0] = nCellsBx * aB;
    	latticeDimensionsB[1] = nCellsBy * bB;
    	latticeDimensionsB[2] = nCellsBz * cB;
    	
    	//iteratorDimensionsA or B
    	iteratorDimensionsA[0] = nCellsAx;
    	iteratorDimensionsA[1] = nCellsAy;
    	iteratorDimensionsA[2] = nCellsAz;
    	iteratorDimensionsA[3] = latticeA.getBasis().getScaledCoordinates().length;
    	iteratorDimensionsB[0] = nCellsBx;
    	iteratorDimensionsB[1] = nCellsBy;
    	iteratorDimensionsB[2] = nCellsBz;
    	iteratorDimensionsB[3] = latticeB.getBasis().getScaledCoordinates().length;
    }
    
    public void setSpeciesA(Species newSpeciesAFixed, Species newSpeciesAMobile) {
        speciesAFixed = newSpeciesAFixed;
        speciesAMobile = newSpeciesAMobile;
    }
    
    public void setSpeciesB(Species newSpeciesBFixed, Species newSpeciesBMobile) {
        speciesBFixed = newSpeciesBFixed;
        speciesBMobile = newSpeciesBMobile;
    }

    public Species getSpeciesAFixed() {
        return speciesAFixed;
    }

    public Species getSpeciesBFixed() {
        return speciesBFixed;
    }

    public Species getSpeciesAMobile() {
        return speciesAMobile;
    }

    public Species getSpeciesBMobile() {
        return speciesBMobile;
    }


    /**
     * Places the molecules in the given box on the positions of the
     * lattice.  
     */
    public void initializeCoordinates(Box box) {
    	AtomSet listMobileA = box.getMoleculeList(speciesAMobile);
    	AtomSet listMobileB = box.getMoleculeList(speciesBMobile);
        AtomSet listFixedA = box.getMoleculeList(speciesAFixed);
        AtomSet listFixedB = box.getMoleculeList(speciesBFixed);
    	
        indexIteratorA.setSize(iteratorDimensionsA);
        indexIteratorB.setSize(iteratorDimensionsB);

        /**  The offset vectors are used to shift the lattices such that
         *  lattice A is above lattice B, and both are centered on the z axis.
         */
        
        Vector3D offsetA = (Vector3D)box.getSpace().makeVector();
        Vector3D offsetB = (Vector3D)box.getSpace().makeVector();
        offsetA.setX(0, -0.5 * latticeDimensionsA[0]);
        offsetA.setX(1, -0.5 * latticeDimensionsA[1]);
        offsetA.setX(2, (latticeDimensionsB[2] - latticeDimensionsA[2])/2.0 );
        offsetB.setX(0, -0.5 * latticeDimensionsB[0]);
        offsetB.setX(1, -0.5 * latticeDimensionsB[1]);
        offsetB.setX(2, -(latticeDimensionsB[2] + latticeDimensionsA[2])/2.0 );
        
        myLatA = new MyLattice(latticeA, offsetA);
        myLatB = new MyLattice(latticeB, offsetB);

        // Place molecules

        int iMobileA = 0;
        int iFixedA = 0;
        
        indexIteratorA.reset();
        while (indexIteratorA.hasNext()) {
        	//System.out.println("At start of while loop over indexIteratorA  " + firstAtomPosition);
        	IAtom a;
        	int[] ii = indexIteratorA.next();
        	//ii[2] goes from 0 to nCellsAz-1, not 1 to nCellsAz, because of 
        	//how setSize method works
            if (ii[2] > ((nCellsAz - 2) - 1)) {
            	a = listFixedA.getAtom(iFixedA);
            	iFixedA++;
            }
            else {
            	a = listMobileA.getAtom(iMobileA);
            	iMobileA++;
            	//System.out.println(ii[2] + "  |  " + a);
            }
            if (a instanceof IMolecule) {
                // initialize coordinates of child atoms
                Conformation config = ((AtomTypeMolecule)a.getType()).getConformation();
                config.initializePositions(((IMolecule)a).getChildList());
            }
            IVector site = (IVector) myLatA.site(ii);
            atomActionTranslateTo.setDestination(site);
            atomActionTranslateTo.actionPerformed(a);
//            System.out.println("A  |  " +a + "  |  " + site.x(2) + "  |  " + ii[2]);
        }
        
        int iMobileB = 0;
        int iFixedB = 0;

        indexIteratorB.reset();
        while (indexIteratorB.hasNext()) {
        	IAtom a;
        	int[] ii = indexIteratorB.next();
        	//ii[2] goes from 0 to nCellsAz-1, not 1 to nCellsAz
            if (ii[2] < 2) {
            	a = listFixedB.getAtom(iFixedB);
            	
            }
            else {
            	a = listMobileB.getAtom(iMobileB);
            	
            }
            if (a instanceof IMolecule) {
                // initialize coordinates of child atoms
                Conformation config = ((AtomTypeMolecule)a.getType()).getConformation();
                config.initializePositions(((IMolecule)a).getChildList());
            }
            IVector site = (IVector) myLatB.site(ii);
            atomActionTranslateTo.setDestination(site);
            atomActionTranslateTo.actionPerformed(a);
            //System.out.println("B  |  " +a + "  |  " + site.x(2) + "  |  " + ii[2]);
        }
    }
    
    
    
    /**
     * Returns a lattice with positions the same as those used in the 
     * most recent use of initializeCoordinates.  Includes any scaling
     * or translation applied to fill the space, and thus will not necessarily
     * be the same positions as specified by the lattice given at construction.
     */
    public SpaceLattice getLatticeAMemento() {
        return myLatA;
    }
    
    public SpaceLattice getLatticeBMemento() {
        return myLatB;
    }
    
    /**
     * Used to store the state of a lattice.
     * 
     * @author nancycribbin, Andrew Schultz, Dr. Kofke
     * 
     */
    private static class MyLattice implements SpaceLattice {

        MyLattice(SpaceLattice l, IVector offset) {
            lattice = l;
            this.offset = offset;
            this.site = l.getSpace().makeVector();
        }

        public Space getSpace() {
            return lattice.getSpace();
        }

        public int D() {
            return lattice.D();
        }

        /**
         * Returns the same IVector instance with each call.
         */
        public Object site(int[] index) {
            site.E((IVector) lattice.site(index));
            site.PE(offset);
            return site;
        }

        public double[] getLatticeConstants() {
            double[] lat = lattice.getLatticeConstants();
            return lat;
        }

        SpaceLattice lattice;
        IVector offset;
        private final IVector site;
    }

    private final BravaisLatticeCrystal latticeA, latticeB;
    private Species speciesAFixed, speciesBFixed, speciesAMobile, speciesBMobile;
    private final IndexIteratorSizable indexIteratorA, indexIteratorB;
    private final AtomActionTranslateTo atomActionTranslateTo;
    int[] iteratorDimensionsA = new int[4];
    int[] iteratorDimensionsB = new int[4];
    //int[] indexIteratorA = new int[4];
    //int[] indexIteratorB = new int[4];
    double[] latticeDimensionsA = new double[3];
    double[] latticeDimensionsB = new double[3];
    private int nCellsAx, nCellsAy, nCellsAz, nCellsBx, nCellsBy, nCellsBz;
    private MyLattice myLatA, myLatB;
    private static final long serialVersionUID = 2L;
}