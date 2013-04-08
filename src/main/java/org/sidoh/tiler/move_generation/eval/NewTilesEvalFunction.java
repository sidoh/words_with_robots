package org.sidoh.tiler.move_generation.eval;

import org.sidoh.wwf_api.game_state.GameStateHelper;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.api.GameState;
import org.sidoh.wwf_api.types.game_state.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NewTilesEvalFunction implements EvaluationFunction {
  private static final Logger LOG = LoggerFactory.getLogger(NewTilesEvalFunction.class);
  private static final int BLANK_VALUE = 15;
  private static final GameStateHelper stateHelper = new GameStateHelper();

  @Override
  public double score(Move move, GameState state) {
    int total = 0;
    List<Tile> remaining = new ArrayList<Tile>(state.getRemainingTiles());

    for (int i = 0; i < Math.min(remaining.size(), move.getTiles().size()); i++) {
      if ( stateHelper.tileIsBlank(remaining.get(i)) ) {
        total += BLANK_VALUE;
      }
      else {
        total += remaining.get(i).getValue();
      }
    }

    return total;
  }
}
