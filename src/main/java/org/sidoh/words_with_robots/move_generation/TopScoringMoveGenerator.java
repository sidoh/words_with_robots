package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.move_generation.context.WwfMoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;

public class TopScoringMoveGenerator implements MoveGenerator<WordsWithFriendsBoard, WwfMoveGeneratorReturnContext>,
  GameStateMoveGenerator<WwfMoveGeneratorReturnContext> {

  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();
  private final AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator;
  private final EvaluationFunction evaluationFunction;

  public TopScoringMoveGenerator(AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator, EvaluationFunction evaluationFunction) {
    this.allMovesGenerator = allMovesGenerator;
    this.evaluationFunction = evaluationFunction;
  }

  @Override
  public WwfMoveGeneratorReturnContext generateMove(Rack rack, WordsWithFriendsBoard board) {
    Iterable<Move> moves = allMovesGenerator.generateAllMoves(rack, board);

    return new WwfMoveGeneratorReturnContext(chooseBestMove(moves, null));
  }

  @Override
  public WwfMoveGeneratorReturnContext generateMove(GameState state) {
    Rack rack = stateHelper.getCurrentPlayerRack(state);
    WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);
    Iterable<Move> moves = allMovesGenerator.generateAllMoves(rack, board);

    return new WwfMoveGeneratorReturnContext(chooseBestMove(moves, state));
  }

  protected Move chooseBestMove(Iterable<Move> moves, GameState state) {
    double bestScore = 0, score;
    Move bestMove = null;

    for (Move move : moves) {
      state = updateGameState(state, move);

      if (bestMove == null) {
        bestMove = move;
        bestScore = evaluationFunction.score(move, state);
      }
      else if ( (score = evaluationFunction.score(move, state)) > bestScore ){
        bestMove = move;
        bestScore = score;
      }
    }

    return bestMove;
  }

  protected GameState updateGameState(GameState state, Move move) {
    if (state == null) {
      return null;
    }
    return stateHelper.applyMove(state, move);
  }
}
