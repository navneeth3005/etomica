package etomica.virial.simulations;

import etomica.Default;
import etomica.MeterAbstract;
import etomica.Phase;
import etomica.Simulation;
import etomica.Space;
import etomica.Species;
import etomica.SpeciesSpheresMono;
import etomica.action.activity.ActivityIntegrate;
import etomica.atom.AtomSequencerFactory;
import etomica.data.AccumulatorAverage;
import etomica.data.AccumulatorRatioAverage;
import etomica.data.DataAccumulator;
import etomica.data.DataPump;
import etomica.integrator.IntervalActionAdapter;
import etomica.integrator.mcmove.MCMoveAtom;
import etomica.potential.P2LennardJones;
import etomica.space3d.Space3D;
import etomica.virial.Cluster;
import etomica.virial.ClusterAbstract;
import etomica.virial.ClusterSum;
import etomica.virial.ClusterWeight;
import etomica.virial.ClusterWeightAbs;
import etomica.virial.ConfigurationCluster;
import etomica.virial.IntegratorClusterMC;
import etomica.virial.MCMoveClusterAtom;
import etomica.virial.MCMoveClusterAtomMulti;
import etomica.virial.MayerGeneral;
import etomica.virial.MayerHardSphere;
import etomica.virial.MeterVirial;
import etomica.virial.P0Cluster;
import etomica.virial.PhaseCluster;
import etomica.virial.cluster.D4;
import etomica.virial.cluster.D5;
import etomica.virial.cluster.D6;
import etomica.virial.cluster.Full;
import etomica.virial.cluster.Standard;

/**
 * Generic simulation using Mayer sampling to evaluate cluster integrals
 */
public class SimulationVirial extends Simulation {

	/**
	 * Constructor for simulation to determine the ratio bewteen reference and target Clusters
	 */
	public SimulationVirial(Space space, double temperature, ClusterWeight aSampleCluster, ClusterAbstract refCluster, ClusterAbstract[] targetClusters) {
		super(space);
		Default.TRUNCATE_POTENTIALS = false;
        sampleCluster = aSampleCluster;
		int nMolecules = sampleCluster.pointCount();
		phase = new PhaseCluster(this,sampleCluster);
		species = new SpeciesSpheresMono(this,AtomSequencerFactory.SIMPLE);
        species.setNMolecules(nMolecules);
        phase.makeMolecules();
        
		integrator = new IntegratorClusterMC(potentialMaster);
		integrator.setTemperature(temperature);
        integrator.addPhase(phase);
        integrator.setEquilibrating(false);
		ai = new ActivityIntegrate(integrator);
		ai.setInterval(1);
		getController().addAction(ai);
		
        MCMoveAtom mcMoveAtom1 = new MCMoveClusterAtom(potentialMaster);
        mcMoveAtom1.setStepSize(1.495);
        integrator.addMCMove(mcMoveAtom1);
		if (nMolecules>2) {
			MCMoveClusterAtomMulti multiMove = new MCMoveClusterAtomMulti(potentialMaster, nMolecules-1);
            multiMove.setStepSize(0.951);
            integrator.addMCMove(multiMove);
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
	
	public MeterAbstract meter;
	public DataAccumulator accumulator;
	public DataPump accumulatorPump;
	public SpeciesSpheresMono species;
	public ActivityIntegrate ai;
	public IntegratorClusterMC integrator;
	public PhaseCluster phase;
    public ClusterAbstract[] allValueClusters;
    public ClusterWeight sampleCluster;

	public void setMeter(MeterAbstract newMeter) {
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
		integrator.addIntervalListener(new IntervalActionAdapter(accumulatorPump));
	}
	
	public static void main(String[] args) {
		Default.makeLJDefaults();
		Default.TRUNCATE_POTENTIALS = false;

		int nPoints = 4;
		double temperature = 1.3;
		double sigmaHSRef = 1.0;
		double b0 = Standard.B2HS(sigmaHSRef);
        double c0 = Standard.C3HS(sigmaHSRef);
        double d0 = Standard.D4HS(sigmaHSRef);
		Default.ATOM_SIZE = 1.0;
		System.out.println("sigmaHSRef: "+sigmaHSRef);
		System.out.println("B2HS: "+b0);
		System.out.println("B3HS: "+c0+" = "+(c0/b0/b0)+" B2HS^2");
		System.out.println("B4HS: "+d0+" = "+(d0/(b0*b0*b0))+" B2HS^3");
		
		MayerHardSphere fRef = new MayerHardSphere(1.0);
		P2LennardJones p2LJ = new P2LennardJones(new Space3D(),1.0,1.0,null);
		System.out.println("LJ sampling");
		MayerGeneral fTarget = new MayerGeneral(p2LJ);
		
		//take care to define other clusters (below) appropriately if using ReeHoover
//		Cluster sampleCluster = new Full(nMolecules, fSample);
//		ClusterSum sampleCluster = new ClusterSum(new Cluster[] {new C3(fSample));
//		ClusterSum refCluster = new ClusterSum(new Cluster[] {new D4(fSample), new D5(fSample), new D6(fSample)});

//		Cluster targetCluster = new Cluster(5,
//			new Cluster.BondGroup(f, new int[][] {{0,1},{0,2},{0,3},{0,4},{1,2},{1,3},{2,3},{2,4},{3,4}}));
//		Cluster targetCluster = new etomica.virial.cluster.ReeHoover(4, new Cluster.BondGroup(f, Standard.D4));

		ClusterAbstract refCluster, targetCluster;
		if (nPoints < 4) {
			refCluster = new Full(nPoints, fRef);
            targetCluster = new Full(nPoints, fTarget);
		}
		else if (nPoints == 4) {
			refCluster = new ClusterSum(new Cluster[] {new D4(fRef,true), new D5(fRef,true), new D6(fRef)}, new double[]{-3./8.,-3./4.,-1./8.});
            targetCluster = new ClusterSum(new Cluster[] {new D4(fTarget,true), new D5(fTarget,true), new D6(fTarget)}, new double[]{-3./8.,-3./4.,-1./8.});
		}
		else throw new RuntimeException("I don't know how to do clusters with more than 4 points!");
		ClusterWeight sampleCluster = ClusterWeightAbs.makeWeightCluster(targetCluster); //refCluster.makeClone(fSample);
		int steps = 10000000;
        Default.BLOCK_SIZE = steps/100;
		
		while (true) {
			SimulationVirial sim = new SimulationVirial(new Space3D(),temperature,sampleCluster,refCluster,new ClusterAbstract[] {targetCluster});
			sim.ai.setMaxSteps(steps);
			sim.getController().run();
            AccumulatorAverage acc = (AccumulatorRatioAverage)sim.accumulator;
            double[][] allYourBase = (double[][])acc.getTranslator().fromArray(acc.getData());
            System.out.println(allYourBase.length);
            System.out.println("average: "+allYourBase[AccumulatorRatioAverage.RATIO.index][1]
                              +" error: "+allYourBase[AccumulatorRatioAverage.RATIO_ERROR.index][1]);
            System.out.println("hard sphere   average: "+allYourBase[AccumulatorRatioAverage.AVERAGE.index][0]
                              +" stdev: "+allYourBase[AccumulatorRatioAverage.STANDARD_DEVIATION.index][0]);
            System.out.println("lennard jones average: "+allYourBase[AccumulatorRatioAverage.AVERAGE.index][1]
                              +" stdev: "+allYourBase[AccumulatorRatioAverage.STANDARD_DEVIATION.index][1]);
		}
	}
}

