package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.WordsWithRobotsTestCase;

import org.apache.thrift.TException;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.old_params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.old_params.WwfMoveGeneratorParamKey;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.WordOrientation;

import java.io.IOException;

public class TestWwfMinimaxLocal extends WordsWithRobotsTestCase {
  public void testIt() throws TException, IOException {
    GameState state = loadGameState("TestWwfMinimaxLocal.testIt.json");

    // Game was over at the time... manually set current user
    state.getMeta().setCurrentMoveUserId(27094722L);

    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    GadDag dict = buildGadDag(
      "ENTOPIC", "RIF", "LATEX", "LO", "AW", "ROW", "LOWER", "REWET", "WOE", "PIX", "ORE", "OOZY"
    );
    GadDagWwfMoveGenerator baseGen = new GadDagWwfMoveGenerator(dict);
    WwfMinimaxLocal gen = new WwfMinimaxLocal(baseGen);
    MoveGeneratorParams params = new MoveGeneratorParams()
      .set(WwfMoveGeneratorParamKey.GAME_STATE, new GameState(state));

    playWord(board, 7, 5, "ENTOPIC", WordOrientation.HORIZONTAL, true);
    playWord(board, 8, 8, "RIF", WordOrientation.HORIZONTAL, true);

    Rack myRack = buildRack("ETOZLAX");
    Rack opRack = buildRack("EUREWUO");

    board.move(gen.generateMove(myRack, board, params));

    System.out.println(board);

    baseGen.generateMove(opRack, board, params);
  }

  public void testDanB() throws IOException, TException {
    GameState state = loadGameState("TestWwfMinimaxLocal.testDanB.json");

    GadDag dict = buildGadDag(
      "BY", "BROSE", "NA", "FAVOR", "BA"
    );
    GadDagWwfMoveGenerator baseGen = new GadDagWwfMoveGenerator(dict);
    WwfMinimaxLocal gen = new WwfMinimaxLocal(baseGen);
    MoveGeneratorParams params = new MoveGeneratorParams()
      .set(WwfMoveGeneratorParamKey.GAME_STATE, state);
    WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

    Rack myRack = buildRack("BOEESZR");
    Rack opRack = buildRack("RRVFRNA");

    board.move(gen.generateMove(myRack, board, params));
    Move opMove = baseGen.generateMove(opRack, board, params);

    assertEquals("moves should match",
      opMove,
      params.get(WwfMoveGeneratorParamKey.BEST_OPPONENT_MOVE));
  }
}
