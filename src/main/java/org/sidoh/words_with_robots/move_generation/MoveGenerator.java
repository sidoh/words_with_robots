package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.context.MoveGeneratorReturnContext;
import org.sidoh.wwf_api.game_state.Board;
import org.sidoh.wwf_api.types.game_state.Rack;

public interface MoveGenerator<T extends Board, R extends MoveGeneratorReturnContext> {
  /**
   * Given the state defined in the parameters, generate a move to play.
   *
   * @param rack the player's rack
   * @param board the current board
   * @return the "best" move according to this move generator
   */
  public R generateMove(Rack rack, T board);
}
