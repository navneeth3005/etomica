package etomica.data.types;

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
 * Created on Jun 16, 2005 by kofke
 */
public class DataGroup extends Data {

    public DataGroup(DataInfo dataInfo, Data[] data) {
        super(dataInfo);
        this.data = (Data[])data.clone();
    }

    public void E(Data data) {
        for (int i=0; i<this.data.length; i++) {
            this.data[i].E(((DataGroup)data).getData(i));
        }
    }

    public Data getData(int i) {
        return data[i];
    }
    
    public int getNData() {
        return data.length;
    }
    
    private final Data[] data;
}
