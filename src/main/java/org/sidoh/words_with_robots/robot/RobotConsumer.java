package org.sidoh.words_with_robots.robot;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.sidoh.words_with_robots.move_generation.WordsWithFriendsMoveGenerator;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParamKey;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.PreemptionContext;
import org.sidoh.words_with_robots.move_generation.params.WwfMoveGeneratorParamKey;
import org.sidoh.wwf_api.StatefulApiProvider;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.ChatMessage;
import org.sidoh.wwf_api.types.api.GameState;
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
 * Consumes active games from the queue, generating moves for the games, and submitting
 * them via the API.
 */
class RobotConsumer implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(RobotConsumer.class);

  /**
   * Cache generated moves in case we can't submit them due to transient network errors.
   * Wouldn't wanna waste all that time re-generating them :)
   */
  private static final Map<Long, Move> moveCache = Maps.newHashMap();

  private final RobotSettings settings;
  private final RobotProducer producer;
  private final StatefulApiProvider apiProvider;
  private PreemptionContext preemptContext;

  /**
   *
   * @param producer a producer that will give us game states
   */
  public RobotConsumer(RobotSettings settings, RobotProducer producer, StatefulApiProvider apiProvider) {
    this.settings = settings;
    this.producer = producer;
    this.apiProvider = apiProvider;
  }

  /**
   * Convenience method for getting the move generator out of the settings map
   *
   * @return
   */
  protected WordsWithFriendsMoveGenerator getMoveGenerator() {
    return (WordsWithFriendsMoveGenerator) settings.get(RobotSettingKey.MOVE_GENERATOR);
  }

  /**
   * Helper that serializes a game state if it's enabled
   *
   * @param state
   * @throws IOException
   * @throws TException
   */
  protected void serializeGameState(GameState state) throws IOException, TException {
    if ( settings.getBoolean(RobotSettingKey.SAVE_GAME_STATES) ) {
      String logDir = settings.getString(RobotSettingKey.LOG_DIRECTORY);
      File stateFile = new File(logDir, String.format("game_states/%d.bin", state.getId()));
      ThriftSerializationHelper.getInstance().serialize(state, stateFile);
    }
  }

  /**
   * If enabled, send a courtesy chat message to our opponent informing them that they're playing
   * against a bot.
   *
   * @param state
   */
  protected void sendCourtesyChat(GameState state) {
    if ( ! settings.getBoolean(RobotSettingKey.SEND_COURTESY_MESSAGES)) {
      return;
    }

    String courtesyMessage = settings.getString(RobotSettingKey.COURTESY_MESSAGE_STRING);

    // Only send courtesy chats if we haven't already sent them
    for (ChatMessage chat : state.getChatMessages()) {
      if ( chat.getMessage().equals(courtesyMessage)) {
        return;
      }
    }

    // Only send courtesy chats to new games
    if ( state.getAllMoves().size() > 1) {
      return;
    }

    LOG.info("Submitting courtesy message to game {}", state.getId());
    apiProvider.submitChatMessage(state.getId(), courtesyMessage);
  }

  @Override
  public void run() {
    while ( true ) {
      try {
        // Wait for a state to be available in the queue
        GameState state = producer.takeGame();
        serializeGameState(state);
        sendCourtesyChat(state);

        try {
          // Reconstruct the game state (rack and board)
          Rack rack = Robot.stateHelper.getCurrentPlayerRack(state);
          WordsWithFriendsBoard board = Robot.stateHelper.createBoardFromState(state);

          // Generate a move
          MoveGeneratorParams params = buildParams(state);
          Move generatedMove = moveCache.containsKey(state.getId())
            ? moveCache.get(state.getId())
            : getMoveGenerator().generateMove(rack, board, params);
          moveCache.put(state.getId(), generatedMove);

          // Submit the generated move
          apiProvider.makeMove(state, Robot.stateHelper.createMoveSubmissionFromPlay(generatedMove));
          moveCache.remove(state.getId());

          // Log stats
          Map<String, Object> stats = (Map<String, Object>) params.get(WwfMoveGeneratorParamKey.GAME_STATS);
          for (Map.Entry<String, Object> entry : stats.entrySet()) {
            LOG.info("Move generator stats: gameId {}, {}={}", state.getId(), entry.getKey(), entry.getValue());
          }
        }
        finally {
          // Mark the game as processed
          producer.releaseGame(state);
          synchronized ( this ) {
            preemptContext = null;
          }
        }
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      catch (Exception e) {
        LOG.error("Exception while consuming game state", e);
      }
    }
  }

  /**
   *
   * @return true if this consumer is currently processing a game
   */
  public synchronized boolean isOccupied() {
    return preemptContext != null;
  }

  /**
   *
   */
  public synchronized void weakPreempt() {
    if ( preemptContext == null ) {
      throw new IllegalStateException("Can't preempt -- not processing");
    }
    preemptContext.weakPreempt();
  }

  /**
   * Causes current
   */
  public synchronized void strongPreempt() {
    if ( preemptContext == null ) {
      throw new IllegalStateException("Can't preempt -- not processing");
    }
    preemptContext.strongPreempt();
  }

  /**
   * Build MoveGeneratorParams for use with a particular game state
   *
   * @param state
   * @return
   */
  public MoveGeneratorParams buildParams(GameState state) {
    synchronized (this) {
      preemptContext = new PreemptionContext();
    }

    return new MoveGeneratorParams()
      .set(WwfMoveGeneratorParamKey.GAME_STATS, Maps.newHashMap())
      .set(WwfMoveGeneratorParamKey.GAME_STATE, state)
      .set(FixedDepthParamKey.PREEMPTION_CONTEXT, preemptContext);
  }
}
