package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;
import org.sidoh.words_with_robots.data_structures.CollectionsHelper;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParamKey;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.PreemptionContext;
import org.sidoh.words_with_robots.move_generation.params.WwfMoveGeneratorParamKey;
import org.sidoh.words_with_robots.move_generation.swap_strategies.SwapStrategy;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Tile;
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

  public FixedDepthMoveGenerator(WordsWithFriendsMoveGenerator inner) {
    this.inner = inner;
  }

  @Override
  public Move generateMove(Rack baseRack, WordsWithFriendsBoard board, MoveGeneratorParams params) {
    // Perform search
    AlgorithmSettings settings = AlgorithmSettings.fromParams(params);
    AlphaBetaClosure inputClosure = AlphaBetaClosure.newClosure(params);
    AlphaBetaClosure closure = alphaBetaSearch(settings, inputClosure);

    List<Tile> tilesToSwap = settings.getSwapStrategy().getTilesToSwap(closure.getState(), closure.getReturnMove());
    if ( tilesToSwap.size() > 0 ) {
      closure.setReturnMove(Move.swap(tilesToSwap));
    }

    // Swap if strategy decided we should
    if ( closure.getReturnMove() != null && closure.getReturnMove().getMoveType() == MoveType.SWAP ) {
      LOG.info("swap strategy decided we should SWAP: {}", closure.getReturnMove());
    }
    // Pass if we have to
    else if ( closure.getReturnMove() == null || closure.getReturnMove().getMoveType() == MoveType.PASS ) {
      closure.setReturnMove(Move.pass());

      LOG.info("couldn't generate move -- forcing a PASS. ab-search returned {}", closure.getReturnValue());
    }
    else {
      LOG.info("generated move: {}. It had index {}, and is worth {} points. ab-search returned: {}",
        closure.getReturnMove().getResult().getResultingWords(),
        closure.getReturnMoveIndex(),
        closure.getReturnMove().getResult().getScore(),
        closure.getReturnValue());
    }

    return closure.getReturnMove();
  }

  protected AlphaBetaClosure alphaBetaSearch(AlgorithmSettings settings, AlphaBetaClosure closure) {
    // If this is a terminal state, evaluate the game state.
    if ( closure.getRemainingDepth() == 0 || closure.getRack().getTilesSize() == 0 ) {
      double score = evaluateState(closure);
      return closure
        .setReturnValue( score )
        .setReachedTerminalState( closure.getRack() == null || closure.getRack().getTilesSize() == 0 );
    }

    PreemptionContext preemptionContext = settings.getPreemptionContext();
    if ( preemptionContext.getPreemptState() == PreemptionContext.State.STRONG_PREEMPT ) {
      return closure;
    }

    double alpha = closure.getAlpha();
    double beta = closure.getBeta();
    MinimaxPlayer player = closure.getPlayer();

    // Try moves in the move cache if there are any. These moves will have been generated by
    // siblings in the call tree, and some of the moves may still be possible in this node.
    // Anything we can do to avoid generating all possible moves gives a huge performance
    // boost.
    if ( closure.getMoveCache() != null ) {
      for (CachedMove cachedMove : closure.getMoveCache()) {
        WordsWithFriendsBoard boardCopy = closure.getBoard().clone();
        Move.Result result = boardCopy.move(cachedMove.getMove());

        // Will need to check if this is still a legal move.
        if ( isValidMove(result) ) {
          LOG.debug("Move cache hit at level {}. Move = {}", closure.getRemainingDepth(), closure.getReturnMove().getResult().getResultingWords());

          double cAlpha = cachedMove.getAlpha();
          double cBeta = cachedMove.getBeta();

          if ( cBeta <= cAlpha ) {
            return cachedMove.toAlphaBetaClosure();
          }
        }
      }
    }

    // If not, generate moves.
    List<Move> moves = getSortedMoves(closure);

    // If no moves are possible, this is a terminal state
    if ( moves.isEmpty() ) {
      return closure
        .setReturnValue( evaluateState(closure) )
        .setReturnMove(Move.pass())
        .setReachedTerminalState(true);
    }

    Move returnMove = null;
    AlphaBetaClosure returnClosure = null;
    MinMaxPriorityQueue<CachedMove> moveCache = MinMaxPriorityQueue
      .maximumSize(settings.getMoveCacheSize())
      .create();

    LOG.debug("At depth {}, considering {} possible moves", closure.getRemainingDepth(), moves.size());

    int movesConsidered = 0;

    for (Move move : moves) {
      // Create copy of board so as to not mess with future calls
      WordsWithFriendsBoard board = closure.getBoard().clone();

      // Make move for recursive calls
      board.move( move );

      // Stop considering moves if this one sucks and we've already considered better moves.
      if ( move.getResult().getScore() < settings.getMinScore() && movesConsidered > 0 ) {
        LOG.debug("stop considering moves after seeing one with score {}", move.getResult().getScore());
        break;
      }

      // Break early if the branching factor limits us
      if ( movesConsidered >= settings.getBranchingFactorLimit() ) {
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

      callClosure = alphaBetaSearch(settings, callClosure);

      // Offer the result of this call to the cache, which may prevent generating moves for future
      // calls. Only cache moves that are actually moves
      if ( callClosure.getReturnMove() != null && callClosure.getReturnMove().getMoveType() != MoveType.PASS ) {
        moveCache.offer( CachedMove.fromClosure(callClosure) );
      }

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
        .setPlayer(player)
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
    MinimaxPlayer player = closure.getPlayer();
    GameState state = closure.getState();

    MinimaxPlayer max = player.isMax() ? player : player.getOther();

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

  /**
   * A helper class to make switching between min/max easy.
   */
  static class MinimaxPlayer {
    private final long[] players;
    private final int selected;
    private final boolean isMin;
    private final MinimaxPlayer other;

    public MinimaxPlayer(long uid1, long uid2, long selected, boolean isMin) {
      this( new long[] { uid1, uid2 },
        uid1 == selected ? 0 : 1,
        isMin);
    }

    public MinimaxPlayer(long[] uids, int selected, boolean isMin) {
      this.players = uids;
      this.selected = selected;
      this.isMin = isMin;
      this.other = new MinimaxPlayer(this);
    }

    private MinimaxPlayer(MinimaxPlayer other) {
      this.players = other.players;
      this.selected = other.selected + 1;
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

    public MinimaxPlayer getOther() {
      return other;
    }

    public static MinimaxPlayer fromGameState(GameState state) {
      List<Long> playerIds = Lists.newArrayList(state.getMeta().getUsersById().keySet());
      return new MinimaxPlayer(playerIds.get(0), playerIds.get(1), state.getMeta().getCurrentMoveUserId(), false);
    }
  }

  /**
   * Wraps a move and the measurements we need to score the move
   */
  protected static class CachedMove implements Comparable<CachedMove> {
    private final double returnValue;
    private final double alpha;
    private final double beta;
    private final Move move;
    private final MinimaxPlayer player;

    private CachedMove(double returnValue, double alpha, double beta, Move move, MinimaxPlayer player) {
      this.returnValue = returnValue;
      this.alpha = alpha;
      this.beta = beta;
      this.move = move;
      this.player = player;
    }

    public Move getMove() {
      return move;
    }

    public double getReturnValue() {
      return returnValue;
    }

    public double getAlpha() {
      return alpha;
    }

    public double getBeta() {
      return beta;
    }

    @Override
    public int compareTo(CachedMove cachedMove) {
      double score1 = player.isMax() ? cachedMove.returnValue : returnValue;
      double score2 = player.isMax() ? returnValue : cachedMove.returnValue;

      return Double.compare(score1, score2);
    }

    public AlphaBetaClosure toAlphaBetaClosure() {
      return new AlphaBetaClosure()
        .setAlpha(alpha)
        .setBeta(beta)
        .setReturnValue(returnValue)
        .setReturnMove(move)
        .setPlayer(player);
    }

    public static CachedMove fromClosure(AlphaBetaClosure closure) {
      return new CachedMove(closure.getReturnValue(),
        closure.getAlpha(),
        closure.getBeta(),
        closure.getReturnMove(),
        closure.getPlayer());
    }
  }

  /**
   * A wrapper around move generator settings -- avoid the overhead of hashtable lookups to get static
   * properties
   */
  protected static class AlgorithmSettings {
    private final int moveCacheSize;
    private final int minScore;
    private final int branchingFactorLimit;
    private final PreemptionContext context;
    private final SwapStrategy swapStrategy;

    private AlgorithmSettings(int moveCacheSize, int minScore, int branchingFactorLimit, PreemptionContext context, SwapStrategy swapStrategy) {
      this.moveCacheSize = moveCacheSize;
      this.minScore = minScore;
      this.branchingFactorLimit = branchingFactorLimit;
      this.context = context;
      this.swapStrategy = swapStrategy;
    }

    public static AlgorithmSettings fromParams(MoveGeneratorParams params) {
      return new AlgorithmSettings(
        params.getInt(FixedDepthParamKey.MOVE_CACHE_SIZE),
        params.getInt(FixedDepthParamKey.MIN_SCORE),
        params.getInt(FixedDepthParamKey.BRANCHING_FACTOR_LIMIT),
        (PreemptionContext) params.get(FixedDepthParamKey.PREEMPTION_CONTEXT),
        (SwapStrategy)params.get(WwfMoveGeneratorParamKey.SWAP_STRATEGY));
    }

    public SwapStrategy getSwapStrategy() {
      return swapStrategy;
    }

    public int getMoveCacheSize() {
      return moveCacheSize;
    }

    public int getMinScore() {
      return minScore;
    }

    public int getBranchingFactorLimit() {
      return branchingFactorLimit;
    }

    public PreemptionContext getPreemptionContext() {
      return context;
    }
  }

  /**
   * A closure to pass around state between nodes in the search tree. Much nicer than a shitload of arguments.
   */
  protected static class AlphaBetaClosure {
    private Rack rack;
    private WordsWithFriendsBoard board;
    private GameState state;
    private int remainingDepth;
    private double alpha;
    private double beta;
    private MinimaxPlayer player;
    private double returnValue;
    private Move returnMove;
    private int returnMoveIndex;
    private MinMaxPriorityQueue<CachedMove> moveCache;
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

    public MinimaxPlayer getPlayer() {
      return player;
    }

    public double getReturnValue() {
      return returnValue;
    }

    public Move getReturnMove() {
      return returnMove;
    }

    public MinMaxPriorityQueue<CachedMove> getMoveCache() {
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

    public AlphaBetaClosure setMoveCache(MinMaxPriorityQueue<CachedMove> moveCache) {
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

    public AlphaBetaClosure setPlayer(MinimaxPlayer player) {
      this.player = player;
      return this;
    }

    public AlphaBetaClosure setReachedTerminalState(boolean value) {
      this.reachedTerminalState = value;
      return this;
    }

    public static AlphaBetaClosure newClosure(MoveGeneratorParams params) {
      GameState state = (GameState) params.get(WwfMoveGeneratorParamKey.GAME_STATE);
      MinimaxPlayer player = MinimaxPlayer.fromGameState(state);
      WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

      return new AlphaBetaClosure()
        .setAlpha(Double.NEGATIVE_INFINITY)
        .setBeta(Double.POSITIVE_INFINITY)
        .setPlayer(player)
        .setState(state)
        .setBoard(board)
        .setRack(stateHelper.buildRack(player.getUserId(), state))
        .setRemainingDepth(params.getInt(FixedDepthParamKey.MAXIMUM_DEPTH));
    }
  }
}
