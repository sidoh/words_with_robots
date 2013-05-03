package org.sidoh.words_with_robots.robot;

import java.io.File;

public enum RobotSettingKey {
  /**
   * Defines the maximum number of active games to keep running. If there are fewer games
   * than this, the robot will try to create more.
   */
  MAX_GAMES(3),

  /**
   * The maximum number of consumer threads. This does not include the producer or the
   * preempter thread, as those always run and are not computationally expensive.
   */
  MAX_THREADS(Runtime.getRuntime().availableProcessors()),

  /**
   * The number of milliseconds to wait in-between polling for active games
   */
  GAME_INDEX_POLL_PERIOD(10000L),

  /**
   * The number of seconds to wait before expiring a game to make room for a new one. This can be
   * set to 0 to disable it.
   */
  INACTIVE_GAME_TTL(43200),

  /**
   * Number of concurrent active games before consumers are weakly preempted, allowing them to run
   * through their minimum execution time, but not max.
   */
  PREEMPTION_THRESHOLD(6),

  /**
   * The move generator to use. If this isn't set, iterative deepening with the default parameters
   * will be used.
   */
  MOVE_GENERATOR(null),

  /**
   * The directory to save log files in. Defaults to current working directory + './log'
   */
  LOG_DIRECTORY(new File(".", "log").getAbsolutePath()),

  /**
   * If true, saves serialized game state files to the log directory
   */
  SAVE_GAME_STATES(true),

  /**
   * If true, send courtesy messages to opponents at the beginning of a game warning them that they're
   * playing against a bot
   */
  SEND_COURTESY_MESSAGES(true),

  /**
   * Message sent to new players if enabled
   */
  COURTESY_MESSAGE_STRING("Hi! Full disclosure: you're playing against WordsWithRobots, an artificial intelligence for WWF. "
                          +"If you're up for the challenge, I would love the opportunity to play with you. "
                          +"Otherwise, have a lovely day!");

  private Object defaultValue;
  private boolean defaultProvided;

  private RobotSettingKey() {
    this.defaultProvided = false;
  }

  private RobotSettingKey(Object defaultValue) {
    this.defaultValue = defaultValue;
    this.defaultProvided = true;
  }

  public Object getDefaultValue() {
    if ( defaultProvided ) {
      return defaultValue;
    }
    else {
      throw new RuntimeException("required argument " + this.name() + " not provided!");
    }
  }
}
