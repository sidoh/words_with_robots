package org.sidoh.words_with_robots.move_generation.swap_strategies;

import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.api.MoveType;
import org.sidoh.wwf_api.types.game_state.Tile;

import java.util.Collections;
import java.util.List;

public class NoAlternativesSwapStrategy extends SwapStrategy {
  @Override
  public List<Tile> getTilesToSwap(GameState state, Move alternative) {
    if ( alternative != null && alternative.getMoveType() != MoveType.PASS ) {
      return Collections.emptyList();
    }

    if ( state.getRemainingTilesSize() == 0 ) {
      return Collections.emptyList();
    }

    List<Tile> rack = state.getRacks().get(state.getMeta().getCurrentMoveUserId());
    int numToSwap = Math.min(rack.size(), state.getRemainingTilesSize());

    return swapLowestScoring(rack, numToSwap);
  }
}
