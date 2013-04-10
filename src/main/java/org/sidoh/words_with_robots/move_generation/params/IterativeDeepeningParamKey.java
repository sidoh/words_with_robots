package org.sidoh.words_with_robots.move_generation.params;

/**
 * Defines some special parameters for use with an iterative deepening algorithm
 */
public enum IterativeDeepeningParamKey implements MoveGeneratorParamKey {
  /**
   * Maximum number of milliseconds to let the algorithm run. Iterative deepening works by
   * iteratively incrementing the number of lookahead plies until it runs out of time.
   */
  MAX_EXECUTION_TIME_IN_MS(60000L);

  private final Object defaultValue;
  private IterativeDeepeningParamKey(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
}
