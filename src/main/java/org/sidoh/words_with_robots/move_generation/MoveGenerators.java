package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.context.MoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.context.NonBlockingReturnContext;
import org.sidoh.wwf_api.game_state.Board;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MoveGenerators {
  /**
   * Wraps the provided move generator in a non-blocking move generator.
   *
   * @param generator the generator to wrap
   * @param <T> type of board the inner generator acts on
   * @param <R> type of return context the inner generator produces
   * @return a non-blocking move generator wrapping the provided one
   */
  public static <T extends Board, R extends MoveGeneratorReturnContext>
  MoveGenerator<T, NonBlockingReturnContext<R>> // returns
  asNonBlockingGenerator(final MoveGenerator<T, R> generator) {
    return new MoveGenerator<T, NonBlockingReturnContext<R>>() {
      @Override
      public NonBlockingReturnContext<R> generateMove(final Rack rack, final T board) {
        Future<R> future = Executors.newSingleThreadExecutor().submit(new Callable<R>() {
          @Override
          public R call() throws Exception {
            return generator.generateMove(rack, board);
          }
        });
        return new NonBlockingReturnContext<R>(future);
      }
    };
  }

  /**
   * Wraps the provided move generator in a non-blocking move generator.
   *
   * @param generator the generator to wrap
   * @param <R> type of return context the inner generator produces
   * @return a non-blocking move generator wrapping the provided one
   */
  public static <R extends MoveGeneratorReturnContext>
  GameStateMoveGenerator<NonBlockingReturnContext<R>> // returns
  asNonBlockingGenerator(final GameStateMoveGenerator<R> generator) {
    return new GameStateMoveGenerator<NonBlockingReturnContext<R>>() {
      @Override
      public NonBlockingReturnContext<R> generateMove(final GameState state) {
        Future<R> future = Executors.newSingleThreadExecutor().submit(new Callable<R>() {
          @Override
          public R call() throws Exception {
            return generator.generateMove(state);
          }
        });
        return new NonBlockingReturnContext<R>(future);
      }
    };
  }
}
