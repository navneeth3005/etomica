package etomica.graphics;
import java.awt.Color;

import etomica.atom.Atom;

/**
 * Class that defines the algorithm used to determine atoms colors when drawn to DisplayPhase.
 * The atomColor method is called just before the atom is drawn to set the graphics color.
 *
 * @author David Kofke
 */
 
public abstract class ColorScheme implements java.io.Serializable {

    protected Color baseColor;
    
    public ColorScheme() {
        this(DEFAULT_ATOM_COLOR);
    }
    public ColorScheme(Color color) {
        baseColor = color;
    }
    
    public abstract Color getAtomColor(Atom a);
    
    public final void setBaseColor(Color c) {baseColor = c;}
    public final Color getBaseColor() {return baseColor;}

    public static Color DEFAULT_ATOM_COLOR = Color.black;
    
    /**
     * Colors all atoms with baseColor.
     */
    public static class Simple extends ColorScheme {
        public Simple() {super();}
        public Simple(java.awt.Color color) {super(color);}
        public Color getAtomColor(Atom a) {return baseColor;}
    }//end of Simple
}//end of ColorScheme
