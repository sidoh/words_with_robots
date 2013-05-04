package org.sidoh.words_with_robots.move_generation.context;

import java.util.concurrent.Future;

public class NonBlockingReturnContext<R extends MoveGeneratorReturnContext> implements MoveGeneratorReturnContext {
  private final Future<R> future;

  public NonBlockingReturnContext(Future<R> future) {
    this.future = future;
  }

  public Future<R> getFuture() {
    return future;
  }
}
