package org.sidoh.tiler.data_structures.gaddag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class GadDag extends GraphForGadDag {
  private static final Logger LOG = LoggerFactory.getLogger(GadDag.class);

  // State that only has a pointer to initial state
  public static final long NULL_STATE = -1;

  public static final long INITIAL_STATE = 0;
  public static final byte CONCAT_OPERATOR = 0;

  private long stateCounter = 1;
  private final GadDagEdge initEdge;

  public GadDag() {
    super(new GadDagEdgeFactory());
    addVertex(INITIAL_STATE);
    addVertex(NULL_STATE);

    this.initEdge = new GadDagEdge((byte)-1, false);

    addEdge(NULL_STATE, INITIAL_STATE, initEdge);
  }

  public void loadDictionary(Reader reader) throws IOException {
    BufferedReader lineReader = new BufferedReader(reader);
    String line = lineReader.readLine();

    LOG.debug("Loading dictionary from: " + reader);

    while (line != null) {
      addWord(line);

      LOG.debug("added " + line);
      line = lineReader.readLine();
    }
  }

  public boolean isWord(String word) {
    if (word == null || word.length() == 0)
      throw new RuntimeException("Can't check null or empty word");

    LOG.debug("Checking if word: {}", word);

    byte[] bytes = getWordBytes(word);
    long st = getInitialState();
    GadDagEdge arc = getArc( st, bytes[0] );

    if (arc == null) {
      LOG.debug("Failed! No initial arc for `{}'", word.charAt(0));
      return false;
    }

    // Follow arc
    st = getEdgeTarget( arc );

    // Needs concat operator now
    arc = getArc( st, CONCAT_OPERATOR );

    if (arc == null) {
      LOG.debug("Failed: no concat operator");
      return false;
    }

    st = getEdgeTarget( arc );

    for (int i = 1; i < bytes.length - 1; i++) {
      arc = getArc( st, bytes[i] );

      if (arc == null) {
        LOG.debug("Failed at letter: " + word.charAt(i));
        return false;
      }

      st = getEdgeTarget( arc );
    }

    LOG.debug("Checking for letter {} in: {}", arc.getWordLetters(), word.charAt(word.length() - 1));

    return arc.hasWordLetter( bytes[bytes.length - 1] );
  }

  public GadDagEdge nextEdge( GadDagEdge edge, String letter ) {
    return nextEdge(edge, (byte) letter.charAt(0));
  }

  public GadDagEdge nextEdge( GadDagEdge edge, byte b ) {
    GadDagEdgeSet outgoing = (GadDagEdgeSet) outgoingEdgesOf( getEdgeTarget( edge ) );

    return outgoing.getEdgeForLetter( b );
  }

  public GadDagEdge getInitEdge() {
    return initEdge;
  }

  public void addWord(String word) {
    long st = getInitialState();
    byte[] wordBytes = getWordBytes(word);

    LOG.debug("adding word {}", word);

    // create path for a_n a_(n-1) ... a_1
    for (int i = wordBytes.length - 1; i >= 2; i--) {
      Byte letter = wordBytes[i];

      st = getEdgeTarget(addArc(st, letter));
    }
    addFinalArc(st, wordBytes[1], wordBytes[0]);

    // create path for a_(n-1) ... a_1 <> a_n
    st = getInitialState();

    for (int i = wordBytes.length - 2; i >= 0; i--) {
      Byte letter = wordBytes[i];

      st = getEdgeTarget(addArc(st, letter));
    }
    st = getEdgeTarget(addFinalArc(st, CONCAT_OPERATOR, wordBytes[wordBytes.length - 1]));

    long firstForceState = st;

    // create remaining parts
    for (int i = wordBytes.length - 3; i >= 0; i--) {
      long forceSt = st;
      st = getInitialState();

      for (int j = i; j >= 0; j--) {
        st = getEdgeTarget(addArc(st, wordBytes[j]));
      }
      st = getEdgeTarget(addArc(st, CONCAT_OPERATOR));
      GadDagEdge forcedArc = forceArc(st, wordBytes[i + 1], forceSt);

      if (firstForceState == forceSt)
        forcedArc.addWordLetter(wordBytes[ wordBytes.length - 1]);
    }
  }

  public long getInitialState() {
    return INITIAL_STATE;
  }

  public long getNewState() {
    return stateCounter++;
  }

  public GadDagEdge getArc(long st, byte letter) {
    assertVertexExist(st);

   return  ((GadDagEdgeSet) outgoingEdgesOf(st)).getEdgeForLetter(letter);
  }

  public GadDagEdge addArc(long st, byte letter) {
    assertVertexExist(st);

    GadDagEdgeSet edges = (GadDagEdgeSet) outgoingEdgesOf(st);
    GadDagEdge edge = edges.getEdgeForLetter(letter);

    if (edge == null) {
      long newState = getNewState();
      addVertex(newState);

      edge = new GadDagEdge(letter, false);
      addEdge(st, newState, edge);
    }

    return edge;
  }

  public GadDagEdge addFinalArc(long st, byte ch1, byte ch2) {
    assertVertexExist(st);

    GadDagEdge edge = addArc(st, ch1);
    edge.addWordLetter(ch2);

    LOG.debug("final arc added (new letter: {}): {}", (char)ch2, edge);

    return edge;
  }

  public GadDagEdge forceArc(long st, byte letter, long destSt) {
    assertVertexExist(st);
    assertVertexExist(destSt);

    GadDagEdgeSet edges = (GadDagEdgeSet) outgoingEdgesOf(st);
    GadDagEdge existingEdge = edges.getEdgeForLetter(letter);

    if (existingEdge != null && getEdgeTarget(existingEdge) != destSt)
      throw new IllegalStateException();
    else if (existingEdge == null) {
      GadDagEdge newEdge = new GadDagEdge(letter, existingEdge != null);
      addEdge(st, destSt, newEdge);

      return newEdge;
    }
    else {
      return existingEdge;
    }
  }

  protected static byte[] getWordBytes(String word) {
    byte[] bytes = word.getBytes();

    if (bytes.length != word.length())
      throw new RuntimeException("unsupported charset");

    return bytes;
  }
}
