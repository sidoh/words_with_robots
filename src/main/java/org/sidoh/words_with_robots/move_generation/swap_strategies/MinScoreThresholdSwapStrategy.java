package org.sidoh.words_with_robots.move_generation.swap_strategies;

import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.game_state.Rack;
import org.sidoh.wwf_api.types.game_state.Tile;

import java.util.Collections;
import java.util.List;

public class MinScoreThresholdSwapStrategy extends SwapStrategy {
  private static final int DEFAULT_THRESHOLD = 5;
  private int threshold;

  public MinScoreThresholdSwapStrategy() {
    this.threshold = DEFAULT_THRESHOLD;
  }

  public MinScoreThresholdSwapStrategy setThreshold(int threshold) {
    this.threshold = threshold;
    return this;
  }

  @Override
  public List<Tile> getTilesToSwap(GameState state, Move alternative) {
    if (alternative.getMoveType() != MoveType.PLAY || alternative.getResult().getScore() < threshold) {
      Rack playerRack = stateHelper.getCurrentPlayerRack(state);

      return swapLowestScoring(state, playerRack.getTiles(), playerRack.getTilesSize());
    }
    else {
      return Collections.emptyList();
    }
  }
}
