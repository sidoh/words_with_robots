package org.sidoh.words_with_robots.move_generation.old_params;

/**
 * Defines a parameter key for use in MoveGeneratorParams. Intended to be extended by
 * enums.
 */
public interface MoveGeneratorParamKey {
  public <V> V getDefaultValue();
}
