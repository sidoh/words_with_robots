package org.sidoh.words_with_robots.data_structures.gaddag;

import org.jgrapht.graph.EdgeSetFactory;

import java.util.Set;

public class GadDagEdgeSetFactory implements EdgeSetFactory<Long, GadDagEdge> {
  @Override
  public Set<GadDagEdge> createEdgeSet(Long value) {
    return new GadDagEdgeSet();
  }
}
