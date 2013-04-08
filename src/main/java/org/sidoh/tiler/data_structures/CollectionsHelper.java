package org.sidoh.tiler.data_structures;

import java.util.*;

public class CollectionsHelper {
  public static <T> Set<T> minus(Set<T> set, T... items) {
    Set<T> copy = new HashSet<T>(set);

    for (T item : items) {
      copy.remove(item);
    }

    return copy;
  }

  public static <T> String join(Iterable<T> values) {
    StringBuilder b = new StringBuilder();

    for (T value : values) {
      b.append(value).append(',');
    }

    return b.toString();
  }

  public static <T> String join(T... values) {
    return join(Arrays.asList(values));
  }

  public static <T> List<T> asList(Iterable<T> itr) {
    List<T> list = new ArrayList<T>();

    for (T t : itr) {
      list.add(t);
    }

    return list;
  }

  public static <T> List<T> asList(Iterator<T> itr) {
    List<T> list = new ArrayList<T>();

    while (itr.hasNext()) {
      T next = itr.next();
      list.add(next);
    }

    return list;
  }

  public static <T extends Comparable<T>> Comparator<T> naturalComparator() {
    return new Comparator<T>() {
      @Override
      public int compare(T t, T t1) {
        if (t == null && t1 == null)
          return 0;
        else if (t == null)
          return -1;
        else
          return t.compareTo(t1);
      }
    };
  }
}
