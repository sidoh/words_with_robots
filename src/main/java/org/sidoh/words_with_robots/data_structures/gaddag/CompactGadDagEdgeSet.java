package org.sidoh.words_with_robots.data_structures.gaddag;

import com.google.common.collect.Lists;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.sidoh.words_with_robots.data_structures.BitFieldLetterSet;
import org.sidoh.words_with_robots.data_structures.LetterSet;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CompactGadDagEdgeSet extends GadDagEdgeSet {
  private static final Comparator<GadDagEdge> EDGE_COMPARATOR = new EdgeComparator();

  private List<GadDagEdge> edges = Lists.newLinkedList();
  private final LetterSet setEdges = new BitFieldLetterSet();
  private GadDagEdge[] compactedEdges;

  @Override
  public boolean containsEdgeForLetter(char letter) {
    return setEdges.contains(letter);
  }

  @Override
  public GadDagEdge getEdgeForLetter(char letter) {
    if (setEdges.contains(letter)) {
      for (GadDagEdge edge : this) {
        if ( edge.getDestinationLetter() == letter ) {
          return edge;
        }
      }

      throw new RuntimeException("Edge exists in edge set, but not in list of edges: " + letter);
    }
    else {
      return null;
    }
  }

  @Override
  public void compact() {
    compactedEdges = edges.toArray(new GadDagEdge[edges.size()]);
    edges = null;
    Arrays.sort(compactedEdges, EDGE_COMPARATOR);
  }

  @Override
  public int size() {
    return edges != null ? edges.size() : compactedEdges.length;
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
    if (edges == null) {
      return new ArrayIterator(compactedEdges);
    }
    else {
      return edges.iterator();
    }
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

  protected static class EdgeComparator implements Comparator<GadDagEdge> {
    @Override
    public int compare(GadDagEdge gadDagEdge, GadDagEdge gadDagEdge1) {
      return Character.valueOf(gadDagEdge.getDestinationLetter()).compareTo(gadDagEdge1.getDestinationLetter());
    }
  }
}
