package etomica.data.meter;

import etomica.DataSource;

/**
 * Meter for recording a function
 *
 * @author David Kofke
 */
public interface DataSourceFunction extends DataSource {
    
    /**
     * Returns the DataSource for the X values of this meter
     */
    public DataSource getXDataSource();
        
}