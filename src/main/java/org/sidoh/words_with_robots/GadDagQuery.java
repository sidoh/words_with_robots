package org.sidoh.words_with_robots;

import org.sidoh.words_with_robots.util.io.StdinPrompts;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;

import java.io.FileReader;
import java.io.IOException;

public class GadDagQuery {
  public static void main(String[] args) throws IOException {
    GadDag gaddag = new GadDag();
    gaddag.loadDictionary(new FileReader(args[0]));

    while (true) {
      String line = StdinPrompts.promptForLine("enter word").toUpperCase();

      System.out.println("`" + line + "' in dictionary: " + gaddag.isWord(line));
    }
  }
}
