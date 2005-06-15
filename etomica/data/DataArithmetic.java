package etomica.data;


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
public interface DataArithmetic {
    public void E(DataArithmetic y);
    public void PE(DataArithmetic y);
    public void ME(DataArithmetic y);
    public void E(double y);
    public void PE(double y);
}
