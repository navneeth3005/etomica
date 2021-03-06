/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial;

import etomica.models.water.SpeciesWater3P;
import etomica.api.ISpecies;
import etomica.space.ISpace;


/**
 * SpeciesFactory that makes SpeciesWater
 */
public class SpeciesFactoryWater3P implements SpeciesFactory, java.io.Serializable {
    public ISpecies makeSpecies(ISpace space) {
        return new SpeciesWater3P(space);
    }
}
