package org.sidoh.words_with_robots.data_structures.gaddag;

import org.sidoh.words_with_robots.data_structures.LetterSet;

public final class GadDagEdge extends IntrusiveEdge {
  private final LetterSet wordLetters;
  private final byte destinationLetter;

  public GadDagEdge(char destinationLetter, LetterSet.Factory letterSetFactory) {
    this.wordLetters = letterSetFactory.build();
    this.destinationLetter = (byte)destinationLetter;
  }

  public boolean hasWordLetter(String l) {
    return hasWordLetter(l.charAt(0));
  }

  public void addWordLetter(char b) {
    wordLetters.add(b);
  }

  public boolean hasWordLetter(char b) {
    return wordLetters.contains(b);
  }

  public char getDestinationLetter() {
    return (char)destinationLetter;
  }
}
