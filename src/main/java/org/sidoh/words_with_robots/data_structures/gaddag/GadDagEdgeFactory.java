package org.sidoh.words_with_robots.data_structures.gaddag;

import org.jgrapht.EdgeFactory;

public class GadDagEdgeFactory implements EdgeFactory<Long, GadDagEdge> {
  @Override
  public GadDagEdge createEdge(Long aLong, Long aLong1) {
    throw new UnsupportedOperationException("use addEdge(Long, Long, GadDageEdge)");
  }
}
