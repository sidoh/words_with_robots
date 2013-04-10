package org.sidoh.words_with_robots.move_generation.params;

import com.google.common.collect.Maps;
import org.sidoh.words_with_robots.move_generation.MoveGenerator;

import java.util.Map;

/**
 * Encapsulates a set of parameters passed to a move generation call
 */
public class MoveGeneratorParams {
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
