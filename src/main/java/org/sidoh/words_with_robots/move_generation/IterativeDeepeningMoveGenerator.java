package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Maps;
import org.sidoh.words_with_robots.move_generation.context.FixedDepthReturnContext;
import org.sidoh.words_with_robots.move_generation.context.NonBlockingReturnContext;
import org.sidoh.words_with_robots.move_generation.context.WwfMoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParams;
import org.sidoh.words_with_robots.move_generation.params.IterativeDeepeningParams;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class IterativeDeepeningMoveGenerator implements GameStateMoveGenerator<WwfMoveGeneratorReturnContext> {
  private static final Logger LOG = LoggerFactory.getLogger(IterativeDeepeningMoveGenerator.class);
  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();

  private final FixedDepthMoveGenerator fixedDepthGenerator;
  private AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator;
  private IterativeDeepeningParams params = new IterativeDeepeningParams();

  public IterativeDeepeningMoveGenerator(AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator) {
    this.allMovesGenerator = allMovesGenerator;
    this.fixedDepthGenerator = new FixedDepthMoveGenerator(allMovesGenerator);
  }

  @Override
  public WwfMoveGeneratorReturnContext generateMove(GameState state) {
    LOG.info("Generating move. Starting at depth 2");
    boolean verboseStats = params.isVerboseStatsEnabled();
    long maxExecutionTime = params.getMaxExecutionTime();
    Map<String, Object> stats = Maps.newHashMap();

    // Reconstruct some smaller parts of the state from the full game state
    Rack rack = stateHelper.getCurrentPlayerRack(state);
    WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

    // Figure out how long we're allowed to run
    long startTime = System.currentTimeMillis();
    long expireTime = System.currentTimeMillis() + maxExecutionTime;

    // Set up and start a producer thread
    Move bestMove = null;
    int currentDepth = 2;

    while ( bestMove == null || !isExpired(startTime, maxExecutionTime)) {
      long timeRemaining = expireTime - System.currentTimeMillis();

      FixedDepthMoveGenerator clone = fixedDepthGenerator
        .deepCopy()
        .updateParam(FixedDepthParams._Fields.MAX_DEPTH, currentDepth);
      Future<FixedDepthReturnContext> future = MoveGenerators.asNonBlockingGenerator(clone)
        .generateMove(state)
        .getFuture();
      FixedDepthReturnContext answer;

      try {
        // If we haven't found a move at all, wait indefinitely. Otherwise, only allow timeRemaining milliseconds
        // to complete the computation
        if ( bestMove == null ) {
          answer = future.get();
        }
        else {
          answer = future.get(timeRemaining, TimeUnit.MILLISECONDS);
        }
      }
      catch (InterruptedException e) {
        LOG.debug("interrupted -- returning best move found: {}", bestMove);
        return new WwfMoveGeneratorReturnContext(bestMove);
      }
      catch (ExecutionException e) {
        throw new RuntimeException(e);
      }
      catch (TimeoutException e) {
        return new WwfMoveGeneratorReturnContext(bestMove);
      }

      bestMove = answer.getMove();

      // Stop if the last depth reached a terminal state (i.e., increasing the depth won't do anything).
      if ( answer.isTerminal()) {
        LOG.info("Reached terminal state at depth {}. Generated move: {}", currentDepth, bestMove);
        break;
      }

      currentDepth++;
    }

    // Spit out the best move that we've found
    LOG.info("Made it to depth {}", currentDepth);
    stats.put(getStatsKey("max_depth"), currentDepth);

    // Find the index of the produced move
    if ( verboseStats ) {
      int moveRank = findMoveRank(rack, board, bestMove);
      stats.put(getStatsKey("move_rank"), moveRank);
      stats.put(getStatsKey("turn_index"), state.getAllMoves().size());
    }

    return new WwfMoveGeneratorReturnContext(bestMove);
  }

  /**
   * Finds the rank for the generated move. The rank is the index of the sorted scores.
   *
   * @param rack
   * @param board
   * @param move
   * @return
   */
  protected int findMoveRank(Rack rack, WordsWithFriendsBoard board, Move move) {
    return allMovesGenerator.getMoveScoreRank(allMovesGenerator.generateAllMoves(rack, board), move);
  }

  /**
   * Construct a stats key.
   *
   * @return
   */
  protected String getStatsKey(String keyPart) {
    return "iterative_deepening_".concat(keyPart);
  }

  /**
   * Determine whether or not execution is expired
   *
   * @param startTime time at which execution began
   * @param maxExecutionTime max number of millis to allow execution
   * @return
   */
  protected static boolean isExpired(long startTime, long maxExecutionTime) {
    // Otherwise, if we've been interrupted, stop immediately
    if ( Thread.currentThread().isInterrupted() ) {
      return true;
    }

    // Lastly, expire if we're out of time
    return System.currentTimeMillis() >= startTime + maxExecutionTime;
  }
}
