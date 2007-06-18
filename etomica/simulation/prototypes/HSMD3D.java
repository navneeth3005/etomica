package etomica.simulation.prototypes;

import etomica.action.PhaseImposePbc;
import etomica.action.SimulationRestart;
import etomica.action.activity.ActivityIntegrate;
import etomica.config.ConfigurationLattice;
import etomica.graphics.ColorSchemeByType;
import etomica.graphics.DeviceNSelector;
import etomica.graphics.DisplayPhase;
import etomica.graphics.SimulationGraphic;
import etomica.integrator.IntegratorHard;
import etomica.lattice.LatticeCubicFcc;
import etomica.nbr.list.NeighborListManager;
import etomica.nbr.list.PotentialMasterList;
import etomica.phase.Phase;
import etomica.potential.P2HardSphere;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.species.Species;
import etomica.species.SpeciesSpheresMono;
import etomica.util.ParameterBase;

/**
 * 
 * Three-dimensional hard-sphere molecular dynamics simulation, using
 * neighbor listing.  
 * <p>
 * Developed as a prototype and example for the construction of a basic simulation.
 *
 * @author David Kofke and Andrew Schultz
 *
 */
public class HSMD3D extends Simulation {

    //the following fields are made accessible for convenience to permit simple
    //mutation of the default behavior

    private static final long serialVersionUID = 1L;
    /**
     * The Phase holding the atoms. 
     */
    public final Phase phase;
    /**
     * The Integrator performing the dynamics.
     */
    public final IntegratorHard integrator;
    /**
     * The single hard-sphere species.
     */
    public final SpeciesSpheresMono species;
    /**
     * The hard-sphere potential governing the interactions.
     */
    public final P2HardSphere potential;
    
    public final PotentialMaster potentialMaster;
    
    /**
     * Sole public constructor, makes a simulation using a 3D space.
     */
    public HSMD3D() {
        this(new HSMD3DParam());
    }
    
    public HSMD3D(HSMD3DParam params) {
        this(Space3D.getInstance(), params);
    }
    
    //we use a second, private constructor to permit the space to
    //appear twice in the call to the superclass constructor; alternatively
    //we could have passed Space3D.getInstance() twice
    private HSMD3D(Space space, HSMD3DParam params) {

        // invoke the superclass constructor
        // "true" is indicating to the superclass that this is a dynamic simulation
        // the PotentialMaster is selected such as to implement neighbor listing
        super(space, true);

        potentialMaster = params.useNeighborLists ? new PotentialMasterList(this, 1.6) : new PotentialMaster(space);

        int numAtoms = params.nAtoms;
        double neighborRangeFac = 1.6;
        double sigma = 1.0;
        if (params.useNeighborLists) {
            ((PotentialMasterList)potentialMaster).setRange(neighborRangeFac*sigma);
        }

        integrator = new IntegratorHard(this, potentialMaster);
        integrator.setIsothermal(false);
        integrator.setTimeStep(0.01);

        ActivityIntegrate activityIntegrate = new ActivityIntegrate(integrator);
        activityIntegrate.setDoSleep(true);
        activityIntegrate.setSleepPeriod(1);
        getController().addAction(activityIntegrate);
        
        species = new SpeciesSpheresMono(this);
        getSpeciesManager().addSpecies(species);
        potential = new P2HardSphere(space, sigma, false);

        potentialMaster.addPotential(potential,new Species[]{species,species});

        phase = new Phase(this);
        addPhase(phase);
        phase.getAgent(species).setNMolecules(numAtoms);
        phase.setDensity(params.eta * 6 / Math.PI);
        new ConfigurationLattice(new LatticeCubicFcc()).initializeCoordinates(phase);
        //deformed
//        phase.setBoundary(
//            new etomica.space.BoundaryDeformablePeriodic(
//            space,getRandom(),
//            new IVector[]{
//              new Vector3D(-4,1,1),
//              new Vector3D(2,6,4),
//              new Vector3D(1,2,6)}));
        //truncated octahedron
//        phase.setBoundary(
//            new etomica.space3d.BoundaryTruncatedOctahedron(this));
        
        integrator.setPhase(phase);

        if (params.useNeighborLists) { 
            NeighborListManager nbrManager = ((PotentialMasterList)potentialMaster).getNeighborManager(phase);
            ((PotentialMasterList)potentialMaster).setRange(sigma*neighborRangeFac);
            integrator.addIntervalAction(nbrManager);
            integrator.addNonintervalListener(nbrManager);
        }
        else {
            integrator.addIntervalAction(new PhaseImposePbc(phase));
        }
    }

    /**
     * Demonstrates how this class is implemented.
     */
    public static void main(String[] args) {
    	final String APP_NAME = "HSMD3D";

        HSMD3DParam params = new HSMD3DParam();
        params.ignoreOverlap = true;
        final etomica.simulation.prototypes.HSMD3D sim = new etomica.simulation.prototypes.HSMD3D(params);
        final SimulationGraphic simGraphic = new SimulationGraphic(sim, APP_NAME);
        DeviceNSelector nSelector = new DeviceNSelector(sim.getController());
        nSelector.setResetAction(new SimulationRestart(sim));
        nSelector.setSpeciesAgent(sim.phase.getAgent(sim.species));

        nSelector.setPostAction(simGraphic.getDisplayPhasePaintAction(sim.phase));
        simGraphic.add(nSelector);

        simGraphic.getController().getReinitButton().setPostAction(simGraphic.getDisplayPhasePaintAction(sim.phase));

        simGraphic.makeAndDisplayFrame(APP_NAME);
        ColorSchemeByType colorScheme = ((ColorSchemeByType)((DisplayPhase)simGraphic.displayList().getFirst()).getColorScheme());
        colorScheme.setColor(sim.species.getMoleculeType(), java.awt.Color.red);
    }

    public static HSMD3DParam getParameters() {
        return new HSMD3DParam();
    }

    /**
     * Inner class for parameters understood by the HSMD3D constructor
     */
    public static class HSMD3DParam extends ParameterBase {
        public int nAtoms = 256;
        public double eta = 0.35;
        public boolean ignoreOverlap = false;
        public boolean useNeighborLists = true;
    }
}
