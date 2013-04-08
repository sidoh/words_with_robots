package org.sidoh.tiler.data_structures.gaddag;

import org.jgrapht.graph.EdgeSetFactory;
import org.sidoh.tiler.data_structures.gaddag.GadDagEdge;
import org.sidoh.tiler.data_structures.gaddag.GadDagEdgeSet;

import java.util.Set;

public class GadDagEdgeSetFactory implements EdgeSetFactory<Long, GadDagEdge> {
  @Override
  public Set<GadDagEdge> createEdgeSet(Long value) {
    return new GadDagEdgeSet();
  }
}
