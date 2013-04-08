package org.sidoh.tiler;

import org.sidoh.io.StdinPrompts;
import org.sidoh.tiler.data_structures.gaddag.GadDag;

import java.io.FileNotFoundException;
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
