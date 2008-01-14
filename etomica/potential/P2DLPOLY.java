package etomica.potential;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import etomica.action.WriteConfigurationP2DLPOLY;
import etomica.atom.AtomSet;
import etomica.box.Box;
import etomica.space.Space;
import etomica.units.Dimension;
import etomica.units.Length;

public class P2DLPOLY implements IPotential {
	
	public P2DLPOLY(Space space){
	    this.space = space;
       	configP2DLPOLY = new WriteConfigurationP2DLPOLY();
    	configP2DLPOLY.setConfName("CONFIG");
	}

	public Space getSpace() {
	    return space;
	}
	
	public int nBody() {
	    return 2;
	}
	
	public Dimension getRangeDimension() {
	    return Length.DIMENSION;
	}

	public double energy(AtomSet atoms) {

		configP2DLPOLY.setMolecule(atoms.getAtom(0), atoms.getAtom(1));
		
		
		
		configP2DLPOLY.actionPerformed();
		int typeInteraction = configP2DLPOLY.getTypeInteraction();
		
		try{
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("DLMULTI");
			int exitVal = proc.waitFor();
			FileReader fileReader = new FileReader("ConfigEnergy");			
			BufferedReader bufReader = new BufferedReader(fileReader);
			
			String line = bufReader.readLine();
			return Double.parseDouble(line);
			
			
		}catch (IOException e){
			throw new RuntimeException(e);
		}catch (InterruptedException err){
			throw new RuntimeException(err);
		}
	}

	public double getRange() {
		return Double.POSITIVE_INFINITY;
	}


	public void setBox(Box box) {
		configP2DLPOLY.setBox(box);
	}
	
	public WriteConfigurationP2DLPOLY getConfigP2DLPOLY() {
		return configP2DLPOLY;
	}

	public void setConfigP2DLPOLY(WriteConfigurationP2DLPOLY configP2DLPOLY) {
		this.configP2DLPOLY = configP2DLPOLY;
	}

	private Box box;
	private WriteConfigurationP2DLPOLY configP2DLPOLY;
	protected final Space space;

}
