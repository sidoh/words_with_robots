package org.sidoh.words_with_robots.data_structures.gaddag;

import java.util.*;

public final class GadDagEdgeSet implements Set<GadDagEdge> {
  private final Map<Byte, GadDagEdge> edges
          = new HashMap<Byte, GadDagEdge>();

  public boolean containsEdgeForLetter(Byte letter) {
    return edges.containsKey(letter);
  }

  public GadDagEdge getEdgeForLetter(Byte letter) {
    return edges.get(letter);
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
    return edges.values().contains(o);
  }

  @Override
  public Iterator<GadDagEdge> iterator() {
    return edges.values().iterator();
  }

  @Override
  public Object[] toArray() {
    return edges.values().toArray();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    return edges.values().toArray(ts);
  }

  @Override
  public boolean add(GadDagEdge gadDagEdge) {
    if (edges.containsKey(gadDagEdge.getDestinationLetter())) {
      return false;
    }

    edges.put(gadDagEdge.getDestinationLetter(), gadDagEdge);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (! edges.containsValue(o)) return false;
    edges.remove(((GadDagEdge)o).getDestinationLetter());
    return true;
  }

  @Override
  public boolean containsAll(Collection<?> objects) {
    return edges.values().containsAll(objects);
  }

  @Override
  public boolean addAll(Collection<? extends GadDagEdge> gadDagEdges) {
    boolean changed = false;
    for (GadDagEdge gadDagEdge : gadDagEdges) {
      changed = changed || add(gadDagEdge);
    }
    return changed;
  }

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
}
