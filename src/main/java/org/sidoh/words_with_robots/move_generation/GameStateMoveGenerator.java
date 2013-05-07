package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.context.MoveGeneratorReturnContext;
import org.sidoh.wwf_api.types.api.GameState;

/**
 * Defines a move generator that uses a {@link GameState} to extract necessary (and extra) information
 *
 * @param <R>
 */
public interface GameStateMoveGenerator<R extends MoveGeneratorReturnContext> {
  /**
   *
   * @param state the entire game state
   * @return the "best" move according to this move generator
   */
  public R generateMove(GameState state);
}
