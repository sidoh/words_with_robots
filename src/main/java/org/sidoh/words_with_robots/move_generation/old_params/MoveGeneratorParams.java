package org.sidoh.words_with_robots.move_generation.old_params;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * Encapsulates a set of parameters passed to a move generation call
 */
public class MoveGeneratorParams {
  private Map<MoveGeneratorParamKey, Object> params;

  public MoveGeneratorParams() {
    this.params = Collections.synchronizedMap(Maps.<MoveGeneratorParamKey, Object>newHashMap());
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

  public Long getLong(MoveGeneratorParamKey param) {
    return (Long)get(param);
  }

  public Boolean getBoolean(MoveGeneratorParamKey param) {
    return (Boolean)get(param);
  }

  public MoveGeneratorParams clone() {
    MoveGeneratorParams copy = new MoveGeneratorParams();
    copy.params = params;
    return copy;
  }
}
