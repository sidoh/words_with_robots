package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.sidoh.words_with_robots.data_structures.CollectionsHelper;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParamKey;
import org.sidoh.words_with_robots.move_generation.params.IterativeDeepeningParamKey;
import org.sidoh.words_with_robots.move_generation.params.KillSignalBeacon;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.PreemptionContext;
import org.sidoh.words_with_robots.move_generation.params.WwfMoveGeneratorParamKey;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IterativeDeepeningMoveGenerator extends WordsWithFriendsMoveGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(IterativeDeepeningMoveGenerator.class);
  private final FixedDepthMoveGenerator allMovesGenerator;

  public IterativeDeepeningMoveGenerator(WordsWithFriendsMoveGenerator allMovesGenerator) {
    this.allMovesGenerator = new FixedDepthMoveGenerator(allMovesGenerator);
  }

  @Override
  public Move generateMove(Rack rack, WordsWithFriendsBoard board, MoveGeneratorParams params) {
    LOG.info("Generating move. Starting at depth 2");
    boolean verboseStats = params.getBoolean(IterativeDeepeningParamKey.VERBOSE_STATS_ENABLED);
    Map<String, Object> stats = (Map<String, Object>)params.get(WwfMoveGeneratorParamKey.GAME_STATS);

    // Get our preemption context, which will allow us to preempt the underlying fixed store
    // when its minimum time to run is up
   PreemptionContext preemptionContext = (PreemptionContext) params.get(FixedDepthParamKey.PREEMPTION_CONTEXT);

    // Set up a kill signal so that we can stop execution when we want
    PreemptionContext childPreemptionContext = new PreemptionContext();
    MoveGeneratorParams childParams = params.clone()
      .set(FixedDepthParamKey.PREEMPTION_CONTEXT, childPreemptionContext);

    // Figure out how long we're allowed to run
    long startTime = System.currentTimeMillis();

    // Set up and start a producer thread
    IterativeDeepeningProducer producer = new IterativeDeepeningProducer(rack, board, childParams);
    Thread producerThread = new Thread(producer);
    producerThread.start();

    // Wait until execution completes or we run out of time
    while ( !producer.isComplete() && !isExpired(startTime, preemptionContext, params) ) {
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
    Move bestMove = producer.getBestMove();

    if ( verboseStats ) {
      stats.put(getStatsKey("move_rank"), findMoveRank(rack, board, bestMove));
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
    if ( move == null || move.getMoveType() != MoveType.PLAY ) {
      return -1;
    }

    Set<Integer> uniqueScores = Sets.newHashSet();
    for (Move possibleMove : allMovesGenerator.generateAllPossibleMoves(rack, board)) {
      uniqueScores.add(possibleMove.getResult().getScore());
    }
    List<Integer> scores = Lists.newArrayList(uniqueScores);
    Collections.sort(scores, Collections.reverseOrder());
    LOG.info("Sorted possible scores: {}", scores);

    if ( scores.size() == 0 ) {
      return -1;
    }
    else {
      return scores.indexOf(move.getResult().getScore());
    }
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
   * @param pcontext preemption context
   * @param params params called
   * @return
   */
  protected static boolean isExpired(long startTime, PreemptionContext pcontext, MoveGeneratorParams params) {
    long minRunTime = params.getLong(IterativeDeepeningParamKey.MIN_EXECUTION_TIME_IN_MS);
    long maxRunTime = params.getLong(IterativeDeepeningParamKey.MAX_EXECUTION_TIME_IN_MS);

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

  private class IterativeDeepeningProducer implements Runnable {
    private final WordsWithFriendsBoard board;
    private final Rack rack;
    private final MoveGeneratorParams params;
    private int currentDepth;
    private Move currentBest;
    private boolean complete;

    public IterativeDeepeningProducer(Rack rack, WordsWithFriendsBoard board, MoveGeneratorParams params) {
      this.board = board;
      this.rack = rack;
      this.params = params;
      this.currentDepth = 2;
      this.complete = false;
    }

    @Override
    public void run() {
      PreemptionContext beacon
        = (PreemptionContext) params.get(FixedDepthParamKey.PREEMPTION_CONTEXT);

      while ( beacon.getPreemptState() == PreemptionContext.State.NOT_PREEMPTED ) {
        LOG.info("Moving to depth {}", currentDepth);
        params.set(FixedDepthParamKey.MAXIMUM_DEPTH, currentDepth);

        try {
          Move best = allMovesGenerator.generateMove(rack, board, params);

          // Don't update the best if we've been preempted.
          if ( beacon.getPreemptState() == PreemptionContext.State.NOT_PREEMPTED ) {
            currentBest = best;
          }
        }
        catch ( Exception e ) {
          LOG.error("Exception generating move: {}", e);
          return;
        }

        // Don't continue if a terminal state was reached
        if ( params.getBoolean(FixedDepthParamKey.REACHED_TERMINAL_STATE) || currentBest.getMoveType() == MoveType.PASS ) {
          LOG.info("Reached terminal state at depth {}", currentDepth);
          complete = true;
          break;
        }

        currentDepth += 2;
      }
    }

    public boolean isComplete() {
      return complete;
    }

    public Move getBestMove() {
      return currentBest;
    }
  }
}
