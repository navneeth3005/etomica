package etomica;
import java.awt.Dimension;
import java.awt.Graphics;
//import gl4java.swing.GLAnimJPanel;
import gl4java.awt.GLAnimCanvas;

/**
 * Parent of classes that use OpenGL to display the phase configuration.
 *
 * @author Steve Hotchkiss
 */
public abstract class DisplayCanvasOpenGL extends GLAnimCanvas implements java.io.Serializable, DisplayCanvasInterface, PhaseEventListener {
    //protected Image offScreen;
    //protected Graphics osg;
        
    protected DisplayPhase displayPhase;
    protected PhaseAction.Inflate inflate;

    /**
     * Flag to indicate if display can be resized
     */
    boolean resizable = false;
    /**
     * Flag to indicate if display can be moved
     */
    boolean movable = false;

    /** 
     * Flag to indicate if value of scale should be superimposed on image
     */
    boolean writeScale = false;
    
    /**
     *  Sets the quality of the rendered image, false = low, true = high
     */
    boolean highQuality = false;

    public DisplayCanvasOpenGL(int width, int height) {
        super(width, height);
        //!!!super(false);
        setBackground(java.awt.Color.black);
    }

    public void phaseAction(PhaseEvent evt) {
        initialize();
    }
        
    public void createOffScreen () {
        //if (offScreen == null) { 
            //createOffScreen(getSize().width, getSize().height);
        //}
    }
    public void createOffScreen (int p) {
        //createOffScreen(p,p);
    }
    public void createOffScreen(int w, int h) {
        //offScreen = createImage(w,h);
        //if(offScreen != null) osg = offScreen.getGraphics();
    }
        
    public void doPaint(Graphics g) {}
    
    public void update(Graphics g) {paint(g);}
        
      
    public void setPhase(Phase p) {
        inflate = new PhaseAction.Inflate(displayPhase.getPhase());
        p.speciesMaster.addListener(this);
    }
              

    public void setMovable(boolean b) {movable = b;}
    public boolean isMovable() {return movable;}
    public void setResizable(boolean b) {resizable = b;}
    public boolean isResizable() {return resizable;}
    public void setWriteScale(boolean s) {writeScale = s;}
    public boolean getWriteScale() {return(writeScale);}
    public void setHighQuality(boolean q) {highQuality = q;}
    public boolean getHighQuality() {return(highQuality);}

    public void setMinimumSize(Dimension temp) {}
    public void setMaximumSize(Dimension temp) {}
    public void setPreferredSize(Dimension temp) {}
    public void initialize() {}
} //end of DisplayCanvas class

