package org.sidoh.words_with_robots.data_structures;

import junit.framework.TestCase;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDagEdge;

import java.io.*;
import java.util.*;

public class TestGadDag extends TestCase {
  public void testAddWord() throws IOException {
    final GadDag gd = new GadDag();

    gd.addWord("care");
    gd.addWord("careen");
  }

  public void testDictionaryFunctionality() throws IOException {
    final GadDag gd = new GadDag();

    Set<String> words = new HashSet<String>(Arrays.asList(
            "wonderful", "wonder", "whoistosay",
            "woid", "void", "brain",
            "brainy"
    ));

    for (String word : words) {
      gd.addWord(word);
    }

    for (String word : words) {
      assertTrue("added word `" + word + "' should be in the dictionary",
              gd.isWord(word));
    }
  }

  private static String byteToString(byte b) {
    return b == GadDag.CONCAT_OPERATOR ? "<>" : ((Character) (char) b).toString();
  }
}

