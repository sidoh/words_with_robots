package org.sidoh.words_with_robots.move_generation.params;

import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;

public class WwfMinimaxLocalParams extends WwfMoveGeneratorParams {
  private static final int DEFAULT_MOVE_CACHE_SIZE = 100;
  private static final double DEFAULT_CACHE_MISS_FLUSH_FACTOR = 0.5f;
  private static final int DEFAULT_MOVE_DIFF_THRESHOLD = Integer.MAX_VALUE;

  private final int cacheSize;
  private final double cacheMissFlushFactor;
  private final int diffThreshold;

  public static class Builder extends AbstractParamsBuilder<WwfMinimaxLocalParams, Builder> {
    private int diffThreshold;
    private int cacheSize;
    private double cacheMissFlushFactor;

    public Builder() {
      super();
      this.cacheSize = DEFAULT_MOVE_CACHE_SIZE;
      this.cacheMissFlushFactor = DEFAULT_CACHE_MISS_FLUSH_FACTOR;
      this.diffThreshold = DEFAULT_MOVE_DIFF_THRESHOLD;
    }

    @Override
    public WwfMinimaxLocalParams build(Rack rack, WordsWithFriendsBoard board) {
      throw new UnsupportedOperationException();
    }

    @Override
    public WwfMinimaxLocalParams build(GameState state) {
      return new WwfMinimaxLocalParams(buildRack(state),
        buildBoard(state),
        getEvaluationFunction(),
        state,
        cacheSize,
        cacheMissFlushFactor,
        diffThreshold);
    }

    @Override
    protected Builder getBuilderInstance() {
      return this;
    }
  }

  protected WwfMinimaxLocalParams(Rack rack, WordsWithFriendsBoard board, EvaluationFunction evalFn, GameState state, int cacheSize, double missFlushFactor, int diffThreshold) {
    super(rack, board, evalFn, state);
    this.cacheSize = cacheSize;
    this.cacheMissFlushFactor = missFlushFactor;
    this.diffThreshold = diffThreshold;
  }

  public int getCacheSize() {
    return cacheSize;
  }

  public double getCacheMissFlushFactor() {
    return cacheMissFlushFactor;
  }

  public int getDiffThreshold() {
    return diffThreshold;
  }

  public GameState getGameState() {
    return super.getGameState();
  }
}
