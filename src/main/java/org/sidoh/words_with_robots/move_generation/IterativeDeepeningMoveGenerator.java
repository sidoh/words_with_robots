package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Maps;
import org.sidoh.words_with_robots.move_generation.context.WwfMoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.old_params.FixedDepthParamKey;
import org.sidoh.words_with_robots.move_generation.old_params.IterativeDeepeningParamKey;
import org.sidoh.words_with_robots.move_generation.old_params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.old_params.PreemptionContext;
import org.sidoh.words_with_robots.move_generation.old_params.WwfMoveGeneratorParamKey;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.IterativeDeepeningGeneratorParams;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class IterativeDeepeningMoveGenerator
  extends WordsWithFriendsMoveGenerator<IterativeDeepeningGeneratorParams, WwfMoveGeneratorReturnContext> {

  private static final Logger LOG = LoggerFactory.getLogger(IterativeDeepeningMoveGenerator.class);
  private final FixedDepthMoveGenerator allMovesGenerator;

  public IterativeDeepeningMoveGenerator(WordsWithFriendsMoveGenerator<?,?> allMovesGenerator) {
    this.allMovesGenerator = new FixedDepthMoveGenerator(allMovesGenerator);
  }

  @Override
  public WwfMoveGeneratorReturnContext generateMove(IterativeDeepeningGeneratorParams params) {
    LOG.info("Generating move. Starting at depth 2");
    boolean verboseStats = params.isVerboseStats();
    GameState state = params.getGameState();
    Map<String, Object> stats = Maps.newHashMap();
    Rack rack = params.getRack();
    WordsWithFriendsBoard board = params.getBoard();

    // Get our preemption context, which will allow us to preempt the underlying fixed store
    // when its minimum time to run is up
   PreemptionContext preemptionContext = params.getPreemptionContext();

    // Set up a kill signal so that we can stop execution when we want
    PreemptionContext childPreemptionContext = new PreemptionContext();

    // Figure out how long we're allowed to run
    long startTime = System.currentTimeMillis();

    // Set up and start a producer thread
    IterativeDeepeningProducer producer = new IterativeDeepeningProducer(rack,
      board,
      state,
      preemptionContext,
      params.getFixedDepthParamsBuilder().clone().setPreemptionContext(childPreemptionContext));
    Thread producerThread = new Thread(producer);
    producerThread.start();

    // Wait until execution completes or we run out of time
    while ( !producer.isComplete() && !isExpired(producer, startTime, preemptionContext, params) ) {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    LOG.info("Preempting producer...");

    // Kill the move generator
    childPreemptionContext.strongPreempt();
    try {
      producerThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    // Spit out the best move that we've found
    LOG.info("Made it to depth {}", producer.currentDepth-2);
    stats.put(getStatsKey("max_depth"), producer.currentDepth-2);

    // Find the index of the produced move
    WwfMoveGeneratorReturnContext bestMove = producer.getBestMove();

    if ( verboseStats ) {
      int moveRank = findMoveRank(rack, board, bestMove.getMove());
      stats.put(getStatsKey("move_rank"), moveRank);
      stats.put(getStatsKey("turn_index"), state.getAllMoves().size());
    }

    return producer.getBestMove();
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
   * @param producer
   * @param startTime time at which execution began
   * @param pcontext preemption context
   * @param params params called
   * @return
   */
  protected static boolean isExpired(IterativeDeepeningProducer producer, long startTime, PreemptionContext pcontext, IterativeDeepeningGeneratorParams params) {
    // Don't expire if we haven't found a move yet
    if ( producer.getBestMove() == null ) {
      return false;
    }

    long minRunTime = params.getMinExecutionTime();
    long maxRunTime = params.getMaxExecutionTime();

    // If we're preempted strongly, we must stop execution immediately
    if ( pcontext.getPreemptState() == PreemptionContext.State.STRONG_PREEMPT ) {
      return true;
    }

    long expireTime;
    if ( pcontext.getPreemptState() == PreemptionContext.State.WEAK_PREEMPT ) {
      expireTime = startTime + minRunTime;
    }
    else {
      expireTime = startTime + maxRunTime;
    }

    return System.currentTimeMillis() >= expireTime;
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

  private class IterativeDeepeningProducer implements Runnable {
    private final WordsWithFriendsBoard board;
    private final Rack rack;
    private int currentDepth;
    private WwfMoveGeneratorReturnContext currentBest;
    private boolean complete;
    private final GameState state;
    private final PreemptionContext preemptionContext;
    private final FixedDepthGeneratorParams.Builder fixedDepthParamsBuilder;

    public IterativeDeepeningProducer(Rack rack,
                                      WordsWithFriendsBoard board,
                                      GameState state,
                                      PreemptionContext preemptionContext,
                                      FixedDepthGeneratorParams.Builder fixedDepthParamsBuilder) {
      this.rack = rack;
      this.board = board;
      this.state = state;
      this.preemptionContext = preemptionContext;
      this.fixedDepthParamsBuilder = fixedDepthParamsBuilder;
      this.currentDepth = 2;
      this.complete = false;
    }

    @Override
    public void run() {
      int numTiles = (WordsWithFriendsBoard.TILES_PER_PLAYER*2)
        + state.getRemainingTilesSize();

      while ( preemptionContext.getPreemptState() == PreemptionContext.State.NOT_PREEMPTED ) {
        LOG.info("Moving to depth {}", currentDepth);
        FixedDepthGeneratorParams params = fixedDepthParamsBuilder
          .setMaxDepth(currentDepth)
          .build(state);

        try {
          WwfMoveGeneratorReturnContext best = allMovesGenerator.generateMove(params);

          // Don't update the best if we've been preempted.
          if ( preemptionContext.getPreemptState() == PreemptionContext.State.NOT_PREEMPTED ) {
            currentBest = best;
          }
        }
        catch ( Exception e ) {
          LOG.error("Exception generating move: {}", e);
          return;
        }

        // Don't continue if a terminal state was reached
        // don't continue if there have been more moves than there were tiles
        if ( currentBest.getMove().getMoveType() == MoveType.PASS
          || currentDepth > numTiles) {
          LOG.info("Reached terminal state at depth {}", currentDepth);
          complete = true;
          break;
        }

        currentDepth += 1;
      }
    }

    public boolean isComplete() {
      return complete;
    }

    public WwfMoveGeneratorReturnContext getBestMove() {
      return currentBest;
    }
  }
}
