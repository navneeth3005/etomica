package etomica;
import etomica.action.AtomAction;
import etomica.action.AtomActionAdapter;
import etomica.atom.AtomFactory;
import etomica.chem.Model;
import etomica.units.Dimension;
import etomica.utility.NameMaker;


 /**
  * A Species holds information about how to construct a molecule, and
  * provides for the management of molecules in a phase.
  * 
  * These are the important features of a Species: <br>
  * <ol>
  * <li>It holds an AtomFactory instance that constructs molecules when
  * needed.
  * <li>It makes a SpeciesAgent class that is placed in each phase (each
  * phase has one species agent from each species). This agent manages the
  * molecules of that species in that phase (counting, adding, removing,
  * etc.) The agent for a given phase may be obtained through the getAgent
  * method. <br>
  * <li>Each Species has a unique species index assigned when it is
  * constructed. The index assignment begins at 0 and is incremented after
  * each Species construction. This index is useful when collecting things
  * in reference to the species (for example, in the use of neighbor lists).
  * </ol>
  * The number of molecules of a species in a phase may be changed at run
  * time. Interactions among all molecules in a phase are defined by
  * associating an intermolecular potential to one or more Species via a
  * call to the setSpecies method of the PotentialMaster for the simulation.
  * 
  * @author David Kofke
  * @author C. Daniel Barnes
  * @see SpeciesMaster
  * @see SpeciesAgent
  * @see PotentialMaster
  */
 
public class Species {

    /**
     * Constructs species with molecules built by the given atom factory.
     */
    public Species(AtomFactory factory) {
        this.factory = factory;
        if (Default.AUTO_REGISTER) {
            Simulation.getDefault().register(this);
        }
        setName(NameMaker.makeName(this.getClass()));
        index = instanceCount++;
    }
    
    /**
     * Constructs species with an atom factory that makes molecules from
     * the given model for the given space.
     */
    public Species(Space space, Model model) {
    	this(model.makeAtomFactory(space));
    }

    public int getIndex() {
        return index;
    }
    
    /**
     * Accessor method of the name of this species.
     * 
     * @return The given name of this species
     */
    public final String getName() {return name;}
    
    /**
     * Method to set the name of this species. The species' name
     * provides a convenient way to label output data that is associated with
     * it.  This method might be used, for example, to place a heading on a
     * column of data. Default name is the base class followed by the integer
     * index of this element.
     * 
     * @param name The name string to be associated with this species.
     */
    public void setName(String name) {this.name = name;}

    /**
     * Overrides the Object class toString method to have it return the output of getName.
     * 
     * @return The name given to the phase
     */
    public String toString() {return getName();}
    
    public AtomFactory moleculeFactory() {return factory;}
    
    /**
     * Nominal number of molecules of this species in each phase.
     * Actual number may differ if molecules have been added or removed to/from the phase
     */
    protected int nMolecules = 20;
    
    /**
     * Accessor method for nominal number of molecules in each phase.  Actual 
     * number of molecules of this species in a given phase is obtained via
     * the getNMolecules method of Species.Agent
     * 
     * @return Nominal number of molecules in each phase
     * @see SpeciesAgent#getNMolecules
     */
    public int getNMolecules() {return nMolecules;}
    public Dimension getNMoleculesDimension() {return Dimension.QUANTITY;}
    
    /**
     * Sets the number of molecules of this species for each phase.
     * Propagates the change to agents of this species in all phases, so
     * it creates the given number of molecules in every phase.
     * 
     * @param n The new number of molecules of this species in each phase
     * @see SpeciesAgent#setNMolecules
     */
    public void setNMolecules(int n) {
        nMolecules = n;
        allAgents(new AtomActionAdapter() {public void actionPerformed(Atom a) {((SpeciesAgent)a).setNMolecules(nMolecules);}});
    }
    
    /**
     * @return a new molecule of this species.
     */
    public Atom makeMolecule() {
        return factory.makeAtom();
    }
    
    /**
     * Performs the given action on all of this species agents in all phases.
     */
    public void allAgents(AtomAction action) {
        if(action == null) return;
        agents.doToAll(action);
    }
    
    /**
     * Constructs an Agent of this species and sets its parent phase
     * 
     * @param p The given parent phase of the agent
     * @return The new agent.  Normally this is returned into the setAgent method of the given phase.
     * @see Species.Agent
     */
    public SpeciesAgent makeAgent(SpeciesMaster parent) {
        Phase phase = parent.node.parentPhase();
        SpeciesAgent a = new SpeciesAgent(factory.space, this, nMolecules, parent.node);
        agents.put(phase,a);   //associate agent with phase; retrieve agent for a given phase using agents.get(p)
        return a;
    }

    /**
     * Returns the agent of this species in the given phase
     * Hashmap is used to connect phase(key)-agent(value) pairs
     * 
     * @param p The phase for which this species' agent is requested
     * @return The agent of this species in the phase
     */
    public final SpeciesAgent getAgent(Phase p) {return agents.get(p);}
        

    final AgentList agents = new AgentList();
    protected final AtomFactory factory;
    private String name;
    private int index;
    private static int instanceCount;

    /**
     * Class that keeps a list of all agents in a way that
     * they can be referenced according to the phase they are in.
     * Uses the phase index to index them.
     * Mimics hash functionality.
     */
    private static final class AgentList {
        
        private SpeciesAgent[] agentArray = new SpeciesAgent[0];
        
        void put(Phase phase, SpeciesAgent agent) {
            int index = phase.index;
            //expand array size
            if(index >= agentArray.length) {
                agentArray = (SpeciesAgent[])etomica.utility.Arrays.resizeArray(agentArray,index+1);
            }
            agentArray[index] = agent;
        }
        
        SpeciesAgent get(Phase phase) {return agentArray[phase.index];}
        
        void doToAll(AtomAction action) {
            for(int i=agentArray.length-1; i>=0; i--) action.actionPerformed(agentArray[i]);
        }
        
    }//end of AgentList
    
}