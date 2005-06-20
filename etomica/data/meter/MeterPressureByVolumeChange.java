package etomica.data.meter;
import etomica.DataInfo;
import etomica.DataSource;
import etomica.EtomicaElement;
import etomica.EtomicaInfo;
import etomica.IteratorDirective;
import etomica.PotentialMaster;
import etomica.Simulation;
import etomica.Space;
import etomica.action.PhaseInflate;
import etomica.data.DataSourceUniform;
import etomica.potential.PotentialCalculationEnergySum;
import etomica.space.Vector;
import etomica.units.Dimension;

/**
 * Evaluates the pressure by examining the change in energy accompanying
 * small changes in volume.
 */
public class MeterPressureByVolumeChange extends MeterArray implements DataSourceFunction, EtomicaElement {
    
    public MeterPressureByVolumeChange(PotentialMaster potentialMaster, boolean[] dimensions) {
        this(null,potentialMaster,dimensions);
    }
    
    public MeterPressureByVolumeChange(Simulation sim, boolean[] dimensions) {
        this(sim,sim.potentialMaster,dimensions);
    }
    
    private MeterPressureByVolumeChange(Simulation sim, PotentialMaster potentialMaster, boolean[] dimensions) {
        super(sim,new DataInfo("Pressure by Volume Change",Dimension.pressure(sim.space.D)),10);
        spaceD = dimensions.length;
        potential = potentialMaster;
        setX(-0.001, 0.001, getNData());
        inflateDimensions = new boolean[spaceD];
        setInflateDimensions(dimensions);
        iteratorDirective = new IteratorDirective();
        inflater = new PhaseInflate(potentialMaster.getSpace());
    }
    
    public static EtomicaInfo getEtomicaInfo() {
        EtomicaInfo info = new EtomicaInfo("Pressure measured by trial volume perturbations");
        return info;
    }

    /**
     * For anisotropic volume change, indicates dimension in which volume is perturbed.
     */
    public final void setInflateDimensions(boolean[] directions) {
        if(directions.length != spaceD) {
            throw new IllegalArgumentException();
        }
        nDimension = 0;
        for(int i=0; i<directions.length; i++) {
            inflateDimensions[i] = directions[i];
            if(inflateDimensions[i]) nDimension++;
            
        }
    }
    /**
     * Accessor method for setInflateDimension.
     */
    public boolean[] getInflateDimensions() {return inflateDimensions;}
    
    public void setX(double min, double max, int n) {
        xDataSource = new DataSourceUniform(n, min, max);
        //x is scaling in volume if isotropic, but is linear scaling if not isotropic
        double dx = (max-min)/n;
        final double[] x = xDataSource.getData();
        for(int i=0; i<x.length; i++) { //disallow x = 0
            if(x[i] == 0.0) x[i] = 0.1*dx;
        }
        scale = new Vector[x.length];
        
        double mult = 1.0/nDimension;
        for(int i=0; i<x.length; i++) {
            scale[i] = Space.makeVector(spaceD);
            scale[i].E(Math.exp(mult*xDataSource.getData()[i]));
            for(int j=0; j<spaceD; j++) {
                if(!inflateDimensions[j]) scale[i].setX(j,1.0);
            }
        }
    }
    
    /**
     * @return Returns the temperature.
     */
    public double getTemperature() {
        return temperature;
    }
    
    /**
     * @param temperature The temperature to set.
     */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public DataSource getXDataSource() {
        return xDataSource;
    }
    
    public double[] getDataAsArray() {
        if (phase == null) throw new IllegalStateException("must call setPhase before using meter");
        inflater.setPhase(phase);
        energy.zeroSum();
        potential.calculate(phase, iteratorDirective, energy);
        double uOld = energy.getSum();
        for(int i=0; i<dataArray.length; i++) {
            inflater.setVectorScale(scale[i]);
            inflater.actionPerformed();
            energy.zeroSum();
            potential.calculate(phase, iteratorDirective, energy);
            double uNew = energy.getSum();
            dataArray[i] = Math.exp(-(uNew-uOld)/temperature
                              + phase.moleculeCount()*xDataSource.getData()[i]);

            //TODO shouldn't this be done outside the loop?
            inflater.undo();
            //System.out.println( "  uNew " + uNew +" uOld " +uOld +" x " + x[i] +" scale" + scale[i]+ " y " +y[i] );
        }
        return dataArray;
    }
    
    private final PhaseInflate inflater;
    Vector[] scale;
    boolean[] inflateDimensions;
    private IteratorDirective iteratorDirective;
    private final PotentialCalculationEnergySum energy = new PotentialCalculationEnergySum();
    private final PotentialMaster potential;
    private int nDimension;
    private int spaceD;
    private DataSourceUniform xDataSource;
    private double temperature = Double.NaN;
    
 /*     public static void main(String[] args) {
        java.awt.Frame f = new java.awt.Frame();   //create a window
        f.setSize(600,350);
        
        Default.ATOM_SIZE =1.2;
        
        Simulation.instance = new Simulation(new Space2DCell());
        Phase phase1 = new Phase();
        MCMoveAtom mcmove= new MCMoveAtom();
        
        DisplayPlot plot1 = new DisplayPlot();

        MeterPressureByVolumeChange meterp = new MeterPressureByVolumeChange();
        meterp.setIsotropic(true);
        
        IntegratorMC integratorMC1 = new IntegratorMC();
	    integratorMC1.add(mcmove);

	    meterp.setPhase(phase1);
	    meterp.setActive(true);
        plot1.setDataSources(meterp);
	    plot1.setWhichValue(MeterAbstract.AVERAGE);
	    SpeciesSpheres speciesSphere1 = new SpeciesSpheres();
	    speciesSphere1.setNMolecules(200);
	    PotentialLJ potentialLJ = new PotentialLJ();
	    P2SimpleWrapper potential = new P2SimpleWrapper(potentialLJ);
	    
	    Controller controller1 = new Controller();
	  
	    DisplayPhase displayPhase1 = new DisplayPhase();
	    MeterEnergy meterEnergy1 = new MeterEnergy();
	    
	    DisplayBox box1 = new DisplayBox();
	    
	    box1.setMeter(meterEnergy1);
	    box1.setWhichValue(MeterAbstract.AVERAGE);
		displayPhase1.setPhase(phase1);
		DeviceSlider temperatureSlider = new DeviceSlider(integratorMC1, "temperature");
	    temperatureSlider.setUnit(new Unit(Kelvin.UNIT));
	    temperatureSlider.setMinimum(50);
	    temperatureSlider.setMaximum(500);
	    
		Simulation.instance.elementCoordinator.go(); 
		
        integratorMC1.setTemperature(Kelvin.UNIT.toSim(10));
        
		Simulation.instance.setBackground(java.awt.Color.blue);		                                    
        f.add(Simulation.instance);         //access the static instance of the simulation to
                                            //display the graphical components
        f.pack();
        f.show();
        f.addWindowListener(new java.awt.event.WindowAdapter() {   //anonymous class to handle window closing
            public void windowClosing(java.awt.event.WindowEvent e) {System.exit(0);}
        });
    }//end of main
*/
}