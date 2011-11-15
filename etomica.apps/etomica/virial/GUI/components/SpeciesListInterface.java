package etomica.virial.GUI.components;

import java.util.ArrayList;




public class SpeciesListInterface {
	
	


	private static int Index=0;
	private int id;
	private Object[] SpeciesL;
	
	public SpeciesListInterface(){
		id = Index;
		SpeciesL = new Object[8];
	}
	
	public void addSpecies(Object object){
		SpeciesL[id] = object;
		id++;
	}
	
	
	public void removeSpecies(){
		SpeciesL[id - 1] = null;
		if(id > 0){
			id--;
		}
		else{
			id = 0;
		}
	}
	public void removeSpeciesAtIndex(int index){
		SpeciesL[index] = null;
		if(id > 0){
			id--;
		}
		else{
			id = 0;
		}
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Object getObject(int Index){
		return SpeciesL[Index];
	}
	
	public ArrayList<String> displayList(){
		ArrayList<String> DisplayArray = new ArrayList<String>();
		
		for (int i=0;i<id;i++){
			if(SpeciesL[i] != null){
				if(SpeciesL[i] instanceof CreateSpeciesLJ_LJ){
					DisplayArray.add("LJ-Spherical2Body");
				}
				else if(SpeciesL[i] instanceof CreateSpeciesLJ_LJQ){
					DisplayArray.add("LJ-Spherical2BodyWithQ");
				}
				else if(SpeciesL[i] instanceof CreateSpeciesLJ_2CLJQ){
					DisplayArray.add("2CenteredLJWithQ");
				}
				
				else if(SpeciesL[i] instanceof CreateSpeciesCO2_2CLJQ){
					DisplayArray.add("CO2-2CenteredLJWithQ");
				}
				else if(SpeciesL[i] instanceof CreateSpeciesCO2_EMP2){
					DisplayArray.add("CO2-EPM2");
				}
				else if(SpeciesL[i] instanceof CreateSpeciesCO2_Trappe){
					DisplayArray.add("CO2-TRAPPE");
				}
				else if(SpeciesL[i] instanceof CreateSpeciesAlkane_TRAPPE){
					DisplayArray.add("n-Alkane-TRAPPE");
				}
				
			}
		}
		
		return DisplayArray;
	}
}