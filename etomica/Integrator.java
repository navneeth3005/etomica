package etomica;

import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;

import etomica.atom.iterator.AtomIteratorListSimple;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.units.Dimension;
import etomica.utility.NameMaker;

/**
 * Integrator is used to define the algorithm used to move the atoms around and
 * generate new configurations in one or more phases. All integrator methods,
 * such as molecular dynamics or Monte Carlo are implemented via subclasses of
 * this Integrator class. The Integrator's activities are managed via the
 * actions of the governing Controller.
 * 
 * @author David Kofke
 */

/*
 * History
 * 
 * 07/10/03 (DAK) made Agent interface public 08/25/03 (DAK) changed default for
 * doSleep to <false> 01/27/04 (DAK) initialized iieCount to inverval (instead
 * of interval+1) in run method; changed setInterval do disallow non-positive
 * interval 04/13/04 (DAK) modified reset such that doReset is called if running
 * is false
 */
public abstract class Integrator implements java.io.Serializable {

    protected final PotentialMaster potential;
    protected Phase firstPhase;
    protected Phase[] phase;
    protected boolean equilibrating = false;
    int phaseCount = 0;
    int phaseCountMax = 1;
    protected int sleepPeriod = 10;
    private final LinkedList intervalListeners = new LinkedList();
    private ListenerWrapper[] listenerWrapperArray = new ListenerWrapper[0];
    int integrationCount = 0;
    protected double temperature = Default.TEMPERATURE;
    protected boolean isothermal = false;
    private String name;
    protected MeterPotentialEnergy meterPE;
    protected double[] currentPotentialEnergy;

    public Integrator(PotentialMaster potentialMaster) {
        setName(NameMaker.makeName(this.getClass()));
        phase = new Phase[phaseCountMax];
        this.potential = potentialMaster;
        meterPE = new MeterPotentialEnergy(potentialMaster);
        if (Default.AUTO_REGISTER) {
            Simulation.getDefault().register(this);
        }
    }


    /**
     * Accessor method of the name of this phase
     * 
     * @return The given name of this phase
     */
    public final String getName() {return name;}
    /**
     * Method to set the name of this simulation element. The element's name
     * provides a convenient way to label output data that is associated with
     * it.  This method might be used, for example, to place a heading on a
     * column of data. Default name is the base class followed by the integer
     * index of this element.
     * 
     * @param name The name string to be associated with this element
     */
    public void setName(String name) {this.name = name;}

    /**
     * Overrides the Object class toString method to have it return the output of getName
     * 
     * @return The name given to the phase
     */
    public String toString() {return getName();}

    /**
     * Performs the elementary integration step, such as a molecular dynamics
     * time step, or a Monte Carlo trial.
     */
    public abstract void doStep();

    /**
     * Defines the actions taken by the integrator to reset itself, such as
     * required if a perturbation is applied to the simulated phase (e.g.,
     * addition or deletion of a molecule). Also invoked when the
     * integrator is started or initialized. This also recalculates the 
     * potential energy.
     */
    public void reset() {
        meterPE.setPhase(phase);
        currentPotentialEnergy = meterPE.getData();
        for (int i=0; i<phase.length; i++) {
            if (currentPotentialEnergy[i] == Double.POSITIVE_INFINITY) {
                if (Default.FIX_OVERLAP) {
                    System.out.println("overlap in "+phase[i]);
                }
                else {
                    throw new RuntimeException("overlap in "+phase[i]);
                }
            }
        }
    }

    /**
     * Perform any action necessary when neighbor lists are updated 
     */
    public void neighborsUpdated() {}
    
    /**
      ;* Returns a new instance of an agent of this integrator for placement in
     * the given atom in the ia (IntegratorAgent) field.
     */
    public abstract Object makeAgent(Atom a);

    /**
     * Initializes the integrator, performing the following steps: (1) deploys
     * agents in all atoms; (2) call doReset method; (3) fires an event
     * indicating to registered listeners indicating that initialization has
     * been performed (i.e. fires IntervalEvent of type field set to
     * INITIALIZE).
     */
    public void initialize() {
        deployAgents();
        reset();
    }

    //how do agents get placed in atoms made during the simulation?
    protected void deployAgents() { //puts an Agent of this integrator in each
        // atom of all phases
        AtomIteratorListSimple iterator = new AtomIteratorListSimple();
        for (int i = 0; i < phaseCount; i++) {
            Phase p = phase[i];
            iterator.setList(p.speciesMaster.atomList);
            iterator.reset();
            while (iterator.hasNext()) {//does only leaf atoms; do atom groups
                // need agents?
                Atom a = iterator.nextAtom();
                a.setIntegratorAgent(makeAgent(a));
            }
        }
    }

    /**
     * sets the temperature for this integrator
     */
    public void setTemperature(double t) {
        temperature = t;
    }

    /**
     * @return the integrator's temperature
     */
    //XXX redundant with temperature(). one of these needs to go
    public final double getTemperature() {
        return temperature;
    }

    /**
     * @return the integrator's temperature
     */
    //XXX redundant with getTemperature(). one of these needs to go
    public final double temperature() {
        return temperature;
    }

    /**
     * @return the dimenension of temperature (TEMPERATURE)
     */ 
    public final Dimension getTemperatureDimension() {
        return Dimension.TEMPERATURE;
    }

    /**
     * @return the potential energy of each phase handled by this integrator
     */
    public double[] getPotentialEnergy() {
        return currentPotentialEnergy;
    }
    
	//Other introspected properties
	public void setIsothermal(boolean b) {
		isothermal = b;
	}

	public boolean isIsothermal() {
		return isothermal;
	}

	/**
	 * @return Returns flag indicating whether integrator is in equilibration mode.
	 */
	public boolean isEquilibrating() {
		return equilibrating;
	}

	/**
	 * @param equilibrating
	 *            Sets equilibration mode of integrator.
	 */
	public void setEquilibrating(boolean equilibrating) {
		this.equilibrating = equilibrating;
	}

	/**
	 * @return true if integrator can perform integration of another phase,
	 *         false if the integrator has all the phases it was built to handle
	 */
	public boolean wantsPhase() {
		return phaseCount < phaseCountMax;
	}

	/**
	 * Performs activities needed to set up integrator to work on given phase.
	 * This method should not be called directly; instead it is invoked by the
	 * phase in its setIntegrator method.
	 * 
	 * @return true if the phase was successfully added to the integrator; false
	 *         otherwise
	 */
	//perhaps should throw an exception rather than returning a boolean "false"
	public boolean addPhase(Phase p) {
		for (int i = 0; i < phaseCount; i++) {
			if (phase[i] == p)
				return false;
		} //check that phase is not already registered
		if (!this.wantsPhase()) {
			return false;
		} //if another phase not wanted, return false
		phase[phaseCount] = p;
		phaseCount++;
		firstPhase = phase[0];
        if (Debug.ON && p.index == Debug.PHASE_INDEX) {
            Debug.setAtoms(p);
        }
		return true;
	}

	/**
	 * Performs activities needed to disconnect integrator from given phase.
	 * This method should not be called directly; instead it is invoked by the
	 * phase in its setIntegrator method
	 */
	public void removePhase(Phase p) {
		for (int i = 0; i < phaseCount; i++) {
			if (phase[i] == p) {//phase found; remove it
				phase[i] = null;
				phaseCount--;
				if (phaseCount > 0)
					phase[i] = phase[phaseCount];
				firstPhase = phase[0];
				break;
			}
		}
	}
    
    public Phase[] getPhase() {
        return phase;
    }

    
    /**
     * Arranges listeners registered with this iterator in order such that
     * those with the smallest (closer to zero) priority value are performed
     * before those with a larger priority value.  This is invoked automatically
     * whenever a listener is added or removed.  It should be invoked explicitly if
     * the priority setting of a registered listener is changed.
     */
    public synchronized void sortListeners() {
        //sort using linked list, but put into array afterwards
        //for rapid looping (avoid repeated construction of iterator)
        Collections.sort(intervalListeners);
        listenerWrapperArray = (ListenerWrapper[])intervalListeners.toArray(new ListenerWrapper[0]);
    }

    /**
     * Adds the given listener to those that receive interval events fired by
     * this integrator.  If listener has already been added to integrator, it is
     * not added again.  If listener is null, NullPointerException is thrown.
     */
	public synchronized void addIntervalListener(IntervalListener iil) {
        if(iil == null) throw new NullPointerException("Cannot add null as a listener to Integrator");
        ListenerWrapper wrapper = findWrapper(iil);
        if(wrapper == null) { //listener not already in list, so OK to add it now
            intervalListeners.add(new ListenerWrapper(iil));
            sortListeners();
        }
	}
    
    /**
     * Finds and returns the ListenerWrapper used to put the given listener in the list.
     * Returns null if listener is not in list.
     */
    private ListenerWrapper findWrapper(IntervalListener iil) {
        Iterator iterator = intervalListeners.iterator();
        while(iterator.hasNext()) {
            ListenerWrapper wrapper = (ListenerWrapper)iterator.next();
            if(wrapper.listener == iil) return wrapper;//found it
        }
        return null;//didn't find it in list      
    }

    /**
     * Removes given listener from those notified of interval events fired
     * by this integrator.  No action results if given listener is null or is not registered
     * with this integrator.
     */
	public synchronized void removeIntervalListener(IntervalListener iil) {
        ListenerWrapper wrapper = findWrapper(iil);
	    intervalListeners.remove(wrapper);
        sortListeners();
	}

	/**
	 * Notifies registered listeners that an interval has passed. Not
	 * synchronized, so unpredictable behavior if listeners are added while
	 * notification is in process (this should be rare).
	 */
	public void fireIntervalEvent(IntervalEvent iie) {
        for(int i=0; i<listenerWrapperArray.length; i++) {
			listenerWrapperArray[i].listener.intervalAction(iie);
		}
	}

	/**
	 * Registers with the given integrator all listeners currently registered
	 * with this integrator. Removes all listeners from this integrator.
	 */
	public synchronized void transferListenersTo(Integrator anotherIntegrator) {
		if (anotherIntegrator == this) return;
        synchronized(anotherIntegrator) {
            for(int i=0; i<listenerWrapperArray.length; i++) {
                anotherIntegrator.intervalListeners.add(listenerWrapperArray[i]);
            }
        }
        anotherIntegrator.sortListeners();
		intervalListeners.clear();
        sortListeners();
	}

	/**
	 * Integrator agent that holds a force vector. Used to indicate that an atom
	 * could be under the influence of a force.
	 */
	public interface Forcible {
		public Space.Vector force();
	}

	public static class IntervalEvent extends EventObject {

		// Typed constants used to indicate the type of event integrator is
		// announcing
		public static final Type START = new Type("Start"); //simulation is starting
		public static final Type INTERVAL = new Type("Interval"); //routine interval event
		public static final Type DONE = new Type("Done"); //simulation is finished
		public static final Type INITIALIZE = new Type("Initialize"); //integrator is initializing

		private final Type type;
		private int interval;

		public IntervalEvent(Integrator source, Type t) {
			super(source);
			type = t;
		}

		public IntervalEvent(Integrator source, int interval) {
			this(source, INTERVAL);
			this.interval = interval;
		}

		public int getInterval() {
			return interval;
		}

		public Type type() {
			return type;
		}

		//class used to mark the different types of interval events
		public final static class Type extends Constants.TypedConstant {
			private Type(String label) {
				super(label);
			}

			public static final Constants.TypedConstant[] choices = new Constants.TypedConstant[] {
					START, INTERVAL, DONE, INITIALIZE };

			public final Constants.TypedConstant[] choices() {
				return choices;
			}
		}
	}

	public interface IntervalListener extends java.util.EventListener {
        /**
         * Action performed by the listener when integrator fires its interval event.
         */
		public void intervalAction(IntervalEvent evt);
        /**
         * Priority assigned to the listener.  A small value will cause the 
         * listener's action to be performed earlier, before another listener having
         * a larger priority value (e.g., priority 100 action is performed before
         * one having a priority of 200).  Listeners that cause periodic boundaries
         * to be applied are given priorities in the range 100-199.  Ordering 
         * is performed only when a listener is added to the integrator, or when
         * the sortListeners method of integrator is called.
         */
        public int getPriority();
    }
    
    /**
     * This class has a natural ordering that is inconsistent with equals.
     */
    private static class ListenerWrapper implements Comparable {
        private final IntervalListener listener;
        private ListenerWrapper(IntervalListener listener) {
            this.listener = listener;
        }
        public int compareTo(Object obj) {
            int priority = listener.getPriority();
            int objPriority = ((ListenerWrapper)obj).listener.getPriority();
            //we do explicit comparison of values (rather than returning
            //their difference) to avoid potential problems with large integers.
            if(priority < objPriority) return -1;
            if(priority == objPriority) return 0;
            return +1;
         }
    }
}

