package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.context.MoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.context.NonBlockingReturnContext;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.wwf_api.game_state.Board;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NonBlockingMoveGenerator<T extends Board,
                                      P extends MoveGeneratorParams<T>,
                                      R extends MoveGeneratorReturnContext>
                                      implements MoveGenerator<T, P, NonBlockingReturnContext<R>> {
  private final MoveGenerator<T, P, R> blockingMoveGenerator;

  public NonBlockingMoveGenerator(MoveGenerator<T, P, R> blockingMoveGenerator) {
    this.blockingMoveGenerator = blockingMoveGenerator;
  }

  @Override
  public NonBlockingReturnContext<R> generateMove(final P params) {
    Future<R> future = Executors.newSingleThreadExecutor().submit(new Callable<R>() {
      @Override
      public R call() throws Exception {
        return blockingMoveGenerator.generateMove(params);
      }
    });
    return new NonBlockingReturnContext<R>(future);
  }
}
