package etomica.performance;

import etomica.*;
public class SpaceP extends Space{

    public static String version() {return "Space2D:01.07.07/"+Space.VERSION;}

    public static int D=3;   

    public SpaceP(int dimension){
        super(dimension);
        D=dimension;

    }

    public final int D() {return D;}
    public final int powerD(int n) {
        switch(D) {
            case 1: return n;
            case 2: return n*n;
            case 3: return n*n*n;
            default: return (int)Math.pow(n,D);
        }
    }
    public final double powerD(double a) {
        switch(D) {
            case 1: return a;
            case 2: return a*a;
            case 3: return a*a*a;
            default: return Math.pow(a,D);
        }
    }

    public static final Vector ORIGIN = new Vector();

    public final Space.Vector origin() {return ORIGIN;}

    

    public double sphereVolume(double r) {return Math.PI*r*r;}  //volume of a sphere of radius r

    public double sphereArea(double r) {return 2.0*Math.PI*r;}  //surface area of sphere of radius r (used for differential shell volume)

    public Space.Vector makeVector() {return new Vector();}

    public Space.Orientation makeOrientation() {return new Orientation();}

    public Space.Tensor makeTensor() {return null;}

    public Space.Tensor makeRotationTensor() {return null;}

    public Space.Coordinate makeCoordinate(Atom a) {

        

        if(a.node instanceof AtomTreeNodeGroup) return new CoordinateGroup(a);

//        else if(a.type instanceof AtomType.Rotator) return new OrientedCoordinate(a);

        else {return new Coordinate(a);}

        

       

    }

    public Space.CoordinatePair makeCoordinatePair() {return new CoordinatePair();}

    public Space.Boundary.Type[] boundaryTypes() {return null;}

    public Space.Boundary makeBoundary() {return makeBoundary(Space3D.Boundary.PERIODIC_SQUARE);}  //default

    public Space.Boundary makeBoundary(Space.Boundary.Type t) {

    /*

        if(t == Boundary.NONE) {return new BoundaryNone();}

        else if(t == Boundary.PERIODIC_SQUARE) {return new BoundaryPeriodicSquare();}

        else if(t == Boundary.SLIDING_BRICK) return new BoundarySlidingBrick();

        else return null;

        */

        return new BoundaryPeriodicSquare();

    }

    

    public static EtomicaInfo getEtomicaInfo() {

        EtomicaInfo info = new EtomicaInfo("Two-dimensional space");

        return info;

    }



    public static final double r2(Vector u1, Vector u2, Boundary b) {

    System.out.println("checking in r2 ");
       /*

        Vector.WORK.x = u1.x - u2.x;

        Vector.WORK.y = u1.y - u2.y;

        b.nearestImage(Vector.WORK);

        */

        //return Vector.WORK.x*Vector.WORK.x + Vector.WORK.y*Vector.WORK.y;

        return 0.0;

    }

    

    

    

    

    public final static class Vector extends Space.Vector{  //declared final for efficient method calls

    
        public void Ev1Pv2(Space.Vector v1, Space.Vector v2) {}
        public void Ev1Mv2(Space.Vector v1, Space.Vector v2) {}
        public void mod(double d) {}
        public void mod(Space.Vector v) {}
        
        public double X[];

        public static final Vector WORK = new Vector();

        public Vector () {X = new double[D];

   //     for(int i=0;i<D;i++)X[i]=0.0;

        }

        public  Vector (double x, double y, double z) {
            X=new double[] {x, y, z};
        }

        public Vector (double[] a) {for(int i=0;i<D;i++)X[i]=a[i];}//should check length of a for exception

        public Vector (Vector u) {this.E(u);}
        
        public boolean equals(Space.Vector v) {return equals((Vector)v);}
        public boolean equals(Vector v) {for(int i=0;i<D;i++) if(X[i] != v.X[i]) return false; return true;}

        public double[] toArray() {return X;}

    	public void setArray(double[] xNew){X = xNew;
    	}//should check length

        //NEED TO CHANGE

        public void sphericalCoordinates(double[] result) {

        }

        public int length() {return D;}

        public int D() {return D;}

        public double x(int i) {return X[i];}

        public void setX(int i, double d) {X[i]=d;}

        public void E(Vector u) {for(int i=0;i<D;i++)X[i]=u.X[i];}
       
        public void E(double[] u) {X[0]=u[0];X[1]=u[1];X[2]=u[2];}  //should check length of array for exception

       // public void E(double[] u) {X=u;}  //should check length of array for exception

        public void E(double a) {for(int i=0;i<D;i++)X[i]=a;}
        public void E(double dx, double dy, double dz){X[0]=dx;X[1]=dy;X[2]=dz;}


        public void E(Space.Vector u) {
             if (u instanceof Space3D.Vector){
         X=u.toArray();
         }
        else 
                E((Vector)u);
        }

        public void Ea1Tv1(double a1, Space.Vector u) {Vector u1=(Vector)u;for(int i=0;i<D;i++)X[i]=a1*u1.X[i];}

        public void PEa1Tv1(double a1, Space.Vector u) {Vector u1=(Vector)u; for(int i=0;i<D;i++)X[i]+=a1*u1.X[i];}

        public void PE(Vector u) {for(int i=0;i<D;i++)X[i]+=u.X[i];}

        public void PE(double a) {for(int i=0;i<D;i++)X[i]+=a;}

        public void ME(Vector u) {for(int i=0;i<D;i++)X[i]-=u.X[i];}

        public void PE(int i, double a) {X[i]+=a;}

        public void TE(double a) {for(int i=0;i<D;i++)X[i]*=a;}

        public void TE(Vector u) {for(int i=0;i<D;i++)X[i]*=u.X[i];}

        

        public void TE(int i, double a) {X[i]*=a;}

        public void DE(double a) {for(int i=0;i<D;i++)X[i]/=a;}

        public void DE(Vector u) {for(int i=0;i<D;i++)X[i]/=u.X[i];}

        public double min() {double m=Double.MAX_VALUE;for(int i=0;i<D;i++)if(X[i]<m) m=X[i]; return m;}

        public double max() {double m=-Double.MAX_VALUE;for(int i=0;i<D;i++)if(X[i]>m) m=X[i]; return m;}

        public double squared() {double sum=0.0; for(int i=0;i<D;i++)sum+=X[i]*X[i]; return sum;}

        public double dot(Vector u) {double sum=0.0;for(int i=0;i<D;i++)sum+=X[i]*u.X[i]; return sum;}
        public double dot(double a[]){return X[0]*a[0]+X[1]*a[1]+X[2]*a[2];}
        public Space.Vector P(Space.Vector u) {return WORK;}//NEED TO CHANGE

        public Space.Vector M(Space.Vector u) { return WORK;}//NEED TO CHANGE

        public Space.Vector T(Space.Vector u) { return WORK;}//NEED TO CHANGE

        public Space.Vector D(Space.Vector u) {return WORK;} //NEED TO CHANGE

        public Space.Vector abs() { return WORK;} // NEED TO CHANGE

        

        

       public void setRandomCube(){for(int i=0;i<D;i++)X[i]=Simulation.random.nextDouble()-0.5;}

       

     

        /**

         * Replaces this vector with its cross-product with the given 3D vector, with result projected

         * onto the 2D plane.  This vector becomes the result of (this vector) X u.

         */

        public void XE(Space3D.Vector u) {

            

        }

        //NEED TO CHANGE

         public Space3D.Vector cross(Space3D.Vector u) {//not thread safe

            /*

            Space3D.Vector.WORK.x = y*u.z;

            Space3D.Vector.WORK.y = -x*u.z;

            Space3D.Vector.WORK.z = x*u.y - y*u.x;

            */

            return Space3D.Vector.WORK;

        }

        //NEED TO CHANGE

        public Space3D.Vector cross(Space2D.Vector u) {//not thread safe

            /*

            Space3D.Vector.WORK.x = 0.0;

            Space3D.Vector.WORK.y = 0.0;

            Space3D.Vector.WORK.z = x*u.y - y*u.x;

            */

            return Space3D.Vector.WORK;

        }

         

         

        public void normalize() {

            

        }

        public void transform(Space.Tensor A) {transform((Tensor)A);}

        public void transform(Space.Boundary b, Space.Vector r0, Space.Tensor A) {}

        public void transform(Boundary b, Vector r0, Tensor A) {

            

        }

        public void randomStep(double d) {} //uniformly distributed random step in x and y, within +/- d

        public void setRandom(double d){ }

        public void setRandom(double dx, double dy) {}

        public void setRandom(Vector u) {}

        public void setRandomSphere() {

         }

        public void randomRotate(double thetaStep){

         }

        

        public double[] getArray(){return X;}
        public void PE(Space.Vector u) {PE((Vector)u);}
        public void ME(Space.Vector u) {ME((Vector)u);}
        public void TE(Space.Vector u) {TE((Vector)u);}
        public void DE(Space.Vector u) {DE((Vector)u);}
        public double dot(Space.Vector u) {return dot((Vector)u);}
    }

        

   public static class Coordinate extends Space.Coordinate {

        public Coordinate nextCoordinate, previousCoordinate;

        public final Vector r = new Vector();  //Cartesian coordinates

        public final Vector p = new Vector();  //Momentum vector

        public final Vector rLast = new Vector();  //vector for saving position

        protected final Vector work = new Vector(); 

        public void inflate(Space.Vector d) {}
        public void inflate(double d) {}

        public Coordinate(Atom a) {super(a);}

        public Space.Vector position() {return r;}

        public Space.Vector momentum() {return p;}

        public double position(int i) {return r.x(i);}

        public double momentum(int i) {return p.x(i);}

        

        public void setNextAtom(Atom a) {

            if(a == null) nextCoordinate = null;

            else {

                nextCoordinate = (Coordinate)a.coord;

                ((Coordinate)a.coord).previousCoordinate = this;

            }

        }

        

        public Atom nextAtom() {return nextCoordinate!=null ? nextCoordinate.atom : null;}

        public Atom previousAtom() {return previousCoordinate!=null ? previousCoordinate.atom : null;}

        public void clearPreviousAtom() {previousCoordinate = null;}

        

        

        

        public void randomizeMomentum(double temperature) { 

        }

        public double kineticEnergy() {return 0.5*p.squared()*rm();}

       //NEED TO CHANGE

        public void freeFlight(double t) {

            /*double tM = t*rm(); // t/mass

            for(int i=r.length;i>0;i--){

            r.X[i] += p.X[i]*tM;

            }

            */

       }

      

        public void transform(Space.Vector r0, Space.Tensor A) {

            r.transform((Boundary)atom.node.parentPhase().boundary(), (Vector)r0, (Tensor)A);}

        /**

        * Moves the atom by some vector distance

        * 

        * @param u

        */

        public void translateBy(Space.Vector u) {r.PE(u);}

        /**

        * Moves the atom by some vector distance

        * 

        * @param u

        */

        public void translateBy(double d, Space.Vector u) {r.PEa1Tv1(d,(Vector)u);}

        /**

        * Moves the atom by some vector distance

        * 

        * @param u

        */

        public void translateTo(Space.Vector u) {r.E(u);}      

        public void displaceBy(Space.Vector u) {rLast.E(r); 
        
        
        translateBy((Vector)u);}

        public void displaceBy(double d, Space.Vector u) {
            
            rLast.E(r); 
          //  System.out.println(" checking saving coordinate");
       // System.out.println(rLast.X[0] +" it should have " + r.X[0]);
        
            
            translateBy(d,(Vector)u);}

        public void displaceTo(Space.Vector u) {rLast.E(r); translateTo((Vector)u);}  

        public void displaceWithin(double d) {work.setRandomCube(); displaceBy(d,work);}

        public void displaceToRandom(etomica.Phase p) {rLast.E(r); translateToRandom(p);}

        public void replace() {r.E(rLast);}

    //    public final void inflate(double s) {r.TE(s);}



        public void accelerateBy(Space.Vector u) {p.PE(u);}

        public void accelerateBy(double d, Space.Vector u) {p.PEa1Tv1(d,u);}



       

        

    }

    public static final class CoordinatePair extends Space.CoordinatePair {
        Coordinate c1;
        Coordinate c2;
        private final Vector dr = new Vector();
        private double dvx, dvy, dvz; //drx, dry, drz;
        public CoordinatePair() {super();}

        public void reset(Space.Coordinate coord1, Space.Coordinate coord2) {
            c1 = (Coordinate)coord1;
            c2 = (Coordinate)coord2;
      //      reset();
        }
        public void reset() {
            for(int i=0; i<D; i++) {dr.X[i] = c2.r.X[i] - c1.r.X[i];}
            c1.atom.node.parentPhase().boundary().nearestImage(dr);
            r2 = dr.squared();
    /*        double rm1 = c1.rm();
            double rm2 = c2.rm();
            dvx = rm2*c2.p.x - rm1*c1.p.x;
            dvy = rm2*c2.p.y - rm1*c1.p.y;
            dvz = rm2*c2.p.z - rm1*c1.p.z;*/
        }
            
        public void reset(Space3D.Vector M) {
         ///   dr.x = c2.r.x - c1.r.x + M.x;
         ///   dr.y = c2.r.y - c1.r.y + M.y;
         ///   dr.z = c2.r.z - c1.r.z + M.z;
            r2 = dr.squared();
        }
        public double r2() {
         //   return r2;
            for(int i=0; i<D; i++) {dr.X[i] = c2.r.X[i] - c1.r.X[i];}
            c1.atom.node.parentPhase().boundary().nearestImage(dr);
            return dr.squared();
        }
            
        public Space.Vector dr() {return dr;}
        public double dr(int i) {return dr.X[i];}
        public double dv(int i) {return 0.0;}//dv.X[i];}
        public double v2() {return dvx*dvx + dvy*dvy + dvz*dvz;}
        public double vDot(Space.Vector u) {return vDot((Space3D.Vector)u);}
        public double vDot(Space3D.Vector u) {return dvx*u.x(0) + dvy*u.x(1) + dvz*u.x(2);}
        public double vDotr() {return 0.0;}//dr.x*dvx + dr.y*dvy + dr.z*dvz;}
        public void push(double impulse) {/*
            c1.p.x += impulse*dr.x;
            c1.p.y += impulse*dr.y;
            c1.p.z += impulse*dr.z;
            c2.p.x -= impulse*dr.x;
            c2.p.y -= impulse*dr.y;
            c2.p.z -= impulse*dr.z;*/
        }
        public void setSeparation(double r2New) {/*
            double ratio = c2.mass()*c1.rm();
            double delta = (Math.sqrt(r2New/this.r2()) - 1.0)/(1 + ratio);
            c1.r.x -= ratio*delta*dr.x;
            c1.r.y -= ratio*delta*dr.y;
            c1.r.z -= ratio*delta*dr.z;
            c2.r.x += ratio*delta*dr.x;
            c2.r.y += ratio*delta*dr.y;
            c2.r.z += ratio*delta*dr.z;*/
        }
    }
    

    

     public static class CoordinateGroup extends Coordinate {
        public Coordinate firstChild, lastChild;
        public CoordinateGroup(Atom a) {super(a);}
        private final Vector work2 = new Vector();

        public final Atom firstAtom() {return (firstChild != null) ? firstChild.atom : null;}
        public final void setFirstAtom(Atom a) {firstChild = (a != null) ? (Coordinate)a.coord : null;}
        public final Atom lastAtom() {return (lastChild != null) ? lastChild.atom : null;}
        public final void setLastAtom(Atom a) {lastChild = (a != null) ? (Coordinate)a.coord : null;}
        public void inflate(Space.Vector d) {}        
        public void inflate(double d) {}        
        public double mass() {
            double massSum = 0.0;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                massSum += coord.mass();
            }
            return massSum;
        } 
        public double rm() {return 1.0/mass();}
        /**
         * Applies transformation to COM of group, keeping all internal atoms at same relative positions.
         */
        public void transform(Space.Vector r0, Space.Tensor A) {
            work.E(position()); //work = r
            work.transform((Boundary)atom.node.parentPhase().boundary(),(Vector)r0, (Tensor)A);
            work.ME(r);//now work vector contains translation vector for COM
            translateBy(work);
        }
        public Space.Vector position() {
            r.E(0.0); double massSum = 0.0;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                r.PEa1Tv1(coord.mass(), coord.position()); massSum += coord.mass();
                if(coord == lastChild) break;
            }
            r.DE(massSum);
            return r;
        }
        public Space.Vector momentum() {
            p.E(0.0);
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                p.PE(coord.momentum());
                if(coord == lastChild) break;
            }
            return p;
        }
        public double position(int i) {
            double sum = 0.0; double massSum = 0.0;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                sum += coord.mass()*coord.position(i); massSum += coord.mass();
                if(coord == lastChild) break;
            }
            sum /= massSum;
            return sum;
        }
        public double momentum(int i) {
            double sum = 0.0;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                sum += coord.mass()*coord.momentum(i);
                if(coord == lastChild) break;
            }
            return sum;
        }
        public double kineticEnergy() {
            double sum = 0.0;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                sum += coord.kineticEnergy();
                if(coord == lastChild) break;
            }
            return sum;
        }
        public void freeFlight(double t) {
            double sum = 0.0;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                coord.freeFlight(t);
                if(coord == lastChild) break;
            }
        }
        public void translateBy(Space.Vector u) {translateBy((Vector)u);}
        public void translateBy(Vector u0) {
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                coord.translateBy(u0);
                if(coord == lastChild) break;
            }
        }
        public void translateBy(double d, Space.Vector u) {
            Vector u0 = (Vector)u;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                coord.translateBy(d, u0);
                if(coord == lastChild) break;
            }
        }
        public void translateTo(Space.Vector u) {
            if(!(u instanceof Vector)) {
                work2.X[0] = ((Space3D.Vector)u).x(0);
                work2.X[1] = ((Space3D.Vector)u).x(1);
                work2.X[2] = ((Space3D.Vector)u).x(2);
            } else work2.E(u);
            work.Ea1Tv1(-1,position()); //position() uses work, so need this first
            work.PE((Vector)work2);
            translateBy(work);
        }
        public void translateTo(Space3D.Vector u) {
            work2.X[0] = u.x(0);
            work2.X[1] = u.x(1);
            work2.X[2] = u.x(2);
            translateTo(work2);
        }
        public void displaceBy(Space.Vector u) {
            Vector u0 = (Vector)u;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                coord.displaceBy(u0);
                if(coord == lastChild) break;
            }
        }
        public void displaceBy(double d, Space.Vector u) {
            Vector u0 = (Vector)u;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                coord.displaceBy(d, u0);
                if(coord == lastChild) break;
            }
        }
        public void displaceTo(Space.Vector u) {
            work.E((Vector)u);
            work.ME(position());
            displaceBy(work);
        }
        public void displaceToRandom(etomica.Phase p) {
            displaceTo((Vector)p.boundary().randomPosition());
        }
        public void replace() {
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                coord.replace();
                if(coord == lastChild) break;
            }
        }
        public void accelerateBy(Space.Vector u) {
            Vector u0 = (Vector)u;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                coord.accelerateBy(u0);
                if(coord == lastChild) break;
            }
        }
        public void accelerateBy(double d, Space.Vector u) {
            Vector u0 = (Vector)u;
            for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                coord.accelerateBy(d, u0);
                if(coord == lastChild) break;
            }
        }
        public final void displaceWithin(double d) {work.setRandomCube(); displaceBy(d,work);}
        
        public void randomizeMomentum(double temperature) {
            switch(((AtomTreeNodeGroup)atom.node).childAtomCount()) {
                case 0: return;
                case 1: firstChild.randomizeMomentum(temperature);//do not zero COM momentum if only one child atom
                        return;
                default://multi-atom group
                    work.E(0.0); double sum=0.0;
                    for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                        coord.randomizeMomentum(temperature);
                        work.PE(coord.momentum());
                        sum++;
                        if(coord == lastChild) break;
                    }
                    work.DE(-sum);
                    for(Coordinate coord=firstChild; coord!=null; coord=coord.nextCoordinate) {
                        coord.accelerateBy(work);
                        if(coord == lastChild) break;
                    }
            }//end switch
        }//end randomizeMomentum
    }//end of CoordinateGroup
/*   public static class OrientedCoordinate extends Coordinate implements Space.Coordinate.AngularP {
        private double L = 0.0; //magnitude of angular momentum
        private final SpaceP.Vector vector = new SpaceP.Vector();//used to return vector quantities (be sure to keep x and y components zero)
        private final double[] I;
        private final Orientation orientation = new Orientation();
        public OrientedCoordinate(Atom a) {
            super(a);
            I = ((AtomType.SphericalTop)a.type).momentOfInertia();
        }
        public Space.Vector angularMomentum() {vector.X[2] = L; return vector;}
        public Space.Vector angularVelocity() {vector.X[2] = L/I[0]; return vector;}
        public void angularAccelerateBy(Space.Vector t) {angularAccelerateBy((Vector)t);}
        public void angularAccelerateBy(Vector t){L += t.X[2];}
        public Space.Orientation orientation() {return orientation;}
        public double kineticEnergy() {return super.kineticEnergy() + 0.5*L*L/I[0];}
        public void freeFlight(double t) {
            super.freeFlight(t);
            orientation.rotateBy(t*L/I[0]);//all elements of I equal for spherical top
        }
    }
*/     
     public static class Orientation extends Space.Orientation {
         //The rotation matrix A operates on the components of a vector in the space-fixed frame to yield the
         //components in the body-fixed frame
         private final double[][] A = new double[D][D];
        private final Vector[] bodyFrame = new Vector[] {new Vector(1.0,0.0, 0.0), new Vector(0.0,1.0,0.0), new Vector(0.0,0.0,1.0)};
        private final double[] angle = new double[3];
         private boolean needToUpdateA = true;
         public void E(Space.Orientation o) {E((Orientation)o);}
         public double[][] getRotMatrix(){return A;};
        public void E(Orientation o) {
          angle[0] = o.angle[0];
          angle[1] = o.angle[1];
          angle[2] = o.angle[2];
          needToUpdateA = true;
        }
         public Space.Vector[] bodyFrame() {return bodyFrame;}
         public double[] angle() {return angle;}
         public void replaceOrientation(){
         
          
        }
        public final void rotateBy(double dt[]) {
            rotateBy(0, dt[0]);
            rotateBy(1, dt[1]);
            rotateBy(2, dt[2]);
        }
        public final void rotateBy(double dt) {
          rotateBy(0, dt);
          rotateBy(1, dt);
          rotateBy(2, dt);
        }
        public void rotateBy(int i, double dt) {
            angle[i] += dt;
            if(angle[i] > Constants.TWO_PI) angle[i] -= Constants.TWO_PI;
            else if(angle[i] < 0.0) angle[i] += Constants.TWO_PI;
            needToUpdateA = true;
        }
        public void randomRotation(double t) {
          rotateBy(0, (2.*Simulation.random.nextDouble()-1.0)*t);
          rotateBy(1, (2.*Simulation.random.nextDouble()-1.0)*t);
          rotateBy(2, (2.*Simulation.random.nextDouble()-1.0)*t);
        }
           public void rotateTo(Space.Vector xAxis) {
             xAxis.normalize();
            Space.Vector yAxis = findPerpend(xAxis);
            
            Space.Vector zAxis = findPerpend(xAxis, yAxis);
            
            A[0][0]=zAxis.x(2)*xAxis.x(0);
            A[0][1]=zAxis.x(2)*xAxis.x(1);
            A[0][2]=-zAxis.x(0);
            A[1][0]=zAxis.x(0)*yAxis.x(2)*xAxis.x(0) - yAxis.x(1)*xAxis.x(1);
            A[1][1]=zAxis.x(0)*yAxis.x(2)*xAxis.x(1)+xAxis.x(0)*yAxis.x(1);
            A[1][2]=zAxis.x(2)*yAxis.x(2);
            A[2][0] =yAxis.x(1)*zAxis.x(0)*xAxis.x(0)+ yAxis.x(2)*xAxis.x(1);
            A[2][1] =yAxis.x(1)*zAxis.x(0)*xAxis.x(1) -yAxis.x(2)*xAxis.x(0);
            A[2][2] = yAxis.x(1)*zAxis.x(2);
            bodyFrame[0].E(A[0]);
            bodyFrame[1].E(A[1]);
            bodyFrame[2].E(A[2]);
            needToUpdateA = false;
          }
      public Space.Vector findPerpend(Space.Vector u){
           SpaceP.Vector v = new SpaceP.Vector();
           double a, b, c;
           if (u.x(0)!= 0){
                b = (2*Simulation.random.nextDouble() - 1.0);
                c = (2*Simulation.random.nextDouble() - 1.0);
                a =- (u.x(1)*b+ u.x(2)*c)/u.x(0);
           }
           else if ( u.x(1)!=0){
                a =(2*Simulation.random.nextDouble() -1.0);
                b =(2*Simulation.random.nextDouble() -1.0);
                c =-b*u.x(1)/u.x(2);
             }
            else {
                a =(2*Simulation.random.nextDouble() -1.0);
                b =(2*Simulation.random.nextDouble() -1.0);
                c =(2*Simulation.random.nextDouble() -1.0);
            }
            v.setX(0, a);
            v.setX(1, b);
            v.setX(2, c);
            v.normalize();
            return v;
            
        }
        
        public Space.Vector findPerpend(Space.Vector u, Space.Vector v){
            return u.cross(v);
        }
        
        
        
        
        private final void updateRotationMatrix() {
          //x-rot
          double theta = angle[0]*(Math.PI / 180);
          double ct = Math.cos(theta);
          double st = Math.sin(theta);
          double Nyx = (double) (A[1][0] * ct + A[2][0] * st);
          double Nyy = (double) (A[1][1] * ct + A[2][1] * st);
          double Nyz = (double) (A[1][2] * ct + A[2][2] * st);
          double Nzx = (double) (A[2][0] * ct - A[1][0] * st);
          double Nzy = (double) (A[2][1] * ct - A[1][1] * st);
          double Nzz = (double) (A[2][2] * ct - A[1][2] * st);
          A[1][0] = Nyx;
          A[1][1] = Nyy;
          A[1][2] = Nyz;
          A[2][0] = Nzx;
          A[2][1] = Nzy;
          A[2][2] = Nzz;
          //y-rot
          theta = angle[1]*(Math.PI / 180);
          ct = Math.cos(theta);
          st = Math.sin(theta);
          double Nxx = (double) (A[0][0] * ct + A[2][0] * st);
          double Nxy = (double) (A[0][1] * ct + A[2][1] * st);
          double Nxz = (double) (A[0][2] * ct + A[2][2] * st);
          Nzx = (double) (A[2][0] * ct - A[0][0] * st);
          Nzy = (double) (A[2][1] * ct - A[0][1] * st);
          Nzz = (double) (A[2][2] * ct - A[0][2] * st);
          A[0][0] = Nxx;
          A[0][1] = Nxy;
          A[0][2] = Nxz;
          A[2][0] = Nzx;
          A[2][1] = Nzy;
          A[2][2] = Nzz;
          //z-rot
          theta = angle[2]*(Math.PI / 180);
          ct = Math.cos(theta);
          st = Math.sin(theta);
          Nyx = (double) (A[1][0] * ct + A[0][0] * st);
          Nyy = (double) (A[1][1] * ct + A[0][1] * st);
          Nyz = (double) (A[1][2] * ct + A[0][2] * st);
          Nxx = (double) (A[0][0] * ct - A[1][0] * st);
          Nxy = (double) (A[0][1] * ct - A[1][1] * st);
          Nxz = (double) (A[0][2] * ct - A[1][2] * st);
          A[1][0] = Nyx;
          A[1][1] = Nyy;
          A[1][2] = Nyz;
          A[0][0] = Nxx;
          A[0][1] = Nxy;
          A[0][2] = Nxz;
          bodyFrame[0].E(A[0]);
          bodyFrame[1].E(A[1]);
          bodyFrame[2].E(A[2]);
          needToUpdateA = false;
         }
      //   public double[][] rotationMatrix() {return A;}
         public void convertToBodyFrame(Vector v) {
             if(needToUpdateA) updateRotationMatrix();
            v.X[0] = A[0][0]*v.X[0] + A[0][1]*v.X[1] + A[0][2]*v.X[2];
            v.X[1] = A[1][0]*v.X[0] + A[1][1]*v.X[1] + A[1][2]*v.X[2];
            v.X[2] = A[2][0]*v.X[0] + A[2][1]*v.X[1] + A[2][2]*v.X[2];
         }
         public void convertToSpaceFrame(Vector v) {
             if(needToUpdateA) updateRotationMatrix();
            v.X[0] = A[0][0]*v.X[0] + A[1][0]*v.X[1] + A[2][0]*v.X[2];
            v.X[1] = A[0][1]*v.X[0] + A[1][1]*v.X[1] + A[2][1]*v.X[2];
            v.X[2] = A[0][2]*v.X[0] +A[1][2]*v.X[1] + A[2][2]*v.X[2];
         }
         public void convertToBodyFrame(Space.Vector v) {convertToBodyFrame((Vector)v);}
         public void convertToSpaceFrame(Space.Vector v) {convertToSpaceFrame((Vector)v);}
    }

    
     //protected static class BoundaryPeriodicSquare extends Boundary implements Space.Boundary.Periodic  {
    public static class BoundaryPeriodicSquare extends Boundary implements Space.Boundary.Periodic  {
        private final Vector temp = new Vector();
        private double d02x,_d02x,d02y,_d02y,d02z,_d02z;
        private double dmx,dmy,dmz;
//        private static Space.Tensor zilch = new Tensor();
        public BoundaryPeriodicSquare() {this(Default.BOX_SIZE,Default.BOX_SIZE,Default.BOX_SIZE);}
        public BoundaryPeriodicSquare(Phase p) {this(p,Default.BOX_SIZE,Default.BOX_SIZE,Default.BOX_SIZE);}
        public BoundaryPeriodicSquare(Phase p, double lx, double ly, double lz) {super(p);dimensions.X[0]=lx; dimensions.X[1]=ly; dimensions.X[2]=lz;}
        public BoundaryPeriodicSquare(double lx, double ly, double lz) {
            super();
            dimensions.X[0]=lx; dimensions.X[1]=ly; dimensions.X[2]=lz;
            dmx=lx;  dmy=ly;  dmz=lz;
            d02x=0.5*dmx;  _d02x=-d02x;
            d02y=0.5*dmy;  _d02y=-d02y;
            d02z=0.5*dmz;  _d02z=-d02z;
        }
        public Space.Boundary.Type type() {return Space3D.Boundary.PERIODIC_SQUARE;}
        public final Vector dimensions = new Vector();
        public final Space.Vector dimensions() {return dimensions;}
        public void setDimensions(Space.Vector v) {dimensions.E(v);}
        
        public Space.Vector randomPosition() {
            temp.X[0] = dimensions.X[0]*Simulation.random.nextDouble();
            temp.X[1] = dimensions.X[1]*Simulation.random.nextDouble();
            temp.X[2] = dimensions.X[2]*Simulation.random.nextDouble();
            return temp;
        }
        
        public void nearestImage(Space.Vector dr) {nearestImage((Vector) dr);}
        public void nearestImage(Vector dr) {
            //System.out.println(" Inside SpaceP.nearestImage");
            /*
            dr.x -= dimensions.x*((dr.x > 0.0) ? Math.floor(dr.x/dimensions.x + 0.5) : Math.ceil(dr.x/dimensions.x - 0.5));
            dr.y -= dimensions.y*((dr.y > 0.0) ? Math.floor(dr.y/dimensions.y + 0.5) : Math.ceil(dr.y/dimensions.y - 0.5));
            dr.z -= dimensions.z*((dr.z > 0.0) ? Math.floor(dr.z/dimensions.z + 0.5) : Math.ceil(dr.z/dimensions.z - 0.5));
            
            */
            while(dr.X[0] > d02x)dr.X[0]-= dmx;
            while(dr.X[0] <_d02x)dr.X[0]+= dmx;
            while(dr.X[1] > d02y)dr.X[1]-=dmy;
            while(dr.X[1] <_d02y)dr.X[1]+=dmy;
            while(dr.X[2] > d02z)dr.X[2]-=dmz;
            while(dr.X[2] <_d02z)dr.X[2]+=dmz;
            
        }
        
        public void nearestImage(double[] dr) {
            while(dr[0] > d02x)dr[0]-= dmx;
            while(dr[0] <_d02x)dr[0]+= dmx;
            while(dr[1] > d02y)dr[1]-=dmy;
            while(dr[1] <_d02y)dr[1]+=dmy;
            while(dr[2] > d02z)dr[2]-=dmz;
            while(dr[2] <_d02z)dr[2]+=dmz;
        }
        
        public void nearestImage(Space3D.Vector dr) {
            throw new RuntimeException("SpaceP.CoordinatePair.nearestImage(Space3D.Vector) not implemented");
            /*  broken because xyz are private in Space3D.Vector
            while(dr.x > d02x)dr.x -= dmx;
            while(dr.x <_d02x)dr.x += dmx;
            while(dr.y > d02y)dr.y -= dmy;
            while(dr.y <_d02y)dr.y += dmy;
            while(dr.z > d02z)dr.z -= dmz;
            while(dr.z <_d02z)dr.z += dmz;*/
      //      System.out.println("nearestImage in SpaceP.BoundaryPeriodicSquare");
        }
        
       // public void centralImage(Coordinate c) {centralImage(c.r);}
       
        public boolean centralImage(Coordinate c) {            
        System.out.println(" I am here in centralImage");
            Vector r = (Vector)c.position();
            temp.X[0] = (r.X[0] > dimensions.X[0]) ? -dimensions.X[0] : (r.X[0] < 0.0) ? dimensions.X[0] : 0.0;
            temp.X[1] = (r.X[1] > dimensions.X[1]) ? -dimensions.X[1] : (r.X[1] < 0.0) ? dimensions.X[1] : 0.0;
            temp.X[2] =  (r.X[2] >dimensions.X[2]) ? -dimensions.X[2] : (r.X[2] < 0.0) ? dimensions.X[2] : 0.0;
            if(temp.X[0] != 0.0 || temp.X[1]!= 0.0 || temp.X[2]!=0.0) c.translateBy(temp);
            return false;//this is not correct in general
        }

        
        
        public boolean centralImage(Space.Vector r) {return centralImage((Vector) r);}
        public boolean centralImage(Vector r) {
            
            while(r.X[0] > dimensions.X[0]) r.X[0] -= dimensions.X[0];
            while(r.X[0] < 0.0)          r.X[0] += dimensions.X[0];
            while(r.X[1] > dimensions.X[1]) r.X[1] -= dimensions.X[1];
            while(r.X[1] < 0.0)          r.X[1] += dimensions.X[1];
            while(r.X[2] > dimensions.X[2]) r.X[2] -= dimensions.X[2];
            while(r.X[2] < 0.0)          r.X[2] += dimensions.X[2];
            return false;//this is not correct in general
            //System.out.println(" here I am in central Image");
    //        r.X[0] -= dimensions.X[0]* ((r.X[0]>0) ? Math.floor(r.X[0]/dimensions.X[0]) : Math.ceil(r.X[0]/dimensions.X[0] - 1.0));
    //        r.X[1] -= dimensions.X[1]*((r.X[1]>0) ? Math.floor(r.X[1]/dimensions.X[1]) : Math.ceil(r.X[1]/dimensions.X[1] - 1.0));
    //        r.X[2] -= dimensions.X[2] *((r.X[2]>0) ? Math.floor(r.X[2]/dimensions.X[2]) : Math.ceil(r.X[2]/dimensions.X[2] - 1.0));
            
        }
        public void inflate(double scale) {dimensions.TE(scale);}
        public void inflate(Space.Vector scale) {dimensions.TE(scale);}
        public double volume() {return dimensions.X[0]*dimensions.X[1]*dimensions.X[2];}
                
        /**
         * imageOrigins and getOverFlowShifts are both probably incorrect, if they are
         * even completed.  They should definitely be checked before being implemented.
         */
        
        int shellFormula, nImages, i, j, k, m;
        double[][] origins;
        public double[][] imageOrigins(int nShells) {
            /*
            shellFormula = (2 * nShells) + 1;
            nImages = shellFormula*shellFormula*shellFormula-1;
            origins = new double[nImages][D];
            for (k=0,i=-nShells; i<=nShells; i++) {
                for (j=-nShells; j<=nShells; j++) {
                    for (m=-nShells; m<=nShells; m++) {
                        if ((i==0 && j==0) && m==0 ) {continue;}
                        origins[k][0] = i*dimensions.x;
                        origins[k][1] = j*dimensions.y;
                        origins[k][2] = m*dimensions.z;
                        k++;
                    }
                }
            }
            */
           
           System.out.println("Inside SpaceP.imageOrigins");
           return null;
            //return origins;
        }
        
        //getOverflowShifts ends up being called by the display routines quite often
        //so, in the interest of speed, i moved these outside of the function;
        int shiftX, shiftY, shiftZ;
        Vector r;
        public float[][] getOverflowShifts(Space.Vector rr, double distance) {
         
            System.out.println(" inside SpaceP.getOverflo..");
            return null;
        }
        
      
    }
    
    

    

    

    

    

    

    

    

    

    

}

