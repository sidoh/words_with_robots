package org.sidoh.words_with_robots;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TJSONProtocol;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.TileBuilder;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Letter;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Tile;
import org.sidoh.wwf_api.types.game_state.WordOrientation;
import org.sidoh.wwf_api.util.ThriftSerializationHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WordsWithRobotsTestCase extends TestCase {
  private static int tileId = 0;

  protected static final GameStateHelper stateHelper = GameStateHelper.getInstance();

  public static void assertResultEquals(int score, List<String> words, Move.Result result) {
    assertEquals("score should match", score, result.getScore());
    assertEquals("words should match", words, result.getResultingWords());
  }

  public static void assertResultEquals(int score, Move.Result result) {
    assertEquals("score should match", score, result.getScore());
  }

  public static GadDag buildGadDag(String... words) {
    GadDag gaddag = new GadDag();

    for (String word : words) {
      gaddag.addWord(word);
    }

    return gaddag;
  }

  public static Move.Result playInitialWord(WordsWithFriendsBoard board, String word, WordOrientation orientation) {
    return playWord(board, 7, 7, word, orientation, true);
  }

  public static Move.Result playWord(WordsWithFriendsBoard board, int row, int col, String word, WordOrientation orientation, boolean keep) {
    List<Tile> letters = new ArrayList<Tile>();

    for (int i = 0; i < word.length(); i++) {
      Character value = word.charAt(i);

      letters.add(new Tile()
              .setId(i)
              .setLetter(new Letter().setValue(String.valueOf(value)))
              .setValue(WordsWithFriendsBoard.TILE_VALUES.get(value)));
    }

    Move.Result result;

    if (keep) {
      result = board.move(Move.play(letters, row, col, orientation));
    }
    else {
      result = board.scoreMove(Move.play(letters, row, col, orientation));
    }

    return result;
  }

  public static List<Tile> getWordTilesFromRack(Rack rack, String word) {
    List<Tile> tiles = Lists.newArrayList();

    for ( int i = 0; i < word.length(); i++ ) {
      String letter = word.substring(i, i+1);
      tiles.add(removeLetterFromRack(rack, letter));
    }

    return tiles;
  }

  public static Tile removeLetterFromRack(Rack rack, String letter) {
    Tile toRemove = null;

    for (Tile tile : rack.getTiles()) {
      if ( tile.getLetter().getValue().equals(letter) ) {
        toRemove = tile;
        break;
      }
    }

    if ( toRemove == null ) {
      throw new RuntimeException("Requested tile: " + letter + " not found in tiles: " + rack.getTiles());
    }

    rack.getTiles().remove(toRemove);
    return toRemove;
  }

  /**
   * Reads a game state from resources and applies it to a game state.
   *
   * @param filename
   * @return
   * @throws IOException
   * @throws TException
   */
  public static GameState loadGameState(String filename) throws IOException, TException {
    filename = String.format("src/resources/game_states/%s", filename);

    if ( filename.endsWith(".json") ) {
      return loadJsonGameState(filename);
    }
    else if ( filename.endsWith(".bin") ) {
      return loadBinaryGameState(filename);
    }
    else {
      throw new RuntimeException("Unknown file extension in: " + filename);
    }
  }

  /**
   * Load JSON game state
   *
   * @param filename
   * @return
   * @throws IOException
   * @throws TException
   */
  public static GameState loadJsonGameState(String filename) throws IOException, TException {
    FileReader stream = new FileReader(filename);
    TDeserializer deserializer = new TDeserializer(new TJSONProtocol.Factory());
    GameState state = new GameState();

    BufferedReader reader = new BufferedReader(stream);
    String line = reader.readLine();
    String full = "";

    while (line != null) {
      full += line;
      line = reader.readLine();
    }

    deserializer.deserialize(state, full, "UTF-8");

    return state;
  }

  /**
   * Load a binary game state
   *
   * @param filename
   * @return
   * @throws IOException
   * @throws TException
   */
  public static GameState loadBinaryGameState(String filename) throws IOException, TException {
    return ThriftSerializationHelper.getInstance().deserialize(new File(filename), new GameState());
  }

  public static Rack buildRack(String letters) {
    Rack rack = new Rack().setCapacity(7);

    for (int i = 0; i < letters.length(); i++) {
      rack.addToTiles(TileBuilder.getTile(letters.substring(i, i+1)));
    }

    return rack;
  }

  public static WordsWithFriendsBoard parseCsvBoard(String csv) {
    WordsWithFriendsBoard board = new WordsWithFriendsBoard();
    String[] letters = csv.split(",\\s*");

    for (int i = 0; i < letters.length; i++) {
      if ("null".equals(letters[i])) continue;

      board.getSlot(i).setTile( TileBuilder.getTile(letters[i]) );
    }

    return board;
  }
}
