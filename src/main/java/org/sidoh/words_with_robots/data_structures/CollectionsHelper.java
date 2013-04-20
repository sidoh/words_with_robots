package org.sidoh.words_with_robots.data_structures;

import com.google.common.collect.Sets;

import java.util.*;

public class CollectionsHelper {
  /**
   *
   * @param set
   * @param items
   * @param <T>
   * @return
   */
  public static <T> Set<T> minus(Set<T> set, T... items) {
    Set<T> copy = Sets.newHashSet(set);

    for (T item : items) {
      copy.remove(item);
    }

    return copy;
  }
}
