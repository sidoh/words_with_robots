package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.data_structures.CollectionsHelper;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDagEdge;
import org.sidoh.words_with_robots.move_generation.context.WwfMoveGeneratorReturnContext;
import org.sidoh.wwf_api.game_state.Direction;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.SlotIterator;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Slot;
import org.sidoh.wwf_api.types.game_state.Tile;
import org.sidoh.wwf_api.types.game_state.WordOrientation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A move generator that uses a GADDAG.
 */
public class GadDagWwfMoveGenerator extends WordsWithFriendsAllMovesGenerator
  implements MoveGenerator<WordsWithFriendsBoard, WwfMoveGeneratorReturnContext>,
             GameStateMoveGenerator<WwfMoveGeneratorReturnContext> {

  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();

  private final GadDag gaddag;

  public GadDagWwfMoveGenerator(GadDag gaddag) {
    this.gaddag = gaddag;
  }

  @Override
  protected Set<Move> generateMoves(int row, int col, Rack rack, WordsWithFriendsBoard board) {
    SlotIterator.Iterator left = new SlotIterator.Iterator(row, col, WordsWithFriendsBoard.DIMENSIONS, WordsWithFriendsBoard.DIMENSIONS, WordOrientation.HORIZONTAL, Direction.BACKWARDS);
    SlotIterator.Iterator up = new SlotIterator.Iterator(row, col, WordsWithFriendsBoard.DIMENSIONS, WordsWithFriendsBoard.DIMENSIONS, WordOrientation.VERTICAL, Direction.BACKWARDS);

    Move blankMoveLeft = Move.play(Collections.<Tile>emptyList(), row, col, WordOrientation.HORIZONTAL);
    Move blankMoveUp = Move.play(Collections.<Tile>emptyList(), row, col, WordOrientation.VERTICAL);

    Set<Move> moves = new HashSet<Move>();

    gen(board, up, "", new HashSet<Tile>(rack.getTiles()), gaddag.getInitEdge(), blankMoveUp, moves);
    gen(board, left, "", new HashSet<Tile>(rack.getTiles()), gaddag.getInitEdge(), blankMoveLeft, moves);

    return moves;
  }

  @Override
  protected boolean isWord(String word) {
    return gaddag.isWord(word);
  }

  private void gen(WordsWithFriendsBoard board, SlotIterator.Iterator itr, String word, Set<Tile> tiles, GadDagEdge edge, Move move, Set<Move> moves) {
    int index = itr.current();
    Slot slot = board.getSlot(index);

    if (slot.getTile() != null) {
      goOn(board, itr.clone(), slot.getTile(), word, tiles, gaddag.nextEdge(edge, slot.getTile().getLetter().getValue()), edge, move.clone(), moves);
    }
    else if (tiles.size() > 0) {
      for (Tile tile : tiles) {
        goOn(board, itr.clone(), tile, word, CollectionsHelper.minus(tiles, tile), gaddag.nextEdge(edge, tile.getLetter().getValue()), edge, move.clone(), moves);
      }
    }
  }

  private void goOn(WordsWithFriendsBoard board, SlotIterator.Iterator itr, Tile l, String word, Set<Tile> tiles, GadDagEdge newEdge, GadDagEdge oldEdge, Move move, Set<Move> moves) {
    Slot slot = board.getSlot( itr.current() );

    if (itr.offset() <= 0) {
      word = l.getLetter().getValue().concat(word);

      if ( slot.getTile() == null && itr.hasNext() )
        move = move.playBack(l);
      else
        move = move.moveBack();

      boolean noLeft = ! itr.hasNext();

      if ( ! noLeft ) {
        noLeft = board.getSlot( itr.next() ).getTile() == null;
        itr.stepForwards();
      }

      if ( oldEdge.hasWordLetter(l.getLetter().getValue()) && noLeft ) {
        moves.add(move.moveForward());
      }

      if ( newEdge != null ) {
        SlotIterator.Iterator moveItr = itr.atOffset(itr.offset() - 1);

        if ( moveItr.hasNext() ) {
          gen(board, moveItr, word, tiles, newEdge, move, moves);
        }

        SlotIterator.Iterator forwardsItr = itr.atOffset(0).withDirection(Direction.FORWARDS);

        if ( itr.hasNext() )
          itr.next();

        // Skip over the hook tile
        if ( forwardsItr.hasNext() )
          forwardsItr.next();

        newEdge = gaddag.nextEdge( newEdge, GadDag.CONCAT_OPERATOR );
        noLeft = !itr.hasNext() || board.getSlot(itr.current()).getTile() == null;
        boolean roomRight = forwardsItr.hasNext();

        if ( newEdge != null && noLeft && roomRight ) {
          gen(board, forwardsItr, word, tiles, newEdge, move, moves);
        }
      }
    }
    else if (itr.offset() > 0) {
      word = word.concat(l.getLetter().getValue());

      if ( slot.getTile() == null && itr.hasNext() )
        move = move.playFront(l);

      boolean noRight = ! itr.hasNext();

      if ( ! noRight ) {
        noRight = board.getSlot( itr.next() ).getTile() == null;
        itr.stepBackwards();
      }

      if ( oldEdge.hasWordLetter( l.getLetter().getValue() ) && noRight ) {
        moves.add(move.moveForward());
      }

      SlotIterator.Iterator rItr = itr.atOffset(itr.offset() + 1);
      if ( newEdge != null && rItr.hasNext() ) {
        gen(board, rItr, word, tiles, newEdge, move, moves);
      }
    }
  }

  @Override
  public WwfMoveGeneratorReturnContext generateMove(Rack rack, WordsWithFriendsBoard board) {
    Move bestMove = null;

    for (Move move : generateAllMoves(rack, board)) {
      board.scoreMove(move);

      if ( bestMove == null || move.getResult().getScore() > bestMove.getResult().getScore() ) {
        bestMove = move;
      }
    }

    return new WwfMoveGeneratorReturnContext(bestMove);
  }

  @Override
  public WwfMoveGeneratorReturnContext generateMove(GameState state) {
    Rack rack = stateHelper.getCurrentPlayerRack(state);
    WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

    return generateMove(rack, board);
  }
}
