package org.sidoh.words_with_robots.move_generation;

import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.WordsWithRobotsTestCase;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.WwfMoveGeneratorParamKey;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveData;
import org.sidoh.wwf_api.types.api.User;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.WordOrientation;

public class TestGadDagWwfMoveGenerator extends WordsWithRobotsTestCase {
  private static final MoveGeneratorParams params = new MoveGeneratorParams()
    .set(WwfMoveGeneratorParamKey.GAME_STATE, null);

  /**
   *
   *           HADOOPY
   *            BOOPY
   *
   */
  public void testPlayParallel() {
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    GadDag gaddag = new GadDag();

    gaddag.addWord("HADOOPY");
    gaddag.addWord("BOOPY");
    gaddag.addWord("AB");
    gaddag.addWord("DO");
    gaddag.addWord("OO");
    gaddag.addWord("OP");
    gaddag.addWord("PY");

    GadDagWwfMoveGenerator gen = new GadDagWwfMoveGenerator(gaddag);
    Rack rack = buildRack("BOOPY");

    playWord(board, 7, 7, "HADOOPY", WordOrientation.HORIZONTAL, true);
    Move.Result generatedResult = board.scoreMove(gen.generateMove(rack, board, params));
    Move.Result expectedResult = playWord(board, 8, 8, "BOOPY", WordOrientation.HORIZONTAL, false);

    assertEquals(expectedResult, generatedResult);
  }

  public void testDoubleBlank() {
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    GadDag gaddag = new GadDag();

    gaddag.addWord("AT");
    gaddag.addWord("ZAZZ");

    GadDagWwfMoveGenerator gen = new GadDagWwfMoveGenerator(gaddag);
    Rack rack = buildRack("**Z");

    playWord(board, 7, 7, "AT", WordOrientation.HORIZONTAL, true);

    Move.Result generatedResult = board.move(gen.generateMove(rack, board, params));
    assertEquals("Should generate the word 'ZAZZ'",
      "ZAZZ",
      generatedResult.getMainWord());
  }

  public void testBlank() {
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    GadDag gaddag = new GadDag();

    gaddag.addWord("TOOT");
    gaddag.addWord("ZOO");
    gaddag.addWord("FOOT");

    GadDagWwfMoveGenerator gen = new GadDagWwfMoveGenerator(gaddag);
    Rack rack = buildRack("*FOT");

    playWord(board, 7, 7, "TOOT", WordOrientation.HORIZONTAL, true);

    Move.Result result = board.move(gen.generateMove(rack, board, params));

    assertEquals(1, result.getResultingWords().size());
    assertEquals("FOOT", result.getResultingWords().get(0));
  }

  public void testIt() {
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    GadDag gaddag = new GadDag();
    gaddag.addWord("BOARDING");
    gaddag.addWord("DOT");
    gaddag.addWord("TOD");
    GadDagWwfMoveGenerator gen = new GadDagWwfMoveGenerator(gaddag);
    Rack rack = buildRack("BORDING");

    playWord(board, 7, 7, "BATTY", WordOrientation.HORIZONTAL, true);
    Move move = gen.generateMove(rack, board, params);

    assertNotNull("it should generate a move", move);
  }


  /*
    R
    E
  B A T T L E
    L A
  */
  public void testMultiWord() {
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    GadDag gaddag = new GadDag();

    gaddag.addWord("BATTLE");
    gaddag.addWord("REAL");
    gaddag.addWord("TA");
    gaddag.addWord("LA");

    GadDagWwfMoveGenerator gen = new GadDagWwfMoveGenerator(gaddag);

    playWord(board, 7, 7, "BATTLE", WordOrientation.HORIZONTAL, true);
    playWord(board, 5, 8, "REL", WordOrientation.VERTICAL, true);

    Rack rack = buildRack("AAAAAAA");

    Move.Result result = board.move(gen.generateMove(rack, board, params));

    assertEquals("score should match", 5, result.getScore());
    assertEquals("words should match", 2, result.getResultingWords().size());
  }

  public void testBoundary() {
    WordsWithFriendsBoard board =
      parseCsvBoard("null, null, null, null, null, null, null, null, null, null, null, s, null, null, null, null, null, null, null, null, null, null, null, null, null, null, h, null, null, null, null, null, null, null, null, null, null, null, null, null, null, o, null, null, null, null, null, null, null, null, null, null, null, null, null, null, v, o, l, t, null, null, null, null, null, null, null, null, null, null, null, e, null, null, r, null, null, null, null, null, null, null, null, null, null, null, null, null, w, e, null, null, null, null, null, null, null, null, l, null, null, null, null, r, e, null, null, null, null, null, null, null, g, o, d, null, h, e, a, d, null, null, null, null, null, null, null, null, p, u, c, e, null, n, null, null, null, null, null, null, null, null, j, null, p, null, h, u, g, s, null, null, null, null, null, null, v, a, t, s, null, null, null, null, o, null, null, null, null, null, null, null, b, e, null, null, null, null, null, f, null, null, null, null, null, null, null, null, e, null, r, e, f, i, t, null, null, null, null, null, null, null, null, null, t, a, x, e, s, null, null, null, null, null, null, null, null, null, null, null, t, null, null, null, null");
    GadDag gaddag = new GadDag();

    gaddag.addWord("NO");
    gaddag.addWord("NNO");
    gaddag.addWord("ON");

    GadDagWwfMoveGenerator gen = new GadDagWwfMoveGenerator(gaddag);

    Rack rack = buildRack("LQIIINR");

    Move.Result result = board.move(gen.generateMove(rack, board, params));
  }

  public void testInitMove() {
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    GadDag gaddag = new GadDag();

    gaddag.addWord("QUEEN");
    gaddag.addWord("NEED");
    gaddag.addWord("DEEN");

    GadDagWwfMoveGenerator gen = new GadDagWwfMoveGenerator(gaddag);
    Rack rack = buildRack("QUEENF");

    Move move = gen.generateMove(rack, board, params);
    board.move(move);

    assertNotNull("should play on center square", board.getSlot(7,7).getTile());
  }

  public void testDianesBreakinIt() {
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    GadDag gaddag = new GadDag();

    gaddag.addWord("VOID");
    gaddag.addWord("TAV");
    gaddag.addWord("DO");
    gaddag.addWord("HI");
    gaddag.addWord("ED");

    GadDagWwfMoveGenerator gen = new GadDagWwfMoveGenerator(gaddag);

    playWord(board, 12, 8, "T", WordOrientation.HORIZONTAL, true);
    playWord(board, 13, 8, "ADHERE", WordOrientation.HORIZONTAL, true);

    Rack rack = buildRack("VOID");

    assertResultEquals(90, board.move(gen.generateMove(rack, board, params)));
  }

  protected static void assertBestMoveGenerated(GameState state, WordsWithFriendsMoveGenerator moveGen) {
    User player1 = state.getMeta().getUsersById().get(state.getMeta().getCreatedByUserId());
    User player2 = stateHelper.getOtherUser(player1, state);

    WordsWithFriendsBoard board = new WordsWithFriendsBoard();

    for (MoveData move : state.getAllMoves()) {
      Rack playerRack = stateHelper.buildRack(player1.getId(), state);
      Move generatedMove = moveGen.generateMove(playerRack, board);

      assertTrue("move made should be worth no more than the generated move",
        move.getPoints() <= generatedMove.getResult().getScore());

      Move actualMove = stateHelper.buildGameStateMove(move, board);
      board.move(actualMove);

      state = stateHelper.applyMove(state, actualMove);
      User tmp = player1;
      player1 = player2;
      player2 = tmp;
    }
  }
}
