package org.sidoh.words_with_robots.move_generation;

import org.apache.thrift.TException;
import org.sidoh.words_with_robots.WordsWithRobotsTestCase;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.params.WwfMinimaxLocalParams;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.WordOrientation;

import java.io.IOException;

public class TestWwfMinimaxLocal extends WordsWithRobotsTestCase {
  private static final WwfMinimaxLocalParams.Builder paramsBuilder
    = new WwfMinimaxLocalParams.Builder();

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

    playWord(board, 7, 5, "ENTOPIC", WordOrientation.HORIZONTAL, true);
    playWord(board, 8, 8, "RIF", WordOrientation.HORIZONTAL, true);

    Rack myRack = buildRack("ETOZLAX");
    Rack opRack = buildRack("EUREWUO");

    board.move(gen.generateMove(paramsBuilder.build(state)).getMove());

    System.out.println(board);

    baseGen.generateMove(paramsBuilder.build(state));
  }

  public void testDanB() throws IOException, TException {
    GameState state = loadGameState("TestWwfMinimaxLocal.testDanB.json");

    GadDag dict = buildGadDag(
      "BY", "BROSE", "NA", "FAVOR", "BA"
    );
    GadDagWwfMoveGenerator baseGen = new GadDagWwfMoveGenerator(dict);
    WwfMinimaxLocal gen = new WwfMinimaxLocal(baseGen);
    WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

    Rack myRack = buildRack("BOEESZR");
    Rack opRack = buildRack("RRVFRNA");

    Move move = gen.generateMove(paramsBuilder.build(state)).getMove();
    board.move(move);
    state = stateHelper.applyMove(state, move);

    // TODO: test something...
  }
}
