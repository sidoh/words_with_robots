package org.sidoh.words_with_robots.data_structures.gaddag;

import com.google.common.collect.Lists;
import org.sidoh.words_with_robots.data_structures.BitFieldLetterSet;
import org.sidoh.words_with_robots.data_structures.LetterSet;

import java.util.Iterator;
import java.util.List;

public class CompactGadDagEdgeSet extends GadDagEdgeSet {
  private final List<GadDagEdge> edges = Lists.newArrayList();
  private final LetterSet setEdges = new BitFieldLetterSet();

  @Override
  public boolean containsEdgeForLetter(char letter) {
    return setEdges.contains(letter);
  }

  @Override
  public GadDagEdge getEdgeForLetter(char letter) {
    if ( !setEdges.contains(letter)) {
      return null;
    }

    for (GadDagEdge edge : edges) {
      if ( edge.getDestinationLetter() == letter ) {
        return edge;
      }
    }

    throw new RuntimeException("Requested edge that doesn't exist, but exists in edge set: " + letter);
  }

  @Override
  public int size() {
    return edges.size();
  }

  @Override
  public boolean isEmpty() {
    return edges.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return edges.contains(o);
  }

  @Override
  public Iterator<GadDagEdge> iterator() {
    return edges.iterator();
  }

  @Override
  public boolean add(GadDagEdge gadDagEdge) {
    if (setEdges.contains(gadDagEdge.getDestinationLetter())) {
      return false;
    }

    setEdges.add(gadDagEdge.getDestinationLetter());
    edges.add(gadDagEdge);
    return true;
  }
}
