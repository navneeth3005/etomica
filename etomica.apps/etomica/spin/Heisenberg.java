package etomica.spin;

import etomica.action.Action;
import etomica.action.SimulationRestart;
import etomica.action.activity.ActivityIntegrate;
import etomica.atom.AtomType;
import etomica.box.Box;
import etomica.box.BoxAgentManager;
import etomica.data.AccumulatorAverageCollapsing;
import etomica.data.DataPump;
import etomica.graphics.DeviceSlider;
import etomica.graphics.DisplayBox;
import etomica.graphics.DisplayBoxSpin2D;
import etomica.graphics.DisplayTextBox;
import etomica.graphics.DisplayTextBoxesCAE;
import etomica.graphics.SimulationGraphic;
import etomica.integrator.IntegratorMC;
import etomica.nbr.site.NeighborSiteManager;
import etomica.nbr.site.PotentialMasterSite;
import etomica.simulation.Simulation;
import etomica.space.Space;
import etomica.space2d.Space2D;
import etomica.species.Species;
import etomica.species.SpeciesSpheresMono;
import etomica.units.systems.LJ;


/**
 * Simulation of a simple 2D Ising model.  Prototype
 * for simulation of a more general magentic system.
 *
 * @author David Kofke
 *
 */
public class Heisenberg extends Simulation {

	private static final String APP_NAME = "Heisenberg";

    public Heisenberg() {
        this(Space2D.getInstance(),60);
    }
    
    /**
     * 
     */
    public Heisenberg(Space space, int nCells) {
        super(space, false);
        potentialMaster = new PotentialMasterSite(this, nCells);
        box = new Box(this);
        addBox(box);
        int numAtoms = space.powerD(nCells);
        spins = new SpeciesSpheresMono(this);
        getSpeciesManager().addSpecies(spins);
        box.setNMolecules(spins, numAtoms);
        new ConfigurationAligned().initializeCoordinates(box);
        
        potential = new P2Spin(space);
        field = new P1MagneticField(space);
        integrator = new IntegratorMC(this, potentialMaster);
        mcmove = new MCMoveSpinFlip(potentialMaster, getRandom());
        integrator.getMoveManager().addMCMove(mcmove);
        
        ActivityIntegrate activityIntegrate = new ActivityIntegrate(integrator);
        getController().addAction(activityIntegrate);

        AtomType type = spins.getLeafType();
        potentialMaster.addPotential(field, new AtomType[] {type});
        potentialMaster.addPotential(potential, new AtomType[] {type, type});
        
        integrator.setBox(box);
        
        meter = new MeterSpin(space);
        meter.setBox(box);
        dAcc = new AccumulatorAverageCollapsing();
        pump = new DataPump(meter, dAcc);
        dAcc.setPushInterval(10);
        integrator.addIntervalAction(pump);
        integrator.setActionInterval(pump, 10);
    }

    private static final long serialVersionUID = 2L;
    public PotentialMasterSite potentialMaster;
    public Box box;
    public SpeciesSpheresMono spins;
    public P2Spin potential;
    public P1MagneticField field;
    private IntegratorMC integrator;
    public MCMoveSpinFlip mcmove;
    public MeterSpin meter;
    public DataPump pump;
    public AccumulatorAverageCollapsing dAcc;
    
    public static void main(String[] args) {
        Heisenberg sim = new Heisenberg(Space2D.getInstance(), 60);
        SimulationGraphic simGraphic = new SimulationGraphic(sim, APP_NAME);
        ((SimulationRestart)simGraphic.getController().getReinitButton().getAction()).setConfiguration(null);
		Action repaintAction = simGraphic.getPaintAction(sim.box);
        DisplayBox displayBox = simGraphic.getDisplayBox(sim.box);

        simGraphic.remove(displayBox);
        BoxAgentManager boxAgentManager = sim.potentialMaster.getCellAgentManager();
        NeighborSiteManager neighborSiteManager = (NeighborSiteManager)boxAgentManager.getAgent(sim.box);
        displayBox.setBoxCanvas(new DisplayBoxSpin2D(displayBox,neighborSiteManager));
        simGraphic.add(displayBox);
        DeviceSlider temperatureSlider = new DeviceSlider(sim.getController(), sim.integrator,"temperature");
        temperatureSlider.setMinimum(0.5);
        temperatureSlider.setMaximum(10.0);
        temperatureSlider.setShowBorder(true);
        LJ lj = new LJ();
        temperatureSlider.setUnit(lj.temperature());
        simGraphic.add(temperatureSlider);
        temperatureSlider.setValue(sim.integrator.getTemperature());
        DeviceSlider fieldSlider = new DeviceSlider(sim.getController(), sim.field, "h");
        fieldSlider.setMinimum(-5.);
        fieldSlider.setMaximum(+5.);
        fieldSlider.setNMajor(5);
        fieldSlider.setValue(0.0);
        fieldSlider.setShowBorder(true);
        fieldSlider.setLabel("Magnetic field");
        simGraphic.add(fieldSlider);
        
        DisplayTextBoxesCAE boxes = new DisplayTextBoxesCAE();
        boxes.setAccumulator(sim.dAcc);
        boxes.setLabel("Magnetization");
        boxes.setLabelType(DisplayTextBox.LabelType.BORDER);
        simGraphic.add(boxes);

        simGraphic.getController().getReinitButton().setPostAction(repaintAction);
        simGraphic.makeAndDisplayFrame(APP_NAME);
    }
}
