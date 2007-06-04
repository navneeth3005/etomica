package etomica.data.meter;
import etomica.EtomicaInfo;
import etomica.atom.AtomSet;
import etomica.atom.AtomTypeLeaf;
import etomica.atom.IAtom;
import etomica.atom.IAtomKinetic;
import etomica.data.Data;
import etomica.data.DataSourceAtomic;
import etomica.data.DataTag;
import etomica.data.IDataInfo;
import etomica.data.types.DataTensor;
import etomica.data.types.DataTensor.DataInfoTensor;
import etomica.phase.Phase;
import etomica.space.IVector;
import etomica.space.Space;
import etomica.units.Energy;

/**
 * A meter to compute the velocity component of the pressure tensor. 
 * Averages a tensor quantity formed from a dyad of the velocity of each atom. 
 * Specifically, the quantity averaged is 1/N * sum(pp/m), where p is the momentum,
 * m is the mass, and the sum is over all N atoms.
 * 
 * @author Rob Riggleman
 */

public class MeterTensorVelocity implements DataSourceAtomic, java.io.Serializable {

    public MeterTensorVelocity(Space space) {
        data = new DataTensor(space);
        dataInfo = new DataInfoTensor("pp/m",Energy.DIMENSION, space);
        atomData = new DataTensor(space);
        tag = new DataTag();
        dataInfo.addTag(tag);
    }
    
    public static EtomicaInfo getEtomicaInfo() {
        EtomicaInfo info = new EtomicaInfo("Velocity tensor, formed from averaging dyad of velocity vector for each atom");
        return info;
    }
    
    public IDataInfo getDataInfo() {
        return dataInfo;
    }
    
    public DataTag getTag() {
        return tag;
    }
       
    public IDataInfo getAtomDataInfo() {
        return dataInfo;
    }
       
    /**
     * Returns the velocity dyad (mass*vv) summed over all atoms, and divided by N
     */
    public Data getData() {
        if (phase == null) throw new IllegalStateException("must call setPhase before using meter");
        data.E(0.0);
        int count = 0;
        AtomSet leafList = phase.getSpeciesMaster().getLeafList();
        int nLeaf = leafList.getAtomCount();
        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            getData(leafList.getAtom(iLeaf));
            data.PE(atomData);
            count++;
        }
        data.TE(1.0/nLeaf);
        return data;
    }
    
    /**
     * Returns the velocity dyad (mass*vv) for the given atom.
     */
    public Data getData(IAtom atom) {
        IVector vel = ((IAtomKinetic)atom).getVelocity();
        atomData.x.Ev1v2(vel, vel);
        atomData.TE(((AtomTypeLeaf)atom.getType()).rm());
        return atomData;
    }

    /**
     * @return Returns the phase.
     */
    public Phase getPhase() {
        return phase;
    }
    /**
     * @param phase The phase to set.
     */
    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    private static final long serialVersionUID = 1L;
    private String name;
    private Phase phase;
    private final DataTensor data, atomData;
    private final DataInfoTensor dataInfo;
    protected DataTag tag;
}
