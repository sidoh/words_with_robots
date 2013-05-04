package org.sidoh.words_with_robots.move_generation.context;

import org.sidoh.wwf_api.game_state.Move;

public class FixedDepthReturnContext extends WwfMoveGeneratorReturnContext {
  private boolean terminal;

  /**
   * @param move the move that the generator suggests
   */
  public FixedDepthReturnContext(Move move) {
    super(move);
    this.terminal = false;
  }

  public boolean isTerminal() {
    return terminal;
  }

  public FixedDepthReturnContext setTerminal(boolean terminal) {
    this.terminal = terminal;
    return this;
  }
}
