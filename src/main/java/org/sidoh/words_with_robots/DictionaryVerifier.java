package org.sidoh.words_with_robots;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.sidoh.words_with_robots.util.dictionary.DictionaryHelper;
import org.sidoh.wwf_api.StatefulApiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DictionaryVerifier {
  private static final Logger LOG = LoggerFactory.getLogger(DictionaryVerifier.class);
  private static final int WORD_GROUP_SIZE = 10;

  public static void main(String[] args) throws IOException {
    String accessToken = args[0];
    StatefulApiProvider apiProvider = new StatefulApiProvider(accessToken);
    BufferedReader reader = new BufferedReader(DictionaryHelper.getDictionaryResource());

    String line = reader.readLine();
    List<String> wordBuffer = Lists.newArrayList();
    Set<String> notInDictionaryWords = Sets.newHashSet();

    while ( line != null ) {
      line = reader.readLine();
      wordBuffer.add(line);

      if ( wordBuffer.size() >= WORD_GROUP_SIZE ) {
        List<String> response = apiProvider.dictionaryLookup(wordBuffer);
        int sizeBefore = notInDictionaryWords.size();
        notInDictionaryWords.addAll(response);
        if ( notInDictionaryWords.size() > sizeBefore ) {
          LOG.info("New words not in dictionary: {}", notInDictionaryWords);
        }
        wordBuffer.clear();
      }
    }
  }
}