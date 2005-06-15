package etomica.data;

import etomica.Data;
import etomica.DataInfo;


/**
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 *
 * @author David Kofke
 *
 */

/*
 * History
 * Created on Jun 15, 2005 by kofke
 */
public class DataDoubleArray extends Data implements DataArithmetic {

    public DataDoubleArray(DataInfo dataInfo) {
        super(dataInfo);
    }

    public void E(DataArithmetic y) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see etomica.data.DataArithmetic#PE(etomica.data.DataArithmetic)
     */
    public void PE(DataArithmetic y) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see etomica.data.DataArithmetic#ME(etomica.data.DataArithmetic)
     */
    public void ME(DataArithmetic y) {

    }

    /* (non-Javadoc)
     * @see etomica.data.DataArithmetic#E(double)
     */
    public void E(double y) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see etomica.data.DataArithmetic#PE(double)
     */
    public void PE(double y) {
        // TODO Auto-generated method stub

    }

    public void setLength(int n) {
        x = new double[n];
    }
    
    public double[] getData() {
        return x;
    }
    
    private double[] x;
}
