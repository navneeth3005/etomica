/**
 * SimulationEditorFrame
 *
 * The SimulationEditorFrame class is responsible for creating a JInternalFrame that contains a static 
 * instance of the SimEditorTabMenu.  This internal frame is where all simulation components are added
 * and deleted.
 *
 * @author Bryan C. Mihalick
 * 8/14/00
 */

package simulate.gui;

public class SimulationEditorFrame extends javax.swing.JInternalFrame {
    SimulationEditor simulationEditor = new SimulationEditor(simulate.Simulation.instance);
    
    public SimulationEditorFrame(){
        setResizable(true);
        setIconifiable(true);
        setMaximizable(true);
        setClosable(true);
        setTitle("Simulation Editor");
        getContentPane().add(simulationEditor);
    }// end of SimulationEditorFrame constructor
    
    public void setSimulationEditor(SimulationEditor ed) {
        getContentPane().remove(simulationEditor);
        simulationEditor = ed;
        getContentPane().add(simulationEditor);
    }
    public SimulationEditor getSimulationEditor() {return simulationEditor;}
    
}// end of SimulationEditorFrame class