package etomica.data;



/**
 * A DataProcessor that can handle multiple sinks, passing the same Data to each.
 *
 * @author David Kofke
 *
 */

/*
 * History
 * Created on Jul 23, 2005 by kofke
 */
public abstract class DataProcessorForked extends DataProcessor implements DataPipeForked {

    public DataProcessorForked() {
        dataFork = new DataFork();
        dataSink = dataFork;
    }
       
    /* (non-Javadoc)
     * @see etomica.data.DataProcessor#getDataSink()
     */
    public DataSink getDataSink() {
        return dataFork.getDataSink(0);
    }
    
    public DataSink getDataSink(int i) {
        return dataFork.getDataSink(i);
    }
    
    public int getDataSinkCount() {
        return dataFork.getDataSinkCount();
    }
    
    /* (non-Javadoc)
     * @see etomica.data.DataPipe#setDataSink(etomica.DataSink)
     */
    public void setDataSink(DataSink dataSink) {
        dataFork.setDataSink(dataSink);
    }
    
    public void setDataSinks(DataSink[] dataSinks) {
        dataFork.setDataSinks(dataSinks);
    }

    /**
     * Adds the given DataSink to those receiving the Data entering this DataFork,
     * keeping all previously entered DataSinks.
     * 
     * @param dataSink
     */
    public void addDataSink(DataSink dataSink) {
        dataFork.addDataSink(dataSink);
    }

    /**
     * Removes the specified data sink.
     * 
     * @param dataSink data sink to be removed from this list, if present.
     */
    public void removeDataSink(DataSink dataSink) {
        dataFork.removeDataSink(dataSink);
    }

    private final DataFork dataFork;

}
