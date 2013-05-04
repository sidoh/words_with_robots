package org.sidoh.words_with_robots.move_generation.params;

import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.words_with_robots.move_generation.old_params.PreemptionContext;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;

public class IterativeDeepeningGeneratorParams extends WwfMoveGeneratorParams {
  private final long maxExecutionTime;
  private final long minExecutionTime;
  private final boolean verboseStats;
  private final PreemptionContext preemptionContext;
  private final FixedDepthGeneratorParams.Builder fixedDepthParamsBuilder;

  private static class DefaultValues {
    public static final long MAX_EXECUTION_TIME = 120000L;
    public static final long MIN_EXECUTION_TIME = 10000L;
    public static final boolean VERBOSE_STATS_ENABLED = true;
    public static final PreemptionContext PREEMPTION_CONTEXT = null;
    public static final FixedDepthGeneratorParams.Builder FIXED_DEPTH_PARAMS_BUILDER
      = new FixedDepthGeneratorParams.Builder();
  }

  public static class Builder extends AbstractBuilder<IterativeDeepeningGeneratorParams, Builder> {
    private long maxExecutionTime = DefaultValues.MAX_EXECUTION_TIME;
    private long minExecutionTime = DefaultValues.MIN_EXECUTION_TIME;
    private boolean verboseStats = DefaultValues.VERBOSE_STATS_ENABLED;
    private PreemptionContext preemptionContext = DefaultValues.PREEMPTION_CONTEXT;
    private FixedDepthGeneratorParams.Builder fixedDepthParamsBuilder = DefaultValues.FIXED_DEPTH_PARAMS_BUILDER;

    @Override
    protected IterativeDeepeningGeneratorParams build(Rack rack, WordsWithFriendsBoard board) {
      throw new UnsupportedOperationException();
    }

    @Override
    public IterativeDeepeningGeneratorParams build(GameState state) {
      return new IterativeDeepeningGeneratorParams(buildRack(state),
        buildBoard(state),
        getEvaluationFunction(),
        state,
        maxExecutionTime,
        minExecutionTime,
        verboseStats,
        preemptionContext,
        fixedDepthParamsBuilder);
    }

    @Override
    protected Builder getBuilderInstance() {
      return this;
    }

    /**
     * Maximum number of milliseconds to let the algorithm run. Iterative deepening works by
     * iteratively incrementing the number of lookahead plies until it runs out of time.
     *
     * @param maxExecutionTime value to set max execution time to
     * @return this
     */
    public Builder setMaxExecutionTime(long maxExecutionTime) {
      this.maxExecutionTime = maxExecutionTime;
      return this;
    }

    /**
     * The minimum number of milliseconds to allow this algorithm to run if a terminal state
     * is not reached. This prevents us from being preempted too early.
     *
     * @param minExecutionTime value to set min execution time to
     * @return this
     */
    public Builder setMinExecutionTime(long minExecutionTime) {
      this.minExecutionTime = minExecutionTime;
      return this;
    }

    /**
     * If true, compute some potentially resource-intensive stats
     *
     * @param verboseStats if true, enable verbose stats
     * @return this
     */
    public Builder setVerboseStats(boolean verboseStats) {
      this.verboseStats = verboseStats;
      return this;
    }

    /**
     *
     * @param preemptionContext
     * @return
     */
    public Builder setPreemptionContext(PreemptionContext preemptionContext) {
      this.preemptionContext = preemptionContext;
      return this;
    }

    /**
     *
     * @param fixedDepthParamsBuilder
     * @return
     */
    public Builder setFixedDepthParamsBuilder(FixedDepthGeneratorParams.Builder fixedDepthParamsBuilder) {
      this.fixedDepthParamsBuilder = fixedDepthParamsBuilder;
      return this;
    }
  }

  protected IterativeDeepeningGeneratorParams(Rack rack,
                                              WordsWithFriendsBoard board,
                                              EvaluationFunction evalFn,
                                              GameState state,
                                              long maxExecutionTime,
                                              long minExecutionTime,
                                              boolean verboseStats,
                                              PreemptionContext preemptionContext,
                                              FixedDepthGeneratorParams.Builder fixedDepthParamsBuilder) {
    super(rack, board, evalFn, state);
    this.maxExecutionTime = maxExecutionTime;
    this.minExecutionTime = minExecutionTime;
    this.verboseStats = verboseStats;
    this.preemptionContext = preemptionContext;
    this.fixedDepthParamsBuilder = fixedDepthParamsBuilder;
  }

  public long getMaxExecutionTime() {
    return maxExecutionTime;
  }

  public long getMinExecutionTime() {
    return minExecutionTime;
  }

  public boolean isVerboseStats() {
    return verboseStats;
  }

  public PreemptionContext getPreemptionContext() {
    return preemptionContext;
  }

  public FixedDepthGeneratorParams.Builder getFixedDepthParamsBuilder() {
    return fixedDepthParamsBuilder;
  }
}
