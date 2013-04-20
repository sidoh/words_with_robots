package org.sidoh.words_with_robots.move_generation.swap_strategies;

import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Tile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NeverSwap extends SwapStrategy{
  @Override
  public List<Tile> getTilesToSwap(GameState state, Move alternative) {
    return Collections.emptyList();
  }
}
