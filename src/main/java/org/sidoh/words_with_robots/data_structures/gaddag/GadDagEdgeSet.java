package org.sidoh.words_with_robots.data_structures.gaddag;

import java.util.Collection;
import java.util.Set;

/**
 *
 */
public abstract class GadDagEdgeSet implements Set<GadDagEdge> {
  /**
   *
   * @param letter
   * @return true if this set contains an outgoing edge for the provided letter
   */
  public abstract boolean containsEdgeForLetter(char letter);

  /**
   * Given a letter, find the corresponding outgoing edge.
   *
   * @param letter
   * @return
   */
  public abstract GadDagEdge getEdgeForLetter(char letter);

  /**
   * Called by the GadDag when user indicates that they're done adding words. Implementation should
   * use this knowledge to compact its memory usage. Default is NOOP.
   */
  public void compact() {

  }

  @Override
  public boolean addAll(Collection<? extends GadDagEdge> gadDagEdges) {
    boolean changed = false;
    for (GadDagEdge gadDagEdge : gadDagEdges) {
      changed = changed || add(gadDagEdge);
    }
    return changed;
  }

  //
  // Operations not supported by GadDagEdgeSet
  //

  @Override
  public boolean retainAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }
}
