
package etomica.paracetamol;

import etomica.action.PDBWriter;
import etomica.action.WriteConfiguration;
import etomica.action.WriteConfigurationDLPOLY;
import etomica.action.activity.ActivityIntegrate;
import etomica.action.activity.Controller;
import etomica.box.Box;
import etomica.data.DataLogger;
import etomica.data.DataPump;
import etomica.data.DataTableWriter;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.integrator.IntegratorMC;
import etomica.integrator.mcmove.MCMoveMolecule;
import etomica.integrator.mcmove.MCMoveRotateMolecule3D;
import etomica.integrator.mcmove.MCMoveStepTracker;
import etomica.lattice.BravaisLatticeCrystal;
import etomica.lattice.crystal.PrimitiveMonoclinic;
import etomica.normalmode.MCMoveHarmonicStep;
import etomica.normalmode.NormalModesFromFile;
import etomica.normalmode.WaveVectorFactory;
import etomica.normalmode.CoordinateDefinition.BasisCell;
import etomica.potential.PotentialDLPOLY;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.space.BoundaryDeformableLattice;
import etomica.space.Space;
import etomica.species.Species;
import etomica.units.Kelvin;

/**
 * 
 * Three-dimensional soft-sphere MC simulation for paracetamol molecule
 *  using the potential model from DL_MULTI package
 *  with inclusion of multipole ewald-summation
 *  
 * To get the lowest minimum cofiguration with MCMoveHarmonicStep
 * 
 * Monoclinic Crystal
 * 
 * The constructor takes in:
 * 		1. Space
 * 		2. Number of molecules
 * 		3. Temperature
 * 		4. Type of simulation 
 * 
 * Selection of type of simulation:
 * 		0. MC Simulation (MCMoveMolecule & MCMoveRotateMolecule3D)
 * 		1. Equilibration (MCMoveHarmonicStep)
 * 		2. SimCalcS      (MCMoveRotateMolecule3D & MCMoveMoleculeCoupledDLPOLY)
 * 
 * @author Tai Tan
 *
 */
public class MCParacetamolMonoclinicDLMULTI extends Simulation{
	private static final long serialVersionUID = 1L;
//	private final static String APP_NAME = "MC Paracetamol Monoclinic";
    public Box box;
    public IntegratorMC integrator;
    public MCMoveMolecule mcMoveMolecule;
    public MCMoveRotateMolecule3D mcMoveRotateMolecule;
    public MCMoveHarmonicStep mcMoveHarmonicStep;
    public MCMoveMoleculeCoupledDLPOLY mcMoveMoleculeCoupledDLPOLY;
    public SpeciesParacetamol species;
    public Controller controller;
    public PotentialMaster potentialMaster;
    public BravaisLatticeCrystal lattice;
    public BoundaryDeformableLattice bdry;
    public PrimitiveMonoclinic primitive;
    public CoordinateDefinitionParacetamol coordDef;
    public ActivityIntegrate actionIntegrate;
    public int simType;
    public static final int MCSIMULATION = 0;
    public static final int EQUILIBRATION = 1;
    public static final int SIMCALCS = 2;

  
    public MCParacetamolMonoclinicDLMULTI(Space space, int numMolecules, double temperature, int simType) {
    	super(space, true);
    	potentialMaster = new PotentialMaster(space);
    	this.simType = simType;
    	
    	/*
    	 * Monoclinic Crystal
    	 */
        
    	primitive = new PrimitiveMonoclinic(space,  12.119, 8.944, 7.278,  1.744806);
    	/*8.944, 12.119, 7.277, 1.74533*/
    	BasisMonoclinicParacetamol basis = new BasisMonoclinicParacetamol();
    	lattice = new BravaisLatticeCrystal (primitive, basis);
        
        integrator = new IntegratorMC(this, potentialMaster);
        integrator.setIsothermal(false);
        //integrator.setThermostatInterval(1);
        integrator.setTemperature(Kelvin.UNIT.toSim(123));
        
        integrator.getMoveManager().setEquilibrating(true);
        
        actionIntegrate = new ActivityIntegrate(integrator, 0, false);
        getController().addAction(actionIntegrate);
        
        ConformationParacetamolMonoclinic conformation = new ConformationParacetamolMonoclinic(space);
        species = new SpeciesParacetamol(this);
        species.getMoleculeType().setConformation(conformation);
        getSpeciesManager().addSpecies(species);
        
        box = new Box(this);
        addBox(box);
        box.setDimensions(Space.makeVector(new double[] {25,25,25}));
        box.setNMolecules(species, numMolecules);        
        
        bdry =  new BoundaryDeformableLattice( primitive, getRandom(), new int []{2, 3, 4});
        bdry.setDimensions(Space.makeVector(new double []{2*12.119, 3*8.944, 4*7.278}));
        box.setBoundary(bdry);
        
        coordDef = new CoordinateDefinitionParacetamol(box, primitive, basis);
        coordDef.setBasisMonoclinic();
        
        if (simType == 0){
        	coordDef.initializeCoordinates(new int []{2, 3, 4});
	        mcMoveMolecule = new MCMoveMolecule(this, potentialMaster);
	        mcMoveMolecule.setStepSize(0.1747);  //Step size to input
	        ((MCMoveStepTracker)mcMoveMolecule.getTracker()).setTunable(true);
	        ((MCMoveStepTracker)mcMoveMolecule.getTracker()).setNoisyAdjustment(true);
	        
	        mcMoveRotateMolecule = new MCMoveRotateMolecule3D(potentialMaster, random);
	        mcMoveRotateMolecule.setStepSize(0.068);
	        ((MCMoveStepTracker)mcMoveRotateMolecule.getTracker()).setNoisyAdjustment(true);
	        
	        
	        integrator.getMoveManager().setEquilibrating(true);
	        integrator.getMoveManager().addMCMove(mcMoveMolecule);
	        integrator.getMoveManager().addMCMove(mcMoveRotateMolecule);
        } else 
        	
        	if (simType == 1){
        		coordDef.initializeCoordinates(new int []{2, 3, 4});
                mcMoveHarmonicStep = new MCMoveHarmonicStep(potentialMaster, getRandom());
                mcMoveHarmonicStep.setCoordinateDefinition(coordDef);
                mcMoveHarmonicStep.setBox(box);
                
                int[] modes = new int[45];
                for (int i=0; i<45; i++){
                	modes[i] = i+3;
                }
           
                NormalModesFromFile normalModes = new NormalModesFromFile("Normal_Modes_Paracetamol_FormII_10.0K",3);
                WaveVectorFactory waveVectorFactory = normalModes.getWaveVectorFactory();
                waveVectorFactory.makeWaveVectors(box);
                
                mcMoveHarmonicStep.setModes(modes);
                mcMoveHarmonicStep.setEigenVectors(normalModes.getEigenvectors(box)[0]);
                mcMoveHarmonicStep.setStepSize(0.006);
                ((MCMoveStepTracker)mcMoveHarmonicStep.getTracker()).setAdjustInterval(5);
                ((MCMoveStepTracker)mcMoveHarmonicStep.getTracker()).setNoisyAdjustment(true);
               
                integrator.getMoveManager().addMCMove(mcMoveHarmonicStep);
                integrator.getMoveManager().setEquilibrating(true);
        	} else 
        		
        		if (simType == 2){
        			
        	        //ConfigurationFile configFile = new ConfigurationFile("FinalCoord_Min_Paracetamol_Monoclinic");
        			//coordinateDefinition.setConfiguration(configFile);
        	        coordDef.initializeCoordinates(new int []{2, 2, 2});
        	        double[] u = new double[]{0.19923028993600184, -0.10831241063851138, -0.3374206766423242, -0.056318915244514295, -0.08373011094015517, -0.20967425215989952, -0.15036406963107662, -0.06903390627642114, 0.2911023015207981, -0.062482609873595024, -0.0836259970912052, -0.17490727322325597, -0.19043886713958358, 0.09585326949021145, 0.29982577023219825, -0.052978731871725596, 0.0794450448846585, 0.20078353728718995, 0.1791946947288432, 0.07473598884040555, -0.33875779999181965, -0.06542404448301108, 0.0856334706980024, 0.20954733660556393};
        	        BasisCell[] cell = coordDef.getBasisCells() ;
        	        for (int i=0; i<cell.length; i++){
        	        	coordDef.setToU(cell[i].molecules, u);
        	        }
        			
        			
        	        mcMoveRotateMolecule = new MCMoveRotateMolecule3D(potentialMaster, random);
        	        mcMoveRotateMolecule.setStepSize(0.068);
        	        ((MCMoveStepTracker)mcMoveRotateMolecule.getTracker()).setNoisyAdjustment(true);
        	      
        	        mcMoveMoleculeCoupledDLPOLY = new MCMoveMoleculeCoupledDLPOLY(potentialMaster, getRandom());
        	        
        	        integrator.getMoveManager().addMCMove(mcMoveMoleculeCoupledDLPOLY);
        	        integrator.getMoveManager().addMCMove(mcMoveRotateMolecule);
        	        integrator.getMoveManager().setEquilibrating(true);
        		} else {
        			
        			throw new RuntimeException ("Need to input type of simulation!!!!");
        		}
   
        WriteConfigurationDLPOLY configDLPOLY = new WriteConfigurationDLPOLY();
        configDLPOLY.setConfName("CONFIG");
        configDLPOLY.setBox(box);
        configDLPOLY.getElementHash().put(HydrogenP.INSTANCE, "HP");
       // configDLPOLY.actionPerformed();
       // System.exit(1);
   
        PotentialDLPOLY potentialDLPOLY = new PotentialDLPOLY(space);
        potentialDLPOLY.setConfigDLPOLY(configDLPOLY);
        potentialMaster.addPotential(potentialDLPOLY, new Species[0]);

        integrator.setBox(box);
    } //end of constructor
    
    public static void main(String[] args) {
    	
    	int numMolecules = 96;
    	double temperature = Kelvin.UNIT.toSim(123);
    	long simSteps = 1000;
    	int simType = 0;
      
        String filename = "Paracetamol_Monoclinic_"+ Kelvin.UNIT.fromSim(temperature)+"K";
        if (args.length > 0) {
            filename = args[0];
        }
        if (args.length > 1) {
            simSteps = Long.parseLong(args[1]);
        }
        if (args.length > 2) {
            temperature = Kelvin.UNIT.toSim(Double.parseDouble(args[2]));
        }
        if (args.length > 3) {
            simType = Integer.parseInt(args[3]);
        }
        
        System.out.println("Running "+ "Monoclinic Paracetamol simulation");
        System.out.println("Type of simulation: "+simType);
        System.out.println(numMolecules + " molecules " +" and temperature "+ Kelvin.UNIT.fromSim(temperature) +"K");
        System.out.println(simSteps+ " steps");
        System.out.println("output data to " + filename);
        
        
    	MCParacetamolMonoclinicDLMULTI sim = 
        	new MCParacetamolMonoclinicDLMULTI(Space.getInstance(3), numMolecules, temperature, simType);

        sim.actionIntegrate.setMaxSteps(simSteps);
        MeterPotentialEnergy meterPE = new MeterPotentialEnergy(sim.potentialMaster);
        meterPE.setBox(sim.box);
        
        DataLogger dataLoggerPE = new DataLogger();
        dataLoggerPE.setWriteInterval(5);
        dataLoggerPE.setFileName("Paracetamol_Monoclinic_"+Kelvin.UNIT.fromSim(temperature)+"K");
        dataLoggerPE.setDataSink(new DataTableWriter());
        dataLoggerPE.setAppending(true);
        dataLoggerPE.setCloseFileEachTime(true);
        
        DataPump PEpump = new DataPump(meterPE, dataLoggerPE);
        sim.getController().getEventManager().addListener(dataLoggerPE);
        
        sim.integrator.addIntervalAction(PEpump);
        sim.integrator.setActionInterval(PEpump, 1);
        
        
        sim.getController().actionPerformed();
        
        WriteConfiguration writeConfig = new WriteConfiguration();
        writeConfig.setConfName("FinalCoord_Paracetamol_Monoclinic_"+Kelvin.UNIT.fromSim(temperature)+"K");
        writeConfig.setBox(sim.box);
        writeConfig.setDoApplyPBC(false);
        writeConfig.actionPerformed();
         
        PDBWriter pdbWriter = new PDBWriter(sim.box);
        pdbWriter.setFileName("GraphicView_Monoclinic_"+ Kelvin.UNIT.fromSim(temperature)+"K.pdb");
        pdbWriter.actionPerformed();
        
    }//end of main
  
}//end of class