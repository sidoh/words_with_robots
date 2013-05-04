package org.sidoh.words_with_robots.move_generation.params;

import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;

/**
 * Defines parameters used by all WordsWithFriends move generators
 */
public class WwfMoveGeneratorParams extends MoveGeneratorParams<WordsWithFriendsBoard> {
  private final EvaluationFunction evalFn;
  private final GameState state;

  public static class Builder extends AbstractParamsBuilder<WwfMoveGeneratorParams, Builder> {
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
