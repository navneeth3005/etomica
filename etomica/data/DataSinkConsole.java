package etomica.data;
import java.io.PrintStream;

import etomica.Data;
import etomica.DataSink;
import etomica.EtomicaElement;
import etomica.EtomicaInfo;
import etomica.units.Unit;
import etomica.utility.NameMaker;

/**
 * Writes data to console or another print stream.
 */
public class DataSinkConsole implements DataSink, EtomicaElement {

    /**
     * Makes class using System.out as the default output stream.
     */
    public DataSinkConsole() {
        this(System.out);
        setName(NameMaker.makeName(this.getClass()));
    }
    
    /**
     * Constructor that permits specification of the output printstream.
     * @param outputStream
     */
    public DataSinkConsole(PrintStream outputStream) {
        this.out = outputStream;
    }
    
    public static EtomicaInfo getEtomicaInfo() {
        EtomicaInfo info = new EtomicaInfo("Pipes data to console");
        return info;
    }
    
    /**
     * Causes the given values to be written to the print stream.
     * Data are written one value per line, all following a header
     * based on the data's label and the current unit.
     */
    public void putData(Data data) {
        out.println(data.getDataInfo().getLabel() + " (" + unit.toString() + ")");
        for(int i=0; i<data.length; i++) {
            out.println(data[i]);
        }
        out.println();
    }

    /**
     * @return Returns the unit.
     */
    public Unit getUnit() {
        return unit;
    }
    /**
     * @param unit The unit to set.
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }
    
    /**
     * Method called to express incredulity.  Short for
     * "No way! Get out of here!"
     * @return Returns the output printstream to which data is written.  
     */
    public PrintStream getOut() {
        return out;
    }
    
    /**
     * Sets the output print stream where data is written.  Default is
     * System.out
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    private Unit unit = Unit.UNDEFINED;
    private PrintStream out = System.out;
    private String name;
}