package org.sidoh.words_with_robots.move_generation.context;

import org.sidoh.wwf_api.game_state.Move;

public class WwfMoveGeneratorReturnContext implements MoveGeneratorReturnContext {
  private final Move move;

  /**
   * @param move the move that the generator suggests
   */
  public WwfMoveGeneratorReturnContext(Move move) {
    this.move = move;
  }

  public Move getMove() {
    return move;
  }
}
