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

    public void E(DataArithmetic y) {
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

    public void PE(double y) {
        x += y;
    }

    public double x;
}
