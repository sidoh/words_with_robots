package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.sidoh.words_with_robots.WordsWithRobotsTestCase;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.context.NonBlockingReturnContext;
import org.sidoh.words_with_robots.move_generation.context.WwfMoveGeneratorReturnContext;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.WordOrientation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestNonBlockingMoveGenerator extends WordsWithRobotsTestCase {
  @Test
  public void testExitsWhenAnswerReturned() {
    GadDag dictionary = new GadDag();
    dictionary.addWord("DOGMATIC");
    dictionary.addWord("DOG");

    AllMovesGenerator<WordsWithFriendsBoard> allMovesGenerator = new GadDagWwfMoveGenerator(dictionary);
    TopScoringMoveGenerator generator = new TopScoringMoveGenerator(allMovesGenerator, new ScoreEvalFunction());
    MoveGenerator<WordsWithFriendsBoard, NonBlockingReturnContext<WwfMoveGeneratorReturnContext>> nonBlockingGenerator
      = MoveGenerators.asNonBlockingGenerator((MoveGenerator<WordsWithFriendsBoard,WwfMoveGeneratorReturnContext>) generator);

    Rack playerRack = buildRack("MATICAB");
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    playWord(board, 7, 7, "DOG", WordOrientation.VERTICAL, true);

    NonBlockingReturnContext<WwfMoveGeneratorReturnContext> context = nonBlockingGenerator.generateMove(playerRack, board);
    try {
      Future<WwfMoveGeneratorReturnContext> future = context.getFuture();
      assertResultEquals(34, Lists.newArrayList("DOGMATIC"), future.get().getMove().getResult());
      assertTrue("Future should report that it's finished",
        future.isDone());
      assertFalse("generation thread should be shut down",
        getAllActiveThreadNames().contains(MoveGenerators.NON_BLOCKING_GEN_THREAD_NAME));
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
