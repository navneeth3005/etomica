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
public class DataInteger extends Data implements DataNumeric {

    public DataInteger(DataInfo dataInfo) {
        super(dataInfo);
    }

    public void E(Data y) {
        x = ((DataInteger)y).x;
    }

    public void E(int y) {
        x = y;
    }
    
    public DataArithmetic toArithmetic(DataArithmetic data) {
        if (data == null) {
            data = new DataDouble(getDataInfo());
        }
        ((DataDouble)data).x = x;
        return data;
    }

    public String toString() {
        return Integer.toString(x);
    }
    public int x;
}
