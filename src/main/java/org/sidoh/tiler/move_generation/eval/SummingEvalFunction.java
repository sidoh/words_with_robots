package org.sidoh.tiler.move_generation.eval;

import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;

import java.util.Arrays;
import java.util.List;

public class SummingEvalFunction implements EvaluationFunction {
  private final List<EvaluationFunction> parts;

  public SummingEvalFunction(EvaluationFunction... parts) {
    this.parts = Arrays.asList(parts);
  }

  @Override
  public double score(Move move, GameState state) {
    int score = 0;

    for (EvaluationFunction part : parts) {
      score += part.score(move, state);
    }

    return score;
  }
}
