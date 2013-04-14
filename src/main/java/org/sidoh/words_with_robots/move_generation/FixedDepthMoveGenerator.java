package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.MinMaxPriorityQueue;
import org.sidoh.words_with_robots.data_structures.CollectionsHelper;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParamKey;
import org.sidoh.words_with_robots.move_generation.params.KillSignalBeacon;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.PreemptionContext;
import org.sidoh.words_with_robots.move_generation.params.WwfMoveGeneratorParamKey;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * (roughly) implements a fixed-depth search move generator. attempts to make a few optimizations by limiting the
 * branching factor and ignoring a few low-score moves.
 *
 */
public class FixedDepthMoveGenerator extends WordsWithFriendsMoveGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(FixedDepthMoveGenerator.class);

  private final WordsWithFriendsMoveGenerator inner;
  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();

  /**
   * A helper class to make switching between min/max easy.
   */
  private static class Player {
    private final long[] players;
    private final int selected;
    private final boolean isMin;
    private final Player other;

    public Player(long uid1, long uid2, long selected, boolean isMin) {
      this( new long[] { uid1, uid2 },
        uid1 == selected ? 0 : 1,
        isMin);
    }

    public Player(long[] uids, int selected, boolean isMin) {
      this.players = uids;
      this.selected = selected;
      this.isMin = isMin;
      this.other = new Player(this);
    }

    private Player(Player other) {
      this.players = other.players;
      this.selected = other.selected;
      this.isMin = !other.isMin;
      this.other = other;
    }

    public boolean isMin() {
      return isMin;
    }

    public boolean isMax() {
      return !isMin;
    }

    public long getUserId() {
      return players[ selected ];
    }

    public long getOtherUserId() {
      return players[ (selected + 1) % 2 ];
    }

    public Player getOther() {
      return other;
    }
  }

  public FixedDepthMoveGenerator(WordsWithFriendsMoveGenerator inner) {
    this.inner = inner;
  }

  @Override
  public Move generateMove(Rack baseRack, WordsWithFriendsBoard board, MoveGeneratorParams params) {
    GameState state = (GameState) params.get(WwfMoveGeneratorParamKey.GAME_STATE);
    long maxPlayer = state.getMeta().getCurrentMoveUserId();
    Player max = new Player( maxPlayer, stateHelper.getOtherUser(maxPlayer, state).getId(), maxPlayer, true );

    AlphaBetaClosure closure = new AlphaBetaClosure()
      .setAlpha( Double.NEGATIVE_INFINITY )
      .setBeta( Double.POSITIVE_INFINITY )
      .setPlayer( max )
      .setState( state )
      .setBoard( board )
      .setRack( stateHelper.buildRack(max.getUserId(), state) )
      .setParams(params)
      .setRemainingDepth( params.getInt(FixedDepthParamKey.MAXIMUM_DEPTH) );

    closure = alphaBetaSearch(closure);

    // Pass if we couldn't generate a move
    if ( closure.getReturnMove() == null ) {
      params.set(FixedDepthParamKey.REACHED_TERMINAL_STATE, true);
      return Move.pass();
    }

    LOG.info("generated move: {}. It had index {}, and is worth {} points. ab-search returned: {}",
      new Object[]{
        closure.getReturnMove().getResult().getResultingWords(),
        closure.getReturnMoveIndex(),
        closure.getReturnMove().getResult().getScore(),
        closure.getReturnValue()});

    params.set(FixedDepthParamKey.REACHED_TERMINAL_STATE, closure.reachedTerminalState());
    return closure.getReturnMove();
  }

  protected AlphaBetaClosure alphaBetaSearch(AlphaBetaClosure closure) {
    // If this is a terminal state, evaluate the game state.
    if ( closure.getRemainingDepth() == 0 || closure.getRack().getTilesSize() == 0 ) {
      double score = evaluateState(closure);
      return closure
        .setReturnValue( score )
        .setReachedTerminalState( closure.getRack() == null || closure.getRack().getTilesSize() == 0 );
    }

    PreemptionContext preemptionContext = (PreemptionContext) closure.getParams().get(FixedDepthParamKey.PREEMPTION_CONTEXT);
    if ( preemptionContext.getPreemptState() == PreemptionContext.State.STRONG_PREEMPT ) {
      return closure;
    }

    double alpha = closure.getAlpha();
    double beta = closure.getBeta();
    Player player = closure.getPlayer();
    MoveGeneratorParams params = closure.getParams();

    // Try moves in the move cache if there are any. These moves will have been generated by
    // siblings in the call tree, and some of the moves may still be possible in this node.
    // Anything we can do to avoid generating all possible moves gives a huge performance
    // boost.
    if ( closure.getMoveCache() != null ) {
      for (AlphaBetaClosure cachedMove : closure.getMoveCache()) {
        WordsWithFriendsBoard boardCopy = closure.getBoard().clone();
        Move.Result result = boardCopy.move(cachedMove.getReturnMove());

        // Will need to check if this is still a legal move.
        if ( isValidMove(result) ) {
          LOG.debug("Move cache hit at level {}. Move = {}", closure.getRemainingDepth(), closure.getReturnMove().getResult().getResultingWords());

          double cAlpha = cachedMove.getAlpha();
          double cBeta = cachedMove.getBeta();

          if ( cBeta <= cAlpha ) {
            return cachedMove;
          }
        }
      }
    }

    // If not, generate moves.
    List<Move> moves = getSortedMoves(closure);

    Move returnMove = null;
    AlphaBetaClosure returnClosure = null;
    MinMaxPriorityQueue<AlphaBetaClosure> moveCache = MinMaxPriorityQueue
      .orderedBy(new ClosureComparator(player))
      .maximumSize(params.getInt(FixedDepthParamKey.MOVE_CACHE_SIZE))
      .create();

    LOG.debug("At depth {}, considering {} possible moves", closure.getRemainingDepth(), moves.size());

    int movesConsidered = 0;

    for (Move move : moves) {
      // Create copy of board so as to not mess with future calls
      WordsWithFriendsBoard board = closure.getBoard().clone();

      // Make move for recursive calls
      board.move( move );

      // Stop considering moves if this one sucks and we've already considered better moves.
      if ( move.getResult().getScore() < params.getInt(FixedDepthParamKey.MIN_SCORE) && movesConsidered > 0 ) {
        LOG.debug("stop considering moves after seeing one with score {}", move.getResult().getScore());
        break;
      }

      // Break early if the branching factor limits us
      if ( movesConsidered >= params.getInt(FixedDepthParamKey.BRANCHING_FACTOR_LIMIT) ) {
        break;
      }

      LOG.debug("considering move with score {}", move.getResult().getScore());

      // Generate new game state stuff
      GameState updatedState = stateHelper.applyMove(closure.getState(), move);
      Rack updatedRack = stateHelper.buildRack(player.getOtherUserId(), updatedState);

      AlphaBetaClosure callClosure = closure
        .clone()
        .setAlpha(alpha)
        .setBeta(beta)
        .setRemainingDepth(closure.getRemainingDepth() - 1)
        .setRack(updatedRack)
        .setState(updatedState)
        .setBoard(board)
        .setReturnMove(move)
        .setMoveCache(moveCache)
        .setReturnMoveIndex(movesConsidered)
        .setPlayer(player.getOther());

      callClosure = alphaBetaSearch(callClosure);

      // Offer the result of this call to the cache, which may prevent generating moves for future
      // calls.
      moveCache.offer(callClosure);

      // The following logic is handled differently depending on which player we're considering
      if ( player.isMax() ) {
        if ( callClosure.getReturnValue() > alpha ) {
          alpha = callClosure.getReturnValue();
          returnClosure = callClosure;
          returnMove = move;
        }

        if (beta <= alpha) {
          break;
        }
      }
      else {
        if ( callClosure.getReturnValue() < beta ) {
          beta = callClosure.getReturnValue();
          returnClosure = callClosure;
          returnMove = move;
        }

        if ( beta <= alpha ) {
          break;
        }
      }

      movesConsidered++;
    }

    // If null, this branch has failed to turn up a better move than a sibling branch.
    if ( returnClosure == null ) {
      returnClosure = new AlphaBetaClosure()
        .setAlpha( alpha )
        .setBeta( beta )
        .setMoveCache( moveCache )
        .setReturnValue( player.isMax() ? alpha : beta )
        .setReturnMove( null );
    }

    LOG.debug("best in this branch: {}", (player.isMax() ? alpha : beta));

    return returnClosure
      .setAlpha( alpha )
      .setBeta( beta )
      .setMoveCache(moveCache)
      .setReturnValue(
        player.isMax() ? alpha : beta)
      .setReturnMove( returnMove );
  }

  protected List<Move> getSortedMoves(AlphaBetaClosure closure) {
    List<Move> allMoves = CollectionsHelper.asList(generateAllPossibleMoves(closure.getRack(), closure.getBoard()));
    Collections.sort(allMoves, MoveScoreComparator.rawScoreComparator());

    return allMoves;
  }

  protected static double evaluateState(AlphaBetaClosure closure) {
    Player player = closure.getPlayer();
    GameState state = closure.getState();

    Player max = player.isMax() ? player : player.getOther();

    double myScore = stateHelper.getScore(max.getUserId(), state);
    double opScore = stateHelper.getScore(max.getOtherUserId(), state);

    return myScore - opScore;
  }

  @Override
  protected Set<Move> generateMoves(int row, int col, Rack rack, WordsWithFriendsBoard board) {
    return inner.generateMoves(row, col, rack, board);
  }

  @Override
  protected boolean isWord(String word) {
    return inner.isWord(word);
  }

  protected static class ClosureComparator implements Comparator<AlphaBetaClosure> {
    private final Comparator<Double> comparator;

    public ClosureComparator(Player player) {
      this.comparator = player.isMin()
        ? CollectionsHelper.<Double>naturalComparator()
        : Collections.reverseOrder(CollectionsHelper.<Double>naturalComparator());
    }

    @Override
    public int compare(AlphaBetaClosure c1, AlphaBetaClosure c2) {
      return comparator.compare(c1.getReturnValue(), c2.getReturnValue());
    }
  }

  protected static class AlphaBetaClosure {
    private Rack rack;
    private WordsWithFriendsBoard board;
    private GameState state;
    private int remainingDepth;
    private double alpha;
    private double beta;
    private Player player;
    private MoveGeneratorParams params;
    private double returnValue;
    private Move returnMove;
    private int returnMoveIndex;
    private MinMaxPriorityQueue<AlphaBetaClosure> moveCache;
    private boolean reachedTerminalState;

    public AlphaBetaClosure() {  }

    public AlphaBetaClosure clone() {
      AlphaBetaClosure value = new AlphaBetaClosure();
      value.rack = rack;
      value.board = board;
      value.state = state;
      value.remainingDepth = remainingDepth;
      value.alpha = alpha;
      value.beta = beta;
      value.player = player;
      value.params = params;
      value.returnValue = returnValue;
      value.returnMove = returnMove;
      value.moveCache = moveCache;
      value.returnMoveIndex = returnMoveIndex;
      value.reachedTerminalState = reachedTerminalState;

      return value;
    }

    public Rack getRack() {
      return rack;
    }

    public WordsWithFriendsBoard getBoard() {
      return board;
    }

    public GameState getState() {
      return state;
    }

    public int getRemainingDepth() {
      return remainingDepth;
    }

    public double getAlpha() {
      return alpha;
    }

    public double getBeta() {
      return beta;
    }

    public Player getPlayer() {
      return player;
    }

    public MoveGeneratorParams getParams() {
      return params;
    }

    public double getReturnValue() {
      return returnValue;
    }

    public Move getReturnMove() {
      return returnMove;
    }

    public MinMaxPriorityQueue<AlphaBetaClosure> getMoveCache() {
      return moveCache;
    }

    public int getReturnMoveIndex() {
      return returnMoveIndex;
    }

    public boolean reachedTerminalState() {
      return reachedTerminalState;
    }

    public AlphaBetaClosure setReturnMoveIndex(int returnMoveIndex) {
      this.returnMoveIndex = returnMoveIndex;
      return this;
    }

    public AlphaBetaClosure setMoveCache(MinMaxPriorityQueue<AlphaBetaClosure> moveCache) {
      this.moveCache = moveCache;
      return this;
    }

    public AlphaBetaClosure setReturnMove(Move returnMove) {
      this.returnMove = returnMove;
      return this;
    }

    public AlphaBetaClosure setReturnValue(double returnValue) {
      this.returnValue = returnValue;
      return this;
    }

    public AlphaBetaClosure setRack(Rack rack) {
      this.rack = rack;
      return this;
    }

    public AlphaBetaClosure setBoard(WordsWithFriendsBoard board) {
      this.board = board;
      return this;
    }

    public AlphaBetaClosure setState(GameState state) {
      this.state = state;
      return this;
    }

    public AlphaBetaClosure setRemainingDepth(int remainingDepth) {
      this.remainingDepth = remainingDepth;
      return this;
    }

    public AlphaBetaClosure setAlpha(double alpha) {
      this.alpha = alpha;
      return this;
    }

    public AlphaBetaClosure setBeta(double beta) {
      this.beta = beta;
      return this;
    }

    public AlphaBetaClosure setPlayer(Player player) {
      this.player = player;
      return this;
    }

    public AlphaBetaClosure setParams(MoveGeneratorParams params) {
      this.params = params;
      return this;
    }

    public AlphaBetaClosure setReachedTerminalState(boolean value) {
      this.reachedTerminalState = value;
      return this;
    }
  }
}
