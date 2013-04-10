package org.sidoh.words_with_robots.move_generation.params;

/**
 * Defines some special parameters for use with an iterative deepening algorithm
 */
public enum IterativeDeepeningParamKey implements MoveGeneratorParamKey {
  MAX_EXECUTION_TIME_IN_MS(10000);

  private final Object defaultValue;
  private IterativeDeepeningParamKey(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
}
