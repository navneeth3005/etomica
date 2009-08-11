package etomica.modules.mu;
import etomica.action.activity.ActivityIntegrate;
import etomica.api.IAtomType;
import etomica.api.IBox;
import etomica.api.IVectorMutable;
import etomica.box.Box;
import etomica.config.ConfigurationLattice;
import etomica.graphics.SimulationGraphic;
import etomica.integrator.IntegratorHard;
import etomica.integrator.IntegratorMD.ThermostatType;
import etomica.lattice.BravaisLatticeCrystal;
import etomica.lattice.LatticeCubicFcc;
import etomica.lattice.LatticeOrthorhombicHexagonal;
import etomica.nbr.list.PotentialMasterList;
import etomica.potential.P1HardBoundary;
import etomica.simulation.Simulation;
import etomica.space.BoundaryRectangularSlit;
import etomica.space.ISpace;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.species.SpeciesSpheresMono;

public class Mu extends Simulation {
    
    private static final long serialVersionUID = 1L;
    public SpeciesSpheresMono species;
    public IBox box;
    public IntegratorHard integrator;
    public ActivityIntegrate activityIntegrate;
    public P2SquareWellOneSide potentialSW;
    public P1MagicWall p1Wall;
    
    public Mu(ISpace _space) {
        super(_space);
        PotentialMasterList potentialMaster = new PotentialMasterList(this, 4, space); //List(this, 2.0);
        
        int N = 300;  //number of atoms
        
        double sigma = 1.0;
        double lambda = 1.5;
        double epsilon = 1.0;
        
        //controller and integrator
	    integrator = new IntegratorHard(this, potentialMaster, space);
	    integrator.setTimeStep(0.02);
	    integrator.setTemperature(1);
	    integrator.setIsothermal(true);
        integrator.setThermostat(ThermostatType.ANDERSEN_SINGLE);
        integrator.setThermostatInterval(1);
        activityIntegrate = new ActivityIntegrate(integrator, 0, true);
        getController().addAction(activityIntegrate);

	    //species and potentials
	    species = new SpeciesSpheresMono(this, space);//index 1
        getSpeciesManager().addSpecies(species);
        
        //instantiate several potentials for selection in combo-box
	    potentialSW = new P2SquareWellOneSide(space, sigma, lambda, epsilon, true);
        potentialMaster.addPotential(potentialSW,new IAtomType[]{species.getLeafType(), species.getLeafType()});
        
        P1HardBoundary p1Boundary = new P1HardBoundary(space);
        p1Boundary.setActive(0, false, true);
        p1Boundary.setActive(0, true, true);
        p1Boundary.setActive(1, false, false);
        p1Boundary.setActive(1, true, false);
        if (space.D() == 3) {
            p1Boundary.setActive(2, false, false);
            p1Boundary.setActive(2, true, false);
        }
        potentialMaster.addPotential(p1Boundary,new IAtomType[]{species.getLeafType()});
	    p1Wall = new P1MagicWall(space, potentialMaster);
	    p1Wall.setEpsilon(epsilon);
	    p1Wall.setSigma(sigma);
	    p1Wall.setLambda(lambda);
	    potentialMaster.addPotential(p1Wall, new IAtomType[]{species.getLeafType()});
        //construct box
	    box = new Box(new BoundaryRectangularSlit(0, space), space);
        addBox(box);
        IVectorMutable dim = space.makeVector();
        double xdim = space.D() == 3 ? 30 : 50;
        dim.E(0.5*xdim);
        dim.setX(0, xdim);
        box.getBoundary().setBoxSize(dim);
        box.setNMolecules(species, N);
        new ConfigurationLattice(space.D() == 3 ? new LatticeCubicFcc(space) : new LatticeOrthorhombicHexagonal(space), space).initializeCoordinates(box);
        integrator.setBox(box);
        integrator.getEventManager().addListener(potentialMaster.getNeighborManager(box));
    }
    
    public static void main(String[] args) {
        Space space = Space3D.getInstance();
        Mu sim = new Mu(space);
        SimulationGraphic simGraphic = new SimulationGraphic(sim, SimulationGraphic.GRAPHIC_ONLY, "", 1, space, sim.getController());
        simGraphic.makeAndDisplayFrame();
    }
}