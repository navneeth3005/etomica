package etomica.virial.simulations;

import etomica.Meter;
import etomica.Phase;
import etomica.Simulation;
import etomica.Space;
import etomica.Species;
import etomica.action.activity.ActivityIntegrate;
import etomica.data.AccumulatorRatioAverage;
import etomica.data.DataAccumulator;
import etomica.data.DataPump;
import etomica.integrator.IntervalActionAdapter;
import etomica.integrator.MCMove;
import etomica.integrator.mcmove.MCMoveAtom;
import etomica.virial.ClusterAbstract;
import etomica.virial.ClusterWeight;
import etomica.virial.ConfigurationCluster;
import etomica.virial.IntegratorClusterMC;
import etomica.virial.MCMoveClusterAtom;
import etomica.virial.MCMoveClusterAtomMulti;
import etomica.virial.MCMoveClusterMolecule;
import etomica.virial.MCMoveClusterMoleculeMulti;
import etomica.virial.MCMoveClusterRotateMolecule3D;
import etomica.virial.MeterVirial;
import etomica.virial.P0Cluster;
import etomica.virial.PhaseCluster;
import etomica.virial.SpeciesFactory;

/**
 * Generic simulation using Mayer sampling to evaluate cluster integrals
 */
public class SimulationVirial extends Simulation {

	/**
	 * Constructor for simulation to determine the ratio bewteen reference and target Clusters
	 */
	public SimulationVirial(Space space, SpeciesFactory speciesFactory, double temperature, ClusterWeight aSampleCluster, ClusterAbstract refCluster, ClusterAbstract[] targetClusters) {
		super(space);
        sampleCluster = aSampleCluster;
		int nMolecules = sampleCluster.pointCount();
		phase = new PhaseCluster(this,sampleCluster);
		species = speciesFactory.makeSpecies(this);//SpheresMono(this,AtomSequencerFactory.SIMPLE);
        species.setNMolecules(nMolecules);
        phase.makeMolecules();
        
		integrator = new IntegratorClusterMC(potentialMaster);
		integrator.setTemperature(temperature);
        integrator.addPhase(phase);
        integrator.setEquilibrating(false);
		ai = new ActivityIntegrate(integrator);
		ai.setInterval(1);
		getController().addAction(ai);
		
        if (phase.randomMolecule().node.isLeaf()) {
            mcMoveAtom1 = new MCMoveClusterAtom(potentialMaster);
            mcMoveAtom1.setStepSize(1.15);
            integrator.addMCMove(mcMoveAtom1);
            if (nMolecules>2) {
                mcMoveMulti = new MCMoveClusterAtomMulti(potentialMaster, nMolecules-1);
                mcMoveMulti.setStepSize(0.41);
                integrator.addMCMove(mcMoveMulti);
            }
        }
        else {
            mcMoveAtom1 = new MCMoveClusterMolecule(potentialMaster);
            mcMoveAtom1.setStepSize(3.0);
            integrator.addMCMove(mcMoveAtom1);
            mcMoveRotate = new MCMoveClusterRotateMolecule3D(potentialMaster,space);
            mcMoveRotate.setStepSize(Math.PI);
            integrator.addMCMove(mcMoveRotate);
            if (nMolecules>2) {
                mcMoveMulti = new MCMoveClusterMoleculeMulti(potentialMaster, nMolecules-1);
                mcMoveMulti.setStepSize(0.41);
                integrator.addMCMove(mcMoveMulti);
            }
        }
		
		P0Cluster p0 = new P0Cluster(space);
        p0.setTemperature(temperature);
		potentialMaster.setSpecies(p0,new Species[]{});
		
        ConfigurationCluster configuration = new ConfigurationCluster(space);
        configuration.setPhase(phase);
        phase.setConfiguration(configuration);

        allValueClusters = new ClusterAbstract[targetClusters.length+1];
        allValueClusters[0] = refCluster;
        System.arraycopy(targetClusters,0,allValueClusters,1,targetClusters.length);
        setMeter(new MeterVirial(allValueClusters,integrator,temperature));
        meter.setLabel("Target/Refernce Ratio");
        setAccumulator(new AccumulatorRatioAverage());
	}
	
	public Meter meter;
	public DataAccumulator accumulator;
	public DataPump accumulatorPump;
	public Species species;
	public ActivityIntegrate ai;
	public IntegratorClusterMC integrator;
	public PhaseCluster phase;
    public ClusterAbstract[] allValueClusters;
    public ClusterWeight sampleCluster;
    public MCMoveAtom mcMoveAtom1;
    public MCMove mcMoveRotate;
    public MCMove mcMoveMulti;

	public void setMeter(Meter newMeter) {
		if (accumulator != null) { 
			if (accumulatorPump != null) {
                // XXX oops, sorry you're screwed.
			    // integrator.removeIntervalListener(accumulatorPump);
				accumulatorPump = null;
			}
			accumulator = null;
		}
		meter = newMeter;
		if (meter != null) {
			meter.setPhase(new Phase[]{phase});
		}
	}

	public void setAccumulator(DataAccumulator newAccumulator) {
		accumulator = newAccumulator;
		if (accumulatorPump == null) {
			accumulatorPump = new DataPump(meter,accumulator);
		}
		else {
			accumulatorPump.setDataSinks(new DataAccumulator[] {accumulator});
		}
		integrator.addListener(new IntervalActionAdapter(accumulatorPump));
	}
}

