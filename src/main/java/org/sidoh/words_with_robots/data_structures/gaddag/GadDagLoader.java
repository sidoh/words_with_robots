package org.sidoh.words_with_robots.data_structures.gaddag;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GadDagLoader {
  public static void main(String[] args) throws IOException {
    File dict = new File(args[0]);
    GadDag gaddag = new GadDag();

    gaddag.loadDictionary(new FileReader(dict));
  }
}
