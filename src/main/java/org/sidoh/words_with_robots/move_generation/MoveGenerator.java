package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.wwf_api.game_state.Board;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.game_state.Rack;

public abstract class MoveGenerator<T extends Board> {

  /**
   * Convenience method that calls gemerateMove(Rack, T, Map) with an empty map for the
   * params.
   *
   * @param rack user's rack containing tiles available for play
   * @param board board containing all previous moves
   * @return the "best" move according to this move generator
   */
  public final Move generateMove(Rack rack, T board) {
    return generateMove(rack, board, new MoveGeneratorParams());
  }

  /**
   * Given a rack and a board, generate a move.
   *
   * @param rack user's rack containing tiles available for play
   * @param board board containing all previous moves
   * @param params additional implementation-specific parameters
   * @return the "best" move according to this move generator
   */
  public abstract Move generateMove(Rack rack, T board, MoveGeneratorParams params);
}
