package org.sidoh.words_with_robots.robot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.sidoh.words_with_robots.data_structures.CountingHashMap;
import org.sidoh.wwf_api.ApiRequestException;
import org.sidoh.wwf_api.StatefulApiProvider;
import org.sidoh.wwf_api.game_state.GameScoreStatus;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.types.api.GameIndex;
import org.sidoh.wwf_api.types.api.GameMeta;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

/**
 * Producer -- produces active games for consumers
 *
 * Intermittently polls the API for new games and adds them to a queue for consumers to process.
 */
class RobotProducer implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(RobotProducer.class);
  private static final GameStateHelper stateHelper = GameStateHelper.getInstance();

  private final SynchronousQueue<GameState> queue;
  private final Set<Long> activeGames;
  private final Map<Long, GameState> gameStateHistory;
  private final Map<Long, GameScoreStatus> gameScoreHistory;
  private int lastUpdate;
  private final RobotSettings settings;
  private final StatefulApiProvider apiProvider;

  public RobotProducer(RobotSettings settings, StatefulApiProvider apiProvider) {
    this.settings = settings;
    this.apiProvider = apiProvider;
    this.queue = new SynchronousQueue<GameState>();
    this.activeGames = Collections.synchronizedSet(Sets.<Long>newHashSet());
    this.gameStateHistory = Maps.newHashMap();
    this.gameScoreHistory = Maps.newHashMap();
    this.lastUpdate = 0;
  }

  /**
   * Returns the number of games actively being processed
   *
   * @return
   */
  public synchronized int numActiveGames() {
    return activeGames.size();
  }

  /**
   * Waits until a game is available in the queue and takes it. Note that consumers should call
   * releaseGame(GameState) when finished so that another consumer can consume this game when
   * it's our turn again.
   *
   * @return an active GameState
   * @throws InterruptedException
   */
  public GameState takeGame() throws InterruptedException {
    GameState state = queue.take();
    activeGames.add(state.getId());

    return state;
  }

  /**
   * Releases the game from the set of states actively being processed. This allows future consumers
   * to also consume this state.
   *
   * @param state
   */
  public synchronized void releaseGame(GameState state) {
    activeGames.remove(state.getId());
  }

  /**
   *
   * @return
   */
  protected GameIndex getGameIndex() throws InterruptedException {
    try {
      GameIndex index;
      if ( lastUpdate == 0 ) {
        index = apiProvider.getGameIndex();
      }
      else {
        index = apiProvider.getGamesWithUpdates(lastUpdate);
        lastUpdate = (int)(System.currentTimeMillis() / 1000L);
      }
      return index;
    }
    catch (ApiRequestException e) {
      LOG.error("Error requesting game index {}", e);

      // Sleep to block the caller
      Thread.sleep(settings.getInteger(RobotSettingKey.GAME_INDEX_POLL_PERIOD));
      return null;
    }
  }

  @Override
  public void run() {
    while ( true ) {
      try {
        // Fetch the index and find games that have pending moves and that aren't already queued
        GameIndex index = getGameIndex();
        if ( index == null ) continue;

        int numActiveGames = 0;
        for (GameMeta gameMeta : index.getGames()) {
          long gameId = gameMeta.getId();

          if ( !gameMeta.isOver() ) {
            numActiveGames++;
          }

          // Only consider games where we can actually make a move
          boolean shouldSkip =
            gameMeta.isOver()
              || gameMeta.getCurrentMoveUserId() != index.getUser().getId()
              || gameMeta.getUsersByIdSize() == 1;

          if ( shouldSkip ) {
            // Cancel expired games
            if ( shouldCancelGame(gameMeta) ) {
              LOG.info("Cancelling game: {}", gameId);
              GameState state = apiProvider.getGameState(gameId);
              apiProvider.makeMove(state, stateHelper.createMoveSubmission(MoveType.GAME_OVER));
            }
            continue;
          }

          if ( !activeGames.contains(gameId) && activeGames.size() < settings.getInteger(RobotSettingKey.MAX_GAMES) ) {
            LOG.info("Added game to processing queue");

            GameState state = apiProvider.getGameState(gameId);
            gameStateHistory.put(state.getId(), state);
            gameScoreHistory.put(state.getId(), stateHelper.getScoreStatus(index.getUser(), state));
            queue.offer(state);
            activeGames.add(gameId);
          }
          else {
            LOG.debug("Skipped active game {}. Active games = {}.", gameId, activeGames);
          }
        }

        // Start new games if we have fewer active games than we'd like.
        for ( int i = 0; i < (settings.getInteger(RobotSettingKey.MAX_GAMES) - numActiveGames); i++) {
          apiProvider.createRandomGame();
        }

        // Log score stats
        CountingHashMap<GameScoreStatus> statusCounts = CountingHashMap.create();
        for (GameScoreStatus gameScoreStatus : gameScoreHistory.values()) {
          statusCounts.increment(gameScoreStatus);
        }

        LOG.debug("Active game status summary: winning {}, losing {}, tied {}.",
            statusCounts.getCount(GameScoreStatus.WINNING),
            statusCounts.getCount(GameScoreStatus.LOSING),
            statusCounts.getCount(GameScoreStatus.TIED));

        Thread.sleep(settings.getLong(RobotSettingKey.GAME_INDEX_POLL_PERIOD));
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Determine whether or not a game should be cancelled (i.e., we should end it preemptively
   * because the opponent is unresponsive.)
   *
   * @param gameMeta
   * @return
   */
  public boolean shouldCancelGame(GameMeta gameMeta) {
    // Don't try to cancel inactive games.
    if ( gameMeta.isOver() ) {
      return false;
    }
    // If there's been a move, use that as the reference point. Otherwise, use game creation time.
    String lastActionTimestamp = gameMeta.isSetLastMove()
      ? gameMeta.getLastMove().getCreatedAt()
      : gameMeta.getCreatedAt();
    int secondSinceLastAction = stateHelper.getNumSecondsEllapsedSinceTimestamp(lastActionTimestamp);

    return secondSinceLastAction > settings.getInteger(RobotSettingKey.INACTIVE_GAME_TTL);
  }
}
