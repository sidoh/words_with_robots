package org.sidoh.tiler.move_generation;

import com.google.common.collect.MinMaxPriorityQueue;
import org.sidoh.tiler.data_structures.CollectionsHelper;
import org.sidoh.tiler.move_generation.eval.EvaluationFunction;
import org.sidoh.tiler.move_generation.eval.ScoreEvalFunction;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.User;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implements minimax two moves deep. One could pretty easily go deeper, but the branching factor
 * is completely insane and this is already reasonably effective.
 *
 * It picks the move that maximizes the difference between the score you get by playing your best move
 * and the score your opponent gets by playing their best move in response to that one.
 *
 * Note that this is possible with WWF because you can know what the opponent's tiles are. This sort
 * of strategy doesn't make sense in a more solidly designed game.
 */
public class WwfMinimaxLocal extends WordsWithFriendsMoveGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(WwfMinimaxLocal.class);
  private static final int DEFAULT_MOVE_CACHE_SIZE = 100;
  private static final double CACHE_MISS_FLUSH_FACTOR = 0.5f;

  private final WordsWithFriendsMoveGenerator inner;
  private static final GameStateHelper stateHelper = new GameStateHelper();

  public WwfMinimaxLocal(WordsWithFriendsMoveGenerator inner) {
    this.inner = inner;
  }

  /**
   *
   * @param baseRack
   * @param board
   * @param callParams
   * @return
   */
  @Override
  public Move generateMove(Rack baseRack, WordsWithFriendsBoard board, MoveGenerator.Params callParams) {
    Params params = (Params)callParams;

    // Pull out params out of params object
    EvaluationFunction eval = params.getEvalFunction();
    int diffThreshold = params.getDefaultThreshold();

    // Generate all possible moves given this game state
    List<Move> allMoves1 = CollectionsHelper.asList( generateAllPossibleMoves(baseRack, board) );
    LOG.info("Generated " + allMoves1.size() + " possible moves.");

    double bestDiff = Double.NEGATIVE_INFINITY;
    Move best = null;

    // Prepare some stuff for game state swapping
    GameState state = params.getGameState();
    User other = stateHelper.getOtherUser(state.getMeta().getCurrentMoveUserId(), state);
    Rack otherRack = stateHelper.buildRack(state.getRacks().get(other.getId()));

    double bestOp = 0;
    Move bestOpMove = null;

    // Optimization: sort the moves by the number of points it's worth. Considering them first tends to trim
    // the search space a lot sooner.
    Collections.sort(allMoves1, Collections.reverseOrder(new MoveScoreComparator(eval, state)));

    // Optimization: remember the top N opponent moves (by score) and try them when considering each of the
    // possible moves. This has a high probability of allowing us to avoid generating possible opponent
    // moves.
    MinMaxPriorityQueue<Move> opMoveCache = MinMaxPriorityQueue
      .orderedBy(Collections.reverseOrder(new MoveScoreComparator(new ScoreEvalFunction(), null)))
      .maximumSize(DEFAULT_MOVE_CACHE_SIZE)
      .create();

    for (Move move : allMoves1) {
      // Make the move
      WordsWithFriendsBoard pboard = board.clone();
      pboard.move(move);
      double moveScore = eval.score(move, state);

      // Update the game state given this move
      GameState updatedState = stateHelper.applyMove(state, move);

      // Try the opponent move cache
      boolean cacheHit = false;

      LOG.debug("trying {} moves in the opponent move cache", opMoveCache.size());

      for (Move opMove : opMoveCache) {
        Move.Result result = pboard.scoreMove(opMove);
        double score = eval.score(opMove, updatedState);

        // We'll have to check if this move is still valid. Our move might've blocked it.
        if ( isValidMove(result) && bestDiff >= (moveScore - score) ) {
          LOG.debug("move cache hit: word = {}, for {} points.", result.getResultingWords(), result.getScore());
          cacheHit = true;
          break;
        }
      }

      // Don't bother generating moves if one of the ones in the cache already worked.
      if ( cacheHit )
        continue;
      // In the event of a cache miss, flush some of the items out of the cache in hopes of populating it
      // with values more likely to result in cache hits.
      else if (opMoveCache.size() == DEFAULT_MOVE_CACHE_SIZE) {
        LOG.debug("cache miss. flushing some items.");

        for (int i = 0; i < DEFAULT_MOVE_CACHE_SIZE * CACHE_MISS_FLUSH_FACTOR; i++) {
          opMoveCache.removeLast();
        }
      }

      // get the opponent's rack so that we can generate their possible moves
      Rack opRack = stateHelper.buildRack(other.getId(), state);

      for (Move opMove : generateAllPossibleMoves(opRack, pboard)) {
        double opScore = eval.score(opMove, updatedState);

        // Try putting this move in the cache
        opMoveCache.offer(opMove);

        if ( opScore > bestOp ) {
          bestOp = opScore;
          bestOpMove = opMove;

          // If this makes the diff any worse than the best diff seen so far, it isn't worth bothering with
          // the rest of the possible opponent moves.
          if ( (moveScore - bestOp) <= bestDiff ) {
            LOG.info("this diff is worse than the best diff. skip remaining opponent moves.");
          }
        }
      }

      double diff = moveScore - bestOp;

      if (diff > bestDiff) {
        LOG.info("New best move: " + move.getResult().getResultingWords() + " (" + move.getResult().getScore() + " points), diff = " + diff);

        bestDiff = diff;
        best = move;
      }

      if (diff >= diffThreshold) {
        LOG.info("Move satisfies the threshold. Exiting early.");

        return best;
      }
    }

    if ( best != null && bestOpMove != null ) {
      bestOpMove = opMoveCache.peekFirst();
      WordsWithFriendsBoard pboard = board.clone();
      pboard.move(best);
      LOG.info("best opponent move: " + bestOpMove.getResult().getResultingWords() + " (score = " + bestOp + ")");
      params.setBestOpMove( bestOpMove );
    }

    if ( best != null ) {
      LOG.info("best move found: " + best.getResult().getResultingWords() + " (" + best.getResult().getScore() + " points)");
    }

    return best;
  }

  @Override
  protected Set<Move> generateMoves(int row, int col, Rack rack, WordsWithFriendsBoard board) {
    return inner.generateMoves(row, col, rack, board);
  }

  @Override
  protected boolean isWord(String word) {
    return inner.isWord(word);
  }
}
