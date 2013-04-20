package org.sidoh.words_with_robots.move_generation.swap_strategies;

import com.google.common.collect.Lists;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Tile;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class SwapStrategy {
  /**
   *
   * @param state
   * @param alternative
   * @return
   */
  public abstract List<Tile> getTilesToSwap(GameState state, Move alternative);

  /**
   *
   * @param rack
   * @return
   */
  protected List<Tile> swapLowestScoring(List<Tile> rack, int n) {
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
}
