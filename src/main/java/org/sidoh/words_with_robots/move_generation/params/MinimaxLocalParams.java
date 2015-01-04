/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.sidoh.words_with_robots.move_generation.params;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimaxLocalParams implements org.apache.thrift.TBase<MinimaxLocalParams, MinimaxLocalParams._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MinimaxLocalParams");

  private static final org.apache.thrift.protocol.TField MOVE_CACHE_SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("moveCacheSize", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField CACHE_MISS_FLUSH_THRESHOLD_FIELD_DESC = new org.apache.thrift.protocol.TField("cacheMissFlushThreshold", org.apache.thrift.protocol.TType.DOUBLE, (short)2);
  private static final org.apache.thrift.protocol.TField DIFF_THRESHOLD_FIELD_DESC = new org.apache.thrift.protocol.TField("diffThreshold", org.apache.thrift.protocol.TType.I32, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new MinimaxLocalParamsStandardSchemeFactory());
    schemes.put(TupleScheme.class, new MinimaxLocalParamsTupleSchemeFactory());
  }

  /**
   * Maximum number of milliseconds to let the algorithm run. Iterative deepening works by
   * iteratively incrementing the number of lookahead plies until it runs out of time.
   */
  public int moveCacheSize; // optional
  public double cacheMissFlushThreshold; // optional
  public int diffThreshold; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * Maximum number of milliseconds to let the algorithm run. Iterative deepening works by
     * iteratively incrementing the number of lookahead plies until it runs out of time.
     */
    MOVE_CACHE_SIZE((short)1, "moveCacheSize"),
    CACHE_MISS_FLUSH_THRESHOLD((short)2, "cacheMissFlushThreshold"),
    DIFF_THRESHOLD((short)3, "diffThreshold");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // MOVE_CACHE_SIZE
          return MOVE_CACHE_SIZE;
        case 2: // CACHE_MISS_FLUSH_THRESHOLD
          return CACHE_MISS_FLUSH_THRESHOLD;
        case 3: // DIFF_THRESHOLD
          return DIFF_THRESHOLD;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __MOVECACHESIZE_ISSET_ID = 0;
  private static final int __CACHEMISSFLUSHTHRESHOLD_ISSET_ID = 1;
  private static final int __DIFFTHRESHOLD_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);
  private _Fields optionals[] = {_Fields.MOVE_CACHE_SIZE,_Fields.CACHE_MISS_FLUSH_THRESHOLD,_Fields.DIFF_THRESHOLD};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MOVE_CACHE_SIZE, new org.apache.thrift.meta_data.FieldMetaData("moveCacheSize", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.CACHE_MISS_FLUSH_THRESHOLD, new org.apache.thrift.meta_data.FieldMetaData("cacheMissFlushThreshold", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    tmpMap.put(_Fields.DIFF_THRESHOLD, new org.apache.thrift.meta_data.FieldMetaData("diffThreshold", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MinimaxLocalParams.class, metaDataMap);
  }

  public MinimaxLocalParams() {
    this.moveCacheSize = 1000;

    this.diffThreshold = 100000;

  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MinimaxLocalParams(MinimaxLocalParams other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.moveCacheSize = other.moveCacheSize;
    this.cacheMissFlushThreshold = other.cacheMissFlushThreshold;
    this.diffThreshold = other.diffThreshold;
  }

  public MinimaxLocalParams deepCopy() {
    return new MinimaxLocalParams(this);
  }

  @Override
  public void clear() {
    this.moveCacheSize = 1000;

    setCacheMissFlushThresholdIsSet(false);
    this.cacheMissFlushThreshold = 0.0;
    this.diffThreshold = 100000;

  }

  /**
   * Maximum number of milliseconds to let the algorithm run. Iterative deepening works by
   * iteratively incrementing the number of lookahead plies until it runs out of time.
   */
  public int getMoveCacheSize() {
    return this.moveCacheSize;
  }

  /**
   * Maximum number of milliseconds to let the algorithm run. Iterative deepening works by
   * iteratively incrementing the number of lookahead plies until it runs out of time.
   */
  public MinimaxLocalParams setMoveCacheSize(int moveCacheSize) {
    this.moveCacheSize = moveCacheSize;
    setMoveCacheSizeIsSet(true);
    return this;
  }

  public void unsetMoveCacheSize() {
    __isset_bit_vector.clear(__MOVECACHESIZE_ISSET_ID);
  }

  /** Returns true if field moveCacheSize is set (has been assigned a value) and false otherwise */
  public boolean isSetMoveCacheSize() {
    return __isset_bit_vector.get(__MOVECACHESIZE_ISSET_ID);
  }

  public void setMoveCacheSizeIsSet(boolean value) {
    __isset_bit_vector.set(__MOVECACHESIZE_ISSET_ID, value);
  }

  public double getCacheMissFlushThreshold() {
    return this.cacheMissFlushThreshold;
  }

  public MinimaxLocalParams setCacheMissFlushThreshold(double cacheMissFlushThreshold) {
    this.cacheMissFlushThreshold = cacheMissFlushThreshold;
    setCacheMissFlushThresholdIsSet(true);
    return this;
  }

  public void unsetCacheMissFlushThreshold() {
    __isset_bit_vector.clear(__CACHEMISSFLUSHTHRESHOLD_ISSET_ID);
  }

  /** Returns true if field cacheMissFlushThreshold is set (has been assigned a value) and false otherwise */
  public boolean isSetCacheMissFlushThreshold() {
    return __isset_bit_vector.get(__CACHEMISSFLUSHTHRESHOLD_ISSET_ID);
  }

  public void setCacheMissFlushThresholdIsSet(boolean value) {
    __isset_bit_vector.set(__CACHEMISSFLUSHTHRESHOLD_ISSET_ID, value);
  }

  public int getDiffThreshold() {
    return this.diffThreshold;
  }

  public MinimaxLocalParams setDiffThreshold(int diffThreshold) {
    this.diffThreshold = diffThreshold;
    setDiffThresholdIsSet(true);
    return this;
  }

  public void unsetDiffThreshold() {
    __isset_bit_vector.clear(__DIFFTHRESHOLD_ISSET_ID);
  }

  /** Returns true if field diffThreshold is set (has been assigned a value) and false otherwise */
  public boolean isSetDiffThreshold() {
    return __isset_bit_vector.get(__DIFFTHRESHOLD_ISSET_ID);
  }

  public void setDiffThresholdIsSet(boolean value) {
    __isset_bit_vector.set(__DIFFTHRESHOLD_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case MOVE_CACHE_SIZE:
      if (value == null) {
        unsetMoveCacheSize();
      } else {
        setMoveCacheSize((Integer)value);
      }
      break;

    case CACHE_MISS_FLUSH_THRESHOLD:
      if (value == null) {
        unsetCacheMissFlushThreshold();
      } else {
        setCacheMissFlushThreshold((Double)value);
      }
      break;

    case DIFF_THRESHOLD:
      if (value == null) {
        unsetDiffThreshold();
      } else {
        setDiffThreshold((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case MOVE_CACHE_SIZE:
      return Integer.valueOf(getMoveCacheSize());

    case CACHE_MISS_FLUSH_THRESHOLD:
      return Double.valueOf(getCacheMissFlushThreshold());

    case DIFF_THRESHOLD:
      return Integer.valueOf(getDiffThreshold());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case MOVE_CACHE_SIZE:
      return isSetMoveCacheSize();
    case CACHE_MISS_FLUSH_THRESHOLD:
      return isSetCacheMissFlushThreshold();
    case DIFF_THRESHOLD:
      return isSetDiffThreshold();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof MinimaxLocalParams)
      return this.equals((MinimaxLocalParams)that);
    return false;
  }

  public boolean equals(MinimaxLocalParams that) {
    if (that == null)
      return false;

    boolean this_present_moveCacheSize = true && this.isSetMoveCacheSize();
    boolean that_present_moveCacheSize = true && that.isSetMoveCacheSize();
    if (this_present_moveCacheSize || that_present_moveCacheSize) {
      if (!(this_present_moveCacheSize && that_present_moveCacheSize))
        return false;
      if (this.moveCacheSize != that.moveCacheSize)
        return false;
    }

    boolean this_present_cacheMissFlushThreshold = true && this.isSetCacheMissFlushThreshold();
    boolean that_present_cacheMissFlushThreshold = true && that.isSetCacheMissFlushThreshold();
    if (this_present_cacheMissFlushThreshold || that_present_cacheMissFlushThreshold) {
      if (!(this_present_cacheMissFlushThreshold && that_present_cacheMissFlushThreshold))
        return false;
      if (this.cacheMissFlushThreshold != that.cacheMissFlushThreshold)
        return false;
    }

    boolean this_present_diffThreshold = true && this.isSetDiffThreshold();
    boolean that_present_diffThreshold = true && that.isSetDiffThreshold();
    if (this_present_diffThreshold || that_present_diffThreshold) {
      if (!(this_present_diffThreshold && that_present_diffThreshold))
        return false;
      if (this.diffThreshold != that.diffThreshold)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(MinimaxLocalParams other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    MinimaxLocalParams typedOther = (MinimaxLocalParams)other;

    lastComparison = Boolean.valueOf(isSetMoveCacheSize()).compareTo(typedOther.isSetMoveCacheSize());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMoveCacheSize()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.moveCacheSize, typedOther.moveCacheSize);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetCacheMissFlushThreshold()).compareTo(typedOther.isSetCacheMissFlushThreshold());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCacheMissFlushThreshold()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cacheMissFlushThreshold, typedOther.cacheMissFlushThreshold);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDiffThreshold()).compareTo(typedOther.isSetDiffThreshold());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDiffThreshold()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.diffThreshold, typedOther.diffThreshold);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("MinimaxLocalParams(");
    boolean first = true;

    if (isSetMoveCacheSize()) {
      sb.append("moveCacheSize:");
      sb.append(this.moveCacheSize);
      first = false;
    }
    if (isSetCacheMissFlushThreshold()) {
      if (!first) sb.append(", ");
      sb.append("cacheMissFlushThreshold:");
      sb.append(this.cacheMissFlushThreshold);
      first = false;
    }
    if (isSetDiffThreshold()) {
      if (!first) sb.append(", ");
      sb.append("diffThreshold:");
      sb.append(this.diffThreshold);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class MinimaxLocalParamsStandardSchemeFactory implements SchemeFactory {
    public MinimaxLocalParamsStandardScheme getScheme() {
      return new MinimaxLocalParamsStandardScheme();
    }
  }

  private static class MinimaxLocalParamsStandardScheme extends StandardScheme<MinimaxLocalParams> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MinimaxLocalParams struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // MOVE_CACHE_SIZE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.moveCacheSize = iprot.readI32();
              struct.setMoveCacheSizeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CACHE_MISS_FLUSH_THRESHOLD
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.cacheMissFlushThreshold = iprot.readDouble();
              struct.setCacheMissFlushThresholdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // DIFF_THRESHOLD
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.diffThreshold = iprot.readI32();
              struct.setDiffThresholdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MinimaxLocalParams struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.isSetMoveCacheSize()) {
        oprot.writeFieldBegin(MOVE_CACHE_SIZE_FIELD_DESC);
        oprot.writeI32(struct.moveCacheSize);
        oprot.writeFieldEnd();
      }
      if (struct.isSetCacheMissFlushThreshold()) {
        oprot.writeFieldBegin(CACHE_MISS_FLUSH_THRESHOLD_FIELD_DESC);
        oprot.writeDouble(struct.cacheMissFlushThreshold);
        oprot.writeFieldEnd();
      }
      if (struct.isSetDiffThreshold()) {
        oprot.writeFieldBegin(DIFF_THRESHOLD_FIELD_DESC);
        oprot.writeI32(struct.diffThreshold);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MinimaxLocalParamsTupleSchemeFactory implements SchemeFactory {
    public MinimaxLocalParamsTupleScheme getScheme() {
      return new MinimaxLocalParamsTupleScheme();
    }
  }

  private static class MinimaxLocalParamsTupleScheme extends TupleScheme<MinimaxLocalParams> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MinimaxLocalParams struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetMoveCacheSize()) {
        optionals.set(0);
      }
      if (struct.isSetCacheMissFlushThreshold()) {
        optionals.set(1);
      }
      if (struct.isSetDiffThreshold()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetMoveCacheSize()) {
        oprot.writeI32(struct.moveCacheSize);
      }
      if (struct.isSetCacheMissFlushThreshold()) {
        oprot.writeDouble(struct.cacheMissFlushThreshold);
      }
      if (struct.isSetDiffThreshold()) {
        oprot.writeI32(struct.diffThreshold);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MinimaxLocalParams struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.moveCacheSize = iprot.readI32();
        struct.setMoveCacheSizeIsSet(true);
      }
      if (incoming.get(1)) {
        struct.cacheMissFlushThreshold = iprot.readDouble();
        struct.setCacheMissFlushThresholdIsSet(true);
      }
      if (incoming.get(2)) {
        struct.diffThreshold = iprot.readI32();
        struct.setDiffThresholdIsSet(true);
      }
    }
  }

}
