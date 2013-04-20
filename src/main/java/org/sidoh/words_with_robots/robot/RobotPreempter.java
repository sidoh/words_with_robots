package org.sidoh.words_with_robots.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class RobotPreempter implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(RobotPreempter.class);

  private final int preemptionThreshold;
  private final RobotProducer producer;
  private final List<RobotConsumer> consumers;

  public RobotPreempter(RobotSettings settings, RobotProducer producer, List<RobotConsumer> consumers) {
    this.preemptionThreshold = settings.getInteger(RobotSettingKey.PREEMPTION_THRESHOLD);
    this.producer = producer;
    this.consumers = consumers;
  }

  @Override
  public void run() {
    while ( true ) {
      if ( producer.numActiveGames() >= preemptionThreshold ) {
        for ( int i = 0; i < (preemptionThreshold - numActiveConsumers()); i++ ) {
          LOG.info("Weak preempting a consumer");
          if (consumers.get(i).isOccupied()) {
            consumers.get(i).weakPreempt();
          }
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
