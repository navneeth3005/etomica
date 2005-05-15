package etomica.virial.cluster;

/**
 * Holds information about a cluster diagram, including the bonds, root points,
 * and score.
 * @author andrew
 */
public class ClusterDiagram {
    public final int mNumBody;
    public final int[][] mConnections;
    public int mNumIdenticalPermutations;
    private int mNumConnections;
    private final int mNumRootPoints;
    private final int[] mRootPoints;
    private final boolean[] mIsRootPoint;
    public int mReeHooverFactor;

    /**
     * Constructs a cluster having numBody points and numRootPoints root points.
     */
    public ClusterDiagram(int numBody, int numRootPoints) {
        mNumBody = numBody;
        mConnections = new int[mNumBody][mNumBody];
        makeFullStar();
        mIsRootPoint = new boolean[mNumBody];
        mNumRootPoints = numRootPoints;
        mRootPoints = new int[mNumRootPoints];
        for (int i=0; i<mNumRootPoints; i++) {
            mRootPoints[i] = i;
            mIsRootPoint[i] = true;
        }
        mReeHooverFactor = 1;
    }

    /**
     * Creates a copy of the given cluster 
     */
    public ClusterDiagram(ClusterDiagram cluster) {
        this(cluster.mNumBody, cluster.mNumRootPoints);
        cluster.copyTo(this);
    }

    public void copyTo(ClusterDiagram destCluster) {
        // number of root points must already be equal
        for (int i = 0; i < mNumBody; i++) {
            System.arraycopy(mConnections[i], 0, destCluster.mConnections[i], 0,
                    mNumBody);
        }
        destCluster.mNumConnections = mNumConnections;
        destCluster.mNumIdenticalPermutations = mNumIdenticalPermutations;
        destCluster.mReeHooverFactor = mReeHooverFactor;
    }
        
    
    public int getNumRootPoints() {
        return mNumRootPoints;
    }
    
    /**
     * returns true if the given point is a root point
     */
    public boolean isRootPoint(int i) {
        return mIsRootPoint[i];
    }
    
    /**
     * returns the number of active connections in the cluster.  
     */
    public int getNumConnections() {
        return mNumConnections;
    }
    
    /**
     * Turns this cluster into a full star -- each point in the cluster is
     * connected to every other point in the cluster.
     */
    public void makeFullStar() {
        int i, j, k;
        for (i = 0; i < mNumBody; i++) {
            k = 0;
            for (j = 0; j < mNumBody; j++) {
                if (i != j) {
                    mConnections[i][k] = j;
                    k++;
                }
            }
            mConnections[i][mNumBody - 1] = -1;
        }
        mNumConnections = mNumBody*(mNumBody-1)/2;
    }

    /**
     * Swaps point1 and point2.  Point1 points to everything point2 pointed
     * to and vica-versa.  Also, everything that pointed to point1 points to 
     * point2 and vica-versa.  Don't swap root and non-root points.
     */
    public void swap(int point1, int point2) {
        // first have things pointing at point1 to point at point2
        // and vica-versa
        for (int i = 0; i < mNumBody; i++) {
            boolean foundPoint1, foundPoint2;
            foundPoint1 = false;
            foundPoint2 = false;
            for (int j = 0; mConnections[i][j] != -1; j++) {
                if (!foundPoint1 && mConnections[i][j] == point1) {
                    mConnections[i][j] = point2;
                    foundPoint1 = true;
                    if (foundPoint2) {
                        break;
                    }
                }
                else if (!foundPoint2 && mConnections[i][j] == point2) {
                    mConnections[i][j] = point1;
                    foundPoint2 = true;
                    if (foundPoint1) {
                        break;
                    }
                }
            }
        }
        // actually swap the list of connections for point1 and point2
        int[] tmpConnections = new int[mNumBody - 1];
        System.arraycopy(mConnections[point1], 0, tmpConnections, 0, mNumBody - 1);
        System.arraycopy(mConnections[point2], 0, mConnections[point1], 0, mNumBody - 1);
        System.arraycopy(tmpConnections, 0, mConnections[point2], 0, mNumBody - 1);
    }

    /**
     * Deletes a connection from one node1 to node2.  The method
     * should be called again to delete the connection from node2 to
     * node1.
     */
    public boolean deleteConnection(int node1, int node2) {
        boolean success = false;
        int[] connections1 = mConnections[node1];
        for (int i = 0; ; i++) {
            int connection = connections1[i];
            if (connection == -1) {
                // ran out of connections. we're done
                break;
            }
            // did we find it?
            success = success || connection == node2;
            if (success) {
                // shift
                mConnections[node1][i] = mConnections[node1][i + 1];
            }
        }
        if (success && node1<node2) {
            mNumConnections--;
        }
        return success;
    }

    /**
     * Adds a connection between the node1 and node2.
     * The method should be called again to create the connection
     * from the node2 to node1.
     */
    public void addConnection(int node1, int node2) {
        int[] connections1 = mConnections[node1];
        for (int i = 0;; i++) {
            if (connections1[i] == node2) {
                throw new RuntimeException("attempted to add already-existing connection");
            }
            if (connections1[i] == -1) {
                connections1[i] = node2;
                if (node1<node2) {
                    mNumConnections++;
                }
                return;
            }
        }
    }

    public String toString() {
        String out = "";
        int i, j;
        for (i = 0; i < mNumBody; i++) {
            for (j = 0; j < mNumBody - 1 && mConnections[i][j] != -1; j++) {
                if (i < mConnections[i][j]) {
                    out += i + " " + mConnections[i][j] + "\t";
                }
            }
        }
        return out;
    }

    /**
     * Determines if the cluster's current configuration has a score greater 
     * than the previously stored score.  The current score is computed only 
     * enough to determine if it is less than the current cluster.
     */
    public boolean scoreGreaterThan(int[] compareScore) {
        for (int i = 1; i < mNumBody / 2 + 1; i++) {
            int myScore = 0;
            for (int thisNode = 0; thisNode < mNumBody; thisNode++) {
                int thisScore = 1 << (mNumBody - 1 - thisNode);
                int target = thisNode + i;
                if (target > mNumBody - 1) target -= mNumBody;
                for (int j = 0; mConnections[thisNode][j] != -1; j++) {
                    if (mConnections[thisNode][j] == target) {
                        myScore += thisScore;
                        if (myScore > compareScore[i]) {
                            // the cluster has a lower score at level i
                            return true;
                        }
                        break;
                    }
                }
            }
            if (myScore < compareScore[i]) {
                return false;
            }
        }
        mNumIdenticalPermutations++;
        return false;
    }

    /**
     * compute a score for the cluster. The score is returned as an array of
     * integers. The first element is the score for connections between i and
     * i+1 and the second element is the score for connections between i and i+2
     * (the zeroth element is always 0). The score puts more importance on early
     * points (having a connection for point 0 will score one higher than having
     * the same connection for every other point).  The score is returned in the 
     * array passed in.
     */
    public void calcScore(int[] score) {
        for (int thisNode = 1; thisNode < 1 + mNumBody/2; thisNode++) {
            score[thisNode] = 0;
        }
        for (int thisNode = 0; thisNode < mNumBody; thisNode++) {
            int thisScore = 1 << (mNumBody - 1 - thisNode);
            for (int i = 1; i < mNumBody / 2 + 1; i++) {
                int target = thisNode + i;
                if (target > mNumBody - 1) target -= mNumBody;
                for (int j = 0; mConnections[thisNode][j] != -1; j++) {
                    if (mConnections[thisNode][j] == target) {
                        score[i] += thisScore;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Resets the cluster to being fully connected except that root points
     * are not connected to each other.  If the cluster is being generated
     * from a ClusterGenerator, ClusterGenerator.reset should be called instead
     * of this method.
     */
    public void reset() {
        makeFullStar();
        mNumIdenticalPermutations = 1;
        for (int i=0; i<mNumRootPoints; i++) {
            for (int j=0; j<i; j++) {
                deleteConnection(i,j);
                deleteConnection(j,i);
            }
        }
    }
    
    /**
     * Sorts list of connections for each node in the diagram such that
     * mConnections[i][j] < mConnections[i][k] for k > j.
     */
    public void sort() {
        for (int i=0; i<mNumBody; i++) {
            int[] iConnections = mConnections[i]; 
            for (int j=0; j<mNumBody-1; j++) {
                if (iConnections[j] == -1) break;
                for (int k=j+1; k<mNumBody; k++) {
                    if (iConnections[k] == -1) break;
                    if (iConnections[j] > iConnections[k]) {
                        int t = iConnections[j];
                        iConnections[j] = iConnections[k];
                        iConnections[k] = t;
                    }
                }
            }
        }
    }

}