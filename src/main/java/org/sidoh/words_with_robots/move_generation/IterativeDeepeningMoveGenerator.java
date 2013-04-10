package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.params.FixedDepthParamKey;
import org.sidoh.words_with_robots.move_generation.params.IterativeDeepeningParamKey;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class IterativeDeepeningMoveGenerator extends WordsWithFriendsMoveGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(IterativeDeepeningMoveGenerator.class);
  private final FixedDepthMoveGenerator allMovesGenerator;

  public IterativeDeepeningMoveGenerator(WordsWithFriendsMoveGenerator allMovesGenerator) {
    this.allMovesGenerator = new FixedDepthMoveGenerator(allMovesGenerator);
  }

  @Override
  public Move generateMove(Rack rack, WordsWithFriendsBoard board, MoveGeneratorParams params) {
    long maxExecutionTime = params.getLong(IterativeDeepeningParamKey.MAX_EXECUTION_TIME_IN_MS);
    long expireTime = System.currentTimeMillis() + maxExecutionTime;
    IterativeDeepeningProducer producer = new IterativeDeepeningProducer(rack, board, params);
    Thread producerThread = new Thread(producer);
    producerThread.start();

    while ( System.currentTimeMillis() < expireTime ) {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    params.set(FixedDepthParamKey.KILL_SIGNAL, true);
    try {
      producerThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    LOG.info("Made it to depth {}", producer.currentDepth-1);

    return producer.getBestMove();
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

    public IterativeDeepeningProducer(Rack rack, WordsWithFriendsBoard board, MoveGeneratorParams params) {
      this.board = board;
      this.rack = rack;
      this.params = params;
      this.currentDepth = 1;
    }

    @Override
    public void run() {
      while ( !params.getBoolean(FixedDepthParamKey.KILL_SIGNAL) ) {
        params.set(FixedDepthParamKey.MAXIMUM_DEPTH, currentDepth++);
        currentBest = allMovesGenerator.generateMove(rack, board, params);
      }
    }

    public Move getBestMove() {
      return currentBest;
    }
  }
}