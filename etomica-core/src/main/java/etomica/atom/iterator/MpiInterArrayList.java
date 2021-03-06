/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.atom.iterator;

import etomica.api.IMoleculeList;
import etomica.atom.MoleculeArrayList;
import etomica.atom.MoleculePair;

/**
 * Returns all pairs formed from two different untabbed lists of molecules.
 * Incorrect behavior will result if both lists refer to the same instance.
 * 
 * @author Tai Boon Tan
 *
 */
public class MpiInterArrayList implements MoleculesetIterator, java.io.Serializable {

    /**
     * Construct iterator with an empty lists. No iterates will be given until
     * non-empty lists are specified via setList.
     */
    public MpiInterArrayList() {
        this(new MoleculeArrayList(), new MoleculeArrayList());
    }

    /**
     * Constructs iterator to return pairs from the given lists. Requires
     * reset() before first use.
     * 
     * @throws IllegalArgumentException
     *             if both lists refer to the same instance
     */
    public MpiInterArrayList(IMoleculeList outerList, IMoleculeList innerList) {
        if (outerList == innerList) {
            throw new IllegalArgumentException(
                    "MpiInterList will not work if both iterators are the same instance");
        }
        setOuterList(outerList);
        setInnerList(innerList);
    }

    /**
     * Sets iterator in condition to begin iteration.
     * 
     * @throws IllegalStateException
     *             if outer and inner lists have been set to the same instance
     */
    public void reset() {
        if (outerList == innerList) {
            throw new IllegalStateException(
                    "MpiInterList will not work correctly if inner and outer lists are the same instance");
        }
        outerIndex = 0;
        if (outerList.getMoleculeCount() == 0) {
            innerIndex = innerList.getMoleculeCount() - 1;
            return;
        }
        innerIndex = -1;
        molecules.atom0 = outerList.getMolecule(outerIndex);
    }

    /**
     * Sets iterator such that hasNext is false.
     */
    public void unset() {
        if (outerList != null) {
            outerIndex = outerList.getMoleculeCount() - 1;
        }
        if (innerList != null) {
            innerIndex = innerList.getMoleculeCount() - 1;
        }
    }

    /**
     * Returns the next iterate pair. Returns null if there are no more
     * iterates.
     */
    public IMoleculeList next() {
        if (innerIndex > innerList.getMoleculeCount() - 2) {
            if (outerIndex > outerList.getMoleculeCount() - 2 || innerList.getMoleculeCount() == 0) {
                return null;
            }
            outerIndex++;
            molecules.atom0 = outerList.getMolecule(outerIndex);
            innerIndex = -1;
        }
        innerIndex++;
        molecules.atom1 = innerList.getMolecule(innerIndex);
        return molecules;
    }

    /**
     * Returns the number of iterates, which is list.size*(list.size-1)/2
     */
    public int size() {
        return outerList.getMoleculeCount() * innerList.getMoleculeCount();
    }

    /**
     * Returns 2, indicating that this is a pair iterator
     */
    public int nBody() {
        return 2;
    }

    /**
     * Sets the list that will be used to generate the pairs. Must call reset()
     * before beginning iteration.
     * 
     * @param newList
     *            the new molecule list for iteration
     */
    public void setOuterList(IMoleculeList newList) {
        this.outerList = newList;
        unset();
    }

    /**
     * Sets the list that will be used to generate the pairs. Must call reset()
     * before beginning iteration.
     * 
     * @param newList
     *            the new molecule list for iteration
     */
    public void setInnerList(IMoleculeList newList) {
        this.innerList = newList;
        unset();
    }

    /**
     * Returns the outer list used to generate the pairs.
     */
    public IMoleculeList getOuterList() {
        return outerList;
    }

    /**
     * Returns the inner list used to generate the pairs.
     */
    public IMoleculeList getInnerList() {
        return innerList;
    }

    private static final long serialVersionUID = 1L;
    private IMoleculeList outerList, innerList;
    private int outerIndex, innerIndex;
    private final MoleculePair molecules = new MoleculePair();
}
