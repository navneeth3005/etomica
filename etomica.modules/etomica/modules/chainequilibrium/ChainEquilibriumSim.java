package etomica.modules.chainequilibrium;

import etomica.action.activity.ActivityIntegrate;
import etomica.api.IAtomLeaf;
import etomica.api.IAtomList;
import etomica.api.IAtomType;
import etomica.api.IAtomTypeSphere;
import etomica.api.IBox;
import etomica.api.IController;
import etomica.api.IMolecule;
import etomica.api.IPotentialMaster;
import etomica.atom.AtomLeafAgentManager;
import etomica.atom.AtomLeafAgentManager.AgentSource;
import etomica.box.Box;
import etomica.data.meter.MeterTemperature;
import etomica.integrator.IntegratorHard;
import etomica.integrator.IntegratorMD.ThermostatType;
import etomica.lattice.LatticeCubicFcc;
import etomica.lattice.LatticeOrthorhombicHexagonal;
import etomica.nbr.list.PotentialMasterList;
import etomica.potential.P2HardSphere;
import etomica.simulation.Simulation;
import etomica.space.BoundaryRectangularPeriodic;
import etomica.space.ISpace;
import etomica.species.SpeciesSpheresMono;
import etomica.units.Kelvin;

public class ChainEquilibriumSim extends Simulation implements AgentSource {

	public IController controller1;
	public IntegratorHard integratorHard;
	public java.awt.Component display;
	public IBox box;
	public MeterTemperature thermometer;
	public SpeciesSpheresMono speciesA;
	public SpeciesSpheresMono speciesB;
//    public SpeciesSpheresMono speciesC;
	public P2HardSphere p2AA, p2BB; //, p2CC, p2BC;
	public P2SquareWellBonded ABbonded; //, ACbonded;
    public ActivityIntegrate activityIntegrate;
    public AtomLeafAgentManager agentManager = null;
    public final IPotentialMaster potentialMaster;
    public final ConfigurationLatticeRandom config;
    public int nCrossLinkersAcid;
    public int nDiol, nDiAcid;
    public int nMonoOl, nMonoAcid;

    public int getNMonoOl() {
        return nMonoOl;
    }

    public void setNMonoOl(int monoOl) {
        nMonoOl = monoOl;
        box.setNMolecules(speciesA, nMonoOl+nDiol);
    }

    public int getNMonoAcid() {
        return nMonoAcid;
    }

    public void setNMonoAcid(int monoAcid) {
        nMonoAcid = monoAcid;
        box.setNMolecules(speciesB, nMonoAcid+nDiAcid+nCrossLinkersAcid);
    }

    public int getNDiol() {
        return nDiol;
    }

    public void setNDiol(int diol) {
        nDiol = diol;
        box.setNMolecules(speciesA, nMonoOl+nDiol);
    }

    public int getNDiAcid() {
        return nDiAcid;
    }

    public void setNDiAcid(int diAcid) {
        nDiAcid = diAcid;
        box.setNMolecules(speciesB, nMonoAcid+nDiAcid+nCrossLinkersAcid);
    }

    public int getNCrossLinkersAcid() {
        return nCrossLinkersAcid;
    }

    public void setNCrossLinkersAcid(int crossLinkersAcid) {
        nCrossLinkersAcid = crossLinkersAcid;
        box.setNMolecules(speciesB, nMonoAcid+nDiAcid+nCrossLinkersAcid);
    }

    public ChainEquilibriumSim(ISpace space) {
        super(space);
        potentialMaster = new PotentialMasterList(this, 3, space);
        ((PotentialMasterList)potentialMaster).setCellRange(1);

        controller1 = getController();

        double diameter = 1.0;
        double lambda = 2.0;

        integratorHard = new IntegratorHard(this, potentialMaster, space);
        integratorHard.setIsothermal(true);
        integratorHard.setTemperature(Kelvin.UNIT.toSim(300));
        integratorHard.setTimeStep(0.002);
        integratorHard.setThermostat(ThermostatType.ANDERSEN_SINGLE);
        integratorHard.setThermostatInterval(1);

        box = new Box(new BoundaryRectangularPeriodic(space, space.D() == 2 ? 60 : 20), space);
        addBox(box);
        integratorHard.setBox(box);
        integratorHard.addIntervalAction(((PotentialMasterList)potentialMaster).getNeighborManager(box));
        
        speciesA = new SpeciesSpheresMono(this, space);
        speciesB = new SpeciesSpheresMono(this, space);
        getSpeciesManager().addSpecies(speciesA);
        getSpeciesManager().addSpecies(speciesB);
        ((IAtomTypeSphere)speciesA.getLeafType()).setDiameter(diameter);
        ((IAtomTypeSphere)speciesB.getLeafType()).setDiameter(diameter);
        box.setNMolecules(speciesA, 50);
        nDiol = 50;
        box.setNMolecules(speciesB, 100);
        nDiAcid = 100;

        config = new ConfigurationLatticeRandom(space.D() == 2 ? new LatticeOrthorhombicHexagonal(space) : new LatticeCubicFcc(space), space, random);
        config.initializeCoordinates(box);

        agentManager = new AtomLeafAgentManager(this,box);

		//potentials
        p2AA = new P2HardSphere(space, diameter, true);
		ABbonded = new P2SquareWellBonded(space, agentManager, diameter / lambda, lambda, 0.0);
        p2BB = new P2HardSphere(space, diameter, true);

		potentialMaster.addPotential(p2AA,
		        new IAtomType[] { speciesA.getLeafType(), speciesA.getLeafType() });
		potentialMaster.addPotential(ABbonded,
		        new IAtomType[] { speciesA.getLeafType(), speciesB.getLeafType() });
		
		potentialMaster.addPotential(p2BB,
		        new IAtomType[] { speciesB.getLeafType(), speciesB.getLeafType() });

		// **** Setting Up the thermometer Meter *****
		
		thermometer = new MeterTemperature(box, space.D());

		activityIntegrate = new ActivityIntegrate(integratorHard, 1, true);
		getController().addAction(activityIntegrate);
	}
    
    public void resetBonds() {
        IAtomList atoms = box.getLeafList();
        for (int i=0; i<atoms.getAtomCount(); i++) {
            IAtomLeaf a = atoms.getAtom(i);
            agentManager.setAgent(a, makeAgent(a));
        }
    }

    public Class getAgentClass() {
        return IAtomLeaf[].class;
    }
    
	/**
	 * Implementation of AtomAgentManager.AgentSource interface. Agent
     * is used to hold bonding partners.
	 */
	public Object makeAgent(IAtomLeaf a) {
	    IMolecule m = a.getParentGroup();
	    int nBonds = 2;
	    if (m.getType() == speciesA) {
	        if (m.getIndex() < nMonoOl) {
	            nBonds = 1;
	        }
	    }
	    else {
	        if (m.getIndex() < nMonoAcid) {
	            nBonds = 1;
	        }
	        else if (m.getIndex() >= nMonoAcid+nDiAcid) {
	            nBonds = 3;
	        }
	    }
		return new IAtomLeaf[nBonds];
	}
    
    public void releaseAgent(Object agent, IAtomLeaf atom) {}
    
    public AtomLeafAgentManager getAgentManager() {
    	return agentManager;
    }
}
