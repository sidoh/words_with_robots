package org.sidoh.words_with_robots;

import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TJSONProtocol;
import org.sidoh.words_with_robots.util.io.StdinPrompts;
import org.sidoh.words_with_robots.data_structures.CollectionsHelper;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.GadDagWwfMoveGenerator;
import org.sidoh.words_with_robots.move_generation.MoveGenerator;
import org.sidoh.words_with_robots.move_generation.WordsWithFriendsMoveGenerator;
import org.sidoh.words_with_robots.move_generation.WwfMinimaxLocal;
import org.sidoh.words_with_robots.move_generation.eval.NewTilesEvalFunction;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.words_with_robots.move_generation.eval.SummingEvalFunction;
import org.sidoh.wwf_api.ApiProvider;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.ChatMessage;
import org.sidoh.wwf_api.types.api.GameIndex;
import org.sidoh.wwf_api.types.api.GameMeta;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveData;
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.api.User;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Tile;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WwfConsole {
  static String authToken;
  static final GadDag gaddag = new GadDag();
  static final WordsWithFriendsMoveGenerator allMovesGen
    = new GadDagWwfMoveGenerator(gaddag);
  static WordsWithFriendsMoveGenerator moveGenerator
    = new WwfMinimaxLocal(allMovesGen);
  static final ApiProvider api = new ApiProvider();
  static final GameStateHelper stateHelper = new GameStateHelper();

  public static void handleGameSelection(User player, GameMeta meta) {
    while (true) {
      GameState state = api.getGameState(authToken, meta.getId());
      WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

      printScores(state);
      System.out.println();

      System.out.println(board);
      System.out.println();

      printRacks(state);

      System.out.println();
      System.out.printf("Remaining tiles: %s\n", rackToString(state.getRemainingTiles()));

      String[] commands = { "play", "pass", "resign", "back", "chat", "sendEndGame", "printGameState", "replay" };
      String command = StdinPrompts.promptForLine("enter command (" + CollectionsHelper.join(commands) + ")");

      if ("back".equals(command)) {
        return;
      }
      else if ("play".equals(command)) {
        Rack rack = stateHelper.buildRack(state.getMeta().getCurrentMoveUserId(), state);
        Move bestMove = null;
        MoveGenerator.Params params = new WwfMinimaxLocal.Params(state)
          .setEvalFunction(new SummingEvalFunction(
            new ScoreEvalFunction(),
            new NewTilesEvalFunction()
          ));

        bestMove = moveGenerator.generateMove(rack, board, params);

        if (bestMove == null) {
          System.out.println("No moves possible!");
          continue;
        }

        board.move(bestMove);

        System.out.println();
        System.out.println("Best move found: " + bestMove.getResult().getMainWord() + " (" + bestMove.getResult().getScore() + " points)");
        System.out.println("Board after:");
        System.out.println(board);

        if (player.getId() == state.getMeta().getCurrentMoveUserId()) {
          List<String> dictionaryResponse = api.dictionaryLookup(authToken, bestMove.getResult().getResultingWords());

          if ( dictionaryResponse.size() != 0 ) {
            throw new RuntimeException("generated a move with words that aren't in the WWF dictionary. failed words are: " + dictionaryResponse);
          }

          String response = StdinPrompts.promptForLine("accept move? (yes/no)");
          if ("yes".equals(response)) {
            api.makeMove(authToken, state, stateHelper.createMoveSubmissionFromPlay(bestMove));
          }
        }
        else {
          System.out.println("can't submit move because it's not your turn!");
        }
      }
      else if ("pass".equals(command)) {
        if (player.getId() == state.getMeta().getCurrentMoveUserId()) {
          api.makeMove(authToken, state, stateHelper.createMoveSubmission(MoveType.PASS));
        }
        else {
          System.out.println("can't pass because it's not your turn.");
        }
      }
      // You can still resign / send a GAME_OVER move even if it's not your turn. It'll result in the game ending,
      // and unless it is your turn, the client will suggest your opponent beat you, even if your score is higher.
      else if ("resign".equals(command)) {
        api.makeMove(authToken, state, stateHelper.createMoveSubmission(MoveType.RESIGN));
      }
      else if ("sendEndGame".equals(command)) {
        api.makeMove(authToken, state, stateHelper.createMoveSubmission(MoveType.GAME_OVER));
      }
      else if ("chat".equals(command)) {
        printChats(state);

        System.out.println();
        String chat = StdinPrompts.promptForLine("enter new chat message (blank to cancel)");

        if (! chat.isEmpty()) {
          api.submitChatMessage(authToken, state.getId(), chat);
        }

        if (state.getMeta().getUnreadChatIdsSize() > 0) {
          api.getUnreadChats(authToken, state.getId());
        }
      }
      else if ("printGameState".equals(command)) {
        printGameState(state);
      }
      else if ("replay".equals(command)) {
        WordsWithFriendsBoard replayBoard = new WordsWithFriendsBoard();

        for (MoveData move : state.getAllMoves()) {
          if ( move.getMoveType() == MoveType.PLAY ) {
            replayBoard.move(stateHelper.buildGameStateMove(move));
            System.out.println(replayBoard);

            StdinPrompts.promptForLine("press enter to continue.");
          }
        }
      }
    }
  }

  public static void handleListCurrentGames() {
    while (true) {
      GameIndex gameIndex = api.getGameIndex(authToken);
      User player = gameIndex.getUser();
      int i = 0;

      for (GameMeta gameMeta : gameIndex.getGames()) {
        if (! gameMeta.isOver()) {
          printGameMetaInfo(i, player, gameMeta);
        }

        i++;
      }

      int selectedGameIndex = StdinPrompts.promptForInt("Enter game number to select (-1 to cancel)");
      if (selectedGameIndex == -1) return;

      GameMeta selectedGame = gameIndex.getGames().get(selectedGameIndex);

      handleGameSelection(player, selectedGame);
    }
  }

  public static void handleCommand() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
    String[] validCommands = { "index", "listUsers", "createRandom", "create", "exit", "setMoveGenAlgo" };
    String command = StdinPrompts.promptForLine("Enter command (" + CollectionsHelper.join(validCommands) + ")");

    if ("index".equals(command)) {
      handleListCurrentGames();
    }
    else if ("exit".equals(command)) {
      System.exit(0);
    }
    else if ("listUsers".equals(command)) {
      GameIndex index = api.getGameIndex(authToken);

      Set<User> allUsers = new HashSet<User>();
      for (GameMeta meta : index.getGames()) {
        for (User user : meta.getUsersById().values()) {
          allUsers.add(user);
        }
      }

      for (User user : allUsers) {
        System.out.printf("%20s %d\n", user.getName(), user.getId());
      }
    }
    else if ("createRandom".equals(command)) {
      api.createRandomGame(authToken);
    }
    else if ("create".equals(command)) {
      long zyngaId = StdinPrompts.promptForLong("enter zynga user id");

      api.createZyngaGame(authToken, zyngaId);
    }
    else if ("setMoveGenAlgo".equals(command)) {
      String algorithm = StdinPrompts.promptForLine("Enter class name");
      String algoClass = String.format("org.sidoh.words_with_robots.move_generation.%s", algorithm);
      Constructor constructor = Class.forName(algoClass).getConstructor(WordsWithFriendsMoveGenerator.class);
      moveGenerator = (WordsWithFriendsMoveGenerator) constructor.newInstance(allMovesGen);
    }
    else {
      System.out.println("invalid command.");
    }
  }

  public static void printChats(GameState state) {
    if (! state.isSetChatMessages()) {
      System.out.println("no chat messages for this game");
      return;
    }

    for (ChatMessage chatMessage : state.getChatMessages()) {
      System.out.printf("[%s] <%s> %s\n",
        chatMessage.getCreatedAt(),
        state.getMeta().getUsersById().get(chatMessage.getUserId()).getName(),
        chatMessage.getMessage());
    }
  }

  public static void printScores(GameState state) {
    System.out.printf("%20s\n", "SCORES");

    User user1 = state.getMeta().getUsersById().get(state.getMeta().getCreatedByUserId());
    User user2 = stateHelper.getOtherUser(user1, state);
    Long id1 = user1.getId();
    Long id2 = user2 == null ? null : user2.getId();

    System.out.printf("%-20s %-20s\n", user1.getName(), user2 == null ? "(waiting for opponent)" : user2.getName());
    System.out.printf("%-20d %-20d\n", state.getScores().get(id1), state.getScores().get(id2));
  }

  public static void printRacks(GameState state) {
    User user1 = state.getMeta().getUsersById().get(state.getMeta().getCreatedByUserId());
    User user2 = stateHelper.getOtherUser(user1, state);

    System.out.printf("%20s's rack: %s\n", user1.getName(), rackToString(state.getRacks().get(user1.getId())));

    if (user2 != null) {
      System.out.printf("%20s's rack: %s\n", user2.getName(), rackToString(state.getRacks().get(user2.getId())));
    }
  }

  public static String rackToString(List<Tile> tiles) {
    StringBuilder b = new StringBuilder();

    for (Tile tile : tiles) {
      b.append(tile.getLetter().getValue()).append(", ");//.append("[").append(tile.getValue()).append("], ");
    }

    if (b.length() > 0) {
      b.setLength(b.length() - 2);
    }

    return b.toString();
  }

  public static void printGameMetaInfo(int num, User player, GameMeta meta) {
    boolean hasChats = meta.getUnreadChatIdsSize() > 0;
    boolean isYourMove = meta.getCurrentMoveUserId() == player.getId();

    User user1 = meta.getUsersById().get(meta.getCreatedByUserId());
    User user2 = stateHelper.getOtherUser(user1, meta);

    System.out.printf("%2d. %20s vs. %-20s %15d %2s %s\n",
      num,
      user1.getName(),
      user2 == null ? " (waiting for opponent)" : user2.getName(),
      meta.getId(),
      hasChats ? "*" : "",
      isYourMove ? "!" : "");
  }

  private static void printGameState(GameState state) {
    TSerializer serializer = new TSerializer(new TJSONProtocol.Factory());
    try {
      System.out.println(serializer.toString(state));
    } catch (TException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws IOException {
    authToken = args[0];
    final Reader dictionary = new FileReader(args[1]);

    gaddag.loadDictionary(dictionary);

    while (true) {
      try {
        handleCommand();
      }
      catch (Exception e) {
        System.out.println();
        System.out.println();
        e.printStackTrace();
        System.out.println();
        System.out.println();
      }
    }
  }
}
