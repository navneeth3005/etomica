package etomica.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;


public class ParmedParser {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String PYTHON_SCRIPT_PATH = "venv/bin/parmed_json";

    /**
     * Parses the given <a href="http://www.gromacs.org/">Gromacs</a> .top and .gro files
     * using the <a href="https://github.com/ParmEd/ParmEd">ParmEd</a> python library.
     * @param topFile File object containing the path to a Gromacs .top file
     * @param groFile File object containing the path to a Gromacs .gro file
     * @return a {@link ParmedStructure} for extracting Etomica simulation components from the ParmEd {@code Structure} object
     */
    public static ParmedStructure parseGromacs(File topFile, File groFile) throws IOException {
        JsonNode root = execParmedPython(topFile, groFile);
        return new ParmedStructure(root);
    }

    public static ParmedStructure parseGromacsResourceFiles(String topFileName, String groFileName) throws IOException {
        return parseGromacs(getResourceFile(topFileName), getResourceFile(groFileName));
    }

    //TODO: method to accept file contents as strings and create temp files for parsing

    private static File getResourceFile(String filename) {
        ClassLoader classLoader = ParmedParser.class.getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }

    /**
     * Runs the python script on the given files
     * @param topFile File object containing the .top file path
     * @param groFile File object containing the .gro file path
     * @return Jackson JsonNode representing the root of the json tree
     * @throws IOException if the given files do not exist
     */
    private static JsonNode execParmedPython(File topFile, File groFile) throws IOException {


        ProcessBuilder pb = new ProcessBuilder(
                PYTHON_SCRIPT_PATH,
                topFile.getCanonicalPath(),
                groFile.getCanonicalPath()
        );

        Process proc = pb.start();

        JsonNode root = mapper.readTree(proc.getInputStream());

        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return root;
    }
}
