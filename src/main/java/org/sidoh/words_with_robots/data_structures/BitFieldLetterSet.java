package org.sidoh.words_with_robots.data_structures;

import org.apache.commons.lang.BitField;

/**
 * A very memory-efficient implementation of LetterSet. Supports only A-Z and ignores case.
 */
public class BitFieldLetterSet implements LetterSet{
  public static class Factory extends LetterSet.Factory {
    public LetterSet build() {
      return new BitFieldLetterSet();
    }
  }

  private int letterBitField = 0;

  @Override
  public void add(char letter) {
    letterBitField |= (1 << getLetterPosition(letter));
  }

  @Override
  public boolean contains(char letter) {
    return ((letterBitField >> getLetterPosition(letter)) & 1) == 1;
  }

  protected int getLetterPosition(char letter) {
    return Character.toUpperCase(letter) - 0x41;
  }
}
