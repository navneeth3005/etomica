package etomica.modules.pistoncylinder;

import etomica.Phase;
import etomica.data.meter.MeterPressureHard;
import etomica.integrator.IntegratorHard;
import etomica.potential.P1HardMovingBoundary;

/**
 * data source front for virial sum from P1HardMovingBoundary
 * returns pressure exerted on the wall by atoms
 */
public class DataSourceWallPressure extends MeterPressureHard {

    public DataSourceWallPressure(P1HardMovingBoundary potential, IntegratorHard integrator) {
        super(integrator);
        wallPotential = potential;
    }
    
    /**
     * Implementation of CollisionListener interface
     * Adds collision virial (from potential) to accumulator
     */
    public void collisionAction(IntegratorHard.Agent agent) {
        if (agent.collisionPotential == wallPotential) {
            virialSum += wallPotential.lastWallVirial();
        }
    }
    
    public double getDataAsScalar(Phase p) {
        double elapsedTime = timer.getData()[0];
        double value = virialSum / elapsedTime;
        timer.reset();
        virialSum = 0;
        return value;
    }
    
    private P1HardMovingBoundary wallPotential;
}
