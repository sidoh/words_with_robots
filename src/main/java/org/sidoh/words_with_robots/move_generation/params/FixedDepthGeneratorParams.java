package org.sidoh.words_with_robots.move_generation.params;

import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.words_with_robots.move_generation.old_params.PreemptionContext;
import org.sidoh.words_with_robots.move_generation.swap_strategies.MinScoreThresholdSwapStrategy;
import org.sidoh.words_with_robots.move_generation.swap_strategies.SwapStrategy;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;

public class FixedDepthGeneratorParams extends WwfMoveGeneratorParams {
  private final int maxDepth;
  private final int moveCacheSize;
  private final int minScore;
  private final int branchingFactorLimit;
  private final PreemptionContext preemptionContext;
  private final SwapStrategy swapStrategy;

  private static class DefaultValues {
    public static final int MOVE_CACHE_SIZE = 0;
    public static final int MIN_SCORE = 3;
    public static final int BRANCHING_FACTOR_LIMIT = 20;
    public static final PreemptionContext PREEMPTION_CONTEXT = null;
    public static final SwapStrategy SWAP_STRATEGY = new MinScoreThresholdSwapStrategy();
    public static final int MAX_DEPTH = 2;
  }

  public static class Builder extends AbstractBuilder<FixedDepthGeneratorParams, Builder> {
    private int maxDepth = DefaultValues.MAX_DEPTH;
    private int moveCacheSize = DefaultValues.MOVE_CACHE_SIZE;
    private int minScore = DefaultValues.MIN_SCORE;
    private int branchingFactorLimit = DefaultValues.BRANCHING_FACTOR_LIMIT;
    private PreemptionContext preemptionContext = DefaultValues.PREEMPTION_CONTEXT;
    private SwapStrategy swapStrategy = DefaultValues.SWAP_STRATEGY;

    @Override
    public FixedDepthGeneratorParams build(GameState state) {
      return new FixedDepthGeneratorParams(buildRack(state),
        buildBoard(state),
        getEvaluationFunction(),
        state,
        maxDepth,
        moveCacheSize,
        minScore,
        branchingFactorLimit,
        preemptionContext,
        swapStrategy);
    }

    @Override
    protected Builder getBuilderInstance() {
      return this;
    }

    /**
     * The maximum size of the move cache. The move cache is passed to sibling nodes and contains the
     * best-scoring moves from previous sibling game states. This sometimes helps us avoid repeated
     * work in re-generating moves for a given state.
     *
     * Unfortunately, it doesn't really work when depth >2 because it cannot know that the outcome
     * will be the same for later calls since the parent of the move could be different. Support for
     * this should probably be removed, but I'd like to do some more investigation before doing that.
     *
     * @param moveCacheSize the value to assign to the cache size
     * @return this
     */
    public Builder setMoveCacheSize(int moveCacheSize) {
      this.moveCacheSize = moveCacheSize;
      return this;
    }


    /**
     * The minimum score a move must have in order for us to consider it. If no moves have a score of
     * at least this value, we'll still consider the first one.
     *
     * @param minScore the value to assign to min score
     * @return this
     */
    public Builder setMinScore(int minScore) {
      this.minScore = minScore;
      return this;
    }


    /**
     * The maximum number of moves to consider at each level. Limiting this helps reduce computation
     * time, but comes at the cost of loss of information (meaning potentially sub-optimal moves will
     * be generated).
     *
     * @param branchingFactorLimit the value to assign to branching factor limit
     * @return this
     */
    public Builder setBranchingFactorLimit(int branchingFactorLimit) {
      this.branchingFactorLimit = branchingFactorLimit;
      return this;
    }


    /**
     * FixedDepthMoveGenerator will look for this signal at every node in the search tree. If it's set
     * to true, it will stop execution and return the best value it's found so far. Use this to stop
     * execution early.
     *
     * @param preemptionContext the value to assign to preemption context
     * @return this
     */
    public Builder setPreemptionContext(PreemptionContext preemptionContext) {
      this.preemptionContext = preemptionContext;
      return this;
    }

    /**
     * Sets the maximum depth to search
     *
     * @param maxDepth
     * @return
     */
    public Builder setMaxDepth(int maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }
  }

  protected FixedDepthGeneratorParams(Rack rack,
                                      WordsWithFriendsBoard board,
                                      EvaluationFunction evalFn,
                                      GameState state,
                                      int maxDepth,
                                      int moveCacheSize,
                                      int minScore,
                                      int branchingFactorLimit,
                                      PreemptionContext preemptionContext,
                                      SwapStrategy swapStrategy) {
    super(rack, board, evalFn, state);
    this.maxDepth = maxDepth;
    this.moveCacheSize = moveCacheSize;
    this.minScore = minScore;
    this.branchingFactorLimit = branchingFactorLimit;
    this.preemptionContext = preemptionContext;
    this.swapStrategy = swapStrategy;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public int getMoveCacheSize() {
    return moveCacheSize;
  }

  public int getMinScore() {
    return minScore;
  }

  public int getBranchingFactorLimit() {
    return branchingFactorLimit;
  }

  public PreemptionContext getPreemptionContext() {
    return preemptionContext;
  }

  public SwapStrategy getSwapStrategy() {
    return swapStrategy;
  }
}
