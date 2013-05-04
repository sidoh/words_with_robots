package org.sidoh.words_with_robots.move_generation.params;

import org.sidoh.wwf_api.game_state.Board;
import org.sidoh.wwf_api.types.game_state.Rack;

/**
 * A generic container for parameters to be passed to a move generator. Classes extending
 * MoveGenerator may choose to extend this class in order to require additional parameters.
 *
 * @param <T> the type of Board that this param container maintains
 */
public abstract class MoveGeneratorParams<T extends Board> implements Cloneable {
  private final Rack rack;
  private final T board;

  public MoveGeneratorParams(Rack rack, T board) {
    this.rack = rack;
    this.board = board;
  }

  /**
   *
   * @return the player's rack. should contain tiles player has at their disposal.
   */
  public Rack getRack() {
    return rack;
  }

  /**
   *
   * @return the current board state
   */
  public T getBoard() {
    return board;
  }
}
