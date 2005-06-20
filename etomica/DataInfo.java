package etomica;

import etomica.units.Dimension;


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
public class DataInfo implements Cloneable {
    public DataInfo(String label, Dimension dimension) {
        this.label = label;
        this.dimension = dimension;
    }
    
    /**
     * @return Returns the dimension.
     */
    public Dimension getDimension() {
        return dimension;
    }
    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }

    private String label;
    private final Dimension dimension;
}