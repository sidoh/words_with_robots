package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.wwf_api.game_state.Board;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.game_state.Rack;

public abstract class MoveGenerator<T extends Board> {
  public static class Params {
    private EvaluationFunction evalFunction;

    public Params() {
      this(new ScoreEvalFunction());
    }

    public Params(EvaluationFunction evalFunction) {
      this.evalFunction = evalFunction;
    }

    public Params(Params params) {
      this.evalFunction = params.evalFunction;
    }

    public Params setEvalFunction(EvaluationFunction evalFunction) {
      this.evalFunction = evalFunction;
      return this;
    }

    public EvaluationFunction getEvalFunction() {
      return evalFunction;
    }
  }

  public abstract Move generateMove(Rack rack, T board, Params params);
}
