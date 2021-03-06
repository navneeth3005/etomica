/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.models.nitrogen;

import etomica.api.IBox;
import etomica.api.IPotentialMaster;
import etomica.api.ISimulation;
import etomica.api.ISpecies;
import etomica.box.Box;
import etomica.data.DataTag;
import etomica.data.IData;
import etomica.data.IEtomicaDataInfo;
import etomica.data.IEtomicaDataSource;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.data.meter.MeterPotentialEnergyFromIntegrator;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataDoubleArray.DataInfoDoubleArray;
import etomica.integrator.IntegratorMC;
import etomica.space.ISpace;
import etomica.units.Null;

/**
 * Meter used for overlap sampling in the target-sampled system.  The meter
 * measures the ratio of the Boltzmann factors for the reference (no rotational energy)
 *  and target potentials.
 * 
 * @author Tai Boon Tan
 */
public class MeterBoltzmannRotPerturb implements IEtomicaDataSource {
    
    public MeterBoltzmannRotPerturb(IntegratorMC integrator, IPotentialMaster potentialMaster, ISpecies species, 
    		ISpace space, ISimulation sim, CoordinateDefinitionNitrogen coordinateDef) {
        this.primaryCoordDef = coordinateDef;
        
        IBox realBox = coordinateDef.getBox();
        secondaryBox = new Box(space);
        sim.addBox(secondaryBox);
       
        secondaryBox.setNMolecules(species, realBox.getNMolecules(species));
        secondaryBox.setBoundary(realBox.getBoundary());
     
        secondaryCoordDef = new CoordinateDefinitionNitrogen(sim, secondaryBox, coordinateDef.getPrimitive(), coordinateDef.getBasis(), space);
        secondaryCoordDef.setIsBeta();
        secondaryCoordDef.setOrientationVectorBeta(space);
        secondaryCoordDef.initializeCoordinates(new int[]{1,1,1});
        
        meterPotentialMeasured = new MeterPotentialEnergy(potentialMaster);
        meterPotentialSampled = new MeterPotentialEnergyFromIntegrator(integrator);
      
        data = new DataDoubleArray(2);
        dataInfo = new DataInfoDoubleArray("Scaled Energies", Null.DIMENSION, new int[]{2});
        data.getData()[0] = 1.0;
        tag = new DataTag();
    }

    public IData getData() {
		double[] sampledCoord = primaryCoordDef.calcU(primaryCoordDef.getBox().getMoleculeList());
		double[] transCoord = new double[sampledCoord.length];
				
		for(int i=0; i<sampledCoord.length; i++){
			if(i>0 && (i%5==3 || i%5==4)){
				transCoord[i] = 0.0;
				
			} else{
				transCoord[i] = sampledCoord[i];
				
			}
		}
		
		secondaryCoordDef.setToU(secondaryBox.getMoleculeList(), transCoord);
		meterPotentialMeasured.setBox(secondaryBox);
		
		double sampledEnergy = meterPotentialSampled.getDataAsScalar();
		double measuredEnergy = meterPotentialMeasured.getDataAsScalar();
		
    	data.getData()[1] = Math.exp(-(measuredEnergy-sampledEnergy)/meterPotentialSampled.getIntegrator().getTemperature()); 
        return data;
    }
    
    public IEtomicaDataInfo getDataInfo() {
        return dataInfo;
    }

    public DataTag getTag() {
        return tag;
    }

	private static final long serialVersionUID = 1L;
	protected final MeterPotentialEnergy meterPotentialMeasured;
	protected final MeterPotentialEnergyFromIntegrator meterPotentialSampled;
    protected final IBox secondaryBox;
    protected final DataDoubleArray data;
    protected final DataInfoDoubleArray dataInfo;
    protected final DataTag tag;
    protected CoordinateDefinitionNitrogen primaryCoordDef, secondaryCoordDef;
    
}
