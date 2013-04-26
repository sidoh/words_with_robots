package org.sidoh.words_with_robots.data_structures;

/**
 * Defines a generic interface for a LetterSet, which is conceptually equivalent to
 * Set&lt;Char&gt;, but allows for more memory-efficient implementations.
 */
public interface LetterSet {
  public abstract class Factory {
    public abstract LetterSet build();
  }

  /**
   * Add a letter to this letter set. After this is set, contains(letter) should return
   * true.
   *
   * @param letter
   */
  public void add(char letter);

  /**
   * Check if a provided letter is contained in the letter set
   *
   * @param letter
   * @return
   */
  public boolean contains(char letter);
}
