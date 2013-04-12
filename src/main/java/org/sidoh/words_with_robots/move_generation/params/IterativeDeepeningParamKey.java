package org.sidoh.words_with_robots.move_generation.params;

/**
 * Defines some special parameters for use with an iterative deepening algorithm
 */
public enum IterativeDeepeningParamKey implements MoveGeneratorParamKey {
  /**
   * Maximum number of milliseconds to let the algorithm run. Iterative deepening works by
   * iteratively incrementing the number of lookahead plies until it runs out of time.
   */
  MAX_EXECUTION_TIME_IN_MS(120000L),

  /**
   * The minimum number of milliseconds to allow this algorithm to run if a terminal state
   * is not reached. This prevents us from being preempted too early.
   */
  MIN_EXECUTION_TIME_IN_MS(10000L);

  private final Object defaultValue;
  private IterativeDeepeningParamKey(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
}
