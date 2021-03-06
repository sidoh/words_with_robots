package org.sidoh.words_with_robots.move_generation.util;

import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;

import java.util.Collections;
import java.util.Comparator;

public class MoveScoreComparator implements Comparator<Move> {
  private final EvaluationFunction fn;
  private final GameState state;

  public MoveScoreComparator(EvaluationFunction fn, GameState state) {
    this.fn = fn;
    this.state = state;
  }

  @Override
  public int compare(Move move, Move move1) {
    double score1 = fn.score(move, state);
    double score2 = fn.score(move1, state);

    if (score1 > score2) {
      return 1;
    }
    else if (score1 < score2) {
      return -1;
    }
    else {
      return 0;
    }
  }

  /**
   * Gets a comparator that puts the highest-scoring moves at the front of the list
   *
   * @return
   */
  public static Comparator<Move> rawScoreComparator() {
    return Collections.reverseOrder(new MoveScoreComparator(new ScoreEvalFunction(), null));
  }
}
