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
public class DataDouble extends Data implements DataArithmetic {

    public DataDouble(DataInfo dataInfo) {
        super(dataInfo);
    }

    public void E(Data y) {
        x = ((DataDouble)y).x;
    }

    public void E(double y) {
        x = y;
    }

    public void ME(DataArithmetic y) {
        x -= ((DataDouble)y).x;
    }

    public void PE(DataArithmetic y) {
        x += ((DataDouble)y).x;
    }

    public void TE(DataArithmetic y) {
        x *= ((DataDouble)y).x;
    }

    public void DE(DataArithmetic y) {
        x /= ((DataDouble)y).x;
    }

    public void PE(double y) {
        x += y;
    }

    public void TE(double y) {
        x *= y;
    }
    
    public boolean isNaN() {
        return Double.isNaN(x);
    }

    public double x;
}
