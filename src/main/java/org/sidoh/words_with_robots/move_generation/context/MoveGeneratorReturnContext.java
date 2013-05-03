package org.sidoh.words_with_robots.move_generation.context;

import com.google.common.collect.Maps;
import org.sidoh.wwf_api.game_state.Move;

import java.util.concurrent.ConcurrentMap;

/**
 * Defines a generic return value for a move generator. All move generators have to return a move.
 * This class also includes a "stats" map that allows move generators to report arbitrary statistics
 * about the move they generated (e.g., move rank).
 */
public abstract class MoveGeneratorReturnContext {
  private final Move move;
  private ConcurrentMap<String, Object> stats;

  /**
   *
   * @param move the move that the generator suggests
   */
  public MoveGeneratorReturnContext(Move move) {
    this.move = move;
    this.stats = Maps.<String, Object>newConcurrentMap();
  }

  /**
   *
   * @return the move that the generator suggests
   */
  public Move getMove() {
    return move;
  }

  /**
   *
   * @return map that the generator used to report stats
   */
  public ConcurrentMap<String, Object> getStats() {
    return stats;
  }
}
