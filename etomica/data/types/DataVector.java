package etomica.data.types;

import etomica.Data;
import etomica.DataInfo;
import etomica.Space;
import etomica.space.Vector;
import etomica.utility.Function;

/**
 * Data object wrapping a single mutable value of type (Space) Vector. Value is
 * final public and can be accessed directly. <br>
 * All arithmetic methods throw ClassCastException if given a Data instance that
 * is not of this type.
 * 
 * @author David Kofke
 *  
 */

/*
 * History Created on Jun 15, 2005 by kofke
 */
public class DataVector extends Data implements DataArithmetic {

    /**
     * Constructs a new instance with the given DataInfo, wrapping a new Vector
     * instance from the given space.
     * 
     * @param space
     *            used to construct the wrapped Vector
     * @param dataInfo
     *            provides information about the wrapped data
     */
    public DataVector(Space space, DataInfo dataInfo) {
        super(dataInfo);
        x = space.makeVector();
    }

    /**
     * Copy constructor.
     */
    public DataVector(DataVector data) {
        super(data);
        x = (Vector) data.x.clone();
    }

    /**
     * Returns a deep copy of this instance. Returned object has its own instances of
     * all fields, set equal to the values of this instance's fields.
     */
    public Data makeCopy() {
        return new DataVector(this);
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
    public void ME(DataArithmetic y) {
        x.ME(((DataVector) y).x);
    }

    /**
     * Plus-equals (+=) operation. Performed element-by-element.
     */
    public void PE(DataArithmetic y) {
        x.PE(((DataVector) y).x);
    }

    /**
     * Times-equals (*=) operation. Performed element-by-element.
     */
    public void TE(DataArithmetic y) {
        x.TE(((DataVector) y).x);
    }

    /**
     * Divide-equals (/=) operation. Performed element-by-element.
     */
    public void DE(DataArithmetic y) {
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
        return x.D();
    }

    /**
     * Returns the i-th vector value.
     */
    public double getValue(int i) {
        if(i < 0 || i>= x.D()) throw new IllegalArgumentException("Illegal value: " + i);
        return x.x(i);
    }

    /**
     * Returns a new array formed by the elements of the wrapped vector.
     */
    public double[] toArray() {
        return x.toArray();
    }

    public DataArithmetic toArithmetic(DataArithmetic data) {
        if (data == null) {
            data = this;
        } else if (data != this) {
            data.E(this);
        }
        return this;
    }

    /**
     * Returns a string formed from the dataInfo label and the vector values.
     */
    public String toString() {
        return dataInfo.getLabel() + " " + x.toString();
    }

    /**
     * The wrapped vector data.
     */
    public final Vector x;
}
