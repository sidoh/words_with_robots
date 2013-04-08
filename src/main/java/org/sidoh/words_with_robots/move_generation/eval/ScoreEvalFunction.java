package org.sidoh.words_with_robots.move_generation.eval;

import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;

public class ScoreEvalFunction implements EvaluationFunction {
  @Override
  public double score(Move move, GameState state) {
    if ( move == null || move.getResult() == null)
      return 0;
    else
      return move.getResult().getScore();
  }
}
