//Source file generated by Etomica

package etomica.simulations;
import etomica.*;

public class _3DSphereWell extends Simulation {

  public _3DSphereWell() {
    super(new etomica.Space3D());
    Simulation.instance = this;
    etomica.Phase phase0  = new etomica.Phase();
    phase0.setConfiguration(new ConfigurationFcc());
    etomica.PotentialSquareWell potentialHardSphere2  = new etomica.PotentialSquareWell();
      potentialHardSphere2.setCoreDiameter(4.0);
    etomica.Controller controller0  = new etomica.Controller();
    etomica.SpeciesSphereWell speciesSphereWell0  = new etomica.SpeciesSphereWell();
      speciesSphereWell0.setDiameter(4.0);
      speciesSphereWell0.setLambda(1.5);
      speciesSphereWell0.setNMolecules(32);
      speciesSphereWell0.setColor(new java.awt.Color(180,0,255));
    etomica.DisplayPhase displayPhase0  = new etomica.DisplayPhase();
    etomica.IntegratorHard integratorHard0  = new etomica.IntegratorHard();
    etomica.P2SimpleWrapper potentialHardSphere3  = new etomica.P2SimpleWrapper(potentialHardSphere2);
  } //end of constructor

  public static void main(String[] args) {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setSize(600,350);

    Simulation sim = new _3DSphereWell();
    sim.mediator().go(); 
    f.getContentPane().add(sim.panel());

    f.pack();
    f.show();
    f.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {System.exit(0);}
    });
  }//end of main
}//end of class
