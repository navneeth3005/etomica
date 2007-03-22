package etomica.data.types;

import etomica.data.Data;
import etomica.data.DataInfo;
import etomica.data.DataInfoFactory;
import etomica.data.IDataInfo;
import etomica.data.IDataInfoFactory;
import etomica.space.IVector;
import etomica.space.Space;
import etomica.units.Dimension;
import etomica.util.Function;

/**
 * Data object wrapping a single mutable value of type (Space) Vector. Value is
 * final public and can be accessed directly. 
 * <p>
 * All arithmetic methods throw ClassCastException if given a Data instance that
 * is not of this type.
 * 
 * @author David Kofke
 *  
 */
public class DataVector implements Data, java.io.Serializable {

    /**
     * Constructs a new instance with the given DataInfo, wrapping a new Vector
     * instance from the given space.
     * 
     * @param space
     *            used to construct the wrapped Vector
     * @param label
     *            a descriptive label for this data
     * @param dimension
     *            the physical dimensions of the data
     */
    public DataVector(Space space) {
        super();
        x = space.makeVector();
    }

    /**
     * Copies the elements of the given vector (wrapped in the Data object)
     * to this vector.
     */
    public void E(Data y) {
        x.E(((DataVector) y).x);
    }

    /**
     * Sets all vector elements to the given value.
     */
    public void E(double y) {
        x.E(y);
    }

    /**
     * Minus-equals (-=) operation.  Performed element-by-element.
     */
    public void ME(Data y) {
        x.ME(((DataVector) y).x);
    }

    /**
     * Plus-equals (+=) operation. Performed element-by-element.
     */
    public void PE(Data y) {
        x.PE(((DataVector) y).x);
    }

    /**
     * Times-equals (*=) operation. Performed element-by-element.
     */
    public void TE(Data y) {
        x.TE(((DataVector) y).x);
    }

    /**
     * Divide-equals (/=) operation. Performed element-by-element.
     */
    public void DE(Data y) {
        x.DE(((DataVector) y).x);
    }

    /**
     * Plus-equals (+=) operation.  Adds given value to all elements.
     */
    public void PE(double y) {
        x.PE(y);
    }

    /**
     * Times-equals (*=) operation. Multiplies all elements by the given value.
     */
    public void TE(double y) {
        x.TE(y);
    }

    /**
     * Returns true if any vector element is not-a-number, as given by Double.isNaN.
     */
    public boolean isNaN() {
        return x.isNaN();
    }

    /**
     * Maps the function on all vector elements, replacing each with the
     * value given by the function applied to it.
     */
    public void map(Function function) {
        x.map(function);
    }

    /**
     * Returns the number of elements in the wrapped vector.
     */
    public int getLength() {
        return x.getD();
    }

    /**
     * Returns the i-th vector value.
     */
    public double getValue(int i) {
        if(i < 0 || i>= x.getD()) throw new IllegalArgumentException("Illegal value: " + i);
        return x.x(i);
    }

    /**
     * Assigns the elements of the wrapped vector to the given array.
     */
    public void assignTo(double[] array) {
        x.assignTo(array);
    }

    /**
     * Returns a string formed from the dataInfo label and the vector values.
     */
    public String toString() {
        return x.toString();
    }
    
    private static final long serialVersionUID = 1L;
    /**
     * The wrapped vector data.
     */
    public final IVector x;
    
    public static class DataInfoVector extends DataInfo {
        
        public DataInfoVector(String label, Dimension dimension, Space space) {
            super(label, dimension);
            this.space = space;
        }
        
        public int getLength() {
            return space.D();
        }
        
        public IDataInfoFactory getFactory() {
            return new DataInfoVectorFactory(this);
        }
        
        public Space getSpace() {
            return space;
        }
        
        public Data makeData() {
            return new DataVector(space);
        }

        private static final long serialVersionUID = 1L;
        protected final Space space;
    }

    public static class DataInfoVectorFactory extends DataInfoFactory {
        protected DataInfoVectorFactory(DataInfoVector template) {
            super(template);
            space = template.space;
        }
        
        public IDataInfo makeDataInfo() {
            return new DataInfoVector(label, dimension, space);
        }
        
        /**
         * Sets the Space
         */
        public void setSpace(Space newSpace) {
            space = newSpace;
        }
        
        /**
         * Returns the Space
         */
        public Space getSpace() {
            return space;
        }

        private static final long serialVersionUID = 1L;
        protected Space space;
    }
}
