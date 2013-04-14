package org.sidoh.words_with_robots.move_generation.params;

import com.google.common.collect.Maps;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;

/**
 * Defines parameters to be used (in general) by words with friends move generators
 */
public enum WwfMoveGeneratorParamKey implements MoveGeneratorParamKey {
  /**
   * The evaluation function used to compare moves. By default, we consider only the number of
   * points each move is worth.
   */
  EVAL_FUNCTION(new ScoreEvalFunction()),

  /**
   * When this is set, allows execution to end early when a move with a greater diff than this
   * value is found. Defaults to Integer.MAX_VALUE, which means we never exit early.
   */
  DEFAULT_DIFF_THRESHOLD(Integer.MAX_VALUE),

  /**
   * This has no use externally. It's used to pass state between recursive calls.
   */
  BEST_OPPONENT_MOVE(null),

  /**
   * Allows move generators to produce statistics which consumers an track
   */
  GAME_STATS,

  /**
   * REQUIRED PARAM! The GameState for the corresponding board.
   */
  GAME_STATE;

  private final Object defaultValue;
  private final boolean required;

  WwfMoveGeneratorParamKey(Object defaultValue) {
    this.defaultValue = defaultValue;
    this.required = false;
  }

  WwfMoveGeneratorParamKey() {
    this.defaultValue = null;
    this.required = true;
  }

  @Override
  public Object getDefaultValue() {
    if ( !required ) {
      return defaultValue;
    }
    else if ( this == GAME_STATS ) {
      return Maps.<String, Object>newHashMap();
    }
    else {
      throw new RuntimeException("Param: " + this + " is required!");
    }
  }
}
