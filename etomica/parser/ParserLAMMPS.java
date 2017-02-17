package etomica.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import etomica.api.IAtomType;
import etomica.api.IPotentialAtomic;
import etomica.api.ISpecies;
import etomica.api.IVectorMutable;
import etomica.atom.AtomTypeLeaf;
import etomica.chem.elements.ElementSimple;
import etomica.config.ConformationGeneric;
import etomica.potential.P2Harmonic;
import etomica.potential.P2LennardJones;
import etomica.potential.P2SoftSphericalTruncatedForceShifted;
import etomica.potential.P2SoftSphericalTruncatedShifted;
import etomica.potential.P2SoftSphericalTruncatedSwitched;
import etomica.potential.P2SoftTruncated;
import etomica.potential.P3BondAngle;
import etomica.potential.P4BondTorsion;
import etomica.potential.Potential2SoftSpherical;
import etomica.potential.PotentialGroup;
import etomica.space.ISpace;
import etomica.space3d.Space3D;
import etomica.species.SpeciesSpheresHetero;

public class ParserLAMMPS {

	public static Stuff makeStuffFromLines(List<String> lines, Options opts) {
        String heading = null;
        IAtomType[] atomTypes = null;
        P2Harmonic[] p2Bonds = null;
        P3BondAngle[] p3Bonds = null;
        P4BondTorsion[] p4Bonds = null;
        int[] atomCounts = null;
        int[] atomTypeId = null;
        double[] charges = null;
        IVectorMutable[] coords = null;
        List<int[]>[] bondedPairs = null;
        List<int[]>[] bondedTriplets = null;
        List<int[]>[] bondedQuads = null;
        char symbol = 'A';
        List<IPotentialAtomic> atomicPotentials = new ArrayList<IPotentialAtomic>();
        
        for (String line : lines) {
        	if (line.length() == 0) continue;
        	String[] fields = line.split("[\\w]*");
        	if (fields[0].matches("^[0-9-]")) {
    			if (line.matches("^[0-9]* atom")) {
    				int n = Integer.parseInt(fields[0]);
    				atomTypeId = new int[n];
    				charges = new double[n];
    				coords = new IVectorMutable[n];
    				continue;
    			}
    			if (line.matches("^[0-9]* atom types")) {
    				atomTypes = new IAtomType[Integer.parseInt(fields[0])];
    				atomCounts = new int[atomTypes.length];
    				continue;
    			}
    			if (line.matches("^[0-9]* bond types")) {
    				p2Bonds = new P2Harmonic[Integer.parseInt(fields[0])];
    				bondedPairs = new ArrayList[p2Bonds.length];
    				for (int i=0; i<p2Bonds.length; i++) {
    					bondedPairs[i] = new ArrayList<int[]>();
    				}
    				continue;
    			}
    			if (line.matches("^[0-9]* angle types")) {
    				p3Bonds = new P3BondAngle[Integer.parseInt(fields[0])];
    				bondedTriplets = new ArrayList[p3Bonds.length];
    				for (int i=0; i<p3Bonds.length; i++) {
    					bondedTriplets[i] = new ArrayList<int[]>();
    				}
    				continue;
    			}
    			if (line.matches("^[0-9]* angle types")) {
    				p4Bonds = new P4BondTorsion[Integer.parseInt(fields[0])];
    				bondedQuads = new ArrayList[p4Bonds.length];
    				for (int i=0; i<p4Bonds.length; i++) {
    					bondedQuads[i] = new ArrayList<int[]>();
    				}
    				continue;
    			}
        		if (heading.matches("^masses")) {
        			int idx = Integer.parseInt(fields[0]);
        			atomTypes[idx] = new AtomTypeLeaf(new ElementSimple(symbol+"", Double.parseDouble(fields[1])));
        			symbol++;
        			continue;
        		}
        		if (heading.matches("^pair coeffs")) {
        			IPotentialAtomic p = new P2LennardJones(opts.space, Double.parseDouble(fields[1]), Double.parseDouble(fields[2]));
        			switch (opts.truncation) {
        			case TRUNCATED:
        				p = new P2SoftTruncated((Potential2SoftSpherical)p,  opts.rc, opts.space);
        				break;
        			case SHIFTED:
        				p = new P2SoftSphericalTruncatedShifted(opts.space, (Potential2SoftSpherical)p, opts.rc);
        				break;
        			case FORCE_SHIFTED:
        				p = new P2SoftSphericalTruncatedForceShifted(opts.space, (Potential2SoftSpherical)p, opts.rc);
        				break;
        			case SWITCHED:
        				p = new P2SoftSphericalTruncatedSwitched(opts.space, (Potential2SoftSpherical)p, opts.rc);
        				break;
        			}
        			atomicPotentials.add(p);
        			continue;
        		}
        		if (heading.matches("^bond coeffs")) {
        			// lammps has U = K*(r-r0)^2   http://lammps.sandia.gov/doc/bond_harmonic.html
        			// P2Harmonic does U = 0.5*w*(r-r0)^2
        			p2Bonds[Integer.parseInt(fields[0])] = new P2Harmonic(opts.space, 2*Double.parseDouble(fields[2]), Double.parseDouble(fields[1]));
        		}
        		if (heading.matches("^angle coeffs")) {
        			int idx = Integer.parseInt(fields[0]);
        			p3Bonds[idx] = new P3BondAngle(opts.space);
        			p3Bonds[idx].setEpsilon(2*Double.parseDouble(fields[1]));
        			p3Bonds[idx].setAngle(Double.parseDouble(fields[2])*180/Math.PI);
        		}
        		if (heading.matches("^dihedral coeffs")) {
        			int idx = Integer.parseInt(fields[0]);
        			p4Bonds[idx] = new P4BondTorsion(opts.space, Double.parseDouble(fields[1]), Double.parseDouble(fields[2]), Double.parseDouble(fields[3]), Double.parseDouble(fields[4]));
        		}
        		if (heading.matches("^atoms")) {
        			int idx = Integer.parseInt(fields[0]);
        			int molIdx = Integer.parseInt(fields[1]);
        			atomTypeId[idx] = Integer.parseInt(fields[2]);
        			atomCounts[atomTypeId[idx]]++;
        			charges[idx] = Double.parseDouble(fields[3]);
        			coords[idx] = opts.space.makeVector();
        			coords[idx].setX(0, Double.parseDouble(fields[4]));
        			coords[idx].setX(1, Double.parseDouble(fields[5]));
        			coords[idx].setX(2, Double.parseDouble(fields[6]));
        		}
        		if (heading.matches("^bonds")) {
        			int idx = Integer.parseInt(fields[1]);
        			bondedPairs[idx].add(new int[]{Integer.parseInt(fields[2]), Integer.parseInt(fields[3])});
        		}
        		if (heading.matches("^angles")) {
        			int idx = Integer.parseInt(fields[1]);
        			bondedTriplets[idx].add(new int[]{Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), Integer.parseInt(fields[4])});
        		}
        		if (heading.matches("^dihedrals")) {
        			int idx = Integer.parseInt(fields[1]);
        			bondedQuads[idx].add(new int[]{Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), Integer.parseInt(fields[4])});
        		}
        		
        	}
        	else {
        		heading = line.toLowerCase();
        	}
        	
        }
        SpeciesSpheresHetero species = new SpeciesSpheresHetero(opts.space, atomTypes);
        species.setChildCount(atomCounts);
        species.setConformation(new ConformationGeneric(coords));
		return new Stuff(null, null, species);
	}

	public static Stuff makeStuff(String fileName, Options opts) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(fileName);
        }catch(IOException e) {
            throw new RuntimeException("Cannot open "+fileName+", caught IOException: " + e.getMessage());
        }
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader bufReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufReader.readLine()) != null) {
            	lines.add(line.trim());
            }
            bufReader.close();
            return makeStuffFromLines(lines, opts);
        }
        catch (IOException ex) {
        	throw new RuntimeException(ex);
        }
	}

	public static class Stuff {
		public Stuff(PotentialGroup intraPotential, PotentialGroup interPotential, ISpecies species) {
			this.intraPotential = intraPotential;
			this.interPotential = interPotential;
			this.species = species;
		}
		public final PotentialGroup intraPotential;
		public final PotentialGroup interPotential;
		public final ISpecies species;
	}
	
	public enum Truncation {NONE, TRUNCATED, SHIFTED, FORCE_SHIFTED, SWITCHED};
	public static class Options {
		public ISpace space = Space3D.getInstance();
		public Truncation truncation = Truncation.NONE;
		public double rc = Double.POSITIVE_INFINITY;
	}
}