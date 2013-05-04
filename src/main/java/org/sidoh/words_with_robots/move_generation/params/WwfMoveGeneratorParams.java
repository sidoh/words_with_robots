package org.sidoh.words_with_robots.move_generation.params;

import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.BoardStorage;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Tile;

/**
 * Defines parameters used by all WordsWithFriends move generators
 */
public class WwfMoveGeneratorParams extends MoveGeneratorParams<WordsWithFriendsBoard> {
  private final EvaluationFunction evalFn;
  private final GameState state;

  public static class Builder extends AbstractBuilder<WwfMoveGeneratorParams, Builder> {
    @Override
    public WwfMoveGeneratorParams build(Rack rack, WordsWithFriendsBoard board) {
      return new WwfMoveGeneratorParams(rack,
        board,
        getEvaluationFunction(),
        null);
    }

    @Override
    public WwfMoveGeneratorParams build(GameState state) {
      return new WwfMoveGeneratorParams(buildRack(state),
        buildBoard(state),
        getEvaluationFunction(),
        state);
    }

    @Override
    protected Builder getBuilderInstance() {
      return this;
    }
  }

  public abstract static class AbstractBuilder<T extends WwfMoveGeneratorParams, B extends AbstractBuilder> {
    private EvaluationFunction evalFn;

    public AbstractBuilder() {
      this.evalFn = new ScoreEvalFunction();
    }

    /**
     *
     * @param evalFn the evaluation function used to determine move ranks
     * @return this
     */
    public B setEvaluationFunction(EvaluationFunction evalFn) {
      this.evalFn = evalFn;
      return getBuilderInstance();
    }

    /**
     *
     * @param rack the player's rack
     * @param board the player's board
     * @return instace of the built object
     */
    protected abstract T build(Rack rack, WordsWithFriendsBoard board);

    /**
     *
     * @param state the game state. rack and board will be extracted
     * @return an instance of the built object
     */
    protected abstract T build(GameState state);

    /**
     *
     * @return
     */
    protected abstract B getBuilderInstance();

    /**
     *
     * @return a rack constructed from game state -- uses the current player's rack
     */
    protected Rack buildRack(GameState state) {
      return new Rack()
        .setCapacity(WordsWithFriendsBoard.TILES_PER_PLAYER)
        .setTiles(state.getRacks().get(state.getMeta().getCurrentMoveUserId()));
    }

    /**
     *
     * @return a board reconstructed from the game state's board
     */
    protected WordsWithFriendsBoard buildBoard(GameState state) {
      return new WordsWithFriendsBoard(new BoardStorage(state.getBoard()));
    }

    protected EvaluationFunction getEvaluationFunction() {
      return evalFn;
    }
  }

  protected WwfMoveGeneratorParams(Rack rack, WordsWithFriendsBoard board, EvaluationFunction evalFn, GameState state) {
    super(rack, board);
    this.evalFn = evalFn;
    this.state = state;
  }

  /**
   *
   * @return the evaluation function used to score generated moves
   */
  public EvaluationFunction getEvaluationFunction() {
    return evalFn;
  }

  /**
   *
   * @return game state object -- may be null
   */
  public GameState getGameState() {
    return state;
  }
}
