package etomica.action;

import etomica.atom.Atom;
import etomica.atom.AtomLeaf;
import etomica.atom.AtomTypeLeaf;
import etomica.simulation.Simulation;
import etomica.space.ICoordinateKinetic;
import etomica.space.IVector;
import etomica.util.IRandom;


/**
 * Action that sets the velocity vector of a given atom to a randomly
 * chosen value sampled from a Boltzmann distribution.
 * 
 * @author David Kofke
 *
 */

public class AtomActionRandomizeVelocity extends AtomActionAdapter {

    /**
     * Constructs class to assign velocities according to the given temperature.
     * May be subsequently changed with setTemperature method.
     */
    public AtomActionRandomizeVelocity(double temperature, IRandom random) {
        setLabel("Randomize velocity");
        setTemperature(temperature);
        this.random = random;
    }

    /**
     * Assigns velocity to atom with components selected from a Maxwell-Boltzmann
     * distribution with temperature most recently set for the action.  If atom 
     * mass is infinite, assigns a zero velocity.
     */
    public void actionPerformed(Atom a) {
        IVector velocity = ((ICoordinateKinetic)((AtomLeaf)a).getCoord()).getVelocity();
        double mass = ((AtomTypeLeaf)a.getType()).getMass();
        if(Double.isInfinite(mass)) {
            velocity.E(0.0);
            return;
        }
        int D = velocity.D();
        for(int i=0; i<D; i++) {
            velocity.setX(i,random.nextGaussian());
        }
        velocity.TE(Math.sqrt(temperature/mass));
    }
    
    /**
     * @return Returns the Boltzmann-distribution temperature used to sample
     * the new velocity.  Default value is that from Default class.
     */
    public double getTemperature() {
        return temperature;
    }
    /**
     * @param temperature The temperature to set.
     */
    public void setTemperature(double temperature) {
        if (temperature < 0) {
            throw new IllegalArgumentException("temperature must be non-negative");
        }
        this.temperature = temperature;
    }
    
    private static final long serialVersionUID = 1L;
    private double temperature;
    protected final IRandom random;

}
