package org.sidoh.words_with_robots;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.sidoh.words_with_robots.data_structures.CountingHashMap;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.GadDagWwfMoveGenerator;
import org.sidoh.words_with_robots.move_generation.IterativeDeepeningMoveGenerator;
import org.sidoh.words_with_robots.move_generation.WordsWithFriendsMoveGenerator;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParamKey;
import org.sidoh.words_with_robots.move_generation.params.KillSignalBeacon;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.PreemptionContext;
import org.sidoh.words_with_robots.move_generation.params.WwfMoveGeneratorParamKey;
import org.sidoh.words_with_robots.util.dictionary.DictionaryHelper;
import org.sidoh.wwf_api.AccessTokenRetriever;
import org.sidoh.wwf_api.StatefulApiProvider;
import org.sidoh.wwf_api.game_state.GameScoreStatus;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.game_state.WordsWithFriendsBoard;
import org.sidoh.wwf_api.types.api.GameIndex;
import org.sidoh.wwf_api.types.api.GameMeta;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.BoardStorage;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * Words with friends - now with no friends and only robots!
 *
 * Implements a simple producer/consumer model with a thread polling for active games and a thread
 * pool of workers consuming active games and generating moves.
 */
public class Robot {
  private static final Logger LOG = LoggerFactory.getLogger(Robot.class);

  /**
   * Defines the default number of maximum active games to keep running. If there are fewer games
   * than this, the robot will try to create more.
   */
  public final static int DEFUALT_MAX_GAMES = 10;

  /**
   * The maximum number of consumer threads.
   */
  public final static int DEFAULT_MAX_THREADS = 4;

  /**
   * The number of milliseconds to wait inbetween polling for active games
   */
  public final static long DEFAULT_POLL_PERIOD = 10000L;

  protected final static GameStateHelper stateHelper = new GameStateHelper();

  private int maxGames;
  private GadDag dictionary;
  private WordsWithFriendsMoveGenerator moveGenerator;
  private int maxThreads;
  private ExecutorService threadPool;
  private long pollPeriod;
  private final StatefulApiProvider apiProvider;
  private KillSignalBeacon killBeacon;

  /**
   *
   * @param apiProvider - an API provider initialized with an access token
   */
  public Robot(StatefulApiProvider apiProvider) {
    this.apiProvider = apiProvider;
    this.maxGames = DEFUALT_MAX_GAMES;
    this.dictionary = new GadDag();
    this.moveGenerator = null;
    this.maxThreads = DEFAULT_MAX_THREADS;
    this.threadPool = Executors.newFixedThreadPool(maxThreads);
    this.pollPeriod = DEFAULT_POLL_PERIOD;
    this.killBeacon = new KillSignalBeacon();
    getMoveGenerator();
  }

  /**
   * Returns the move generator being used by the robot. If one hasn't been specified, it defaults to
   * using an iterative deepening move generator with the default parameters.
   *
   * @return the move generator being used by this robot
   */
  public synchronized WordsWithFriendsMoveGenerator getMoveGenerator() {
    if ( this.moveGenerator == null ) {
      try {
        dictionary.loadDictionary(DictionaryHelper.getDictionaryResource());
        moveGenerator = new IterativeDeepeningMoveGenerator(new GadDagWwfMoveGenerator(dictionary));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return moveGenerator;
  }

  /**
   * Update the move generator used by the robot
   *
   * @param moveGenerator
   * @return
   */
  public Robot setMoveGenerator(WordsWithFriendsMoveGenerator moveGenerator) {
    this.moveGenerator = moveGenerator;
    return this;
  }

  /**
   * Update the maximum number of consumer threads. Note that this has no effect if called after
   * start()
   *
   * @param maxThreads
   * @return
   */
  public Robot setMaxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
    this.threadPool = Executors.newFixedThreadPool(maxThreads);
    return this;
  }

  /**
   * Update the polling period -- the number of milliseconds to wait inbetween polling for active
   * games.
   *
   * @param pollPeriod
   * @return
   */
  public Robot setPollPeriod(long pollPeriod) {
    this.pollPeriod = pollPeriod;
    return this;
  }

  /**
   * Start the robot
   */
  public void start() {
    RobotProducer producer = new RobotProducer();
    for ( int i = 0; i < maxThreads; i++ ) {
      threadPool.submit(new RobotConsumer(producer));
    }

    Thread producerThread = new Thread(producer);
    producerThread.start();
  }

  /**
   * Consumes active games from the queue, generating moves for the games, and submitting
   * them via the API.
   */
  protected class RobotConsumer implements Runnable {
    private final RobotProducer producer;
    private PreemptionContext preemptContext;

    /**
     *
     * @param producer a producer that will give us game states
     */
    public RobotConsumer(RobotProducer producer) {
      this.producer = producer;
    }

    @Override
    public void run() {
      while ( !killBeacon.shouldKill() ) {
        try {
          // Wait for a state to be available in the queue
          GameState state = producer.takeGame();

          // Reconstruct the game state (rack and board)
          Rack rack = stateHelper.getCurrentPlayerRack(state);
          WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);

          // Generate a move
          MoveGeneratorParams params = buildParams(state);
          Move generatedMove = getMoveGenerator().generateMove(rack, board, params);

          // Submit the generated move
          apiProvider.makeMove(state, stateHelper.createMoveSubmissionFromPlay(generatedMove));

          // Mark the game as processed
          producer.releaseGame(state);
          synchronized ( this ) {
            preemptContext = null;
          }
        }
        catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        catch (Exception e) {
          LOG.error("Exception while consuming game state: \n{}",
            Joiner.on('\n').join(e.getStackTrace()));
        }
      }
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
        .set(WwfMoveGeneratorParamKey.GAME_STATE, state)
        .set(FixedDepthParamKey.PREEMPTION_CONTEXT, preemptContext);
    }
  }

  /**
   * Producer -- produces active games for consumers
   *
   * Intermittently polls the API for new games and adds them to a queue for consumers to process.
   */
  protected class RobotProducer implements Runnable {
    private final SynchronousQueue<GameState> queue;
    private final Set<Long> activeGames;
    private final Map<Long, GameState> gameStateHistory;
    private final Map<Long, GameScoreStatus> gameScoreHistory;
    private int lastUpdate;

    public RobotProducer() {
      this.queue = new SynchronousQueue<GameState>();
      this.activeGames = Collections.synchronizedSet(Sets.<Long>newHashSet());
      this.gameStateHistory = Maps.newHashMap();
      this.gameScoreHistory = Maps.newHashMap();
      this.lastUpdate = 0;
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

    @Override
    public void run() {
      while ( !killBeacon.shouldKill() ) {
        try {
          LOG.info("Polling for new games...");

          // Fetch the index and find games that have pending moves and that aren't already queued
          GameIndex index;
          if ( lastUpdate == 0) {
            index = apiProvider.getGameIndex();
          }
          else {
            index = apiProvider.getGamesWithUpdates(lastUpdate);
          }
          int numActiveGames = 0;

          // Don't get old games again
          lastUpdate = (int)(System.currentTimeMillis() / 1000L);

          for (GameMeta gameMeta : index.getGames()) {
            long gameId = gameMeta.getId();

            if ( !gameMeta.isOver() ) {
              numActiveGames++;
            }

            // Only consider games where it's our turn
            if ( gameMeta.isOver() ) {
              continue;
            }
            if ( gameMeta.getCurrentMoveUserId() != index.getUser().getId() ) {
              continue;
            }
            if ( gameMeta.getUsersByIdSize() == 1 ) {
              continue;
            }

            if ( !activeGames.contains(gameId) && activeGames.size() < maxGames ) {
              LOG.info("Added game to processing queue");

              GameState state = apiProvider.getGameState(gameId);
              gameStateHistory.put(state.getId(), state);
              gameScoreHistory.put(state.getId(), stateHelper.getScoreStatus(index.getUser(), state));
              queue.offer(state);
              activeGames.add(gameId);
            }
            else {
              LOG.info("Skipped active game {}. Active games = {}.", gameId, activeGames);
            }
          }

          // Start new games if we have fewer active games than we'd like.
          for ( int i = 0; i < (maxGames - numActiveGames); i++) {
            apiProvider.createRandomGame();
          }

          // Log score stats
          CountingHashMap<GameScoreStatus> statusCounts = CountingHashMap.create();
          for (GameScoreStatus gameScoreStatus : gameScoreHistory.values()) {
            statusCounts.increment(gameScoreStatus);
          }

          LOG.info("Active game status summary: winning {}, losing {}, tied {}.",
            new Object[] {
              statusCounts.getCount(GameScoreStatus.WINNING),
              statusCounts.getCount(GameScoreStatus.LOSING),
              statusCounts.getCount(GameScoreStatus.TIED)
            });

          Thread.sleep(pollPeriod);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      LOG.info("Producer shutting down");
    }
  }

  public static void main(String[] args) throws IOException {
    String accessToken;
    if ( args.length == 0 ) {
      accessToken = new AccessTokenRetriever().promptForAccessToken();
    }
    else {
      accessToken = args[0];
    }
    StatefulApiProvider apiProvider = new StatefulApiProvider(accessToken);

    Robot robot = new Robot(apiProvider);
    robot.start();
  }
}
