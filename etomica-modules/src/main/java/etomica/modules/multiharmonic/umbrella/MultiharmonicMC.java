/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.modules.multiharmonic.umbrella;

import etomica.action.activity.ActivityIntegrate;
import etomica.api.IAtomType;
import etomica.api.IBox;
import etomica.box.Box;
import etomica.data.AccumulatorRatioAverageCovariance;
import etomica.data.DataPump;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.integrator.IntegratorMC;
import etomica.listener.IntegratorListenerAction;
import etomica.modules.multiharmonic.MCMoveMultiHarmonic;
import etomica.potential.P1Harmonic;
import etomica.potential.PotentialMaster;
import etomica.potential.PotentialMasterMonatomic;
import etomica.simulation.Simulation;
import etomica.space.BoundaryRectangularNonperiodic;
import etomica.space1d.Space1D;
import etomica.space1d.Vector1D;
import etomica.species.SpeciesSpheresMono;


/**
 * MC version of multi-harmonic simulation.  This version runs much faster.
 *
 * @author Andrew Schultz
 */
public class MultiharmonicMC extends Simulation {

    public MultiharmonicMC() {
        super(Space1D.getInstance());
        PotentialMaster potentialMasterA = new PotentialMasterMonatomic(this);
        PotentialMaster potentialMasterB = new PotentialMasterMonatomic(this);
        species = new SpeciesSpheresMono(this, space);
        addSpecies(species);

        box = new Box(new BoundaryRectangularNonperiodic(space), space);
        addBox(box);
        box.getBoundary().setBoxSize(new Vector1D(3.0));
        box.setNMolecules(species, 10);

        integrator = new IntegratorMC(this, potentialMasterA);
        integrator.setBox(box);
        integrator.setTemperature(1.0);
        potentialA = new P1Harmonic(space);
        moveA = new MCMoveMultiHarmonic(potentialA, random);
        integrator.getMoveManager().addMCMove(moveA);
        potentialMasterA.addPotential(potentialA, new IAtomType[] {species.getLeafType()});
        
        potentialB = new P1Harmonic(space);
        moveB = new MCMoveMultiHarmonic(potentialB, random);
        integrator.getMoveManager().addMCMove(moveB);
        potentialMasterB.addPotential(potentialB, new IAtomType[] {species.getLeafType()});

        MeterPotentialEnergy meterPEAinA = new MeterPotentialEnergy(potentialMasterA);
        meterPEAinA.setBox(box);
        MeterPotentialEnergy meterPEBinA = new MeterPotentialEnergy(potentialMasterB);
        meterPEBinA.setBox(box);
        meterUmbrella = new MeterUmbrella(meterPEAinA, meterPEBinA, 1.0);

        accumulator = new AccumulatorRatioAverageCovariance(1);
        dataPumpA = new DataPump(meterUmbrella, accumulator);
        integrator.getEventManager().addListener(new IntegratorListenerAction(dataPumpA));

        
        activityIntegrate = new ActivityIntegrate(integrator, 1, false);
        getController().addAction(activityIntegrate);
    }

    private static final long serialVersionUID = 1L;
    protected final SpeciesSpheresMono species;
    protected final IBox box;
    protected final P1Harmonic potentialA, potentialB;
    protected final IntegratorMC integrator;
    protected final MCMoveMultiHarmonic moveA, moveB;
    protected final ActivityIntegrate activityIntegrate;
    protected final MeterUmbrella meterUmbrella;
    protected final AccumulatorRatioAverageCovariance accumulator;
    protected final DataPump dataPumpA;
}
