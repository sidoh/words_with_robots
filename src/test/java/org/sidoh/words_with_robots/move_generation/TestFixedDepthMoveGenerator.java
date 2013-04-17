package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Lists;
import org.apache.thrift.TException;
import org.sidoh.words_with_robots.WordsWithRobotsTestCase;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParamKey;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.WwfMoveGeneratorParamKey;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.TileBuilder;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.BoardStorage;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Tile;
import org.sidoh.wwf_api.types.game_state.WordOrientation;

import java.io.IOException;

public class TestFixedDepthMoveGenerator extends WordsWithRobotsTestCase {
  public void testItsDaMan() throws IOException, TException {
    GameState state = loadGameState("TestFixedDepthMoveGenerator.testItsdaman.bin");

    // Make sure that a lower scoring move is chosen if it means the score differential is
    // better
    GadDag dict = buildGadDag( "BT", "UT", "TUB", "TABARETS" );
    WordsWithFriendsMoveGenerator allMovesGenerator = new GadDagWwfMoveGenerator(dict);
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

    MoveGeneratorParams params = new MoveGeneratorParams()
      .set(FixedDepthParamKey.MAXIMUM_DEPTH, 8)
      .set(WwfMoveGeneratorParamKey.GAME_STATE, state);
    System.out.println(moveGenerator.generateMove(rack, board, params));
  }
}
