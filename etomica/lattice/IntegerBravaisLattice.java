package etomica.lattice;
import java.util.Random;

/**
 * Arbitrary-dimension Bravais Lattice having no primitive vectors, but only integer indices.  
 * Lattice is constructed recursively, so that at top level (dimension D) the lattice is
 * represented by an array of lattices each of dimension D-1.  This continues on down to
 * the zero-dimensional lattice, which contains a single Site object.
 * Each dimension of the lattice may be of different length (i.e., the lattice can be rectangular 
 * and need not be strictly cubic).<br>
 * 
 * @author David Kofke
 */
public class IntegerBravaisLattice implements AbstractLattice {

    private SiteIterator iterator;
    private int nRows, siteCount;
    public final int D;                                //dimension of lattice
    public static Random random;  //random-number generator for selecting a random site
    private int dimensions[];
    static {random = new Random();}
    
    IntegerBravaisLattice[] rows;         //array of D-1 dimensional lattices (generically called a "row" here)
    private IntegerBravaisLattice parentLattice;  //D+1 dimensional lattice in which this lattice is a row (null for highest-level lattice)
    private int index;                     //index of row array for this lattice in parentLattice 
    
    /**
     * Constructor for top-level lattice, with site coordinates generated by simple default IndexedCoordinate.Factory object.
     */
    public IntegerBravaisLattice(int[] dim, SiteFactory siteFactory) {
        this(null, 0, dim.length, dim, siteFactory, 
             new IndexedCoordinate.Factory() {public IndexedCoordinate makeCoordinate(int[] i) {return new Coordinate(i);}});
        setupNeighbors(rows);
        dimensions = (int[])dim.clone();
    }
    /**
     * Constructor for top-level lattice, with site coordinates generated by given IndexedCoordinate.Factory object.
     */
    public IntegerBravaisLattice(int[] dim, 
                                 SiteFactory siteFactory, 
                                 IndexedCoordinate.Factory coordFactory) { 
        this(null, 0, dim.length, dim, siteFactory, coordFactory);
        setupNeighbors(rows);
        dimensions = (int[])dim.clone();
    }
    /**
     * Constructor for lowest-level "zero-dimensional" lattice consisting of one site.
     */
    private IntegerBravaisLattice(IntegerBravaisLattice parent, 
                                  int idx, 
                                  SiteFactory siteFactory, 
                                  IndexedCoordinate.Factory coordFactory) { 
        parentLattice = parent;
        index = idx;
        D = 0;
        IntegerBravaisLattice top = topLattice();
        int[] coordIndex = new int[top.D()];
        IntegerBravaisLattice lattice = this;
        for(int i=0; i<coordIndex.length; i++) {
            coordIndex[i] = lattice.index();
            lattice = lattice.parentLattice();
        }
        iterator = new SingletIterator(siteFactory.makeSite(top, new SiteIterator.Neighbor(), coordFactory.makeCoordinate(coordIndex)));
        iterator.first().neighborIterator().setSite(iterator.first());
        siteCount = 1;
        nRows = 0;
    }
    /**
     * Sub-lattice constructor.
     */
    private IntegerBravaisLattice(IntegerBravaisLattice parent, 
                                  int idx, 
                                  int D, 
                                  int[] dim, 
                                  SiteFactory siteFactory, 
                                  IndexedCoordinate.Factory coordFactory) {
        parentLattice = parent;
        index = idx;
        this.D = D;
        int d = D-1;
        nRows = dim[d];
        rows = new IntegerBravaisLattice[nRows];
        for(int i=0; i<nRows; i++) {
            rows[i] = (d>0) ? new IntegerBravaisLattice(this, i, d, dim, siteFactory, coordFactory) : //sub-lattice constructory
                              new IntegerBravaisLattice(this, i,         siteFactory, coordFactory);  //zero-D lattice constructor
        }
        iterator = new LatticeIterator(rows); //iterator of all sites in this sublattice
        siteCount = nRows*rows[0].siteCount();
    }
    
    /**
     * Array giving the number of sites in each dimension of the lattice
     */
    public int[] dimensions() {return dimensions;}
    
    private void setupNeighbors(IntegerBravaisLattice[] rows) {
        for(int i=0; i<rows.length; i++) {
            SiteIterator pIterator = (i==0) ? rows[0].iterator() : rows[i-1].iterator();
            SiteIterator nIterator = (i==0) ? rows[rows.length-1].iterator() : rows[i].iterator();
            pIterator.reset();
            nIterator.reset();
            while(pIterator.hasNext()) {
                Site pSite = pIterator.next(); 
                Site nSite = nIterator.next();
                pSite.neighborIterator().addUp(nSite);
                nSite.neighborIterator().addDown(pSite);
            }
        }
        if(rows[0].D() == 0) return;
        for(int i=0; i<rows.length; i++) {setupNeighbors(rows[i].rows);}
    }
    
    IntegerBravaisLattice topLattice() {
        return (parentLattice == null) ? this : parentLattice.topLattice();
    }
        
    //Lattice methods
    public int D() {return D;}
    public final int siteCount() {return siteCount;}  
    public int coordinationNumber() {return 2*D;}
    public Site site(AbstractLattice.Coordinate coord) {return site((IndexedCoordinate)coord);}
    public Site site(IndexedCoordinate coord) {
        return site(coord.index());
    }
    public Site site(int[] idx) {
        //exceptions needed to check type and size of index
        return (D==0) ? iterator.first() : rows[idx[D-1]].site(idx);
    }
        
    /**
     * Returns a random site in the lattice
     * Iteratively chooses a row at random and calls randomSite for that row
     */
    public Site randomSite() {
        int i = (int)Math.floor(nRows*random.nextDouble());
        return (D==0) ? iterator.first() : rows[i].randomSite();}
        
    public SiteIterator iterator() {return iterator;}     //iterator for all sites in lattice
    
    //LatticeBravais methods
    IntegerBravaisLattice parentLattice() {return parentLattice;}
    int index() {return index;}
    
    /**
     * Interface for a coordinate that can specify a site in an IntegerBravaisLattice.
     * Such a coordinate must have an index method that returns an array of integers, which
     * specify the indices of the site in each lattice dimension.
     */
    public interface IndexedCoordinate extends AbstractLattice.Coordinate {
        public int[] index();
        
        interface Factory { public IndexedCoordinate makeCoordinate(int[] index);}
    }
    
    public static class Coordinate implements IndexedCoordinate {
        //first value (index[0]) indicates the site in the row
        //second value indicates which row in the plane, third value which plane in the cube, etc.
        private final int[] index;
        public Coordinate(int[] i) {index = i;}
        public int[] index() {return index;}
        public String toString() {
            String value = "(";
            for(int i=0; i<index.length; i++) value += index[i];
            value += ")";
            return value;
        }
    }
    
    /**
     * General iterator of sites on LatticeBravais.
     * Recursive formulation.  SingletIterator is used for zero-D lattices.
     */
    private static final class LatticeIterator implements SiteIterator {  //might instead do this by creating a big array of all sites and loop through it
        private boolean hasNext;
        private int iRow;
        private SiteIterator current;
        private final IntegerBravaisLattice[] rows;
        private final int nRows;
        public LatticeIterator(IntegerBravaisLattice[] r) {rows = r; nRows = r.length; reset();}   //constructor
        public Site first() {return rows[0].iterator().first();}
        public boolean hasNext() {return hasNext;}
        public void reset() {
            iRow = 0;
            current = rows[0].iterator();
            current.reset();
            hasNext = current.hasNext();
        }
        public Site next() {
            Site nextSite = current.next();
            if(!current.hasNext()) {  //no more in current row
                iRow++;
                if(iRow == nRows) {hasNext = false;} //that was the last row
                else {
                    current = rows[iRow].iterator();
                    current.reset();
                }
            }
            return nextSite;
        }
        public int size() {
            int count = 1;
            for(int i=0; i<nRows; i++) {count *= rows[i].iterator().size();}
            return count;
        }
        public void allSites(SiteAction act) {
            for(int i=0; i<nRows; i++) {rows[i].iterator().allSites(act);}
        }
    }
        
    //Iterator for a lattice that contains only one site (zero-D lattice)
    private static final class SingletIterator implements SiteIterator {
        public final Site site;
        private final Site.Linker link;
        private boolean hasNext;
        public SingletIterator(Site s) {
            site = s; 
            hasNext = true; 
            link = new Site.Linker(s, null);
        }
        public boolean hasNext() {return hasNext;}
        public Site first() {return site;}
        public void reset() {hasNext = true;}
        public Site next() {hasNext = false; return site;}
        public void allSites(SiteAction act) {act.actionPerformed(site);}
        public int size() {return 1;}
    }
    
    /**
     * Main method to demonstrate use of IntegerBravaisLattice and to aid debugging
     */
    public static void main(String[] args) {
        System.out.println("main method for IntegerBravaisLattice");
        System.out.println();
        IntegerBravaisLattice lattice = new IntegerBravaisLattice(new int[] {3,2,4}, new Site.Factory());
        
        System.out.println("Total number of sites: "+lattice.siteCount());
        System.out.println();

        System.out.println("Coordinate printout");
        SiteIterator iterator = lattice.iterator();
        iterator.reset();
        while(iterator.hasNext()) {  //print out coordinates of each site
            System.out.print(iterator.next().toString()+" ");
        }
        System.out.println();
        System.out.println();
        
        SiteAction printSites = new SiteAction() {public void actionPerformed(Site s) {System.out.print(s.toString()+" ");}};
        System.out.println("Same, using allSites method");
        iterator.allSites(printSites);
        System.out.println();
        System.out.println();
        
        System.out.println("Both again, using a duplicate List iterator");
        SiteIterator.List copy = new SiteIterator.List(iterator);
        copy.reset();
        while(copy.hasNext()) {System.out.print(copy.next().toString()+" ");}
        System.out.println();
        System.out.println();
        copy.allSites(printSites);
        System.out.println();
        System.out.println();
        
        System.out.println("Now with a List cursor iterator");
        SiteIterator.Cursor cursor = copy.makeCursor();
        cursor.reset();
        while(cursor.hasNext()) {System.out.print(cursor.next().toString()+" ");}
        System.out.println();
        System.out.println();

        System.out.print("Accessing site (2,1,0): ");
        Site testSite = lattice.site(new Coordinate(new int[] {2,1,0}));
        System.out.println(testSite.toString());
        System.out.println();
        
        System.out.println("Sites up-neighbor to this site:");
        iterator = testSite.neighborIterator();
        ((SiteIterator.Neighbor)iterator).resetUp();
        while(iterator.hasNext()) {  //print out coordinates of each site
            System.out.print(iterator.next().toString()+" ");
        }
        System.out.println();
        System.out.println();
        
        System.out.println("Sites down-neighbor to this site:");
        iterator = testSite.neighborIterator();
        ((SiteIterator.Neighbor)iterator).resetDown();
        while(iterator.hasNext()) {  //print out coordinates of each site
            System.out.print(iterator.next().toString()+" ");
        }
        System.out.println();
        System.out.println();

        System.out.println("All neighbor sites, using a cursor:");
        SiteIterator.Neighbor.Cursor nbrCursor = testSite.neighborIterator().makeCursor();
        nbrCursor.reset();
        while(nbrCursor.hasNext()) {  //print out coordinates of each site
            System.out.print(nbrCursor.next().toString()+" ");
        }
        System.out.println();
        System.out.println();
        System.out.print("A randomly selected site: ");
        System.out.println(lattice.randomSite().toString());
    }
}