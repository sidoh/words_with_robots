package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.context.MoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.wwf_api.game_state.Board;
import org.sidoh.wwf_api.game_state.Move;

public interface MoveGenerator<T extends Board,
                               P extends MoveGeneratorParams<T>,
                               R extends MoveGeneratorReturnContext> {
  /**
   * Given the state defined in the parameters, generate a move to play.
   *
   * @param params parameters required by the implementation. these include things specifying game state
   * @return the "best" move according to this move generator
   */
  public R generateMove(P params);
}
