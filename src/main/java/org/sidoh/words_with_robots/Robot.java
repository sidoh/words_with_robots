package org.sidoh.words_with_robots;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.sidoh.words_with_robots.data_structures.CountingHashMap;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.GadDagWwfMoveGenerator;
import org.sidoh.words_with_robots.move_generation.IterativeDeepeningMoveGenerator;
import org.sidoh.words_with_robots.move_generation.WordsWithFriendsMoveGenerator;
import org.sidoh.words_with_robots.move_generation.params.FixedDepthParamKey;
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
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
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
  public final static int DEFUALT_MAX_GAMES = 20;

  /**
   * The maximum number of consumer threads.
   */
  public final static int DEFAULT_MAX_THREADS = 4;

  /**
   * The number of milliseconds to wait in-between polling for active games
   */
  public final static long DEFAULT_POLL_PERIOD = 10000L;

  /**
   * The number of seconds to wait before expiring a game to make room for a new one
   */
  public final static int DEFAULT_GAME_EXPIRE_TIME = 86400;

  /**
   * Number of concurrent active games before consumers are weakly preempted, allowing them to run
   * through their minimum execution time, but not max.
   */
  public final static int DEFAULT_PREEMPTION_THRESHOLD = 6;

  protected final static GameStateHelper stateHelper = new GameStateHelper();

  private int maxGames;
  private GadDag dictionary;
  private WordsWithFriendsMoveGenerator moveGenerator;
  private int maxThreads;
  private ExecutorService threadPool;
  private long pollPeriod;
  private final StatefulApiProvider apiProvider;
  private int preemptionThreshold;

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
    this.preemptionThreshold = DEFAULT_PREEMPTION_THRESHOLD;
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
   * Update the preemption threshold
   *
   * @param threshold
   * @return
   */
  public Robot setPreemptionThreshold(int threshold) {
    this.preemptionThreshold = threshold;
    return this;
  }

  /**
   * Start the robot
   */
  public void start() {
    RobotProducer producer = new RobotProducer();
    List<RobotConsumer> consumers = Lists.newArrayList();
    for ( int i = 0; i < maxThreads; i++ ) {
      RobotConsumer consumer = new RobotConsumer(producer);
      consumers.add(consumer);
      threadPool.submit(consumer);
    }

    Thread producerThread = new Thread(producer);
    producerThread.start();

    RobotPreempter preempter = new RobotPreempter(producer, consumers);
    Thread preempterThread = new Thread(preempter);
    preempterThread.start();
  }

  private class RobotPreempter implements Runnable {
    private final RobotProducer producer;
    private final List<RobotConsumer> consumers;

    public RobotPreempter(RobotProducer producer, List<RobotConsumer> consumers) {
      this.producer = producer;
      this.consumers = consumers;
    }

    @Override
    public void run() {
      while ( true ) {
        if ( producer.numActiveGames() >= preemptionThreshold ) {
          for ( int i = 0; i < (preemptionThreshold - numActiveConsumers()); i++ ) {
            LOG.info("Weak preempting a consumer");
            consumers.get(i).weakPreempt();
          }
        }

        try {
          Thread.sleep(100);
        }
        catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }

    /**
     *
     * @return the number of currently occupied consumers
     */
    protected int numActiveConsumers() {
      int count = 0;
      for (RobotConsumer consumer : consumers) {
        if ( consumer.isOccupied() ) {
          count++;
        }
      }
      return count;
    }
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
      while ( true ) {
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

    @Override
    public void run() {
      while ( true ) {
        try {
          // Fetch the index and find games that have pending moves and that aren't already queued
          GameIndex index;
          if ( lastUpdate == 0 ) {
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

            if ( !activeGames.contains(gameId) && activeGames.size() < maxGames ) {
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
          for ( int i = 0; i < (maxGames - numActiveGames); i++) {
            apiProvider.createRandomGame();
          }

          // Log score stats
          CountingHashMap<GameScoreStatus> statusCounts = CountingHashMap.create();
          for (GameScoreStatus gameScoreStatus : gameScoreHistory.values()) {
            statusCounts.increment(gameScoreStatus);
          }

          LOG.debug("Active game status summary: winning {}, losing {}, tied {}.",
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

      return secondSinceLastAction > DEFAULT_GAME_EXPIRE_TIME;
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
