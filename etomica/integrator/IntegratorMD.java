package etomica.integrator;

import etomica.action.AtomActionRandomizeVelocity;
import etomica.atom.AtomSet;
import etomica.atom.AtomTypeLeaf;
import etomica.atom.IAtom;
import etomica.atom.IAtomKinetic;
import etomica.box.Box;
import etomica.box.BoxAtomAddedEvent;
import etomica.box.BoxEvent;
import etomica.box.BoxListener;
import etomica.data.DataSourceScalar;
import etomica.data.meter.MeterKineticEnergy;
import etomica.data.meter.MeterTemperature;
import etomica.exception.ConfigurationOverlapException;
import etomica.potential.PotentialMaster;
import etomica.space.IVector;
import etomica.units.Dimension;
import etomica.units.Time;
import etomica.util.Debug;
import etomica.util.EnumeratedType;
import etomica.util.IRandom;
/**
 * Superclass of all molecular-dynamics integrators.
 * Extends the Integrator class by adding methods that 
 * set the time step.
 */

public abstract class IntegratorMD extends IntegratorBox implements BoxListener {

    public IntegratorMD(PotentialMaster potentialMaster, IRandom random, 
            double timeStep, double temperature) {
        super(potentialMaster,temperature);
        this.random = random;
        setTimeStep(timeStep);
        thermostat = ThermostatType.ANDERSEN;
        setThermostatInterval(100);
        meterKE = new MeterKineticEnergy();
        atomActionRandomizeVelocity = new AtomActionRandomizeVelocity(temperature, random);
        meterTemperature = new MeterTemperature();
        momentum = potentialMaster.getSpace().makeVector();
    }

    /**
     * Sets integration time step.
     * Updates zero-point counters used internally to manage the elapsedTime method
     */
    public void setTimeStep(double t) {
        timeStep = t;
    }
    public final double getTimeStep() {return timeStep;}
    public Dimension getTimeStepDimension() {return Time.DIMENSION;}
    
    public void setBox(Box p) {
        if (box != null) {
            box.getEventManager().removeListener(this);
        }
        super.setBox(p);
        meterTemperature.setBox(p);
        meterKE.setBox(p);
        box.getEventManager().addListener(this);
    }

    protected void setup() {
        try {
            super.setup();
        }
        catch (ConfigurationOverlapException e) {}
        currentTime = 0;
        thermostatCount = 1;
        meterKE.setBox(box);
        doThermostat();
    }
    
    /**
     * reset the integrator's kinetic energy tracker
     */
    public void reset() throws ConfigurationOverlapException {
        meterKE.setBox(box);
        currentKineticEnergy = meterKE.getDataAsScalar();
        super.reset();
    }
    
    public void doStepInternal() {
        currentTime += timeStep;
    }
    
    public double getCurrentTime() {
        return currentTime;
    }

    /**
     * @return the current kinetic energy as tracked by the integrator
     */
    public double getKineticEnergy() {
        return currentKineticEnergy;
    }

    public void setIsothermal(boolean b) {
        super.setIsothermal(b);
        if (initialized && isothermal) {
            // trigger immediate thermostat
            thermostatCount = 1;
            doThermostat();
        }
    }
    
    public void setTemperature(double t) {
        if (t == temperature) return;
        super.setTemperature(t);
        if (initialized) {
            atomActionRandomizeVelocity.setTemperature(temperature);
            // trigger immediate thermostat
            thermostatCount = 1;
            doThermostat();
        }
    }
    
    public static class ThermostatType extends EnumeratedType {
        protected ThermostatType(String label) {super(label);}       
        private static final long serialVersionUID = 1L;
        public static final ThermostatType VELOCITY_SCALING = new ThermostatType("Velocity Scaling");
        public static final ThermostatType ANDERSEN = new ThermostatType("Anderson");
        public static final ThermostatType ANDERSEN_SINGLE = new ThermostatType("Andersen Single");
        //public static final ThermostatType NOSE_HOOVER;
        public static ThermostatType[] choices() {
            return new ThermostatType[] {VELOCITY_SCALING,ANDERSEN,ANDERSEN_SINGLE};
        }

        /**
         * Required to guarantee singleton when deserializing.
         * @return the singleton INSTANCE
         */
        private Object readResolve() {
            ThermostatType[] choices = choices();
            for (int i=0; i<choices.length; i++) {
                if (this.toString().equals(choices[i].toString())) {
                    return choices[i];
                }
            }
            throw new RuntimeException("unknown thermostat type: "+this);
        }
    }
    
    /**
     * Sets the type of thermostat used by the integrator.
     * @param aThermostat the desired thermostat
     */
    public void setThermostat(ThermostatType aThermostat) {
        thermostat = aThermostat;
    }

    public ThermostatType getThermostat() {
        return thermostat;
    }

    /**
     * Sets the number of integrator intervals between thermostat
     * actions (for velocity scaling and Andersen thermostat).
     * @param interval number of integrator intervals between thermostat
     * activity
     */
    public void setThermostatInterval(int interval) {
        if (interval < 1) throw new IllegalArgumentException("Thermostat Interval must be positive");
        thermostatInterval = interval;
        thermostatCount = interval;
    }

    /**
     * thermostat implementation.  This method takes whatever action is appropriate
     * for the integrator's thermostat and updates the state of the integrator.
     */
    public void doThermostat() {
        if (--thermostatCount == 0) {
            thermostatCount = thermostatInterval;
            if (thermostat == ThermostatType.ANDERSEN || !initialized) {
                // if initializing the system always randomize the velocity
                randomizeMomenta();
                currentKineticEnergy = meterKE.getDataAsScalar();
            }
            if (thermostat == ThermostatType.VELOCITY_SCALING || !isothermal) {
                scaleMomenta();
                currentKineticEnergy = meterKE.getDataAsScalar();
            }
            else if (thermostat == ThermostatType.ANDERSEN_SINGLE) {
                if (initialized) {
                    AtomSet atomList = box.getLeafList();
                    int index = random.nextInt(atomList.getAtomCount());
                    IAtomKinetic a = (IAtomKinetic)atomList.getAtom(index);
                    double m = ((AtomTypeLeaf)a.getType()).getMass();
                    currentKineticEnergy -= 0.5*m*a.getVelocity().squared();
                    randomizeMomentum(a);
                    currentKineticEnergy += 0.5*m*a.getVelocity().squared();
                }
            }
            // ANDERSEN was handled at the start
            else if (thermostat != ThermostatType.ANDERSEN) {
                throw new RuntimeException("Unknown thermostat: "+thermostat);
            }
        }
    }
    
    /**
     * randomizes the velocities for the given box using velocities
     * chosen form a Maxwell-Boltzmann distribution as in the Andersen 
     * thermostat.  The state of the integrator needs to be updated 
     * after calling this method.
     * @param aBox box whose atomic momenta are to be randomized
     */
    protected void randomizeMomenta() {
        atomActionRandomizeVelocity.setTemperature(temperature);
        AtomSet leafList = box.getLeafList();
        int nLeaf = leafList.getAtomCount();
        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            atomActionRandomizeVelocity.actionPerformed(leafList.getAtom(iLeaf));
        }
    }
    
    /**
     * randomizes the velocity of an atom in the given box using velocities
     * chosen form a Maxwell-Boltzmann distribution as in the Andersen 
     * thermostat.  The state of the integrator needs to be updated 
     * after calling this method.
     * @param atom whose momenta is be randomized
     */
    protected void randomizeMomentum(IAtomKinetic atom) {
        atomActionRandomizeVelocity.setTemperature(temperature);
        atomActionRandomizeVelocity.actionPerformed(atom);
    }
    
    /**
     * Crude method to enforce constant-temperature constraint
     * Scales momenta of all atoms by a constant factor so that 
     * box adheres to setpoint temperature.  The state of the 
     * integrator may need to be updated after calling this method.
     * @return the factor velocities were scaled by 
     */
    protected double scaleMomenta() {
        momentum.E(0);
        AtomSet leafList = box.getLeafList();
        int nLeaf = leafList.getAtomCount();
        if (nLeaf > 1) {
            for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
                IAtom a = leafList.getAtom(iLeaf);
                double mass = ((AtomTypeLeaf)a.getType()).getMass();
                if (mass != Double.POSITIVE_INFINITY) {
                    momentum.PEa1Tv1(mass,((IAtomKinetic)a).getVelocity());
                }
            }
            momentum.TE(1.0/nLeaf);
            //set net momentum to 0
            for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
                IAtomKinetic a = (IAtomKinetic)leafList.getAtom(iLeaf);
                double rm = ((AtomTypeLeaf)a.getType()).rm();
                if (rm != 0) {
                    a.getVelocity().PEa1Tv1(-rm,momentum);
                }
            }
            if (Debug.ON) {
                momentum.E(0);
                for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
                    IAtomKinetic a = (IAtomKinetic)leafList.getAtom(iLeaf);
                    double mass = ((AtomTypeLeaf)a.getType()).getMass();
                    if (mass != Double.POSITIVE_INFINITY) {
                        momentum.PEa1Tv1(mass,a.getVelocity());
                    }
                }
                momentum.TE(1.0/nLeaf);
                if (Math.sqrt(momentum.squared()) > 1.e-10) {
                    System.out.println("Net momentum per leaf atom is "+momentum+" but I expected it to be 0");
                }
            }
            momentum.E(0);
        }
        
        // calculate current kinetic temperature
        double t = meterTemperature.getDataAsScalar();
        if (t == temperature) return 1.0;
        double s = Math.sqrt(temperature / t);
        double scale = s;
        if (t == 0) {
            randomizeMomenta();
            t = meterTemperature.getDataAsScalar();
            s = Math.sqrt(temperature / t);
        }
        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            IAtomKinetic a = (IAtomKinetic)leafList.getAtom(iLeaf);
            a.getVelocity().TE(s); //scale momentum
        }
        return scale;
    }

    /**
     * returns the temperature meter used for velocity rescaling.
     */
    public DataSourceScalar getMeterTemperature() {
        return meterTemperature;
    }
    /**
     * Sets the temperature meter used to calculate temperature for
     * velocity rescaling.  You only need to call this method if 
     * the standard MeterTemperature won't work.
     */
    public void setMeterTemperature(MeterTemperature meter) {
        meterTemperature = meter;
        meter.setBox(box);
    }
    
    public void actionPerformed(BoxEvent event) {
        if (event instanceof BoxAtomAddedEvent) {
            IAtom atom = ((BoxAtomAddedEvent)event).getAtom();
            if (atom instanceof IAtomKinetic) {
                randomizeMomentum((IAtomKinetic)atom);
            }
        }
    }

    private static final long serialVersionUID = 2L;
    /**
     * Elementary time step for the MD simulation
     */
    protected final IRandom random;
    protected double timeStep;
    protected double currentKineticEnergy;
    protected ThermostatType thermostat;
    private int thermostatCount, thermostatInterval;
    protected MeterKineticEnergy meterKE;
    private AtomActionRandomizeVelocity atomActionRandomizeVelocity;
    private MeterTemperature meterTemperature;
    private final IVector momentum;
    protected double currentTime;
}

