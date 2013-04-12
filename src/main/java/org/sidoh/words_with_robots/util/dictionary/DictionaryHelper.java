package org.sidoh.words_with_robots.util.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class DictionaryHelper {
  public static Reader getDictionaryResource() {
    try {
      InputStream resource = ClassLoader.getSystemResourceAsStream("wwf-dictionary.gz");
      resource = new GZIPInputStream(resource);
      return new InputStreamReader(resource);
    }
    catch ( IOException e ) {
      throw new RuntimeException(e);
    }
  }
}
