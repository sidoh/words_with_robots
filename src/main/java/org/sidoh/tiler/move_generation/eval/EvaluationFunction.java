package org.sidoh.tiler.move_generation.eval;

import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;

public interface EvaluationFunction {
  /**
   * Should return a score for a given move. Higher is better.
   *
   * @param move
   * @param state
   * @return
   */
  public double score(Move move, GameState state);
}
