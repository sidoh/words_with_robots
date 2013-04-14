package org.sidoh.words_with_robots.robot;

import com.google.common.collect.Maps;

import java.util.Map;

public class RobotSettings {
  private Map<RobotSettingKey, Object> settings = Maps.newHashMap();

  public Object get(RobotSettingKey setting) {
    return settings.containsKey(setting) ? settings.get(setting) : setting.getDefaultValue();
  }

  public Integer getInteger(RobotSettingKey setting) {
    return (Integer)get(setting);
  }

  public Long getLong(RobotSettingKey setting) {
    return (Long)get(setting);
  }

  public String getString(RobotSettingKey setting) {
    return (String)get(setting);
  }

  public Boolean getBoolean(RobotSettingKey setting) {
    return (Boolean)get(setting);
  }

  public RobotSettings set(RobotSettingKey setting, Object value) {
    settings.put(setting, value);
    return this;
  }
}
