package etomica.data.meter;

import etomica.Data;
import etomica.DataInfo;
import etomica.Meter;
import etomica.Simulation;
import etomica.data.DataDoubleArray;

/**
 * Meter for recording and averaging a 1D array of type double.
 */
 
 /* History
  * Added 8/3/04
  */
public abstract class MeterArray extends Meter {
    
	/**
	 * Constructor with default nDataPerPhase = 1
	 */
    public MeterArray(DataInfo dataInfo) {
        this(null,dataInfo,1);
    }
    
    public MeterArray(Simulation sim, DataInfo dataInfo, int nData) {
        super(sim);
        data = new DataDoubleArray(dataInfo);
        data.setLength(nData);
        dataArray = data.getData();
    }
    
    public abstract double[] getDataAsArray();
    
    public final Data getData() {
        getDataAsArray();
        return data;
    }
    
    protected void setNData(int nData) {
        data.setLength(nData);
        dataArray = data.getData();
    }
    
    public int getNData() {
        return dataArray.length;
    }
    
    private final DataDoubleArray data;
    protected double[] dataArray;
}//end of MeterArray class	 
