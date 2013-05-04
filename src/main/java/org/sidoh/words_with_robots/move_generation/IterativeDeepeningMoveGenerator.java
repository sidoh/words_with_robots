package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Maps;
import org.sidoh.words_with_robots.move_generation.context.FixedDepthReturnContext;
import org.sidoh.words_with_robots.move_generation.context.WwfMoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.IterativeDeepeningGeneratorParams;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class IterativeDeepeningMoveGenerator
  extends WordsWithFriendsMoveGenerator<IterativeDeepeningGeneratorParams,
                                        WwfMoveGeneratorReturnContext,
                                        IterativeDeepeningGeneratorParams.Builder> {


  private static final Logger LOG = LoggerFactory.getLogger(IterativeDeepeningMoveGenerator.class);
  private final NonBlockingMoveGenerator<WordsWithFriendsBoard, FixedDepthGeneratorParams, FixedDepthReturnContext> fixedDepthGenerator;
  private WordsWithFriendsMoveGenerator<?, ?, ?> allMovesGenerator;

  public IterativeDeepeningMoveGenerator(WordsWithFriendsMoveGenerator<?,?,?> allMovesGenerator) {
    this.allMovesGenerator = allMovesGenerator;
    this.fixedDepthGenerator = MoveGenerators.asNonBlockingGenerator(new FixedDepthMoveGenerator(allMovesGenerator));
  }

  @Override
  public WwfMoveGeneratorReturnContext generateMove(IterativeDeepeningGeneratorParams params) {
    LOG.info("Generating move. Starting at depth 2");
    boolean verboseStats = params.isVerboseStats();
    GameState state = params.getGameState();
    Map<String, Object> stats = Maps.newHashMap();
    Rack rack = params.getRack();
    WordsWithFriendsBoard board = params.getBoard();

    // Figure out how long we're allowed to run
    long startTime = System.currentTimeMillis();
    long expireTime = System.currentTimeMillis() + params.getMaxExecutionTime();

    // Set up and start a producer thread
    Move bestMove = null;
    int currentDepth = 2;
    FixedDepthGeneratorParams.Builder paramsBuilder = params.getFixedDepthParamsBuilder();

    while ( bestMove == null || !isExpired(startTime, params)) {
      long timeRemaining = expireTime - System.currentTimeMillis();

      FixedDepthGeneratorParams childParams = paramsBuilder
        .setMaxDepth(currentDepth)
        .build(state);
      Future<FixedDepthReturnContext> future = fixedDepthGenerator
        .generateMove(childParams)
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
    return getMoveScoreRank(allMovesGenerator.generateAllPossibleMoves(rack, board), move);
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
   *
   * @param startTime time at which execution began
   * @param params params called
   * @return
   */
  protected static boolean isExpired(long startTime, IterativeDeepeningGeneratorParams params) {
    // Otherwise, if we've been interrupted, stop immediately
    if ( Thread.currentThread().isInterrupted() ) {
      return true;
    }

    // Lastly, expire if we're out of time
    return System.currentTimeMillis() >= startTime + params.getMaxExecutionTime();
  }

  @Override
  protected Set<Move> generateMoves(int row, int col, Rack rack, WordsWithFriendsBoard board) {
    return allMovesGenerator.generateMoves(row, col, rack, board);
  }

  @Override
  protected boolean isWord(String word) {
    return allMovesGenerator.isWord(word);
  }

  @Override
  protected WwfMoveGeneratorReturnContext createReturnContext(Move move) {
    return allMovesGenerator.createReturnContext(move);
  }

  @Override
  public IterativeDeepeningGeneratorParams.Builder getParamsBuilder() {
    return new IterativeDeepeningGeneratorParams.Builder();
  }
}
