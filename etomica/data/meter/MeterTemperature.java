package etomica.data.meter;

import etomica.EtomicaElement;
import etomica.EtomicaInfo;
import etomica.Phase;
import etomica.units.Dimension;

/**
 * Meter for measurement of the temperature based on kinetic-energy
 * equipartition
 */

/*
 * History of changes 7/03/02 (DAK) Changes to tie in with function of
 * kinetic-energy meter.
 */

public final class MeterTemperature extends DataSourceScalar implements
		EtomicaElement {

	public MeterTemperature() {
		super();
		setLabel("Temperature");
		meterKE = new MeterKineticEnergy();
	}

	public static EtomicaInfo getEtomicaInfo() {
		EtomicaInfo info = new EtomicaInfo(
				"Records temperature as given via kinetic energy");
		return info;
	}

	public double getDataAsScalar(Phase phase) {
        if (phase == null) throw new IllegalStateException("must call setPhase before using meter");
		return (2. / (double) (phase.atomCount() * phase.boundary().dimensions().D()))
				* meterKE.getDataAsScalar(phase);
	}

	public Dimension getDimension() {
		return Dimension.TEMPERATURE;
	}

	private final MeterKineticEnergy meterKE;
}