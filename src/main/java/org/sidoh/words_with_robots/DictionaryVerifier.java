package org.sidoh.words_with_robots;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.sidoh.words_with_robots.util.dictionary.DictionaryHelper;
import org.sidoh.wwf_api.StatefulApiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DictionaryVerifier {
  private static final Logger LOG = LoggerFactory.getLogger(DictionaryVerifier.class);
  private static final int WORD_GROUP_SIZE = 100;

  public static void main(String[] args) throws IOException {
    String accessToken = args[0];
    StatefulApiProvider apiProvider = new StatefulApiProvider(accessToken);
    BufferedReader reader = new BufferedReader(DictionaryHelper.getDictionaryResource());

    LOG.info("Attempting to read progress");
    File progressFile = new File("known_non_words.txt");
    List<String> notInDictionaryWords = Lists.newArrayList();
    String lastNonWord = null;
    if ( progressFile.exists() ) {
      BufferedReader progressReader = new BufferedReader(new FileReader(progressFile));
      String progressLine = progressReader.readLine();
      while ( progressLine != null ) {
        notInDictionaryWords.add(progressLine);
        progressLine = progressReader.readLine();
      }
      Collections.sort(notInDictionaryWords);
      lastNonWord = notInDictionaryWords.get(notInDictionaryWords.size() - 1);
      progressReader.close();
    }

    LOG.info("Initialized - seeking to: " + lastNonWord);

    String line = reader.readLine();
    List<String> wordBuffer = Lists.newArrayList();

    while ( line != null ) {
      if ( lastNonWord != null && line.compareTo(lastNonWord) < 0 ) continue;
      wordBuffer.add(line);

      if ( wordBuffer.size() >= WORD_GROUP_SIZE ) {
        List<String> response = apiProvider.dictionaryLookup(wordBuffer);
        int sizeBefore = notInDictionaryWords.size();
        notInDictionaryWords.addAll(response);
        LOG.info("tried - {}",wordBuffer);
        if ( notInDictionaryWords.size() > sizeBefore ) {
          LOG.info("New words not in dictionary: {}", notInDictionaryWords);
          PrintWriter progressWriter = new PrintWriter(new FileWriter(progressFile));
          for (String notInDictionaryWord : notInDictionaryWords) {
            progressWriter.println(notInDictionaryWord);
          }
          progressWriter.close();
        }
        wordBuffer.clear();
      }
      line = reader.readLine();
    }
  }
}