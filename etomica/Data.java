package etomica;


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
public class Data implements Cloneable {

    public Data(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }
    
    /**
     * @return Returns the dataInfo.
     */
    public DataInfo getDataInfo() {
        return dataInfo;
    }
    
    private final DataInfo dataInfo;
}
