package etomica.junit;

import etomica.atom.AtomFactory;
import etomica.atom.AtomFactoryHetero;
import etomica.atom.AtomFactoryMono;
import etomica.atom.AtomSet;
import etomica.atom.AtomTypeLeaf;
import etomica.atom.AtomTypeMolecule;
import etomica.atom.AtomTypeSphere;
import etomica.atom.iterator.AtomIteratorTreeBox;
import etomica.box.Box;
import etomica.simulation.ISimulation;
import etomica.simulation.Simulation;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.species.Species;
import etomica.species.SpeciesSpheres;
import etomica.species.SpeciesSpheresMono;

/**
 * Contains some convenience methods and fields useful for implementing unit
 * tests.
 * 
 * @author David Kofke
 *  
 */
public class UnitTestUtil {

    public static boolean VERBOSE = false;

    /**
     * Private to prevent instantiation
     */
    private UnitTestUtil() {
        super();
    }

    public static ISimulation makeStandardSpeciesTree() {
        return makeStandardSpeciesTree(new int[] { 5, 7 }, 3, new int[] { 10, 10 });
    }

    /**
     * Makes tree hierarchy of three species and one or more boxs. First
     * species has multiatoms molecules, second species has monatomic molecules
     * (with leaf atoms in molecule layer), and third species has an arbitrary
     * tree structure.
     * 
     * @param n0
     *            number of atoms of species0 in each box
     * @param nA0
     *            number of atoms per molecule of species0
     * @param n1
     *            number of atoms of species1 in each box
     * @param n2
     *            number of atoms of species2 in each box
     * @param n2A
     *            tree specification of species 2, e.g., {2,4} indicates that
     *            each molecule has 2 subgroups, each with 4 atoms (such as
     *            CH3-CH3)
     * @return root of species hierarchy
     */

    public static ISimulation makeStandardSpeciesTree(int[] n0, int nA0,
            int[] n1) {
        Space space = Space3D.getInstance();
        ISimulation sim = new Simulation(space, false);
        Species species0 = null;
        Species species1 = null;
        int nBox = 0;
        if (n0 != null) {
            species0 = new SpeciesSpheres(sim, nA0);
            sim.getSpeciesManager().addSpecies(species0);
            nBox = n0.length;
        }
        if (n1 != null) {
            species1 = new SpeciesSpheresMono(sim);
            sim.getSpeciesManager().addSpecies(species1);
            nBox = n1.length;
        }
        for (int i = 0; i < nBox; i++) {
            Box box = new Box(sim);
            sim.addBox(box);
            if (species0 != null)
                box.setNMolecules(species0, n0[i]);
            if (species1 != null)
                box.setNMolecules(species1, n1[i]);
        }
        return sim;
    }

    /**
     * Makes tree hierarchy of one or more species in a single box. Number of
     * species if determined by length of nMolecules array. Each molecule is
     * heterogeneous, formed from atoms of different types. For example:
     * <ul>
     * <li>nMolecules = {5,3} will form two species, with five molecules of
     * speciesA and 3 molecule of SpeciesB.
     * <li>Then with nAtoms = {{2,1,4},{2,3}}, a speciesA molecule will contain
     * 2+1+4 = 7 atoms, with 2 of type-a, 1 of type-b, and 4 of type-c, and a
     * speciesB molecule will contain 2+3 = 5 atoms, with 2 of type-d and 3 of
     * type-e. All types are different instance of AtomTypeSphere.
     * </ul>
     * 
     * @param nMolecules
     *            number of molecules made of each species. Number of species is
     *            determined by length of this array.
     * @param nAtoms
     *            first index corresponds to species, so nAtoms.length should
     *            equal nMolecules.length. Number of types in a species-j
     *            molecule is given by the length of the nAtoms[j] subarray, and
     *            the elements of this array give the number of atoms of each
     *            type used to form a molecule.
     * @return root of the species hierarchy
     */
    public static ISimulation makeMultitypeSpeciesTree(int[] nMolecules,
            int[][] nAtoms) {
        Space space = Space3D.getInstance();
        ISimulation sim = new Simulation(space, false);
        //        new SpeciesSpheres(sim);
        Box box = new Box(sim);
        sim.addBox(box);
        for (int i = 0; i < nMolecules.length; i++) {
            Species species = new Species();
            AtomFactoryHetero factory = new AtomFactoryHetero(sim, species);
            species.setMoleculeFactory(factory);
            AtomFactory[] childFactories = new AtomFactory[nAtoms[i].length];
            for (int j = 0; j < childFactories.length; j++) {
                AtomTypeLeaf atomType = new AtomTypeSphere(sim);
                childFactories[j] = new AtomFactoryMono(space, atomType);
            }
            factory.setChildFactory(childFactories);
            factory.setChildCount(nAtoms[i]);
            sim.getSpeciesManager().addSpecies(species);
            box.setNMolecules(species, nMolecules[i]);
        }
        return sim;
    }
    
    public static void main(String[] arg) {
        ISimulation sim = makeStandardSpeciesTree();
        Box[] boxs = sim.getBoxs();
        for (int i=0; i<boxs.length; i++) {
            AtomIteratorTreeBox iterator = new AtomIteratorTreeBox();
            iterator.setBox(boxs[i]);
            iterator.setDoAllNodes(true);
            iterator.reset();
            for (AtomSet atom = iterator.next(); atom != null;
                 atom = iterator.next()) {
                System.out.println(atom.toString());
            }
        }
    }

}
