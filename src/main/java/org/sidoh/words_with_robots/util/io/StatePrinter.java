package org.sidoh.words_with_robots.util.io;

import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.User;
import org.sidoh.wwf_api.types.game_state.Tile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class StatePrinter {
  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();
  private static StatePrinter instance;

  private StatePrinter() { }

  public static StatePrinter getInstance() {
    if ( instance == null ) {
      instance = new StatePrinter();
    }
    return instance;
  }

  /**
   * Formats game state as a string. Since GameState is a thrift type, we can't modify the
   * toString.
   *
   * @param state
   * @return
   */
  public String getGameStateAsString(GameState state) {
    StringWriter out = new StringWriter();
    writeGameState(state, out);
    return out.toString();
  }

  /**
   * Write a pretty summary of game state
   *
   * @param state
   * @param out
   */
  public void writeGameState(GameState state, Writer out) {
    PrintWriter wrappedOut = new PrintWriter(out);

    writeScores(state, wrappedOut);
    wrappedOut.println();
    wrappedOut.println(stateHelper.createBoardFromState(state));
    wrappedOut.println();
    writeRacks(state, wrappedOut);
    wrappedOut.printf("Remaining tiles: %s\n", rackToString(state.getRemainingTiles()));
  }

  /**
   * Pretty format scores.
   *
   * @param state
   * @param out
   */
  protected void writeScores(GameState state, PrintWriter out) {
    out.printf("%20s\n", "SCORES");

    User user1 = state.getMeta().getUsersById().get(state.getMeta().getCreatedByUserId());
    User user2 = stateHelper.getOtherUser(user1, state);
    Long id1 = user1.getId();
    Long id2 = user2 == null ? null : user2.getId();

    out.printf("%-20s %-20s\n", user1.getName(), user2 == null ? "(waiting for opponent)" : user2.getName());
    out.printf("%-20d %-20d\n", state.getScores().get(id1), state.getScores().get(id2));
  }

  /**
   * Pretty print racks
   *
   * @param state
   */
  protected void writeRacks(GameState state, PrintWriter out) {
    User user1 = state.getMeta().getUsersById().get(state.getMeta().getCreatedByUserId());
    User user2 = stateHelper.getOtherUser(user1, state);

    out.printf("%20s's rack: %s\n", user1.getName(), rackToString(state.getRacks().get(user1.getId())));

    if (user2 != null) {
      out.printf("%20s's rack: %s\n", user2.getName(), rackToString(state.getRacks().get(user2.getId())));
    }
  }

  /**
   * Pretty print a list of tiles
   *
   * @param tiles
   * @return
   */
  protected String rackToString(List<Tile> tiles) {
    StringBuilder b = new StringBuilder();

    for (Tile tile : tiles) {
      b.append(tile.getLetter().getValue()).append(", ");
    }

    if (b.length() > 0) {
      b.setLength(b.length() - 2);
    }

    return b.toString();
  }
}
