package org.sidoh.words_with_robots;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.sidoh.words_with_robots.data_structures.CountingHashMap;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.GadDagWwfMoveGenerator;
import org.sidoh.words_with_robots.util.dictionary.DictionaryHelper;
import org.sidoh.words_with_robots.util.io.StdinPrompts;
import org.sidoh.wwf_api.Bag;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveData;
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.api.User;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Tile;
import org.sidoh.wwf_api.util.ThriftSerializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating statistics from a directory containing serialized game states
 */
public class StatsCollector {
  private static final Logger LOG = LoggerFactory.getLogger(StatsCollector.class);
  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();
  private GadDagWwfMoveGenerator moveGenerator;

  public StatsCollector() throws IOException {
    GadDag dictionary = new GadDag();
    dictionary.loadDictionary(DictionaryHelper.getDictionaryResource());
    this.moveGenerator = new GadDagWwfMoveGenerator(dictionary);
  }

  public static class StatsEntry {
    private int moveRank;
    private int myScore;
    private int opponentScore;

    public StatsEntry setMoveRank(int moveRank) {
      this.moveRank = moveRank;
      return this;
    }

    public StatsEntry setMyScore(int myScore) {
      this.myScore = myScore;
      return this;
    }

    public StatsEntry setOpponentScore(int opponentScore) {
      this.opponentScore = opponentScore;
      return this;
    }

    public int getMoveRank() {
      return moveRank;
    }

    public int getMyScore() {
      return myScore;
    }

    public int getOpponentScore() {
      return opponentScore;
    }

    @Override
    public String toString() {
      return "StatsEntry{" +
        "moveRank=" + moveRank +
        ", myScore=" + myScore +
        ", opponentScore=" + opponentScore +
        '}';
    }
  }

  public List<List<StatsEntry>> processGameStateDirectory(File gameStateDir, String username) throws IOException, TException {
    List<List<StatsEntry>> results = Lists.newArrayList();

    for (File file : gameStateDir.listFiles()) {
      if ( file.getName().endsWith(".bin")) {
        LOG.info("Processing {}", file);
        results.add(processGameState(file, username));
      }
    }

    return results;
  }

  public List<StatsEntry> processGameState(File gameState, String username) throws IOException, TException {
    GameState state = new GameState();
    ThriftSerializationHelper.getInstance().deserialize(gameState, state);

    User user = stateHelper.getUserFromUsername(state, username);

    if ( user == null ) {
      throw new RuntimeException("Provided username not found in game state");
    }

    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    Bag bag = new Bag(state.getMeta().getRandomSeed());
    User currentUser = state.getMeta().getUsersById().get(state.getMeta().getCreatedByUserId());

    Map<Long, List<StatsEntry>> stats = Maps.newHashMap();
    Map<Long, List<Tile>> racks = Maps.newHashMap();
    CountingHashMap<Long> scores = new CountingHashMap<Long>();

    stats.put(currentUser.getId(), Lists.<StatsEntry>newArrayList());
    stats.put(stateHelper.getOtherUser(currentUser, state).getId(), Lists.<StatsEntry>newArrayList());
    racks.put(currentUser.getId(), bag.pullTiles(WordsWithFriendsBoard.TILES_PER_PLAYER));
    racks.put(stateHelper.getOtherUser(currentUser, state).getId(), bag.pullTiles(WordsWithFriendsBoard.TILES_PER_PLAYER));

    for (MoveData moveData : state.getAllMoves()) {
      if ( moveData.getMoveType() == MoveType.PLAY && moveData.getTiles().size() != 0 ) {
        Move move = stateHelper.buildGameStateMove(moveData, board);
        board.scoreMove(move);

        List<Tile> rack = racks.get(currentUser.getId());
        Iterable<Move> allMoves = moveGenerator.generateAllPossibleMoves(new Rack().setTiles(rack), board);

        int moveRank =  moveGenerator.getMoveScoreRank(allMoves, move);

        scores.increment(currentUser.getId(), move.getResult().getScore());
        if (currentUser.getId() == user.getId()) {
          stats.get(currentUser.getId()).add(
            new StatsEntry()
              .setMoveRank(moveRank)
              .setMyScore(scores.get(currentUser.getId()).getValue())
              .setOpponentScore(scores.get(stateHelper.getOtherUser(currentUser, state).getId()).getValue()));
        }

        board.move(move);
      }
      if ( moveData.getMoveType() == MoveType.PLAY || moveData.getMoveType() == MoveType.SWAP ) {
        List<Tile> rack = racks.get(currentUser.getId());
        rack.removeAll(moveData.getTiles());
        rack.addAll(bag.pullTiles(Math.min(bag.getRemainingTiles().size(), moveData.getTiles().size())));
      }
      if ( moveData.getMoveType() == MoveType.SWAP ) {
        bag.returnTiles(moveData.getTiles());
      }

      currentUser = stateHelper.getOtherUser(currentUser, state);
    }

    return stats.get(user.getId());
  }

  public static void main(String[] args) throws IOException, TException {
    File directory = StdinPrompts.promptForFile("Enter directory", true, false);
    String username = StdinPrompts.promptForLine("Enter username");

    StatsCollector collector = new StatsCollector();
    List<List<StatsEntry>> results = collector.processGameStateDirectory(directory, username);
    Joiner joiner = Joiner.on(',');

    for (List<StatsEntry> result : results) {
      List<Integer> ranks = Lists.newArrayList();
      List<Integer> scores = Lists.newArrayList();
      List<Integer> opScores = Lists.newArrayList();

      for (StatsEntry statsEntry : result) {
        ranks.add(statsEntry.getMoveRank());
        scores.add(statsEntry.getMyScore());
        opScores.add(statsEntry.getOpponentScore());
      }

      System.out.printf("ranks,%s\n", joiner.join(ranks));
      System.out.printf("scores,%s\n", joiner.join(scores));
      System.out.printf("opScores,%s\n", joiner.join(opScores));
    }
  }
}
