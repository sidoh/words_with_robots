package org.sidoh.words_with_robots.data_structures;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestBitFieldLetterSet {
  @Test
  public void testAddingWorks() {
    LetterSet set = new BitFieldLetterSet();

    set.add('a');
    assertTrue(set.contains('a'));
  }

  @Test
  public void testAddingMultipleWords() {
    LetterSet set = new BitFieldLetterSet();

    String lettersToAdd = "ASDFGHJKLZXCVBNM";
    Set<Character> addedLetters = Sets.newHashSet();

    for (int i = 0; i < lettersToAdd.length(); i++) {
      set.add(lettersToAdd.charAt(i));
      addedLetters.add(lettersToAdd.charAt(i));
    }

    for (int i = 0; i < lettersToAdd.length(); i++) {
      char letter = lettersToAdd.charAt(i);
      assertTrue("set should contain " + letter + " after adding it",
        set.contains(letter));
    }

    for (char c = 'A'; c <= 'Z'; c++) {
      if (!addedLetters.contains(c)) {
        assertFalse("set shouldn't contain " + c + " because it wasn't added",
          set.contains(c));
      }
    }
  }
}
