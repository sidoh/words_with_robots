package org.sidoh.words_with_robots.move_generation.old_params;

/**
 * Used to signal to move generation algorithms that they should stop what they're doing and return the
 * best move that they've found so far (i.e., allows move generation algorithms to be compliant with
 * preemption).
 *
 * This allows for two types of preemption. The first is strong preemption, which should signal to
 * the thread that it should immediately clean up and return. The other is weak preemption, which signals
 * to the thread that it should quit when it thinks its taken its fair share of the CPU.
 */
public class PreemptionContext {
  public enum State {
    NOT_PREEMPTED, WEAK_PREEMPT, STRONG_PREEMPT
  }

  public State preemptState = State.NOT_PREEMPTED;

  /**
   * Signals to the thread that it should immediately clean up and return.
   */
  public void strongPreempt() {
    preemptState = State.STRONG_PREEMPT;
  }

  /**
   * Signals to the thread that its caller would like it to exit within a reasonable timeframe.
   */
  public void weakPreempt() {
    preemptState = State.WEAK_PREEMPT;
  }

  /**
   *
   * @return the current preempt state
   */
  public State getPreemptState() {
    return preemptState;
  }
}
