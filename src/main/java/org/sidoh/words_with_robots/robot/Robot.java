package org.sidoh.words_with_robots.robot;

import com.google.common.collect.Lists;
import org.sidoh.words_with_robots.data_structures.gaddag.GadDag;
import org.sidoh.words_with_robots.move_generation.GadDagWwfMoveGenerator;
import org.sidoh.words_with_robots.move_generation.IterativeDeepeningMoveGenerator;
import org.sidoh.words_with_robots.move_generation.WordsWithFriendsMoveGenerator;
import org.sidoh.words_with_robots.move_generation.WwfMinimaxLocal;
import org.sidoh.words_with_robots.util.dictionary.DictionaryHelper;
import org.sidoh.wwf_api.AccessTokenRetriever;
import org.sidoh.wwf_api.StatefulApiProvider;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Words with friends - now with no friends and only robots!
 *
 * Implements a simple producer/consumer model with a thread polling for active games and a thread
 * pool of workers consuming active games and generating moves.
 */
public class Robot {
  private static final Logger LOG = LoggerFactory.getLogger(Robot.class);
  protected final static GameStateHelper stateHelper = GameStateHelper.getInstance();

  private GadDag dictionary;
  private WordsWithFriendsMoveGenerator moveGenerator;
  private ExecutorService threadPool;
  private StatefulApiProvider apiProvider;
  private RobotSettings settings;

  /**
   *
   * @param apiProvider - an API provider initialized with an access token
   */
  public Robot(StatefulApiProvider apiProvider, RobotSettings settings) {
    this.settings = settings;
    this.apiProvider = apiProvider;
    this.dictionary = new GadDag();
    this.moveGenerator = null;
    this.threadPool = Executors.newFixedThreadPool(settings.getInteger(RobotSettingKey.MAX_THREADS));

    // Load default move generator if one isn't provided
    if ( settings.get(RobotSettingKey.MOVE_GENERATOR) == null ) {
      settings.set(RobotSettingKey.MOVE_GENERATOR, getMoveGenerator());
    }
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
        //moveGenerator = new WwfMinimaxLocal(new GadDagWwfMoveGenerator(dictionary));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return moveGenerator;
  }

  /**
   * Start the robot
   */
  public void start() {
    RobotProducer producer = new RobotProducer(settings, apiProvider);
    List<RobotConsumer> consumers = Lists.newArrayList();
    int maxThreads = settings.getInteger(RobotSettingKey.MAX_THREADS);

    for ( int i = 0; i < maxThreads; i++ ) {
      RobotConsumer consumer = new RobotConsumer(settings, producer, apiProvider);
      consumers.add(consumer);
      threadPool.submit(consumer);
    }

    Thread producerThread = new Thread(producer);
    producerThread.start();

    RobotPreempter preempter = new RobotPreempter(settings, producer, consumers);
    Thread preempterThread = new Thread(preempter);
    preempterThread.start();
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
    RobotSettings settings = new RobotSettings();

    String logDirectory = settings.getString(RobotSettingKey.LOG_DIRECTORY);
    if ( logDirectory != null ) {
      new File(logDirectory).mkdirs();

      if ( settings.getBoolean(RobotSettingKey.SAVE_GAME_STATES) ) {
        new File(logDirectory, "game_states").mkdirs();
      }
    }

    Robot robot = new Robot(apiProvider, settings);
    robot.start();
  }
}
