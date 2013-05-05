package org.sidoh.words_with_robots.move_generation;

import org.apache.thrift.TException;
import org.sidoh.words_with_robots.WordsWithRobotsTestCase;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.WordOrientation;

import java.io.IOException;

public class TestFixedDepthMoveGenerator extends WordsWithRobotsTestCase {
  public void testItsDaMan() throws IOException, TException {
    GameState state = loadGameState("TestFixedDepthMoveGenerator.testItsdaman.bin");

    // Make sure that a lower scoring move is chosen if it means the score differential is
    // better
    GadDag dict = buildGadDag( "BT", "UT", "TUB", "TABARETS" );
    WordsWithFriendsAllMovesGenerator allMovesGenerator = new GadDagWwfMoveGenerator(dict);
    FixedDepthMoveGenerator moveGenerator = new FixedDepthMoveGenerator(allMovesGenerator);
    WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

    // Play move that wasn't persisted in the state
    Rack rack = new Rack().setTiles(state.getRacks().get(state.getMeta().getCurrentMoveUserId()));
    Move bigottedMove = // LOL I'M SO FUNNY
      Move.play(getWordTilesFromRack(rack, "BIGO"),
        0,
        9,
        WordOrientation.VERTICAL);
    board.move(bigottedMove);
    state = stateHelper.applyMove(state, bigottedMove);

    // Pretend it's our turn again
    state.getMeta().setCurrentMoveUserId(stateHelper.getOtherUser(state.getMeta().getCurrentMoveUserId(), state).getId());

    Move move = moveGenerator.generateMove(state).getMove();
    board.move(move);

    // Here, the move should be 'TUB' on the B in 'BIGOT'. This is a lower scoring move than
    // the alternative, but it blocks the opponent from getting a very beneficial play by playing
    // 'TABARETS' across the same B.
    assertEquals(move.getResult().getMainWord(), "TUB");
  }

  public void testUser() {
    FixedDepthMoveGenerator.MinimaxPlayer player = new FixedDepthMoveGenerator.MinimaxPlayer(1, 2, 1, false);

    assertTrue("Should be max", player.isMax());
    assertEquals("Should have ID of first player", 1L, player.getUserId());
    assertEquals("Other player ID should be second", 2L, player.getOtherUserId());

    player = player.getOther();
    assertTrue("Other should be min", player.isMin());
    assertEquals("Other should have ID of second player", 2L, player.getUserId());
    assertEquals("Other should have other ID of first player", 1L, player.getOtherUserId());
  }
}
