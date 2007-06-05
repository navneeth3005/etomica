package etomica.simulation.prototypes;
import etomica.action.PhaseImposePbc;
import etomica.action.activity.ActivityIntegrate;
import etomica.action.activity.Controller;
import etomica.config.ConfigurationLattice;
import etomica.graphics.DisplayPhase;
import etomica.integrator.IntegratorHard;
import etomica.integrator.IntervalActionAdapter;
import etomica.lattice.LatticeOrthorhombicHexagonal;
import etomica.phase.Phase;
import etomica.potential.P2SquareWell;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.space.Space;
import etomica.space2d.Space2D;
import etomica.species.Species;
import etomica.species.SpeciesSpheresMono;

/**
 * Simple square-well molecular dynamics simulation in 2D
 */
 
public class SwMd2D extends Simulation {
    
    private static final long serialVersionUID = 1L;
    public IntegratorHard integrator;
    public SpeciesSpheresMono species;
    public Phase phase;
    public P2SquareWell potential;
    public Controller controller;
    public DisplayPhase display;

    public SwMd2D() {
        this(Space2D.getInstance());
    }
    public SwMd2D(Space space) {
        super(space);
        PotentialMaster potentialMaster = new PotentialMaster(space);
        defaults.makeLJDefaults();
        defaults.atomSize = 0.8;
        integrator = new IntegratorHard(this, potentialMaster);
        integrator.setTimeStep(0.01);
        integrator.setEventInterval(5);
        ActivityIntegrate activityIntegrate = new ActivityIntegrate(this,integrator);
        activityIntegrate.setSleepPeriod(1);
        integrator.setTimeStep(0.02);
        integrator.setTemperature(300.);
        getController().addAction(activityIntegrate);
        species = new SpeciesSpheresMono(this);
        getSpeciesManager().addSpecies(species);
        phase = new Phase(this);
        addPhase(phase);
        phase.getAgent(species).setNMolecules(50);
        new ConfigurationLattice(new LatticeOrthorhombicHexagonal()).initializeCoordinates(phase);
        potential = new P2SquareWell(this);
        potentialMaster.addPotential(potential,new Species[]{species,species});
        
        integrator.setPhase(phase);
        integrator.addListener(new IntervalActionAdapter(new PhaseImposePbc(phase)));
    } 
}