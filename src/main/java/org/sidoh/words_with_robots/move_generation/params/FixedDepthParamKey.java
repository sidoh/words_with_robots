package org.sidoh.words_with_robots.move_generation.params;

/**
 * Params to be used with a fixed depth move algorithm
 */
public enum FixedDepthParamKey implements MoveGeneratorParamKey {
  /**
   * The maximum number of levels to search
   */
  MAXIMUM_DEPTH(4),

  /**
   * The maximum size of the move cache. The move cache is passed to sibling nodes and contains the
   * best-scoring moves from previous sibling game states. This sometimes helps us avoid repeated
   * work in re-generating moves for a given state.
   */
  MOVE_CACHE_SIZE(100),

  /**
   * The minimum score a move must have in order for us to consider it. If no moves have a score of
   * at least this value, we'll still consider the first one.
   */
  MIN_SCORE(10),

  /**
   * The maximum number of moves to consider at each level. Limiting this helps reduce computation
   * time, but comes at the cost of loss of information (meaning potentially sub-optimal moves will
   * be generated).
   */
  BRANCHING_FACTOR_LIMIT(20),

  /**
   * FixedDepthMoveGenerator will look for this signal at every node in the search tree. If it's set
   * to true, it will stop execution and return the best value it's found so far. Use this to stop
   * execution early.
   */
  KILL_SIGNAL(new KillSignalBeacon());

  private final Object defaultValue;
  private FixedDepthParamKey(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
}
