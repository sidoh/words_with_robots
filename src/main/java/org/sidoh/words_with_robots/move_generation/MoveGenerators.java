package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.context.MoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.wwf_api.game_state.Board;

public class MoveGenerators {
  /**
   * Wraps the provided move generator in a non-blocking move generator.
   *
   * @param generator the generator to wrap
   * @param <T> type of board the inner generator acts on
   * @param <P> type of parameter list the generator expects
   * @param <R> type of return context the inner generator produces
   * @return a non-blocking move generator wrapping the provided one
   */
  public static
  <T extends Board, P extends MoveGeneratorParams<T>, R extends MoveGeneratorReturnContext>
  NonBlockingMoveGenerator<T, P, R>
  asNonBlockingGenerator(MoveGenerator<T, P, R> generator) {
    return new NonBlockingMoveGenerator<T, P, R>(generator);
  }
}
