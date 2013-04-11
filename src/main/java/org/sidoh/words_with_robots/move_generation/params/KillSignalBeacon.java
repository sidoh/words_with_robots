package org.sidoh.words_with_robots.move_generation.params;

/**
 * Used to signal to move generation algorithms that they should stop what they're doing and return the
 * best move that they've found so far. Callers can set this parameter and call kill(). The move generation
 * algorithm polls the value at every node and stops execution if it is set to true.
 */
public class KillSignalBeacon {
  private boolean shouldKill = false;

  public void kill() {
    shouldKill = true;
  }

  public boolean shouldKill() {
    return shouldKill;
  }
}
