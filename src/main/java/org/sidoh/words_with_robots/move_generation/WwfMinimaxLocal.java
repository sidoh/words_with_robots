package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;
import org.sidoh.words_with_robots.move_generation.context.WwfMoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.words_with_robots.move_generation.util.MoveScoreComparator;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.User;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
public class WwfMinimaxLocal { //implements GameStateMoveGenerator<WwfMoveGeneratorReturnContext> {
//
//  private static final Logger LOG = LoggerFactory.getLogger(WwfMinimaxLocal.class);
//
//  private final AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator;
//  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();
//
//  public WwfMinimaxLocal(AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator) {
//    this.allMovesGenerator = allMovesGenerator;
//  }
//
//  @Override
//  public WwfMoveGeneratorReturnContext generateMove(GameState state) {
//    // Pull out params out of params object
//    EvaluationFunction eval = params.getEvaluationFunction();
//    int diffThreshold = params.getDiffThreshold();
//    Rack baseRack = params.getRack();
//    WordsWithFriendsBoard board = params.getBoard();
//
//    // Generate all possible moves given this game state
//    List<Move> allMoves1 = Lists.newArrayList( allMovesGenerator.generateAllMoves(baseRack, board) );
//    LOG.info("Generated " + allMoves1.size() + " possible moves.");
//
//    double bestDiff = Double.NEGATIVE_INFINITY;
//    Move best = null;
//
//    // Prepare some stuff for game state swapping
//    User other = stateHelper.getOtherUser(state.getMeta().getCurrentMoveUserId(), state);
//    Rack otherRack = stateHelper.buildRack(state.getRacks().get(other.getId()));
//
//    double bestOp = 0;
//    Move bestOpMove = null;
//
//    // Optimization: sort the moves by the number of points it's worth. Considering them first tends to trim
//    // the search space a lot sooner.
//    Collections.sort(allMoves1, MoveScoreComparator.rawScoreComparator());
//
//    // Optimization: remember the top N opponent moves (by score) and try them when considering each of the
//    // possible moves. This has a high probability of allowing us to avoid generating possible opponent
//    // moves.
//    MinMaxPriorityQueue<Move> opMoveCache = MinMaxPriorityQueue
//      .orderedBy(MoveScoreComparator.rawScoreComparator())
//      .maximumSize(params.getCacheSize())
//      .create();
//
//    for (Move move : allMoves1) {
//      // Make the move
//      WordsWithFriendsBoard pboard = board.clone();
//      pboard.move(move);
//      double moveScore = eval.score(move, state);
//
//      // Update the game state given this move
//      GameState updatedState = stateHelper.applyMove(state, move);
//
//      // Try the opponent move cache
//      boolean cacheHit = false;
//
//      LOG.debug("trying {} moves in the opponent move cache", opMoveCache.size());
//
//      for (Move opMove : opMoveCache) {
//        Move.Result result = pboard.scoreMove(opMove);
//        double score = eval.score(opMove, updatedState);
//
//        // We'll have to check if this move is still valid. Our move might've blocked it.
//        if ( allMovesGenerator.isValidMove(pboard, opMove) && bestDiff >= (moveScore - score) ) {
//          LOG.debug("move cache hit: word = {}, for {} points.", result.getResultingWords(), result.getScore());
//          cacheHit = true;
//          break;
//        }
//      }
//
//      // Don't bother generating moves if one of the ones in the cache already worked.
//      if ( cacheHit )
//        continue;
//      // In the event of a cache miss, flush some of the items out of the cache in hopes of populating it
//      // with values more likely to result in cache hits.
//      else if (opMoveCache.size() == params.getCacheSize()) {
//        LOG.debug("cache miss. flushing some items.");
//
//        for (int i = 0; i < params.getCacheSize() * params.getCacheMissFlushFactor(); i++) {
//          opMoveCache.removeLast();
//        }
//      }
//
//      // get the opponent's rack so that we can generate their possible moves
//      Rack opRack = stateHelper.buildRack(other.getId(), state);
//
//      for (Move opMove : allMovesGenerator.generateAllMoves(opRack, pboard)) {
//        double opScore = eval.score(opMove, updatedState);
//
//        // Try putting this move in the cache
//        opMoveCache.offer(opMove);
//
//        if ( opScore > bestOp ) {
//          bestOp = opScore;
//          bestOpMove = opMove;
//
//          // If this makes the diff any worse than the best diff seen so far, it isn't worth bothering with
//          // the rest of the possible opponent moves.
//          if ( (moveScore - bestOp) <= bestDiff ) {
//            LOG.info("this diff is worse than the best diff. skip remaining opponent moves.");
//          }
//        }
//      }
//
//      double diff = moveScore - bestOp;
//
//      if (diff > bestDiff) {
//        LOG.info("New best move: " + move.getResult().getResultingWords() + " (" + move.getResult().getScore() + " points), diff = " + diff);
//
//        bestDiff = diff;
//        best = move;
//      }
//
//      if (diff >= diffThreshold) {
//        LOG.info("Move satisfies the threshold. Exiting early.");
//
//        return createReturnContext(best);
//      }
//    }
//
//    if ( best != null ) {
//      LOG.info("best move found: " + best.getResult().getResultingWords() + " (" + best.getResult().getScore() + " points)");
//    }
//
//    return createReturnContext(best);
//  }
}
