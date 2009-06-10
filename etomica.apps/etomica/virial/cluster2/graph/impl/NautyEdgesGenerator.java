package etomica.virial.cluster2.graph.impl;

import java.io.BufferedReader;
import java.io.IOException;

import etomica.virial.cluster2.graph.Edges;
import etomica.virial.cluster2.graph.EdgesFilter;
import etomica.virial.cluster2.graph.EdgesRepresentation;
import etomica.virial.cluster2.graph.GraphFactory;
import etomica.virial.cluster2.nauty.ProcessWrapper;

public class NautyEdgesGenerator extends AbstractEdgesGenerator {

  private static final String FLAG_NAUTY = "Nauty";
  private EdgesRepresentation representation;
  private ProcessWrapper nauty;
  private BufferedReader nautyReader;

  public NautyEdgesGenerator(EdgesRepresentation rep,
      ProcessWrapper nautyProcess, EdgesFilter filter) {

    super(false, filter);
    nauty = nautyProcess;
    representation = rep;
    computeTags();
  }

  public NautyEdgesGenerator(EdgesRepresentation rep,
      ProcessWrapper nautyProcess) {

    this(rep, nautyProcess, null);
  }

  protected void run() {

    try {
      nauty.run();
      nautyReader = new BufferedReader(nauty.getProcessOutput());
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Nauty IOException");
    }
  }

  @Override
  protected void computeTags() {

    getInternalTags().add(getTag());
    getInternalTags().addAll(nauty.getProcessInfo().getTags());
    if (getEdgesFilter() != null) {
      getInternalTags().addAll(getEdgesFilter().getTags());
    }
  }

  @Override
  protected String getTag() {

    return NautyEdgesGenerator.FLAG_NAUTY;
  }

  @Override
  protected Edges push() {

    String line;
    // the enumeration is starting now, so run the modified nauty
    if (!isStarted()) {
      run();
    }
    try {
      // first line: size of the automorphism group associated with the graph
      line = nautyReader.readLine();
      if (line == null || line.isEmpty()) {
        return null;
      }
      // number of isomorphisms: N!/automorphism_group_size
      double coefficient = 1;
      int automorphismGroupSize = Integer.valueOf(line);
      if (automorphismGroupSize > 0) {
        for (int i = 1; i <= representation.getNodeCount(); i++) {
          coefficient *= i;
        }
        coefficient /= automorphismGroupSize;
      }
      // second line: encoding of the graph as a bit string; this
      // graph is the representative of its automorphism group
      return GraphFactory.nautyEdges(nautyReader.readLine(), representation,
          coefficient);
    }
    catch (IOException e) {
      return null;
    }
  }
}