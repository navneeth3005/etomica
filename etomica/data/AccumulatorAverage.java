/*
 * History
 * Created on Jul 26, 2004 by kofke
 */
package etomica.data;

import etomica.Constants;
import etomica.Data;
import etomica.DataSink;
import etomica.DataTranslator;
import etomica.Default;
import etomica.units.Dimension;

/**
 * Accumulator that keeps statistics for averaging and error analysis.
 */
public class AccumulatorAverage extends DataAccumulator {

	public AccumulatorAverage() {
		super();
        allData = new double[getDataLength()][]; 
		setBlockSize(Default.BLOCK_SIZE);
        setPushInterval(100);
	}
	
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
        blockCountDown = blockSize;
	}
	public int getBlockSize() {
		return blockSize;
	}

    /**
     * Add the given values to the sums and block sums.  If any 
     * of the given data values is NaN, method returns with no 
     * effect on accumulation sums.
     */
    public void addData(Data data) {
        DataArithmetic value = (DataArithmetic)data;
        if(value.isNaN()) return;
        if (mostRecent == null) {
            initialize(data);
        }
  		mostRecent.E(data);
  	    blockSum.PE(value);
        work.E(data);
        work.TE(value);
        blockSumSq.PE(work);
		if(--blockCountDown == 0) {//count down to zero to determine completion of block
		    doBlockSum();
        }
    }
    
    protected void doBlockSum() {
        count++;
        blockCountDown = blockSize;
        blockSum.TE(1/(double)blockSize);//compute block average
        sum.PE(blockSum);
        work.E((Data)blockSum);
        work.TE(blockSum);
        sumSquare.PE(work);
        sumSquareBlock.PE(blockSumSq);
        //reset blocks
        mostRecentBlock.E((Data)blockSum);
        blockSum.E(0.0);
        blockSumSq.E(0.0);
    }
    
    public Data getData() {
        if (mostRecent == null) return null;
        int currentBlockCount = blockSize - blockCountDown;
        double countFraction = (double)currentBlockCount/(double)blockSize;
        double currentCount = count + countFraction;
        if(count+currentBlockCount > 0) {
//            double currentBlockAverage = blockSum[i]/currentBlockCount;
//            if (countFraction > 0) {
//                average = (sum[i] + countFraction*currentBlockAverage)/currentCount;
//            }
//            else {
            average.E((Data)sum);
            average.TE(1/(double)count);
            work.E((Data)average);
            work.TE(average);
            error.E((Data)sumSquare);
            error.TE(1/(double)count);
            error.ME(work);
            error.TE(1/(double)(count-1));
            error.map(sqrt);
            standardDeviation.E((Data)sumSquareBlock);
            standardDeviation.PE(blockSumSq);
            standardDeviation.TE(1/currentCount*blockSize);
            standardDeviation.ME(work);
            standardDeviation.map(sqrt);
//            mrBlock = (!Double.isNaN(mostRecentBlock[i])) ? mostRecentBlock[i] : currentBlockAverage;
        }
        return dataGroup;
    }
   
	/**
	 * Resets all sums to zero
	 */
    public void reset() {
        count = 0;
        sum.E(0);
        sumSquare.E(0);
        sumSquareBlock.E(0);
        blockSum.E(0);
        blockSumSq.E(0);
        error.E(Double.NaN);
        mostRecent.E(Double.NaN);
        mostRecentBlock.E(Double.NaN);
        average.E(Double.NaN);
        standardDeviation.E(Double.NaN);
        blockCountDown = blockSize;
    }
    
    protected void initialize(Data value) {
        sum = (DataArithmetic)value.clone();
        sumSquare = (DataArithmetic)value.clone();
        sumSquareBlock = (DataArithmetic)value.clone();
        standardDeviation = (DataArithmetic)value.clone();
        average = (DataArithmetic)value.clone();
        error = (DataArithmetic)value.clone();
        blockSum = (DataArithmetic)value.clone();
        blockSumSq = (DataArithmetic)value.clone();
        mostRecent = (DataArithmetic)value.clone();
        mostRecentBlock = (DataArithmetic)value.clone();
        reset();
        dataGroup = new DataGroup(value.getDataInfo(),new Data[]{(Data)mostRecent,
                (Data)average,(Data)error,(Data)standardDeviation,(Data)mostRecentBlock});
        
        for(int i=0; i<dataSinkList.length; i++) {
            if(dataSinkList[i] instanceof SinkWrapper) {
                ((SinkWrapper)dataSinkList[i]).pusher.initialize(value);
            }
        }
    }
    
    /**
     * Creates a new array that is a redimensioning of the
     * given array, resized to the given integer value.
     * Truncates or pads with zeros as needed, and returns the
     * resized array.  Used by setNData.
     */
    protected double[] redimension(int n, double[] old) {
    	double[] newArray = new double[n];
    	if(saveOnRedimension && old != null) {
    		int k = (n > old.length) ? old.length : n;
            System.arraycopy(old, 0, newArray, 0, k);
    		//need to handle updating of counters, which should be different for new and old sums if saving on redimension
    		throw new etomica.exception.MethodNotImplementedException("Capability to save data on redimension not yet implemented"); 
    	}
    	return newArray;
    }

    public DataTranslator getTranslator() {
        return translator;
    }
    
	public DataType[] dataChoices() {return CHOICES;}
    
    public DataPusher makeDataPusher(Type[] types) {
       AccumulatorPusher newPusher = new AccumulatorPusher(types);
       addDataSink(newPusher.makeDataSink());
       return newPusher;
    }
    
    /**
	 * Typed constant that can be used to indicated the quantity
	 * to be taken from a meter (e.g., average, error, current value, etc.).
	 * Used primarily by Display objects.
	 */
	public static class Type extends etomica.data.DataType {
        protected Type(String label, int index) {
            super(label);
            this.index = index;
        }       
        public Constants.TypedConstant[] choices() {return CHOICES;}
        public final int index;
    }//end of ValueType
    protected static final Type[] CHOICES = 
        new Type[] {
            new Type("Latest value", 0),
            new Type("Average", 1), 
            new Type("67% Confidence limits", 2),
            new Type("Standard deviation", 3),
            new Type("Latest block average", 4)};
    public static final Type MOST_RECENT = CHOICES[0];
    public static final Type AVERAGE = CHOICES[1];
    public static final Type ERROR = CHOICES[2];
    public static final Type STANDARD_DEVIATION = CHOICES[3];
    public static final Type MOST_RECENT_BLOCK = CHOICES[4];
	
    public int getCount() {
        return count;
    }

	/**
	 * @return Returns the saveOnRedimension.
	 */
	public boolean isSaveOnRedimension() {
		return saveOnRedimension;
	}
	/**
	 * @param saveOnRedimension The saveOnRedimension to set.
	 */
	public void setSaveOnRedimension(boolean saveOnRedimension) {
		this.saveOnRedimension = saveOnRedimension;
		if(saveOnRedimension) throw new IllegalArgumentException("Save on redimension not yet implemented correctly");
	}
    
    public int getDataLength() {
        return 5;
    }
	
    protected DataArithmetic sum, sumSquare, blockSum, blockSumSq, sumSquareBlock;
    protected DataArithmetic mostRecent;
    protected DataArithmetic mostRecentBlock;
    protected DataArithmetic average, error, standardDeviation;
    protected DataArithmetic work;
    protected DataGroup dataGroup;
    protected int count, blockCountDown;
    protected int blockSize;
    protected boolean saveOnRedimension = false;
    
    //array concatenating mostRecent, average, etc. for return by getData
    protected double[] data;
    
    //the elements of allData point to mostRecent, average, etc. arrays (see setNData)
    protected final double[][] allData;
    
    protected DataTranslator translator;

    private class AccumulatorPusher extends DataPusher {
        
        AccumulatorPusher(Type[] types) {
            indexes = new int[types.length];
            selectedAllData = new double[types.length][];
            for(int i=0; i<indexes.length; i++) {
                indexes[i] = types[i].index;
            }
            setNData(AccumulatorAverage.this.nData);
        }

        //push data obtained from outer class 
        protected void pushData() {
            if(nData == 1) {
                for(int i=0; i<indexes.length; i++) {
                    selectedData[i] = selectedAllData[i][0];
                }
            } else {
                for(int i=0, k=0; i<indexes.length; i++, k+=nData) {
                    System.arraycopy(selectedAllData[i], 0, selectedData, k, nData);
                }
            }
            pushData(selectedData);
        }
        
        void setNData(int nData) {
            selectedData = new double[indexes.length*nData];
            for(int i=0; i<indexes.length; i++) {
                selectedAllData[i] = allData[indexes[i]];
            }
            selectedTranslator = new DataTranslatorArray(indexes.length,nData);
        }
        
        public DataTranslator getTranslator() {
            return selectedTranslator;
        }
        
        protected DataSink makeDataSink() {
            return new SinkWrapper(this);
        }
        
        private double[] selectedData;
        private final double[][] selectedAllData;
        private final int[] indexes;
        private DataTranslator selectedTranslator;
    }//end of AccumulatorPusher
    
    /**
     * Wraps an AccumulatorPusher instance so that it may be put 
     * in the AccumulatorAverage's list of data sinks.
     */
    private static class SinkWrapper implements DataSink {
        final AccumulatorPusher pusher;
        SinkWrapper(AccumulatorPusher pusher) {
            this.pusher = pusher;
        }
        public void putData(double[] dummy) {
            pusher.pushData();
        }
        public void setLabel(String s) {pusher.setLabel(s);}
        public void setDimension(Dimension d) {pusher.setDimension(d);}
        public void setDefaultLabel(String s) {pusher.setDefaultLabel(s);}
    }
    
}//end of AccumulatorAverage
