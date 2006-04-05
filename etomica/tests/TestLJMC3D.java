package etomica.tests;
import etomica.action.PhaseImposePbc;
import etomica.action.activity.ActivityIntegrate;
import etomica.action.activity.Controller;
import etomica.atom.AtomSourceRandomLeafSeq;
import etomica.config.ConfigurationFile;
import etomica.data.AccumulatorAverage;
import etomica.data.DataPump;
import etomica.data.meter.MeterPotentialEnergyFromIntegrator;
import etomica.data.meter.MeterPressure;
import etomica.data.types.DataDouble;
import etomica.data.types.DataGroup;
import etomica.integrator.IntegratorMC;
import etomica.integrator.IntervalActionAdapter;
import etomica.integrator.mcmove.MCMoveAtom;
import etomica.nbr.cell.PotentialMasterCell;
import etomica.phase.Phase;
import etomica.potential.P2LennardJones;
import etomica.potential.P2SoftSphericalTruncated;
import etomica.simulation.Simulation;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.species.Species;
import etomica.species.SpeciesSpheresMono;

/**
 * Simple Lennard-Jones Monte Carlo simulation in 3D.
 * Initial configurations at http://rheneas.eng.buffalo.edu/etomica/tests/
 */
 
public class TestLJMC3D extends Simulation {
    
    public IntegratorMC integrator;
    public MCMoveAtom mcMoveAtom;
    public SpeciesSpheresMono species;
    public Phase phase;
    public P2LennardJones potential;
    public Controller controller;

    public TestLJMC3D(Space space, int numAtoms) {
        super(space, false, new PotentialMasterCell(space));
        defaults.makeLJDefaults();
	    integrator = new IntegratorMC(this);
	    mcMoveAtom = new MCMoveAtom(this);
        mcMoveAtom.setAtomSource(new AtomSourceRandomLeafSeq());
        mcMoveAtom.setStepSize(0.2*defaults.atomSize);
        integrator.getMoveManager().addMCMove(mcMoveAtom);
        integrator.setEquilibrating(false);
        ActivityIntegrate activityIntegrate = new ActivityIntegrate(this,integrator);
        activityIntegrate.setMaxSteps(200000);
        getController().addAction(activityIntegrate);
        species = new SpeciesSpheresMono(this);
        species.setNMolecules(numAtoms);
	    phase = new Phase(this);
        phase.setDensity(0.65);
        potential = new P2LennardJones(this);
        double truncationRadius = 3.0*potential.getSigma();
        if(truncationRadius > 0.5*phase.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  Max allowed is"+0.5*phase.getBoundary().getDimensions().x(0));
        }
        P2SoftSphericalTruncated potentialTruncated = new P2SoftSphericalTruncated(potential, truncationRadius);
        ((PotentialMasterCell)potentialMaster).setCellRange(3);
        ((PotentialMasterCell)potentialMaster).setRange(potentialTruncated.getRange());
        potentialTruncated.setCriterion(etomica.nbr.NeighborCriterion.ALL);
        potentialMaster.addPotential(potentialTruncated, new Species[] {species, species});
        integrator.addMCMoveListener(((PotentialMasterCell)potentialMaster).getNbrCellManager(phase).makeMCMoveListener());
        
        new ConfigurationFile(space,"LJMC3D"+Integer.toString(numAtoms)).initializeCoordinates(phase);
        integrator.setPhase(phase);
        ((PotentialMasterCell)potentialMaster).updateTypeList(phase);
        PhaseImposePbc imposePBC = new PhaseImposePbc(phase);
        imposePBC.actionPerformed();
        ((PotentialMasterCell)potentialMaster).getNbrCellManager(phase).assignCellAll();
//        WriteConfiguration writeConfig = new WriteConfiguration("LJMC3D"+Integer.toString(numAtoms),phase,1);
//        integrator.addListener(writeConfig);
    }
 
    public static void main(String[] args) {
        int numAtoms = 500;
        if (args.length > 0) {
            numAtoms = Integer.valueOf(args[0]).intValue();
        }
        TestLJMC3D sim = new TestLJMC3D(Space3D.getInstance(), numAtoms);

        sim.getDefaults().blockSize = 10;
        MeterPressure pMeter = new MeterPressure(sim.potentialMaster,sim.space);
        pMeter.setTemperature(sim.integrator.getTemperature());
        pMeter.setPhase(sim.phase);
        AccumulatorAverage pAccumulator = new AccumulatorAverage(sim);
        DataPump pPump = new DataPump(pMeter,pAccumulator);
        IntervalActionAdapter iaa = new IntervalActionAdapter(pPump,sim.integrator);
        iaa.setActionInterval(2*numAtoms);
        MeterPotentialEnergyFromIntegrator energyMeter = new MeterPotentialEnergyFromIntegrator(sim.integrator);
        energyMeter.setPhase(sim.phase);
        AccumulatorAverage energyAccumulator = new AccumulatorAverage(sim);
        DataPump energyManager = new DataPump(energyMeter, energyAccumulator);
        energyAccumulator.setBlockSize(50);
        new IntervalActionAdapter(energyManager, sim.integrator);
        
        sim.getController().actionPerformed();
        
        double Z = ((DataDouble)((DataGroup)pAccumulator.getData()).getData(AccumulatorAverage.AVERAGE.index)).x*sim.phase.volume()/(sim.phase.moleculeCount()*sim.integrator.getTemperature());
        double avgPE = ((DataDouble)((DataGroup)energyAccumulator.getData()).getData(AccumulatorAverage.AVERAGE.index)).x;
        avgPE /= numAtoms;
        System.out.println("Z="+Z);
        System.out.println("PE/epsilon="+avgPE);
        double temp = sim.integrator.getTemperature();
        double Cv = ((DataDouble)((DataGroup)energyAccumulator.getData()).getData(AccumulatorAverage.STANDARD_DEVIATION.index)).x;
        Cv /= temp;
        Cv *= Cv/numAtoms;
        System.out.println("Cv/k="+Cv);
        
        if (Double.isNaN(Z) || Math.abs(Z-0.11) > 0.15) {
            System.exit(1);
        }
        if (Double.isNaN(avgPE) || Math.abs(avgPE+4.355) > 0.04) {
            System.exit(1);
        }
        if (Double.isNaN(Cv) || Math.abs(Cv-0.6) > 0.4) {
            System.exit(1);
        }
    }

}
