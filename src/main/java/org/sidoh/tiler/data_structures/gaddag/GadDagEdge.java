package org.sidoh.tiler.data_structures.gaddag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class GadDagEdge extends IntrusiveEdge {
  private final Set<Byte> wordLetters;
  private final byte destinationLetter;
  private final boolean duplicate;

  public GadDagEdge(byte destinationLetter, boolean duplicate) {
    this.destinationLetter = destinationLetter;
    this.duplicate = duplicate;
    this.wordLetters = new HashSet<Byte>();
  }

  public Set<Byte> getWordLetters() {
    return Collections.unmodifiableSet(wordLetters);
  }

  public boolean hasWordLetter(String l) {
    return hasWordLetter(l.getBytes()[0]);
  }

  public void addWordLetter(Byte b) {
    wordLetters.add(b);
  }

  public boolean hasWordLetter(Byte b) {
    return wordLetters.contains(b);
  }

  public byte getDestinationLetter() {
    return destinationLetter;
  }

  public boolean isDuplicate() {
    return duplicate;
  }

  @Override
  public String toString() {
    return "GadDagEdge{" +
      "wordLetters=" + wordLetters + "("+decodeLetters(wordLetters) +")"+
      ", destinationLetter=" + destinationLetter +
      ", duplicate=" + duplicate +
      '}';
  }

  public static String decodeLetters(Collection<Byte> bytes) {
    StringBuilder b = new StringBuilder();

    for (Byte aByte : bytes) {
      b.append(new String(new byte[] { aByte })).append(',');
    }

    return b.toString();
  }
}
