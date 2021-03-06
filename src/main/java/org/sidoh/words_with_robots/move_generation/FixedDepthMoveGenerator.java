package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Lists;
import org.sidoh.words_with_robots.move_generation.context.FixedDepthReturnContext;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParams;
import org.sidoh.words_with_robots.move_generation.util.MoveScoreComparator;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * (roughly) implements a fixed-depth search move generator. attempts to make a few optimizations by limiting the
 * branching factor and ignoring a few low-score moves.
 *
 */
public class FixedDepthMoveGenerator implements GameStateMoveGenerator<FixedDepthReturnContext> {
  private static final Logger LOG = LoggerFactory.getLogger(FixedDepthMoveGenerator.class);
  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();
  private final AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator;
  private final FixedDepthParams params;

  public FixedDepthMoveGenerator(AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator) {
    this(allMovesGenerator, new FixedDepthParams());
  }

  public FixedDepthMoveGenerator(AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator, FixedDepthParams params) {
    this.allMovesGenerator = allMovesGenerator;
    this.params = params;
  }

  public FixedDepthMoveGenerator deepCopy() {
    return new FixedDepthMoveGenerator(allMovesGenerator, params.deepCopy());
  }

  public FixedDepthMoveGenerator updateParam(FixedDepthParams._Fields key, Object value) {
    params.setFieldValue(key, value);
    return this;
  }

  @Override
  public FixedDepthReturnContext generateMove(GameState state) {
    // Perform search
    AlphaBetaClosure inputClosure = AlphaBetaClosure.newClosure(state, params);
    AlphaBetaClosure closure = alphaBetaSearch(inputClosure);

    // Pass if we have to
    if ( closure.getReturnMove() == null || closure.getReturnMove().getMoveType() == MoveType.PASS ) {
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

    return new FixedDepthReturnContext(closure.getReturnMove())
      .setTerminal(closure.reachedTerminalState());
  }

  protected AlphaBetaClosure alphaBetaSearch(AlphaBetaClosure closure) {
    // If this is a terminal state, evaluate the game state.
    if ( closure.getRemainingDepth() == 0 || closure.getRack().getTilesSize() == 0 ) {
      double score = evaluateState(closure);
      return closure
        .setReturnValue( score )
        .setReachedTerminalState( closure.getRack() == null || closure.getRack().getTilesSize() == 0 );
    }

    if ( Thread.currentThread().isInterrupted() ) {
      return closure;
    }

    double alpha = closure.getAlpha();
    double beta = closure.getBeta();
    MinimaxPlayer player = closure.getPlayer();

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

    LOG.debug("At depth {}, considering {} possible moves", closure.getRemainingDepth(), moves.size());

    int movesConsidered = 0;

    for (Move move : moves) {
      // Create copy of board so as to not mess with future calls
      WordsWithFriendsBoard board = closure.getBoard().clone();

      // Make move for recursive calls
      board.move( move );

      // Stop considering moves if this one sucks and we've already considered better moves.
      if ( move.getResult().getScore() < params.getMinScore() && movesConsidered > 0 ) {
        LOG.debug("stop considering moves after seeing one with score {}", move.getResult().getScore());
        break;
      }

      // Break early if the branching factor limits us
      if ( movesConsidered >= params.getBranchingFactorLimit()) {
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
        .setReturnMoveIndex(movesConsidered)
        .setPlayer(player.getOther());

      callClosure = alphaBetaSearch(callClosure);

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
        .setAlpha(alpha)
        .setBeta(beta)
        .setReturnValue( player.isMax() ? alpha : beta )
        .setPlayer(player)
        .setReturnMove( null );
    }

    LOG.debug("best in this branch: {}", (player.isMax() ? alpha : beta));

    return returnClosure
      .setAlpha(alpha)
      .setBeta(beta)
      .setReturnValue(
        player.isMax() ? alpha : beta)
      .setReturnMove( returnMove );
  }

  protected List<Move> getSortedMoves(AlphaBetaClosure closure) {
    List<Move> allMoves = Lists.newArrayList(allMovesGenerator.generateAllMoves(closure.getRack(), closure.getBoard()));
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

  /**
   * A helper class to make switching between min/max easy.
   */
  public static class MinimaxPlayer {
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

    public static AlphaBetaClosure newClosure(GameState state, FixedDepthParams params) {
      MinimaxPlayer player = MinimaxPlayer.fromGameState(state);
      WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

      return new AlphaBetaClosure()
        .setAlpha(Double.NEGATIVE_INFINITY)
        .setBeta(Double.POSITIVE_INFINITY)
        .setPlayer(player)
        .setState(state)
        .setBoard(board)
        .setRack(stateHelper.buildRack(player.getUserId(), state))
        .setRemainingDepth(params.getMaxDepth());
    }
  }
}
