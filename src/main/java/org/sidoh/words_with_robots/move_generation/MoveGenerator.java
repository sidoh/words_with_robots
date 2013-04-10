package org.sidoh.words_with_robots.move_generation;

import com.google.common.collect.Maps;
import org.sidoh.words_with_robots.move_generation.eval.EvaluationFunction;
import org.sidoh.words_with_robots.move_generation.eval.ScoreEvalFunction;
import org.sidoh.wwf_api.game_state.Board;
import org.sidoh.wwf_api.game_state.Move;
import org.sidoh.wwf_api.types.game_state.Rack;

import java.util.Collections;
import java.util.Map;

public abstract class MoveGenerator<T extends Board> {
  /**
   * Defines a parameter key for use in MoveGeneratorParams. Intended to be extended by
   * enums.
   */
  public interface MoveGeneratorParamKey {
    public Object getDefaultValue();
  }

  /**
   * Encapsulates a set of parameters passed to a move generation call
   */
  public static class MoveGeneratorParams {
    private final Map<MoveGeneratorParamKey, Object> params;

    public MoveGeneratorParams() {
      this.params = Maps.newHashMap();
    }

    public MoveGeneratorParams set(MoveGeneratorParamKey key, Object value) {
      params.put(key, value);
      return this;
    }

    public Object get(MoveGeneratorParamKey param) {
      if ( params.containsKey(param) ) {
        return params.get(param);
      }
      else {
        return param.getDefaultValue();
      }
    }

    public Integer getInt(MoveGeneratorParamKey param) {
      return (Integer)get(param);
    }
  }

  /**
   * Convenience method that calls gemerateMove(Rack, T, Map) with an empty map for the
   * params.
   *
   * @param rack user's rack containing tiles available for play
   * @param board board containing all previous moves
   * @return the "best" move according to this move generator
   */
  public final Move generateMove(Rack rack, T board) {
    return generateMove(rack, board, new MoveGeneratorParams());
  }

  /**
   * Given a rack and a board, generate a move.
   *
   * @param rack user's rack containing tiles available for play
   * @param board board containing all previous moves
   * @param params additional implementation-specific parameters
   * @return the "best" move according to this move generator
   */
  public abstract Move generateMove(Rack rack, T board, MoveGeneratorParams params);
}
