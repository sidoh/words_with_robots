package org.sidoh.tiler.move_generation;

import org.sidoh.tiler.move_generation.eval.EvaluationFunction;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.TileBuilder;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Letter;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class WordsWithFriendsMoveGenerator extends MoveGenerator<WordsWithFriendsBoard> {
  private static final Logger LOG = LoggerFactory.getLogger(WordsWithFriendsMoveGenerator.class);

  public static class Params extends MoveGenerator.Params {
    private final GameState gameState;
    private Integer defaultThreshold;
    private Move opMove;

    public Params(GameState state) {
      this.gameState = state;
      this.defaultThreshold = Integer.MAX_VALUE;
    }

    public GameState getGameState() {
      return gameState;
    }

    public Integer getDefaultThreshold() {
      return defaultThreshold;
    }

    public Params setDefaultThreshold(int value) {
      this.defaultThreshold = value;
      return this;
    }

    public Params setBestOpMove(Move opMove) {
      this.opMove = opMove;
      return this;
    }

    public Move getOpMove() {
      return opMove;
    }
  }

  @Override
  public Move generateMove(Rack baseRack, WordsWithFriendsBoard board, MoveGenerator.Params params) {
    if (! (params instanceof Params)) {
      throw new IllegalArgumentException("invalid params");
    }

    EvaluationFunction evalFn = params.getEvalFunction();
    GameState state = ((Params)params).getGameState();
    Move bestMove = null;
    double bestScore = 0;


    for (Move move : generateAllPossibleMoves(baseRack, board)) {
      double score = evalFn.score(move, state);

      if ( bestMove == null || score > bestScore ) {
        LOG.info("Found new best move worth {} points: {}", move.getResult().getScore(), move.getResult().getResultingWords());

        bestMove = move;
        bestScore = score;
      }
    }

    return bestMove;
  }

  public Iterable<Move> generateAllPossibleMoves(final Rack baseRack, final WordsWithFriendsBoard board) {
    // If there have been no moves, then only valid to play on (7, 7).
    if ( ! board.hasTiles() ) {
      Set<Move> possibleMoves = new HashSet<Move>();

      for (Rack rack : expandBlanks(baseRack)) {
        // No need to check for validity since only one word can be formed
        for (Move move : generateMoves(7, 7, rack, board)) {
          board.scoreMove(move);

          possibleMoves.add(move);
        }
      }

      return possibleMoves;
    }
    else  {
      return new Iterable<Move>() {
        @Override
        public Iterator<Move> iterator() {
          return new AllMovesIterator(baseRack, board);
        }
      };
    }
  }

  /**
   *
   * @param move
   * @return true if and only if the provided move results only in valid words
   */
  protected boolean isValidMove(Move.Result move) {
    for (String word : move.getResultingWords()) {
      if ( ! isWord(word) )
        return false;
    }

    return true;
  }

  /**
   * Returns a set of possible racks where blanks are bound to letters (but still are worth 0 points).
   *
   * @param rack
   * @return
   */
  protected static Set<Rack> expandBlanks(Rack rack) {
    Rack base = new Rack().setCapacity(rack.getCapacity());
    List<Tile> blanks = new ArrayList<Tile>();

    for (Tile tile : rack.getTiles()) {
      if (! tile.getLetter().getValue().equals(WordsWithFriendsBoard.BLANK_LETTER)) {
        base.addToTiles(tile);
      }
      else {
        blanks.add(tile);
      }
    }

    if (blanks.size() == 0) {
      return Collections.singleton(rack);
    }

    Set<Letter> choices = new HashSet<Letter>();
    for (TileBuilder tileBuilder : WordsWithFriendsBoard.TILES) {
      Tile built = tileBuilder.build();

      if ( ! built.getLetter().getValue().equals( WordsWithFriendsBoard.BLANK_LETTER) )
        choices.add(built.getLetter());
    }

    if (blanks.size() == 1) {
      return expand1Blank(base, choices, blanks.get(0));
    }
    else if (blanks.size() == 2) {
      return expand2Blanks(base, choices, blanks.get(0), blanks.get(1));
    }
    else {
      throw new RuntimeException("shouldn't have more than 2 blanks!");
    }
  }

  /**
   * Sloppy and quick
   *
   * @param baseRack
   * @param choices
   * @param blank1
   * @return
   */
  protected static Set<Rack> expand1Blank(Rack baseRack, Set<Letter> choices, Tile blank1) {
    Set<Rack> ret = new HashSet<Rack>();

    for (Letter choice : choices) {
      Rack val = baseRack.deepCopy();
      val.addToTiles(blank1.deepCopy().setLetter(choice));

      ret.add(val);
    }

    return ret;
  }

  /**
   * sloppy and quick x  2
   * @param baseRack
   * @param choices
   * @param blank1
   * @param blank2
   * @return
   */
  protected static Set<Rack> expand2Blanks(Rack baseRack, Set<Letter> choices, Tile blank1, Tile blank2) {
    Set<Rack> vals = new HashSet<Rack>(expand1Blank(baseRack, choices, blank1));
    Set<Rack> ret = new HashSet<Rack>();

    for (Rack br : vals) {
      for (Letter choice : choices) {
        Rack val = br.deepCopy();
        val.addToTiles(blank2.deepCopy().setLetter(choice));

        ret.add(val);
      }
    }

    return vals;
  }

  /**
   * Generate all possible moves when playing at (col, row).
   *
   * @param row
   * @param col
   * @param rack
   * @param board
   * @return
   */
  protected abstract Set<Move> generateMoves(int row, int col, Rack rack, WordsWithFriendsBoard board);

  /**
   *
   * @param word
   * @return true if the provided word is valid for the game
   */
  protected abstract boolean isWord(String word);

  protected class AllMovesIterator implements Iterator<Move> {
    private final Set<Rack> racks;
    private WordsWithFriendsBoard board;
    private int nextSlot;
    private Deque<Move> remainingMoves;
    private final int indexBound;

    public AllMovesIterator(Rack baseRack, WordsWithFriendsBoard board) {
      this.racks = expandBlanks(baseRack);
      this.board = board;
      this.nextSlot = -1;
      this.remainingMoves = new LinkedList<Move>();
      this.indexBound = WordsWithFriendsBoard.DIMENSIONS * WordsWithFriendsBoard.DIMENSIONS;

      advanceToNextLegalSlot();
      fillMoves();
    }

    private void advanceToNextLegalSlot() {
      nextSlot++;

      while ( nextSlot < indexBound && board.getSlot(nextSlot).getTile() == null && ! board.getAdjacentSlots(nextSlot).hasAnyTouching() ) {
        nextSlot++;
      }
    }

    @Override
    public boolean hasNext() {
      return !remainingMoves.isEmpty();
    }

    @Override
    public Move next() {
      if ( remainingMoves.isEmpty() ) {
        throw new NoSuchElementException();
      }

      Move next = remainingMoves.removeFirst();

      if ( remainingMoves.isEmpty() ) {
        fillMoves();
      }

      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove isn't supported");
    }

    private void fillMoves() {
      while ( remainingMoves.isEmpty() && nextSlot < indexBound ) {
        int row = WordsWithFriendsBoard.getRowFromIndex(nextSlot);
        int col = WordsWithFriendsBoard.getColFromIndex(nextSlot);

        remainingMoves = new LinkedList<Move>( getAllMoves(row, col) );
        advanceToNextLegalSlot();
      }
    }

    private Set<Move> getAllMoves(int row, int col) {
      Set<Move> mergedMoves = new HashSet<Move>();

      for (Rack boundRack : racks) {
        Set<Move> possibleMoves = generateMoves(row, col, boundRack, board);

        for (Move possibleMove : possibleMoves) {
          // Score the move to generate the result
          board.scoreMove(possibleMove);

          if ( isValidMove(possibleMove.getResult()) ) {
            mergedMoves.add(possibleMove);
          }
        }
      }

      return mergedMoves;
    }
  }

  public static Params defaultParams() {
    return new Params(null);
  }
}
