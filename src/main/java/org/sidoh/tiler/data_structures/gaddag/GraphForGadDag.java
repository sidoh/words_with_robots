package org.sidoh.tiler.data_structures.gaddag;

/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ----------------------
 * AbstractBaseGraph.java
 * ----------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   John Long. Sichi
 *                   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 10-Aug-2003 : General edge refactoring (BN);
 * 06-Nov-2003 : Change edge sharing semantics (JVS);
 * 07-Feb-2004 : Enabled serialization (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 01-Jun-2005 : Added EdgeListFactory (JVS);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */

import java.io.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.util.*;


/**
 * The most general implementation of the {@link org.jgrapht.Graph} interface.
 * Its subclasses add various restrictions to get more specific graphs. The
 * decision whether it is directed or undirected is decided at construction time
 * and cannot be later modified (see constructor for details).
 *
 * <p>This graph implementation guarantees deterministic vertex and edge set
 * ordering (via {@link LinkedHashMap} and {@link LinkedHashSet}).</p>
 *
 * @author Barak Naveh
 * @since Jul 24, 2003
 */
public class GraphForGadDag
        extends AbstractGraph<Long, GadDagEdge>
        implements DirectedGraph<Long, GadDagEdge>,
        Cloneable,
        Serializable
{
  //~ Static fields/initializers ---------------------------------------------

  private static final long serialVersionUID = -1263088497616142427L;

  private static final String LOOPS_NOT_ALLOWED = "loops not allowed";

  //~ Instance fields --------------------------------------------------------

  boolean allowingLoops;

  private EdgeFactory<Long, GadDagEdge> edgeFactory;
  private EdgeSetFactory<Long, GadDagEdge> edgeSetFactory;
  private Map<GadDagEdge, IntrusiveEdge> edgeMap;
  private transient Set<GadDagEdge> unmodifiableEdgeSet = null;
  private transient Set<Long> unmodifiableVertexSet = null;
  private Specifics specifics;
  private boolean allowingMultipleEdges;

  private transient TypeUtil<Long> vertexTypeDecl = null;

  //~ Constructors -----------------------------------------------------------

  /**
   * Construct a new pseudograph. The pseudograph can either be directed or
   * undirected, depending on the specified edge factory.
   *
   * @param ef the edge factory of the new graph.
   *
   * @throws NullPointerException if the specified edge factory is <code>
   * null</code>.
   */
  public GraphForGadDag(EdgeFactory<Long, GadDagEdge> ef)
  {
    if (ef == null) {
      throw new NullPointerException();
    }

    edgeMap = new LinkedHashMap<GadDagEdge, IntrusiveEdge>();
    edgeFactory = ef;
    allowingLoops = false;
    allowingMultipleEdges = false;

    specifics = createSpecifics();

    this.edgeSetFactory = new GadDagEdgeSetFactory();
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * @see Graph#getAllEdges(Object, Object)
   */
  public Set<GadDagEdge> getAllEdges(Long sourceVertex, Long targetVertex)
  {
    return specifics.getAllEdges(sourceVertex, targetVertex);
  }

  /**
   * Returns <code>true</code> if and only if self-loops are allowed in this
   * graph. A self loop is an edge that its source and target vertices are the
   * same.
   *
   * @return <code>true</code> if and only if graph loops are allowed.
   */
  public boolean isAllowingLoops()
  {
    return allowingLoops;
  }

  /**
   * Returns <code>true</code> if and only if multiple edges are allowed in
   * this graph. The meaning of multiple edges is that there can be many edges
   * going from vertex v1 to vertex v2.
   *
   * @return <code>true</code> if and only if multiple edges are allowed.
   */
  public boolean isAllowingMultipleEdges()
  {
    return allowingMultipleEdges;
  }

  /**
   * @see Graph#getEdge(Object, Object)
   */
  public GadDagEdge getEdge(Long sourceVertex, Long targetVertex)
  {
    return specifics.getEdge(sourceVertex, targetVertex);
  }

  /**
   * @see Graph#getEdgeFactory()
   */
  public EdgeFactory<Long, GadDagEdge> getEdgeFactory()
  {
    return edgeFactory;
  }

  /**
   * Set the {@link EdgeSetFactory} to use for this graph. Initially, a graph
   * is created with a default implementation which always supplies an {@link
   * java.util.ArrayList} with capacity 1.
   *
   * @param edgeSetFactory factory to use for subsequently created edge sets
   * (this call has no effect on existing edge sets)
   */
  public void setEdgeSetFactory(EdgeSetFactory<Long, GadDagEdge> edgeSetFactory)
  {
    this.edgeSetFactory = edgeSetFactory;
  }

  /**
   * @see Graph#addEdge(Object, Object)
   */
  public GadDagEdge addEdge(Long sourceVertex, Long targetVertex)
  {
    assertVertexExist(sourceVertex);
    assertVertexExist(targetVertex);

    if (!allowingMultipleEdges
            && containsEdge(sourceVertex, targetVertex))
    {
      return null;
    }

    if (!allowingLoops && sourceVertex.equals(targetVertex)) {
      throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
    }

    GadDagEdge e = edgeFactory.createEdge(sourceVertex, targetVertex);

    if (containsEdge(e)) { // this restriction should stay!

      return null;
    } else {
      IntrusiveEdge intrusiveEdge =
              createIntrusiveEdge(e, sourceVertex, targetVertex);

      edgeMap.put(e, intrusiveEdge);
      specifics.addEdgeToTouchingVertices(e);

      return e;
    }
  }

  /**
   * @see Graph#addEdge(Object, Object, Object)
   */
  public boolean addEdge(Long sourceVertex, Long targetVertex, GadDagEdge e)
  {
    if (e == null) {
      throw new NullPointerException();
    } else if (containsEdge(e)) {
      return false;
    }

    assertVertexExist(sourceVertex);
    assertVertexExist(targetVertex);

    if (!allowingMultipleEdges
            && containsEdge(sourceVertex, targetVertex))
    {
      return false;
    }

    if (!allowingLoops && sourceVertex.equals(targetVertex)) {
      throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
    }

    IntrusiveEdge intrusiveEdge =
            createIntrusiveEdge(e, sourceVertex, targetVertex);

    edgeMap.put(e, intrusiveEdge);
    specifics.addEdgeToTouchingVertices(e);

    return true;
  }

  private IntrusiveEdge createIntrusiveEdge(
          GadDagEdge e,
          Long sourceVertex,
          Long targetVertex)
  {
    IntrusiveEdge intrusiveEdge;
    if (e instanceof IntrusiveEdge) {
      intrusiveEdge = (IntrusiveEdge) e;
    } else {
      intrusiveEdge = new IntrusiveEdge();
    }
    intrusiveEdge.source = sourceVertex;
    intrusiveEdge.target = targetVertex;
    return intrusiveEdge;
  }

  /**
   * @see Graph#addVertex(Object)
   */
  public boolean addVertex(Long v)
  {
    if (v == null) {
      throw new NullPointerException();
    } else if (containsVertex(v)) {
      return false;
    } else {
      specifics.addVertex(v);

      return true;
    }
  }

  /**
   * @see Graph#getEdgeSource(Object)
   */
  public Long getEdgeSource(GadDagEdge e)
  {
    return TypeUtil.uncheckedCast(
            getIntrusiveEdge(e).source,
            vertexTypeDecl);
  }

  /**
   * @see Graph#getEdgeTarget(Object)
   */
  public Long getEdgeTarget(GadDagEdge e)
  {
    return TypeUtil.uncheckedCast(
            getIntrusiveEdge(e).target,
            vertexTypeDecl);
  }

  private IntrusiveEdge getIntrusiveEdge(GadDagEdge e)
  {
    if (e instanceof IntrusiveEdge) {
      return (IntrusiveEdge) e;
    }

    return edgeMap.get(e);
  }

  /**
   * Returns a shallow copy of this graph instance. Neither edges nor vertices
   * are cloned.
   *
   * @return a shallow copy of this set.
   *
   * @throws RuntimeException
   *
   * @see java.lang.Object#clone()
   */
  public Object clone()
  {
    try {
      TypeUtil<GraphForGadDag> typeDecl = null;

      GraphForGadDag newGraph =
              TypeUtil.uncheckedCast(super.clone(), typeDecl);

      newGraph.edgeMap = new LinkedHashMap<GadDagEdge, IntrusiveEdge>();

      newGraph.edgeFactory = this.edgeFactory;
      newGraph.unmodifiableEdgeSet = null;
      newGraph.unmodifiableVertexSet = null;

      // NOTE:  it's important for this to happen in an object
      // method so that the new inner class instance gets associated with
      // the right outer class instance
      newGraph.specifics = newGraph.createSpecifics();

      Graphs.addGraph(newGraph, this);

      return newGraph;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  /**
   * @see Graph#containsEdge(Object)
   */
  public boolean containsEdge(GadDagEdge e)
  {
    return edgeMap.containsKey(e);
  }

  /**
   * @see Graph#containsVertex(Object)
   */
  public boolean containsVertex(Long v)
  {
    return specifics.getVertexSet().contains(v);
  }

  /**
   * @see UndirectedGraph#degreeOf(Object)
   */
  public int degreeOf(Long vertex)
  {
    return specifics.degreeOf(vertex);
  }

  /**
   * @see Graph#edgeSet()
   */
  public Set<GadDagEdge> edgeSet()
  {
    if (unmodifiableEdgeSet == null) {
      unmodifiableEdgeSet = Collections.unmodifiableSet(edgeMap.keySet());
    }

    return unmodifiableEdgeSet;
  }

  /**
   * @see Graph#edgesOf(Object)
   */
  public Set<GadDagEdge> edgesOf(Long vertex)
  {
    return specifics.edgesOf(vertex);
  }

  /**
   * @see DirectedGraph#inDegreeOf(Object)
   */
  public int inDegreeOf(Long vertex)
  {
    return specifics.inDegreeOf(vertex);
  }

  /**
   * @see DirectedGraph#incomingEdgesOf(Object)
   */
  public Set<GadDagEdge> incomingEdgesOf(Long vertex)
  {
    return specifics.incomingEdgesOf(vertex);
  }

  /**
   * @see DirectedGraph#outDegreeOf(Object)
   */
  public int outDegreeOf(Long vertex)
  {
    return specifics.outDegreeOf(vertex);
  }

  /**
   * @see DirectedGraph#outgoingEdgesOf(Object)
   */
  public Set<GadDagEdge> outgoingEdgesOf(Long vertex)
  {
    return specifics.outgoingEdgesOf(vertex);
  }

  /**
   * @see Graph#removeEdge(Object, Object)
   */
  public GadDagEdge removeEdge(Long sourceVertex, Long targetVertex)
  {
    GadDagEdge e = getEdge(sourceVertex, targetVertex);

    if (e != null) {
      specifics.removeEdgeFromTouchingVertices(e);
      edgeMap.remove(e);
    }

    return e;
  }

  /**
   * @see Graph#removeEdge(Object)
   */
  public boolean removeEdge(GadDagEdge e)
  {
    if (containsEdge(e)) {
      specifics.removeEdgeFromTouchingVertices(e);
      edgeMap.remove(e);

      return true;
    } else {
      return false;
    }
  }

  /**
   * @see Graph#removeVertex(Object)
   */
  public boolean removeVertex(Long v)
  {
    if (containsVertex(v)) {
      Set<GadDagEdge> touchingEdgesList = edgesOf(v);

      // cannot iterate over list - will cause
      // ConcurrentModificationException
      removeAllEdges(new ArrayList<GadDagEdge>(touchingEdgesList));

      specifics.getVertexSet().remove(v); // remove the vertex itself

      return true;
    } else {
      return false;
    }
  }

  /**
   * @see Graph#vertexSet()
   */
  public Set<Long> vertexSet()
  {
    if (unmodifiableVertexSet == null) {
      unmodifiableVertexSet =
              Collections.unmodifiableSet(specifics.getVertexSet());
    }

    return unmodifiableVertexSet;
  }

  /**
   * @see Graph#getEdgeWeight(Object)
   */
  public double getEdgeWeight(GadDagEdge e)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see WeightedGraph#setEdgeWeight(Object, double)
   */
  public void setEdgeWeight(GadDagEdge e, double weight)
  {
    throw new UnsupportedOperationException();
  }

  private Specifics createSpecifics()
  {
    return new DirectedSpecifics();
  }

  //~ Inner Classes ----------------------------------------------------------

  /**
   * .
   *
   * @author Barak Naveh
   */
  private abstract class Specifics
          implements Serializable
  {
    private static final long serialVersionUID = 785196247314761183L;

    public abstract void addVertex(Long vertex);

    public abstract Set<Long> getVertexSet();

    /**
     * .
     *
     * @param sourceVertex
     * @param targetVertex
     *
     * @return
     */
    public abstract Set<GadDagEdge> getAllEdges(Long sourceVertex,
                                       Long targetVertex);

    /**
     * .
     *
     * @param sourceVertex
     * @param targetVertex
     *
     * @return
     */
    public abstract GadDagEdge getEdge(Long sourceVertex, Long targetVertex);

    /**
     * Adds the specified edge to the edge containers of its source and
     * target vertices.
     *
     * @param e
     */
    public abstract void addEdgeToTouchingVertices(GadDagEdge e);

    /**
     * .
     *
     * @param vertex
     *
     * @return
     */
    public abstract int degreeOf(Long vertex);

    /**
     * .
     *
     * @param vertex
     *
     * @return
     */
    public abstract Set<GadDagEdge> edgesOf(Long vertex);

    /**
     * .
     *
     * @param vertex
     *
     * @return
     */
    public abstract int inDegreeOf(Long vertex);

    /**
     * .
     *
     * @param vertex
     *
     * @return
     */
    public abstract Set<GadDagEdge> incomingEdgesOf(Long vertex);

    /**
     * .
     *
     * @param vertex
     *
     * @return
     */
    public abstract int outDegreeOf(Long vertex);

    /**
     * .
     *
     * @param vertex
     *
     * @return
     */
    public abstract Set<GadDagEdge> outgoingEdgesOf(Long vertex);

    /**
     * Removes the specified edge from the edge containers of its source and
     * target vertices.
     *
     * @param e
     */
    public abstract void removeEdgeFromTouchingVertices(GadDagEdge e);
  }

  /**
   * A container for vertex edges.
   *
   * <p>In this edge container we use array lists to minimize memory toll.
   * However, for high-degree vertices we replace the entire edge container
   * with a direct access subclass (to be implemented).</p>
   *
   * @author Barak Naveh
   */
  private static class GadDagEdgeContainer
          implements Serializable
  {
    private static final long serialVersionUID = 7494242245729767106L;
    Set<GadDagEdge> incoming;
    Set<GadDagEdge> outgoing;

    GadDagEdgeContainer(EdgeSetFactory<Long, GadDagEdge> edgeSetFactory,
                          Long vertex)
    {
      incoming = edgeSetFactory.createEdgeSet(vertex);
      outgoing = edgeSetFactory.createEdgeSet(vertex);
    }

    public Set<GadDagEdge> getIncoming() {
      return incoming;
    }

    public Set<GadDagEdge> getOutgoing() {
      return outgoing;
    }

    /**
     * .
     *
     * @param e
     */
    public void addIncomingEdge(GadDagEdge e)
    {
      incoming.add(e);
    }

    /**
     * .
     *
     * @param e
     */
    public void addOutgoingEdge(GadDagEdge e)
    {
      outgoing.add(e);
    }

    /**
     * .
     *
     * @param e
     */
    public void removeIncomingEdge(GadDagEdge e)
    {
      incoming.remove(e);
    }

    /**
     * .
     *
     * @param e
     */
    public void removeOutgoingEdge(GadDagEdge e)
    {
      outgoing.remove(e);
    }
  }

  /**
   * .
   *
   * @author Barak Naveh
   */
  private class DirectedSpecifics
          extends Specifics
          implements Serializable
  {
    private static final long serialVersionUID = 8971725103718958232L;
    private static final String NOT_IN_DIRECTED_GRAPH =
            "no such operation in a directed graph";

    private Map<Long, GadDagEdgeContainer> vertexMapDirected =
            new LinkedHashMap<Long, GadDagEdgeContainer>();

    public void addVertex(Long v)
    {
      // add with a lazy edge container entry
      vertexMapDirected.put(v, null);
    }

    public Set<Long> getVertexSet()
    {
      return vertexMapDirected.keySet();
    }

    /**
     * @see Graph#getAllEdges(Object, Object)
     */
    public Set<GadDagEdge> getAllEdges(Long sourceVertex, Long targetVertex)
    {
      Set<GadDagEdge> edges = null;

      if (containsVertex(sourceVertex)
              && containsVertex(targetVertex))
      {
        edges = new ArrayUnenforcedSet<GadDagEdge>();

        GadDagEdgeContainer ec = getEdgeContainer(sourceVertex);

        Iterator<GadDagEdge> iter = ec.outgoing.iterator();

        while (iter.hasNext()) {
          GadDagEdge e = iter.next();

          if (getEdgeTarget(e).equals(targetVertex)) {
            edges.add(e);
          }
        }
      }

      return edges;
    }

    /**
     * @see Graph#getEdge(Object, Object)
     */
    public GadDagEdge getEdge(Long sourceVertex, Long targetVertex)
    {
      if (containsVertex(sourceVertex)
              && containsVertex(targetVertex))
      {
        GadDagEdgeContainer ec = getEdgeContainer(sourceVertex);

        Iterator<GadDagEdge> iter = ec.outgoing.iterator();

        while (iter.hasNext()) {
          GadDagEdge e = iter.next();

          if (getEdgeTarget(e).equals(targetVertex)) {
            return e;
          }
        }
      }

      return null;
    }

    /**
     */
    public void addEdgeToTouchingVertices(GadDagEdge e)
    {
      Long source = getEdgeSource(e);
      Long target = getEdgeTarget(e);

      getEdgeContainer(source).addOutgoingEdge(e);
      getEdgeContainer(target).addIncomingEdge(e);
    }

    /**
     * @see UndirectedGraph#degreeOf(Object)
     */
    public int degreeOf(Long vertex)
    {
      throw new UnsupportedOperationException(NOT_IN_DIRECTED_GRAPH);
    }

    /**
     * @see Graph#edgesOf(Object)
     */
    public Set<GadDagEdge> edgesOf(Long vertex)
    {
      ArrayUnenforcedSet<GadDagEdge> inAndOut =
              new ArrayUnenforcedSet<GadDagEdge>(getEdgeContainer(vertex).incoming);
      inAndOut.addAll(getEdgeContainer(vertex).outgoing);

      // we have two copies for each self-loop - remove one of them.
      if (allowingLoops) {
        Set<GadDagEdge> loops = getAllEdges(vertex, vertex);

        for (int i = 0; i < inAndOut.size();) {
          Object e = inAndOut.get(i);

          if (loops.contains(e)) {
            inAndOut.remove(i);
            loops.remove(e); // so we remove it only once
          } else {
            i++;
          }
        }
      }

      return Collections.unmodifiableSet(inAndOut);
    }

    /**
     */
    public int inDegreeOf(Long vertex)
    {
      return getEdgeContainer(vertex).incoming.size();
    }

    /**
     */
    public Set<GadDagEdge> incomingEdgesOf(Long vertex)
    {
      return getEdgeContainer(vertex).getIncoming();
    }

    /**
     */
    public int outDegreeOf(Long vertex)
    {
      return getEdgeContainer(vertex).outgoing.size();
    }

    /**
     */
    public Set<GadDagEdge> outgoingEdgesOf(Long vertex)
    {
      return getEdgeContainer(vertex).getOutgoing();
    }

    /**
     */
    public void removeEdgeFromTouchingVertices(GadDagEdge e)
    {
      Long source = getEdgeSource(e);
      Long target = getEdgeTarget(e);

      getEdgeContainer(source).removeOutgoingEdge(e);
      getEdgeContainer(target).removeIncomingEdge(e);
    }

    /**
     * A lazy build of edge container for specified vertex.
     *
     * @param vertex a vertex in this graph.
     *
     * @return EdgeContainer
     */
    private GadDagEdgeContainer getEdgeContainer(Long vertex)
    {
      assertVertexExist(vertex);

      GadDagEdgeContainer ec = vertexMapDirected.get(vertex);

      if (ec == null) {
        ec = new GadDagEdgeContainer(edgeSetFactory, vertex);
        vertexMapDirected.put(vertex, ec);
      }

      return ec;
    }
  }

  /**
   * A container of for vertex edges.
   *
   * <p>In this edge container we use array lists to minimize memory toll.
   * However, for high-degree vertices we replace the entire edge container
   * with a direct access subclass (to be implemented).</p>
   *
   * @author Barak Naveh
   */
  private static class UndirectedEdgeContainer<VV, EE>
          implements Serializable
  {
    private static final long serialVersionUID = -6623207588411170010L;
    Set<EE> vertexEdges;
    private transient Set<EE> unmodifiableVertexEdges = null;

    UndirectedEdgeContainer(
            EdgeSetFactory<VV, EE> edgeSetFactory,
            VV vertex)
    {
      vertexEdges = edgeSetFactory.createEdgeSet(vertex);
    }

    /**
     * A lazy build of unmodifiable list of vertex edges
     *
     * @return
     */
    public Set<EE> getUnmodifiableVertexEdges()
    {
      if (unmodifiableVertexEdges == null) {
        unmodifiableVertexEdges =
                Collections.unmodifiableSet(vertexEdges);
      }

      return unmodifiableVertexEdges;
    }

    /**
     * .
     *
     * @param e
     */
    public void addEdge(EE e)
    {
      vertexEdges.add(e);
    }

    /**
     * .
     *
     * @return
     */
    public int edgeCount()
    {
      return vertexEdges.size();
    }

    /**
     * .
     *
     * @param e
     */
    public void removeEdge(EE e)
    {
      vertexEdges.remove(e);
    }
  }

  /**
   * .
   *
   * @author Barak Naveh
   */
  private class UndirectedSpecifics
          extends Specifics
          implements Serializable
  {
    private static final long serialVersionUID = 6494588405178655873L;
    private static final String NOT_IN_UNDIRECTED_GRAPH =
            "no such operation in an undirected graph";

    private Map<Long, UndirectedEdgeContainer<Long, GadDagEdge>> vertexMapUndirected =
            new LinkedHashMap<Long, UndirectedEdgeContainer<Long, GadDagEdge>>();

    public void addVertex(Long v)
    {
      // add with a lazy edge container entry
      vertexMapUndirected.put(v, null);
    }

    public Set<Long> getVertexSet()
    {
      return vertexMapUndirected.keySet();
    }

    /**
     * @see Graph#getAllEdges(Object, Object)
     */
    public Set<GadDagEdge> getAllEdges(Long sourceVertex, Long targetVertex)
    {
      Set<GadDagEdge> edges = null;

      if (containsVertex(sourceVertex)
              && containsVertex(targetVertex))
      {
        edges = new ArrayUnenforcedSet<GadDagEdge>();

        Iterator<GadDagEdge> iter =
                getEdgeContainer(sourceVertex).vertexEdges.iterator();

        while (iter.hasNext()) {
          GadDagEdge e = iter.next();

          boolean equalStraight =
                  sourceVertex.equals(getEdgeSource(e))
                          && targetVertex.equals(getEdgeTarget(e));

          boolean equalInverted =
                  sourceVertex.equals(getEdgeTarget(e))
                          && targetVertex.equals(getEdgeSource(e));

          if (equalStraight || equalInverted) {
            edges.add(e);
          }
        }
      }

      return edges;
    }

    /**
     * @see Graph#getEdge(Object, Object)
     */
    public GadDagEdge getEdge(Long sourceVertex, Long targetVertex)
    {
      if (containsVertex(sourceVertex)
              && containsVertex(targetVertex))
      {
        Iterator<GadDagEdge> iter =
                getEdgeContainer(sourceVertex).vertexEdges.iterator();

        while (iter.hasNext()) {
          GadDagEdge e = iter.next();

          boolean equalStraight =
                  sourceVertex.equals(getEdgeSource(e))
                          && targetVertex.equals(getEdgeTarget(e));

          boolean equalInverted =
                  sourceVertex.equals(getEdgeTarget(e))
                          && targetVertex.equals(getEdgeSource(e));

          if (equalStraight || equalInverted) {
            return e;
          }
        }
      }

      return null;
    }

    /**
     */
    public void addEdgeToTouchingVertices(GadDagEdge e)
    {
      Long source = getEdgeSource(e);
      Long target = getEdgeTarget(e);

      getEdgeContainer(source).addEdge(e);

      if (!source.equals(target)) {
        getEdgeContainer(target).addEdge(e);
      }
    }

    /**
     */
    public int degreeOf(Long vertex)
    {
      if (allowingLoops) { // then we must count, and add loops twice

        int degree = 0;
        Set<GadDagEdge> edges = getEdgeContainer(vertex).vertexEdges;

        for (GadDagEdge e : edges) {
          if (getEdgeSource(e).equals(getEdgeTarget(e))) {
            degree += 2;
          } else {
            degree += 1;
          }
        }

        return degree;
      } else {
        return getEdgeContainer(vertex).edgeCount();
      }
    }

    /**
     */
    public Set<GadDagEdge> edgesOf(Long vertex)
    {
      return getEdgeContainer(vertex).getUnmodifiableVertexEdges();
    }

    /**
     * @see DirectedGraph#inDegreeOf(Object)
     */
    public int inDegreeOf(Long vertex)
    {
      throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
    }

    /**
     * @see DirectedGraph#incomingEdgesOf(Object)
     */
    public Set<GadDagEdge> incomingEdgesOf(Long vertex)
    {
      throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
    }

    /**
     * @see DirectedGraph#outDegreeOf(Object)
     */
    public int outDegreeOf(Long vertex)
    {
      throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
    }

    /**
     * @see DirectedGraph#outgoingEdgesOf(Object)
     */
    public Set<GadDagEdge> outgoingEdgesOf(Long vertex)
    {
      throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
    }

    /**
     */
    public void removeEdgeFromTouchingVertices(GadDagEdge e)
    {
      Long source = getEdgeSource(e);
      Long target = getEdgeTarget(e);

      getEdgeContainer(source).removeEdge(e);

      if (!source.equals(target)) {
        getEdgeContainer(target).removeEdge(e);
      }
    }

    /**
     * A lazy build of edge container for specified vertex.
     *
     * @param vertex a vertex in this graph.
     *
     * @return EdgeContainer
     */
    private UndirectedEdgeContainer<Long, GadDagEdge> getEdgeContainer(Long vertex)
    {
      assertVertexExist(vertex);

      UndirectedEdgeContainer<Long, GadDagEdge> ec = vertexMapUndirected.get(vertex);

      if (ec == null) {
        ec = new UndirectedEdgeContainer<Long, GadDagEdge>(
                edgeSetFactory,
                vertex);
        vertexMapUndirected.put(vertex, ec);
      }

      return ec;
    }
  }
}
