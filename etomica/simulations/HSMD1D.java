//Source file generated by Etomica

package etomica.simulations;

import etomica.*;

public class HSMD1D extends Simulation {

  public HSMD1D() {
    super(new etomica.Space1D());
    Simulation.instance = this;
    etomica.Phase phase0  = new etomica.Phase();
    etomica.PotentialHardSphere potentialHardSphere0  = new etomica.PotentialHardSphere();
      potentialHardSphere0.setCollisionDiameter(2.0);
    etomica.Controller controller0  = new etomica.Controller();
    etomica.SpeciesSpheres speciesSpheres0  = new etomica.SpeciesSpheres();
      speciesSpheres0.setDiameter(2.0);
      speciesSpheres0.setNMolecules(7);
      speciesSpheres0.setColor(new java.awt.Color(0,255,0));
    etomica.DisplayPhase displayPhase0  = new etomica.DisplayPhase();
    etomica.IntegratorHard integratorHard0  = new etomica.IntegratorHard();
      integratorHard0.setIsothermal(true);
      integratorHard0.setTemperature(20.);
    etomica.P2SimpleWrapper potentialHardSphere1  = new etomica.P2SimpleWrapper(potentialHardSphere0);
  } //end of constructor

  public static void main(String[] args) {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setSize(1200,650);

    Simulation sim = new HSMD1D();
    sim.mediator().go(); 
    f.getContentPane().add(sim.panel());

    f.pack();
    f.show();
    f.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {System.exit(0);}
    });
  }//end of main
}//end of class
