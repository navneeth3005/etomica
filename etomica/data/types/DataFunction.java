package etomica.data.types;

import java.util.Arrays;

import etomica.Data;
import etomica.DataInfo;
import etomica.utility.Function;

/**
 * Data object that wraps two arrays of double, such that one is considered
 * to have a dependence on the other.  The dependence is not enforced
 * by this data structure, rather this structure is used to encapsulate a
 * set of x-t value pairs, where the function x(t) is applied by some other
 * means.
 *
 * @author David Kofke and Andrew Schultz
 *  
 */

/*
 * History Created on Jun 15, 2005 by kofke
 */
public class DataFunction extends Data implements DataArithmetic {

    /**
     * Function is x(t).
     * @param xDataInfo  dataInfo for the dependent variable
     * @param tDataInfo  dataInfo for the independent variable
     */
    public DataFunction(DataInfo xDataInfo, DataInfo tDataInfo) {
        super(dataInfo);
    }

    /**
     * Copy constructor.
     */
    public DataFunction(DataFunction data) {
        super(data);
        x = data.x;
    }
    
    /**
     * Returns a copy of this instance.  Returned object has its own instances of
     * all fields, set equal to the values of this instance's fields.
     */
    public Data makeCopy() {
        return new DataFunction(this);
    }

    public void E(Data y) {
        System.arraycopy(((DataFunction) y).x, 0, x, 0, x.length);
    }

    public void PE(DataArithmetic y) {
        double[] yx = ((DataFunction) y).x;
        for (int i = 0; i < x.length; i++) {
            x[i] += yx[i];
        }

    }

    public void ME(DataArithmetic y) {
        double[] yx = ((DataFunction) y).x;
        for (int i = 0; i < x.length; i++) {
            x[i] -= yx[i];
        }
    }

    public void TE(DataArithmetic y) {
        double[] yx = ((DataFunction) y).x;
        for (int i = 0; i < x.length; i++) {
            x[i] *= yx[i];
        }

    }

    public void DE(DataArithmetic y) {
        double[] yx = ((DataFunction) y).x;
        for (int i = 0; i < x.length; i++) {
            x[i] /= yx[i];
        }

    }

    public void E(double y) {
        Arrays.fill(x, y);
    }

    public void PE(double y) {
        for (int i = 0; i < x.length; i++) {
            x[i] += y;
        }
    }

    public void TE(double y) {
        for (int i = 0; i < x.length; i++) {
            x[i] *= y;
        }
    }

    public void map(Function function) {
        for (int i = 0; i < x.length; i++) {
            x[i] = function.f(x[i]);
        }
    }

    public void setLength(int n) {
        x = new double[n];
    }

    public double[] getData() {
        return x;
    }

    public boolean isNaN() {
        for (int i = 0; i < x.length; i++) {
            if (Double.isNaN(x[i]))
                return true;
        }
        return false;
    }
    
    public DataArithmetic toArithmetic(DataArithmetic data) {
        if (data == null) {
            data = this;
        }
        else if (data != this) {
            data.E(this);
        }
        return data;
    }
    
    public String toString() {
        return x.toString();
    }

    private double[] x;
    private final DataDoubleArray t;
}