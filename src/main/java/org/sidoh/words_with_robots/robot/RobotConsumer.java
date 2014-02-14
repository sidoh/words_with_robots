package org.sidoh.words_with_robots.robot;

import com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.sidoh.words_with_robots.move_generation.GameStateMoveGenerator;
import org.sidoh.words_with_robots.move_generation.WordsWithFriendsAllMovesGenerator;
import org.sidoh.words_with_robots.move_generation.context.WwfMoveGeneratorReturnContext;
import org.sidoh.words_with_robots.util.io.StatePrinter;
import org.sidoh.wwf_api.MoveValidationException;
import org.sidoh.wwf_api.StatefulApiProvider;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.ChatMessage;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Rack;
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

  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();
  private static final StatePrinter statePrinter = StatePrinter.getInstance();

  private final RobotSettings settings;
  private final RobotProducer producer;
  private final StatefulApiProvider apiProvider;

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
  protected GameStateMoveGenerator<? extends WwfMoveGeneratorReturnContext> getMoveGenerator() {
    return (GameStateMoveGenerator<? extends WwfMoveGeneratorReturnContext>) settings.get(RobotSettingKey.MOVE_GENERATOR);
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
        Thread.currentThread().setName(String.format("%s - %s vs. %s, gameid = %d",
          this.getClass().getSimpleName(),
          producer.getUser().getName(),
          stateHelper.getOtherUser(producer.getUser(), state).getName(),
          state.getMeta().getId()));

        serializeGameState(state);
        sendCourtesyChat(state);

        try {
          // Reconstruct the game state (rack and board)
          Rack rack = stateHelper.getCurrentPlayerRack(state);
          WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

          Move generatedMove = moveCache.containsKey(state.getId())
            ? moveCache.get(state.getId())
            : getMoveGenerator().generateMove(state).getMove();
          moveCache.put(state.getId(), generatedMove);

          // Submit the generated move
          try {
            List<String> wordsNotInDictionary = apiProvider.dictionaryLookup(generatedMove.getResult().getResultingWords());

            if (wordsNotInDictionary.isEmpty()) {
              GameState updatedState = apiProvider.makeMove(state, stateHelper.createMoveSubmissionFromPlay(generatedMove));
              moveCache.remove(state.getId());

              LOG.info("Game state after move:\n{}", statePrinter.getGameStateAsString(updatedState));
            } else {
              throw new RuntimeException("The following aren't considered words by WWF: " + wordsNotInDictionary);
            }
          }
          catch (MoveValidationException e) {
            LOG.error("Permanent error submitting move -- generated move was invalid", e);
          }

          // Log stats
//          Map<String, Object> stats = (Map<String, Object>) params.get(WwfMoveGeneratorParamKey.GAME_STATS);
//          for (Map.Entry<String, Object> entry : stats.entrySet()) {
//            LOG.info("Move generator stats: gameId {}, {}={}", state.getId(), entry.getKey(), entry.getValue());
//          }
        }
        finally {
          // Mark the game as processed
          producer.releaseGame(state);
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
}
