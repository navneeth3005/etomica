package etomica.data.meter;
import etomica.Atom;
import etomica.EtomicaElement;
import etomica.EtomicaInfo;
import etomica.Meter;
import etomica.Phase;
import etomica.Space;
import etomica.atom.iterator.AtomIteratorListTabbed;
import etomica.data.DataSourceAtomic;
import etomica.data.DataSourceScalar;
import etomica.data.DataSourceUniform;
import etomica.data.types.DataDouble;
import etomica.space.Boundary;
import etomica.space.Vector;
import etomica.units.Dimension;

/**
 * Meter that takes a (scalar) Meter and records its property as a 1-dimensional function of position in the simulation volume.
 * The measured property must be a quantity that can be associated with a single atom.
 * The position coordinate lies along an arbitrary direction vector.  The profile abscissa is a ratio of the position relative
 * to its maximum value along the chosen direction, and thus lies between zero and unity.
 * 
 * @author Rob Riggleman
 */
public class MeterProfile extends MeterFunction implements EtomicaElement {
    
    /**
     * Vector describing the orientation of the profile.
     * For example, (1,0) is along the x-axis.
     */
    final Vector profileVector;
    final Vector position;
    /**
     * Meter that defines the property being profiled.
     */
    DataSourceAtomic meter;
    
    private double profileNorm = 1.0;
    private final AtomIteratorListTabbed ai1 = new AtomIteratorListTabbed();
    
    /**
     * Default constructor sets profile along the y-axis, with 100 histogram points.
     */
    public MeterProfile(Space space) {
        super(new DataSourceUniform());
        setNDataPerPhase(((DataSourceUniform)xDataSource).getNValues());
        profileVector = space.makeVector();
        profileVector.setX(0, 1.0);
        position = space.makeVector();
    }
    
    public static EtomicaInfo getEtomicaInfo() {
        EtomicaInfo info = new EtomicaInfo("Breaks a meter's measurements into a profile taken along some direction in phase");
        return info;
    }
    
    /**
     * Returns the ordinate label for the profile, obtained from the associated meter.
     */
    public String getLabel() {return ((DataSourceScalar)meter).getLabel();}
    
    /**
     * Indicates that the abscissa coordinate is dimensionless.
     * Abscissa is formed as the ratio of the profile position relative to its maximum value.
     */
    public Dimension getXDimension() {return Dimension.NULL;}
    
    /**
     * Returns the dimensions of the ordinate, obtained from the associated meter.
     */
    public Dimension getDimension() {return (meter==null) ? null : ((DataSourceScalar)meter).getDimension();}
        
    /**
     * The meter that defines the profiled quantity
     */
    public Meter getMeter() {return (Meter)meter;}
    
    /**
     * Accessor method for the meter that defines the profiled quantity.
     */
    public void setMeter(DataSourceAtomic m) {
        meter = m;
    }
    
    /**
     * Accessor method for vector describing the direction along which the profile is measured.
     * Each atom position is dotted along this vector to obtain its profile abscissa value.
     */
    public Vector getProfileVector() {return profileVector;}
    
    /**
     * Accessor method for vector describing the direction along which the profile is measured.
     * Each atom position is dotted along this vector to obtain its profile abscissa value.
     * The given vector is converted to a unit vector, if not already.
     */
    public void setProfileVector(Vector v) {
        profileVector.E(v);
        profileVector.normalize();
    }
    
    /**
     * Returns the profile for the current configuration.
     */
    public double[] getDataAsArray(Phase p) {
        Boundary boundary = p.boundary();
        profileNorm = 1.0/boundary.dimensions().dot(profileVector);
        for (int i = 0; i <nDataPerPhase; i++) {
            phaseData[i] = 0.0;
        }
        ai1.setList(p.speciesMaster.atomList);
        ai1.reset();
        while(ai1.hasNext()) {
            Atom a = ai1.nextAtom();
            double value = ((DataDouble)meter.getData(a)).x;
            position.E(a.coord.position());
            position.PE(boundary.centralImage(position));
            int i = ((DataSourceUniform)xDataSource).getIndex(position.dot(profileVector)*profileNorm);
            phaseData[i] += value;
        }
        double dx = (((DataSourceUniform)xDataSource).getXMax() - ((DataSourceUniform)xDataSource).getXMin())/nDataPerPhase;
        double norm = 1.0/(p.atomCount()*dx);
        for (int i =0; i < nDataPerPhase; i++) {
            phaseData[i] *= norm;
        }
        return phaseData;
    }
}//end of MeterProfile