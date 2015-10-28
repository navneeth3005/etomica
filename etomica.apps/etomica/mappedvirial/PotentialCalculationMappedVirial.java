package etomica.mappedvirial;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IPotentialAtomic;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.AtomLeafAgentManager;
import etomica.atom.AtomPair;
import etomica.atom.iterator.IteratorDirective;
import etomica.integrator.IntegratorVelocityVerlet.MyAgent;
import etomica.potential.Potential2SoftSpherical;
import etomica.potential.PotentialCalculation;
import etomica.space.ISpace;


/**
 * PotentialCalculation that implements mapped-averaged virial (to get the
 * pressure).
 * 
 * @author Andrew Schultz
 */
public class PotentialCalculationMappedVirial implements PotentialCalculation {

    protected final IBox box;
    protected final IteratorDirective allAtoms;
    protected final AtomLeafAgentManager<MyAgent> forceManager;
    protected final ISpace space;
    protected double beta;
    protected final IVectorMutable dr;
    protected double c1;
    protected final double[][] cumint;
    protected final AtomPair pair;
    protected final double vol;
    protected final double[] q;
    protected final double[] rCutoff;
    protected double x0;
    protected final double[] sum;
    protected final double[] epsilon;
    protected final boolean switchev;
    protected final double[] x;
    protected final int nbins;

    public PotentialCalculationMappedVirial(ISpace space, IBox box, int nbins, double[] rCutoff, AtomLeafAgentManager<MyAgent> forceManager, double pCut) {
        switchev = false;
        this.space = space;
        this.box = box;
        this.nbins = nbins;
        pair = new AtomPair();
        this.forceManager = forceManager;
        allAtoms = new IteratorDirective();
        dr = space.makeVector();
        cumint = new double[rCutoff.length][nbins+1];
        vol = box.getBoundary().volume();
        this.rCutoff = rCutoff;
        q = new double[rCutoff.length];
        sum = new double[rCutoff.length];
        epsilon = new double[rCutoff.length];
        x0 = 0.95*pCut;
        for (int i=0; i<rCutoff.length; i++) {
            epsilon[i] = (0.95*pCut-rCutoff[i])*0.3;
            if (epsilon[i] > 0.3*rCutoff[i]) epsilon[i] = 0.3*rCutoff[i];
        }
        x = new double[rCutoff.length];
    }

    public void setX0(double newX0) {
        x0 = newX0;
    }

    public double[] getEpsilon() {
        return epsilon;
    }
    
    public void setTemperature(double T, Potential2SoftSpherical p2) {
        beta = 1/T;
        double rc = p2.getRange();
        c1 = Math.log(rc+1)/nbins;
        int D = space.D();
        int nbins = cumint[0].length-1;
        for (int j=0; j<rCutoff.length; j++) {
            
            for (int i=1; i<=nbins; i++) {
                double r = Math.exp(c1*i)-1;
                double r2 = r*r;
                double u = p2.u(r2);
                double evm1 = 0;
                if (switchev) {
                    double eu = Math.exp(-beta*u);
                    evm1 = (eu-1)*(1-theta(r,j));
                }
                else {
                    double v = u*(1-theta(r,j));
                    evm1 = Math.exp(-beta*v)-1;
                }
                q[j] += (D==2 ? r : r2)*evm1*c1*(r+1);
                cumint[j][i] = cumint[j][i-1] + (D==2 ? r : r2)*(evm1+1)*c1*(r+1);
//                if (rCutoff[j]==1 && r>0.864 && r<0.896) System.out.println(r+" "+(evm1+1)+" "+cumint[j][i]);
            }
            q[j] *= 4*Math.PI;
//            System.out.println(rCutoff[j]+" "+q[j]);
        }
    }
    
    protected double calcXs(int idx, double r, double u) {
        double y = cumint(idx, r);
        double evm1 = 0;
        if (switchev) {
            double eu = Math.exp(-beta*u);
            evm1 = (eu-1)*(1-theta(r,idx));
        }
        else {
            double v = u*(1-theta(r,idx));
            evm1 = Math.exp(-beta*v)-1;
        }
        return -r + space.D()*y/(r*r*(evm1+1));
    }

    protected double cumint(int idx, double r) {
        // r = (exp(c1*i) - 1)
        double i = Math.log(r+1)/c1;
        int ii = (int)i;
        double y = cumint[idx][ii] + (cumint[idx][ii+1]-cumint[idx][ii])*(i-ii);
        return y;
    }

    public double theta(double r, int idx) {
        return 0.5*(1+Math.tanh((r-rCutoff[idx])/epsilon[idx]));
    }

    public double dtheta(double r, int idx) {
        double cosh = Math.cosh((r-rCutoff[idx])/epsilon[idx]);
        return 0.5/(epsilon[idx]*cosh*cosh);
    }

    public double getQ(int idx) {
        return q[idx];
    }

    public void reset() {
        for (int j=0; j<sum.length; j++) {
            sum[j] = 0;
        }
    }
    
    public void doCalculation(IAtomList atoms, IPotentialAtomic potential) {
        if (!(potential instanceof Potential2SoftSpherical)) return;
        Potential2SoftSpherical p2 = (Potential2SoftSpherical)potential;
        IAtom a = atoms.getAtom(0);
        IAtom b = atoms.getAtom(1);

        dr.Ev1Mv2(b.getPosition(),a.getPosition());
        box.getBoundary().nearestImage(dr);
        double r2 = dr.squared();
        double r = Math.sqrt(r2);
        if (r > p2.getRange()) return;
        IVector fi = forceManager.getAgent(a).force;
        IVector fj = forceManager.getAgent(b).force;
        pair.atom1 = b;
        double u = p2.u(r2);
        double eu = Math.exp(-beta*u);
        double fifj = (fi.dot(dr) - fj.dot(dr))/r;
        double fij = p2.du(r2);
        for (int jj=0; jj<rCutoff.length; jj++) {
            double xs = calcXs(jj, r, u);
            double theta = theta(r, jj);
            double up = fij/r;
            double vp = 0;
            if (switchev) {
                double ev = 1 + (eu-1)*(1-theta);
                vp = (up*eu*(1-theta) + (eu-1)*dtheta(r,jj)/beta)/ev;
            }
            else {
                vp = up*(1-theta) - u*dtheta(r,jj);
            }
            double wp = 0.5*fifj;
            sum[jj] += r*(vp-up);
            if (r < x0) {
                sum[jj] += xs*(vp-wp);
            }
        }
    }

    public double[] getPressure() {
        int D = space.D();
        double density = box.getMoleculeList().getMoleculeCount()/vol;

        for (int j=0; j<rCutoff.length; j++) {
            x[j] = density/beta - 0.5*q[j]*density*density/beta  + sum[j]/(D*vol);
        }
        return x;
    }
}