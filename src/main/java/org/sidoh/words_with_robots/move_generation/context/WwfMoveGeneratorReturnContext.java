package org.sidoh.words_with_robots.move_generation.context;

import org.sidoh.wwf_api.game_state.Move;

public class WwfMoveGeneratorReturnContext extends MoveGeneratorReturnContext {
  /**
   * @param move the move that the generator suggests
   */
  public WwfMoveGeneratorReturnContext(Move move) {
    super(move);
  }
}
