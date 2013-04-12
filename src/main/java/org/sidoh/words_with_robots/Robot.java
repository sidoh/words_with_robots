package org.sidoh.words_with_robots;

import com.google.common.collect.Sets;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.GadDagWwfMoveGenerator;
import org.sidoh.words_with_robots.move_generation.IterativeDeepeningMoveGenerator;
import org.sidoh.words_with_robots.move_generation.WordsWithFriendsMoveGenerator;
import org.sidoh.words_with_robots.move_generation.params.KillSignalBeacon;
import org.sidoh.words_with_robots.move_generation.params.MoveGeneratorParams;
import org.sidoh.words_with_robots.move_generation.params.WwfMoveGeneratorParamKey;
import org.sidoh.words_with_robots.util.dictionary.DictionaryHelper;
import org.sidoh.wwf_api.AccessTokenRetriever;
import org.sidoh.wwf_api.StatefulApiProvider;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

public class Robot {
  private static final Logger LOG = LoggerFactory.getLogger(Robot.class);

  public final static int DEFUALT_MAX_GAMES = 10;
  public final static int DEFAULT_MAX_THREADS = 4;
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

  public Robot setMoveGenerator(WordsWithFriendsMoveGenerator moveGenerator) {
    this.moveGenerator = moveGenerator;
    return this;
  }

  public Robot setMaxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
    this.threadPool = Executors.newFixedThreadPool(maxThreads);
    return this;
  }

  public Robot setPollPeriod(long pollPeriod) {
    this.pollPeriod = pollPeriod;
    return this;
  }

  public void start() {
    SynchronousQueue<GameState> gameStateQueue = new SynchronousQueue<GameState>();
    Set<Long> activeGames = Collections.synchronizedSet(Sets.<Long>newHashSet());

    RobotProducer producer = new RobotProducer(gameStateQueue, activeGames);
    for ( int i = 0; i < maxThreads; i++ ) {
      threadPool.submit(new RobotConsumer(gameStateQueue, activeGames));
    }

    Thread producerThread = new Thread(producer);
    producerThread.start();
  }

  /**
   * Consumes games from the queue
   */
  protected class RobotConsumer implements Runnable {
    private final SynchronousQueue<GameState> queue;
    private final Set<Long> activeGames;

    public RobotConsumer(SynchronousQueue<GameState> queue, Set<Long> activeGames) {
      this.queue = queue;
      this.activeGames = activeGames;
    }

    @Override
    public void run() {
      while ( !killBeacon.shouldKill() ) {
        try {
          LOG.info("There are {} active games in the queue", queue.size());

          GameState state = queue.take();
          LOG.info("Popped game off of producer queue");

          Rack rack = stateHelper.getCurrentPlayerRack(state);
          WordsWithFriendsBoard board = stateHelper.createBoardFromState(state);
          MoveGeneratorParams params = new MoveGeneratorParams()
            .set(WwfMoveGeneratorParamKey.GAME_STATE, state);
          LOG.info("Reconstructed state: \n{}", board);

          Move generatedMove = getMoveGenerator().generateMove(rack, board, params);
          LOG.info("Generated move: {}", generatedMove);

          apiProvider.makeMove(state, stateHelper.createMoveSubmissionFromPlay(generatedMove));
          activeGames.remove(state.getId());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Intermittently polls the API for new games and adds them to a queue for consumers to process.
   */
  protected class RobotProducer implements Runnable {
    private final SynchronousQueue<GameState> queue;
    private final Set<Long> activeGames;

    public RobotProducer(SynchronousQueue<GameState> queue, Set<Long> activeGames) {
      this.queue = queue;
      this.activeGames = activeGames;
    }

    @Override
    public void run() {
      while ( !killBeacon.shouldKill() ) {
        try {
          LOG.info("Polling for new games...");

          // Fetch the index and find games that have pending moves and that aren't already queued
          GameIndex index = apiProvider.getGameIndex();
          int numActiveGames = 0;

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

            if ( !activeGames.contains(gameId) && activeGames.size() < maxGames ) {
              LOG.info("Added game to processing queue");

              GameState state = apiProvider.getGameState(gameId);
              queue.offer(state);
              activeGames.add(gameId);
            }
          }

          // Start new games if we have fewer active games than we'd like.
          for ( int i = 0; i < (maxGames - numActiveGames); i++) {
            apiProvider.createRandomGame();
          }

          Thread.sleep(pollPeriod);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      LOG.info("Producer shutting down");
    }
  }

  public static void main(String[] args) throws IOException {
    String accessToken = new AccessTokenRetriever().promptForAccessToken();
    StatefulApiProvider apiProvider = new StatefulApiProvider(accessToken);

    Robot robot = new Robot(apiProvider);
    robot.start();
  }
}
