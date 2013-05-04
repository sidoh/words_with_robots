package org.sidoh.words_with_robots.move_generation.params;

import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.BoardStorage;
import org.sidoh.wwf_api.types.game_state.Rack;

/**
 *
 * @param <T> the type of parameters object that this builder should produce
 * @param <B> the type of this builder -- used to allow builder chaining syntax
 */
public abstract class AbstractParamsBuilder<T extends WwfMoveGeneratorParams, B extends AbstractParamsBuilder> {
  private EvaluationFunction evalFn;

  public AbstractParamsBuilder() {
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
   * Builds a parameters object from the provided rack and board. Note that using this instead of
   * {@link AbstractParamsBuilder#build(org.sidoh.wwf_api.types.api.GameState)} provides less information
   * to the move generator. Some move generators require the extra information provided in
   * {@link GameState}. As such, it's preferable to not use this method if a {@link GameState} object
   * is readily available.
   *
   * @param rack the player's rack
   * @param board the player's board
   * @return instace of the built object
   * @throws UnsupportedOperationException if {@link GameState} is required.
   */
  public abstract T build(Rack rack, WordsWithFriendsBoard board) throws UnsupportedOperationException;

  /**
   * Constructs all necessary parameters from a given GameState object
   *
   * @param state the game state. rack and board will be extracted
   * @return an instance of the built object
   */
  public abstract T build(GameState state);

  /**
   *
   * @return an instance of the builder -- used to allow for chaining
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
