package org.sidoh.words_with_robots.move_generation;

import org.sidoh.wwf_api.game_state.Board;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.game_state.Rack;

public interface AllMovesGenerator<T extends Board> {
  /**
   * Given a board and a rack, generate all possible moves.
   *
   * @param rack
   * @param board
   * @return
   */
  public Iterable<Move> generateAllMoves(Rack rack, T board);

  /**
   * Returns the rank of the provided move in relation to a list of all moves.
   *
   * @param allMoves all moves possible
   * @param move the move in question
   * @return
   */
  public int getMoveScoreRank(Iterable<Move> allMoves, Move move);

  /**
   *
   * @param board
   * @param move
   * @return true iff the provided move is a valid play
   */
  public boolean isValidMove(T board, Move move);
}
