package org.sidoh.words_with_robots.move_generation.swap_strategies;

import com.google.common.collect.Lists;
import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Tile;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class SwapStrategy {
  protected static final GameStateHelper stateHelper = GameStateHelper.getInstance();

  /**
   *
   * @param state
   * @param alternative
   * @return
   */
  public abstract List<Tile> getTilesToSwap(GameState state, Move alternative);

  /**
   * Swap the N lowest scoring tiles, as long as there are N tiles to swap. Otherwise, swap
   * the largest legal value less than N.
   *
   * @param state
   * @param rack
   * @param n
   * @return
   */
  protected List<Tile> swapLowestScoring(GameState state, List<Tile> rack, int n) {
    n = Math.min(n, state.getRemainingTilesSize());
    rack = Lists.newArrayList(rack);
    Collections.sort(rack, new Comparator<Tile>() {
      @Override
      public int compare(Tile tile, Tile tile1) {
        return Integer.valueOf(tile.getValue()).compareTo(tile1.getValue());
      }
    });

    return rack.subList(0, n);
  }

  public static SwapStrategy neverSwap() {
    return new NeverSwap();
  }

  public static SwapStrategy swapWhenNoAlternatives() {
    return new NoAlternativesSwapStrategy();
  }

  public static SwapStrategy minScoreThreshold() {
    return new MinScoreThresholdSwapStrategy();
  }
}
