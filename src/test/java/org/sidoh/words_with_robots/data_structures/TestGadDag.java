package org.sidoh.words_with_robots.data_structures;

import junit.framework.TestCase;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.sidoh.tiler.data_structures.gaddag.GadDag;
import org.sidoh.tiler.data_structures.gaddag.GadDagEdge;

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

//  public void testLoadDictionary() throws IOException {
//    GadDag gd = new GadDag();
//
//    gd.loadDictionary(new FileReader("/tmp/enable1"));
//
//    BufferedReader reader = new BufferedReader(new FileReader("/tmp/enable1"));
//    String line = reader.readLine();
//
//    while (line != null) {
//      assertTrue("should contain " + line, gd.isWord(line));
//
//      line = reader.readLine();
//    }
//  }

  private static String byteToString(byte b) {
    return b == GadDag.CONCAT_OPERATOR ? "<>" : ((Character) (char) b).toString();
  }

  public static DOTExporter<Long, GadDagEdge> getDotExporter(final GadDag gd) {
    return new DOTExporter<Long, GadDagEdge>(
            new VertexNameProvider<Long>() {
              @Override
              public String getVertexName(Long aLong) {
                return aLong.toString();
              }
            },
            new VertexNameProvider<Long>() {
              @Override
              public String getVertexName(Long aLong) {
                if (aLong == 0) return "initial";
  //                else {
  //                  GadDagEdge edge = gd.dawg.incomingEdgesOf(aLong).iterator().next();
  //                  byte letter = edge.getDestinationLetter();
  //                  char cLetter = (char)letter;
  //
  //                  return letter == GadDag.GadDagInternalGraph.CONCAT_OPERATOR
  //                          ? "<>"
  //                          : ((Character)cLetter).toString();
  //                }
                else return aLong.toString();
              }
            },
            new EdgeNameProvider<GadDagEdge>() {
              @Override
              public String getEdgeName(GadDagEdge gadDagEdge) {
                return byteToString(gadDagEdge.getDestinationLetter()) + " / " + gd.getEdgeTarget(gadDagEdge);
              }
            },
            new ComponentAttributeProvider<Long>() {
              @Override
              public Map<String, String> getComponentAttributes(Long component) {
                return null;
              }
            },
            new ComponentAttributeProvider<GadDagEdge>() {
              @Override
              public Map<String, String> getComponentAttributes(GadDagEdge component) {
                if (!component.isDuplicate()) return null;
                else return new HashMap<String, String>() {{
                  put("color", "red");
                }};
              }
            });
  }
}

