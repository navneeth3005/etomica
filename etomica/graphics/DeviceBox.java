//class includes a main method to demonstrate and test its use
package etomica.graphics;
import etomica.*;

//import etomica.units.*;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;

/**
 * A simple device the permits editing of a single value via a textbox 
 * with an associated label.
 *
 * @author David Kofke
 */
 
 /* History of changes
  * 09/05/02 (DAK) new from DisplayBox
  */
 
public class DeviceBox extends Device implements EtomicaElement {
    
    public String getVersion() {return "DeviceBox:02.09.05/"+Device.VERSION;}
    /**
     * Descriptive text label to be displayed with the value
     */
    protected JLabel label;
    private Constants.CompassDirection labelPosition = Constants.NORTH;
    /**
     * Object for displaying the value as a text field
     */
    protected JTextField value;
    /**
     * Displayed panel that holds the label and value
     * (not yet used; meant to implement to make lightweight display)
     */
    protected JPanel panel = new JPanel(new java.awt.BorderLayout());
    /**
     * Modulator that relays the changes to the object controlled by the device
     */
    protected ModulatorAbstract modulator;
    /**
     * Integer specifying the number of significant figures to be displayed.
     * Default is 4.
     */
    int precision;
    private LabelType labelType;
    
    /**
     * Physical units associated with the displayed value.
     * Default is null (dimensionless).
     */
    protected etomica.units.Unit unit;
    
    public DeviceBox() {
        this(Simulation.instance);
    }
    public DeviceBox(ModulatorAbstract m) {
        this(Simulation.instance, m);
    }
    public DeviceBox(Simulation sim, ModulatorAbstract m) {
        this(sim);
        setModulator(m);
    }
    public DeviceBox(Simulation sim) {
        super(sim);
        label = new JLabel("Label");
        value = new JTextField("");
        value.setEditable(true);
        panel.add(value, java.awt.BorderLayout.CENTER);
        setLabelType(STRING);
 //       panel.setMinimumSize(new java.awt.Dimension(80,60));
        unit = new etomica.units.Unit(etomica.units.BaseUnit.Null.UNIT);
        setPrecision(4);
        
        value.addActionListener(new BoxListener());
        
    }//end of constructor
    
    public static EtomicaInfo getEtomicaInfo() {
        EtomicaInfo info = new EtomicaInfo("Simple textbox editor of a single value");
        return info;
    }
    
    /**
     * Updates the display of the box with the current value given by the modulator.
     */
    public void doUpdate() {
        if(modulator == null) return;
//        value.setText(Double.toString(m.getValue()));
        value.setText(format(unit.fromSim(modulator.getValue()),precision));
    }
    
    /**
     * Accessor method to set the physical units of the displayed value.
     * Text describing unit is used in label.
     */
    public void setUnit(etomica.units.Unit u) {
        unit = u;
        setLabel();
    }
    /**
     * Returns the physical units of the displayed value.
     */
    public etomica.units.Unit getUnit() {return unit;}
    
    /**
     * Returns the dimensions of the quantity being measured.
     * Obtained from the meter associated with this display.
     */
    public etomica.units.Dimension dimension() {
        if(modulator != null) return modulator.getDimension();
        else return etomica.units.Dimension.NULL;
    }
    
    public java.awt.Component graphic(Object obj) {return panel;}
    
    /**
     * Accessor method of the precision, which specifies the number of significant figures to be displayed.
     */
    public int getPrecision() {return precision;}
    /**
     * Accessor method of the precision, which specifies the number of significant figures to be displayed.
     */
    public void setPrecision(int n) {
        value.setColumns(n);
        precision = n;
    }
    
    /**
     * Specifies the modulator that receives the edit.
     */
    public void setModulator(ModulatorAbstract m) {
        modulator = m;
        if(m == null) return;
        setUnit(m.getDimension().defaultIOUnit());
        setLabel();
        doUpdate();
    }
    
    /**
     * Accessor method for the modulator that receives the edit.
     */
    public ModulatorAbstract getModulator() {
        return modulator;
    }
    
    /**
     * Sets the value of a descriptive label using the meter's label and the unit's symbol (abbreviation).
     */
    private void setLabel() {
        if(modulator == null) return;
        String suffix = (unit.symbol().length() > 0) ? " ("+unit.symbol()+")" : "";
        setLabel(modulator.getLabel()+suffix);
    }
    
    /**
     * Sets the value of a descriptive label using the given string.
     */
    public void setLabel(String s) {
        label.setText(s);
        if(labelType == BORDER) {
            panel.setBorder(new javax.swing.border.TitledBorder(s));
        }
        if(labelType == STRING) setLabelPosition(labelPosition);
/*        JLabel oldLabel = label;
        label = new JLabel(s);
        panel.remove(oldLabel);
        panel.add(label, 0);
        support.firePropertyChange("label",oldLabel,label);*/
    }
    /**
     * @return the current value of the descriptive label.
     */
    public String getLabel() {return label.getText();}
    

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
        if(labelType != BORDER) panel.setBorder(new javax.swing.border.EmptyBorder(2,2,2,2));
        if(labelType != STRING) panel.remove(label);
        setLabel(label.getText());
    }
    public LabelType getLabelType() {
        return labelType;
    }

    public void setLabelPosition(Constants.CompassDirection position) {
        labelPosition = position;
        if(labelType != STRING) return;
        panel.remove(label);
        panel.add(label,position.toString());//toString() returns the corresponding BorderLayout constant
//        support.firePropertyChange("label",oldLabel,label);
        panel.revalidate();
        panel.repaint();
    }
    
    public Constants.CompassDirection getLabelPosition() {return labelPosition;}
        
        
    private class BoxListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            double x = Double.parseDouble(value.getText());
            if(modulator!=null) modulator.setValue(unit.toSim(x));
       }
    }
    
    
    /**
     * Demonstrates how this class is implemented.
     */
    public static void main(String[] args) {

        SimulationGraphic sim = new SimulationGraphic();
	    IntegratorHard integrator = new IntegratorHard();
	    Species species = new SpeciesSpheresMono();
	    species.setNMolecules(25);
	    Phase phase = new Phase();
	    Controller controller = new Controller();
	    Display display = new DisplayPhase();
		
        Potential2 potential = new P2SquareWell();
        potential.setSpecies(species, species);
 //       Potential2 potential = new P2HardSphere(sim);
		sim.elementCoordinator.go();

        //part that is unique to this demonstration
        integrator.setIsothermal(true);
        Modulator mod1 = new Modulator(integrator, "temperature");
        //DisplayBox showing the current value (default is most recent, but this is zero because meter is inactive (not keeping averages), and thus doesn't hold a most-recent value)
        DeviceBox box0 = new DeviceBox(mod1);
        //here's a DisplayBox tied to a Modulator
		DisplayBox box1 = new DisplayBox();
		box1.setDatumSource(mod1);
        //end of unique part
                                            
		Simulation.instance.elementCoordinator.go(); 		                                    
        SimulationGraphic.makeAndDisplayFrame(Simulation.instance);
    }//end of main  
  //  */

    
    
    /********** Utility method for formatting a double to a string **************/
    
    /**
     * Formats a double with a specified number of digits.
     * When java converts a <tt>double</tt> to a <tt>String</tt>
     * it retains the full precision of the number. This can
     * generate 15 decimal places! This method truncates this output
     * to some specified number of decimal places.
     * @param d the double to format
     * @param precision the number of digits desired
     * @return returns the formatted string
     *
     * Taken from the comphys package of Richard Gonsalves of the
     * SUNY Buffalo Department of Physics
     */

    public static String format (double d, int precision) {

        if (d == Double.NaN ||
            d == Double.POSITIVE_INFINITY ||
            d == Double.NEGATIVE_INFINITY)
            return Double.toString(d);
        
        StringBuffer buffer = new StringBuffer(20);
        
        if (d < 0) {
            d = -d;
            buffer.append('-');
        }

        if (d == 0) {
            buffer.append("0.0");
            for (int p = 0; p < precision - 1; p++)
                buffer.append('0');
            return buffer.toString();
        }

        int exponent = 0;
        while (d >= 10) {
            ++exponent;
            d /= 10;
        }
        while (d < 1) {
            --exponent;
            d *= 10;
        }

        if (precision < 0)
            precision = -precision;
        int p = precision;
        while (--p > 0)
            d *= 10;
        long ld = (long) Math.round(d);
        char[] digits = new char[precision];
        p = precision;
	long ld_div_10 = 0;
	long ld_save = ld;
        while (--p >= 0) {
	    ld_div_10 = ld / 10;
            digits[p] = (char) ('0' + ( ld - (ld_div_10 * 10) ));
            ld = ld_div_10;
        }
	if (ld_div_10 > 0) {
	    ld = ld_save / 10;
	    p = precision;
	    while (--p >= 0) {
		ld_div_10 = ld / 10;
		digits[p] = (char) ('0' + ( ld - (ld_div_10 * 10) ));
		ld = ld_div_10;
	    }
	    ++exponent;
	}

        int decimalPoint = 0;
        if (Math.abs(exponent) < 6 || Math.abs(exponent) < precision) {
            while (exponent > 0) {
                ++decimalPoint;
                --exponent;
            }
            while (exponent < 0) {
                --decimalPoint;
                ++exponent;
            }
        }

        if (decimalPoint < 0) {
            buffer.append("0.");
            while (decimalPoint < -1) {
                buffer.append("0");
                ++decimalPoint;
            }
        }

        for (p = 0; p < precision; p++) {
            buffer.append(digits[p]);
            if (p == decimalPoint)
                if (p < precision - 1)
                    buffer.append(".");
        }

        if (exponent != 0)
            buffer.append("E" + exponent);

        return buffer.toString();

    }

    /**
     * Typed constant used to indicate the type of label to be used with the display.
     */
     
	public static class LabelType extends Constants.TypedConstant {
        public LabelType(String label) {super(label);}       
        public Constants.TypedConstant[] choices() {return (Constants.TypedConstant[])CHOICES;}
        public static final LabelType[] CHOICES = 
            new LabelType[] {
                new LabelType("Border"),
                new LabelType("String")};
    }//end of LabelType
    public static final LabelType BORDER = LabelType.CHOICES[0];
    public static final LabelType STRING = LabelType.CHOICES[1];
    
}