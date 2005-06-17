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
public abstract class Data implements Cloneable {

    public Data(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }
    
    /**
     * @return Returns the dataInfo.
     */
    public DataInfo getDataInfo() {
        return dataInfo;
    }
    
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new Error("Assertion failure");  //can't happen
        }
    }
    
    public abstract void E(Data data);
    
    private final DataInfo dataInfo;
}
