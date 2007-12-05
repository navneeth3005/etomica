package etomica.data;

import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataFunction;
import etomica.data.types.DataDoubleArray.DataInfoDoubleArray;
import etomica.data.types.DataFunction.DataInfoFunction;
import etomica.units.Dimension;

public class DataSourcePoints implements DataSource, DataSourceIndependent {

    private static final long serialVersionUID = 1L;
    private DataTag tag;
	private IDataInfo depDataInfo;
	private DataInfoDoubleArray indDataInfo;
    private DataDoubleArray independentData = null;
    private DataFunction dependentData = null;
    private String label;
    private Dimension xDimension;
    private Dimension yDimension;

    public DataSourcePoints(String label, Dimension xDimension, Dimension yDimension) {

    	this.label = label;
    	this.xDimension = xDimension;
    	this.yDimension = yDimension;

	    independentData = new DataDoubleArray(new int[] {0}, new double[0]);
	    dependentData = new DataFunction(new int[]{0}, new double[0]);

        tag = new DataTag();
        
		indDataInfo = new DataInfoDoubleArray(label, xDimension, new int[] {0});
		indDataInfo.addTag(tag);

		depDataInfo = new DataInfoFunction(label, yDimension, this);
		depDataInfo.addTag(tag);
		
    }

    public IDataInfo getDataInfo() {
        return depDataInfo;
    }

    public DataTag getTag() {
        return tag;
    }

    public Data getData() {
        return dependentData;
    }

    public void update(double[] xpts, double[] ypts) {

    	if(xpts.length == ypts.length) {

    		indDataInfo = null;
    		depDataInfo = null;

    		indDataInfo = new DataInfoDoubleArray(label, xDimension, new int[] {xpts.length});
    		indDataInfo.addTag(tag);

    		depDataInfo = new DataInfoFunction(label, yDimension, this);
    		depDataInfo.addTag(tag);

    	    independentData = null;
    	    dependentData = null;

    	    independentData = new DataDoubleArray(new int[] {xpts.length}, xpts);
    	    dependentData = new DataFunction(new int[]{ypts.length}, ypts);
    	}
    	else {
            throw new IllegalArgumentException("DataSourcePoints.update() : X and Y data dimensions are NOT the same length!");
    	}

    }

	public DataDoubleArray getIndependentData(int i) {
		return independentData;
	}

    public DataInfoDoubleArray getIndependentDataInfo(int i) {
        return indDataInfo;
    }

    public int getIndependentArrayDimension() {
    	return independentData.getArrayDimension();
    }

}
