package etomica.paracetamol;

import java.util.ArrayList;

import etomica.action.activity.ActivityIntegrate;
import etomica.action.activity.Controller;
import etomica.atom.AtomType;
import etomica.atom.AtomTypeMolecule;
import etomica.box.Box;
import etomica.config.ConfigurationFile;
import etomica.data.DataPump;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.graphics.ColorSchemeByType;
import etomica.graphics.DisplayBox;
import etomica.graphics.DisplayTextBox;
import etomica.graphics.SimulationGraphic;
import etomica.integrator.IntegratorMC;
import etomica.integrator.mcmove.MCMoveMolecule;
import etomica.integrator.mcmove.MCMoveRotateMolecule3D;
import etomica.lattice.BravaisLattice;
import etomica.lattice.BravaisLatticeCrystal;
import etomica.lattice.crystal.PrimitiveOrthorhombic;
import etomica.normalmode.MCMoveHarmonic;
import etomica.normalmode.NormalModesFromFile;
import etomica.normalmode.WaveVectorFactory;
import etomica.potential.P2SoftSphericalTruncated;
import etomica.potential.PotentialMaster;
import etomica.simulation.Simulation;
import etomica.space.BoundaryRectangularPeriodic;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.units.ElectronVolt;
import etomica.units.Kelvin;
import etomica.units.Pixel;

/**
 * 
 * Three-dimensional soft-sphere MC simulation for paracetamol molecule
 * 
 * Orthorhombic Crystal
 * 
 * @author Tai Tan
 *
 */
public class MCParacetamolOrthorhombicMoveHarmonic extends Simulation {

	private static final long serialVersionUID = 1L;
	private final static String APP_NAME = "MC Move Harmonic Paracetamol Orthorhombic";
    public Box box;
    public IntegratorMC integrator;
    public MCMoveMolecule mcMoveMolecule;
    public MCMoveRotateMolecule3D mcMoveRotateMolecule;
    public SpeciesParacetamol species;
    public P2ElectrostaticDreiding potentialCC , potentialCHy , potentialHyHy;
    public P2ElectrostaticDreiding potentialCN , potentialNO  , potentialNN  ;
    public P2ElectrostaticDreiding potentialHyN, potentialHyO , potentialOO  ;
    public P2ElectrostaticDreiding potentialCO , potentialHpHp, potentialCHp ;
    public P2ElectrostaticDreiding potentialHpN, potentialOHp , potentialHyHp;
    public Controller controller;

  
    public MCParacetamolOrthorhombicMoveHarmonic() {
        this(192);
    }
    
    private MCParacetamolOrthorhombicMoveHarmonic(int numMolecules) {

    	super(Space3D.getInstance(), false);
    	
        potentialMaster = new PotentialMaster(space);
    	
    	/*
    	 * Orthorhombic Crystal
    	 */
    	
        PrimitiveOrthorhombic primitive = new PrimitiveOrthorhombic(space, 17.248, 12.086, 7.382);
        // 17.248, 12.086, 7.382
        BasisOrthorhombicParacetamol basis = new BasisOrthorhombicParacetamol();
        lattice = new BravaisLatticeCrystal(primitive, basis); 
        
        integrator = new IntegratorMC(this, potentialMaster);
        integrator.setIsothermal(false);
        integrator.setTemperature(Kelvin.UNIT.toSim(100));
        
        
        /*
         * Harmonic Simulation
         */
        MCMoveHarmonic moveHarmonic = new MCMoveHarmonic(random);
        integrator.getMoveManager().addMCMove(moveHarmonic);
        
        NormalModesFromFile normalModes = new NormalModesFromFile("Normal_Modes_Paracetamol_FormII_100.0K", 3);
        normalModes.setTemperature(Kelvin.UNIT.toSim(100));
        
        WaveVectorFactory waveVectorFactory = normalModes.getWaveVectorFactory();
        waveVectorFactory.makeWaveVectors(box);
        moveHarmonic.setOmegaSquared(normalModes.getOmegaSquared(box), waveVectorFactory.getCoefficients());
        moveHarmonic.setEigenVectors(normalModes.getEigenvectors(box));
        moveHarmonic.setWaveVectors(waveVectorFactory.getWaveVectors());
        moveHarmonic.setWaveVectorCoefficients(waveVectorFactory.getCoefficients());
        moveHarmonic.setTemperature(Kelvin.UNIT.toSim(1));
        
        actionIntegrate = new ActivityIntegrate(integrator, 0, false);
        //actionIntegrate.setMaxSteps(1);
        getController().addAction(actionIntegrate);
        
        ConformationParacetamolOrthorhombic conformation = new ConformationParacetamolOrthorhombic(space);
        species = new SpeciesParacetamol(this);
        species.getMoleculeType().setConformation(conformation);
        getSpeciesManager().addSpecies(species);
        
        box = new Box(this);
        addBox(box);
        box.setDimensions(Space.makeVector(new double[] {25,25,25}));
        box.setNMolecules(species, numMolecules);        
        
        /*
         * Intermolecular Potential
         */
    
        double truncationRadiusCC   = 3.0* 3.395524116;
        double truncationRadiusCHy  = 3.0* 2.670105986;
        double truncationRadiusHyHy = 3.0* 2.099665865;
        double truncationRadiusCN   = 3.0* 3.237739512;
        double truncationRadiusNO   = 3.0* 3.035146951;
        double truncationRadiusNN   = 3.0* 3.087286897;
        double truncationRadiusHyN  = 3.0* 2.546030404;
        double truncationRadiusHyO  = 3.0* 2.503031484;
        double truncationRadiusOO   = 3.0* 2.983887553;
        double truncationRadiusCO   = 3.0* 3.183058614;
        double truncationRadiusHpHp = 3.0* 1.543178334;
        double truncationRadiusCHp  = 3.0* 2.289082817;
        double truncationRadiusHpN  = 3.0* 2.182712943;
        double truncationRadiusOHp  = 3.0* 2.145849932;
        double truncationRadiusHyHp = 3.0* 1.800044389;
        
        potentialCC   = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim(3832.14700),
        		0.277778, ElectronVolt.UNIT.toSim(25.286949));
        potentialCHy  = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim( 689.53672),
        		0.272480, ElectronVolt.UNIT.toSim( 5.978972));
        potentialHyHy = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim( 124.07167), 
        		0.267380, ElectronVolt.UNIT.toSim( 1.413698));
        potentialCN   = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim(3179.51460),
        		0.271003, ElectronVolt.UNIT.toSim(19.006710));
        potentialNO   = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim(2508.04480),
        		0.258398, ElectronVolt.UNIT.toSim(12.898341));
        potentialNN   = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim(2638.02850),
        		0.264550, ElectronVolt.UNIT.toSim(14.286224));
        potentialHyN  = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim( 572.10541),
        		0.265957, ElectronVolt.UNIT.toSim( 4.494041));
        potentialHyO  = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim( 543.91604),
        		0.259740, ElectronVolt.UNIT.toSim( 4.057452));
        potentialOO   = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim(2384.46580),
        		0.252525, ElectronVolt.UNIT.toSim(11.645288));
        potentialCO   = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim(3022.85020), 
        		0.264550, ElectronVolt.UNIT.toSim(17.160239));
        potentialHpHp = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim(  52.12899), 
        		0.214592, ElectronVolt.UNIT.toSim( 0.222819));
        potentialCHp  = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim( 446.95185), 
        		0.242131, ElectronVolt.UNIT.toSim( 2.373693));
        potentialHpN  = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim( 370.83387), 
        		0.236967, ElectronVolt.UNIT.toSim( 1.784166));
        potentialOHp  = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim( 352.56176), 
        		0.232019, ElectronVolt.UNIT.toSim( 1.610837));
        potentialHyHp = new P2ElectrostaticDreiding(space, ElectronVolt.UNIT.toSim(  80.42221), 
        		0.238095, ElectronVolt.UNIT.toSim( 0.561248));
        
        // CA-CA
        if(truncationRadiusCC > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialCC = new P2SoftSphericalTruncated (potentialCC, truncationRadiusCC); 
        potentialMaster.addPotential(interpotentialCC, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).cType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).cType} );
        
        // CA-HY
        if(truncationRadiusCHy > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialCHy = new P2SoftSphericalTruncated (potentialCHy, truncationRadiusCHy); 
        potentialMaster.addPotential(interpotentialCHy, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).cType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).hyType} );
        
        // HY-HY
        if(truncationRadiusHyHy > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialHyHy = new P2SoftSphericalTruncated (potentialHyHy, truncationRadiusHyHy); 
        potentialMaster.addPotential(interpotentialHyHy, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).hyType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).hyType} );
               
        // CA-NI
        if(truncationRadiusCN > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialCN = new P2SoftSphericalTruncated (potentialCN, truncationRadiusCN); 
        potentialMaster.addPotential(interpotentialCN, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).cType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).nType} );
        
        // NI-OX
        if(truncationRadiusNO > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialNO = new P2SoftSphericalTruncated (potentialNO, truncationRadiusNO); 
        potentialMaster.addPotential(interpotentialNO, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).nType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).oType} );
        
        //NI-NI
        if(truncationRadiusNN > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialNN = new P2SoftSphericalTruncated (potentialNN, truncationRadiusNN); 
        potentialMaster.addPotential(interpotentialNN, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).nType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).nType} );
        
        // HY-NI
        if(truncationRadiusHyN > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialHyN = new P2SoftSphericalTruncated (potentialHyN, truncationRadiusHyN); 
        potentialMaster.addPotential(interpotentialHyN, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).hyType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).nType} );
        
        // HY-OX
        if(truncationRadiusHyO > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large. " +
            		" Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialHyO = new P2SoftSphericalTruncated (potentialHyO, truncationRadiusHyO); 
        potentialMaster.addPotential(interpotentialHyO, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).hyType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).oType} );
             
        // OX-OX
        if(truncationRadiusOO > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large. " +
            		" Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialOO = new P2SoftSphericalTruncated (potentialOO, truncationRadiusOO); 
        potentialMaster.addPotential(interpotentialOO, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).oType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).oType} );
        
        // CA-OX
        if(truncationRadiusCO > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large. " +
            		" Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialCO = new P2SoftSphericalTruncated (potentialCO, truncationRadiusCO); 
        potentialMaster.addPotential(interpotentialCO, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).cType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).oType} );
        
        // HP-HP
        if(truncationRadiusHpHp > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large. " +
            		" Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialHpHp = new P2SoftSphericalTruncated (potentialHpHp, truncationRadiusHpHp); 
        potentialMaster.addPotential(interpotentialHpHp, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).hpType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).hpType} );
        
        // CA-HP
        if(truncationRadiusCHp > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large. " +
            		" Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialCHp = new P2SoftSphericalTruncated (potentialCHp, truncationRadiusCHp); 
        potentialMaster.addPotential(interpotentialCHp, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).cType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).hpType} );
               
        // HP-NI
        if(truncationRadiusHpN > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialHpN = new P2SoftSphericalTruncated (potentialHpN, truncationRadiusHpN); 
        potentialMaster.addPotential(interpotentialHpN, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).hpType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).nType} );
        
        // OX-HP
        if(truncationRadiusOHp > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large. " +
            		" Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialOHp = new P2SoftSphericalTruncated (potentialOHp, truncationRadiusOHp); 
        potentialMaster.addPotential(interpotentialOHp, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).oType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).hpType} );
        
        // HY-HP
        if(truncationRadiusHyHp > 0.5*box.getBoundary().getDimensions().x(0)) {
            throw new RuntimeException("Truncation radius too large.  " +
            		"Max allowed is"+0.5*box.getBoundary().getDimensions().x(0));
            }
        P2SoftSphericalTruncated interpotentialHyHp = new P2SoftSphericalTruncated (potentialHyHp, truncationRadiusHyHp); 
        potentialMaster.addPotential(interpotentialHyHp, new AtomType[]{(
        		(AtomFactoryParacetamol)species.getMoleculeFactory()).hyType, ((AtomFactoryParacetamol)species.getMoleculeFactory()).hpType} );
  
        potentialMaster.lrcMaster().setEnabled(false);
       /*
        *
        */
        
        bdry =  new BoundaryRectangularPeriodic(space, getRandom(), 1); //unit cell
        bdry.setDimensions(Space.makeVector(new double []{2*17.248, 3*12.086, 4*7.382}));
        box.setBoundary(bdry);

        coordDef = new CoordinateDefinitionParacetamol(box, primitive, basis);
        coordDef.setBasisOrthorhombic();
        coordDef.initializeCoordinates(new int []{2, 3, 4});
 
        moveHarmonic.setCoordinateDefinition(coordDef);
        
        integrator.setBox(box);
        
        
    } //end of constructor
    
   
    public static void main(String[] args) {
    	int numMolecules = 192;
        etomica.paracetamol.MCParacetamolOrthorhombicMoveHarmonic sim = new etomica.paracetamol.MCParacetamolOrthorhombicMoveHarmonic(numMolecules);
        SimulationGraphic simGraphic = new SimulationGraphic(sim, APP_NAME, 1);
        Pixel pixel = new Pixel(10);
        simGraphic.getDisplayBox(sim.box).setPixelUnit(pixel);
        //sim.getController().actionPerformed();
        ConfigurationFile configFile = new ConfigurationFile("Coord_Paracetamol_FormII_100.0_K");
        configFile.initializeCoordinates(sim.box);

        
   /*****************************************************************************/    
        
        MeterPotentialEnergy meterPE = new MeterPotentialEnergy(sim.potentialMaster);
        meterPE.setBox(sim.box);
        DisplayTextBox PEbox = new DisplayTextBox();
        DataPump PEpump = new DataPump(meterPE, PEbox);
        ArrayList dataStreamPumps = simGraphic.getController().getDataStreamPumps();
        dataStreamPumps.add(PEpump);
        
        sim.actionIntegrate.setMaxSteps(5);
        sim.integrator.addIntervalAction(PEpump);
        sim.integrator.setActionInterval(PEpump, 500);
           
        simGraphic.add(PEbox);
        
        simGraphic.makeAndDisplayFrame(APP_NAME);
        simGraphic.getDisplayBox(sim.box).setPixelUnit(new Pixel(10));
        ColorSchemeByType colorScheme = ((ColorSchemeByType)((DisplayBox)simGraphic.
        		displayList().getFirst()).getColorScheme());
        AtomTypeMolecule atomType = sim.species.getMoleculeType();
        colorScheme.setColor(atomType.getChildTypes()[0], java.awt.Color.red);
        colorScheme.setColor(atomType.getChildTypes()[1], java.awt.Color.gray);
        colorScheme.setColor(atomType.getChildTypes()[2], java.awt.Color.blue);
        colorScheme.setColor(atomType.getChildTypes()[3], java.awt.Color.white);
        colorScheme.setColor(atomType.getChildTypes()[4], java.awt.Color.white);
        
        simGraphic.getDisplayBox(sim.box).repaint();
        
    }//end of main

    public PotentialMaster potentialMaster;
    public BravaisLattice lattice;
    public BoundaryRectangularPeriodic bdry;
    public ActivityIntegrate actionIntegrate;
    public CoordinateDefinitionParacetamol coordDef;
}//end of class