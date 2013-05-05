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

public class FixedDepthParams implements org.apache.thrift.TBase<FixedDepthParams, FixedDepthParams._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("FixedDepthParams");

  private static final org.apache.thrift.protocol.TField MIN_SCORE_FIELD_DESC = new org.apache.thrift.protocol.TField("minScore", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField BRANCHING_FACTOR_LIMIT_FIELD_DESC = new org.apache.thrift.protocol.TField("branchingFactorLimit", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField MAX_DEPTH_FIELD_DESC = new org.apache.thrift.protocol.TField("maxDepth", org.apache.thrift.protocol.TType.I32, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new FixedDepthParamsStandardSchemeFactory());
    schemes.put(TupleScheme.class, new FixedDepthParamsTupleSchemeFactory());
  }

  /**
   * The minimum score value a move must have in order for it to be
   * considered.
   */
  public int minScore; // optional
  /**
   * The maximum number of moves to consider. Moves are sorted by score and the
   * top N scoring moves are explored. This effectively truncates the search
   * tree and hopefully prunes branches without much promise.
   */
  public int branchingFactorLimit; // optional
  /**
   * The maximum depth to explore
   */
  public int maxDepth; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * The minimum score value a move must have in order for it to be
     * considered.
     */
    MIN_SCORE((short)1, "minScore"),
    /**
     * The maximum number of moves to consider. Moves are sorted by score and the
     * top N scoring moves are explored. This effectively truncates the search
     * tree and hopefully prunes branches without much promise.
     */
    BRANCHING_FACTOR_LIMIT((short)2, "branchingFactorLimit"),
    /**
     * The maximum depth to explore
     */
    MAX_DEPTH((short)3, "maxDepth");

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
        case 1: // MIN_SCORE
          return MIN_SCORE;
        case 2: // BRANCHING_FACTOR_LIMIT
          return BRANCHING_FACTOR_LIMIT;
        case 3: // MAX_DEPTH
          return MAX_DEPTH;
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
  private static final int __MINSCORE_ISSET_ID = 0;
  private static final int __BRANCHINGFACTORLIMIT_ISSET_ID = 1;
  private static final int __MAXDEPTH_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);
  private _Fields optionals[] = {_Fields.MIN_SCORE,_Fields.BRANCHING_FACTOR_LIMIT,_Fields.MAX_DEPTH};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MIN_SCORE, new org.apache.thrift.meta_data.FieldMetaData("minScore", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.BRANCHING_FACTOR_LIMIT, new org.apache.thrift.meta_data.FieldMetaData("branchingFactorLimit", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.MAX_DEPTH, new org.apache.thrift.meta_data.FieldMetaData("maxDepth", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(FixedDepthParams.class, metaDataMap);
  }

  public FixedDepthParams() {
    this.minScore = 5;

    this.branchingFactorLimit = 100;

    this.maxDepth = 2;

  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public FixedDepthParams(FixedDepthParams other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.minScore = other.minScore;
    this.branchingFactorLimit = other.branchingFactorLimit;
    this.maxDepth = other.maxDepth;
  }

  public FixedDepthParams deepCopy() {
    return new FixedDepthParams(this);
  }

  @Override
  public void clear() {
    this.minScore = 5;

    this.branchingFactorLimit = 100;

    this.maxDepth = 2;

  }

  /**
   * The minimum score value a move must have in order for it to be
   * considered.
   */
  public int getMinScore() {
    return this.minScore;
  }

  /**
   * The minimum score value a move must have in order for it to be
   * considered.
   */
  public FixedDepthParams setMinScore(int minScore) {
    this.minScore = minScore;
    setMinScoreIsSet(true);
    return this;
  }

  public void unsetMinScore() {
    __isset_bit_vector.clear(__MINSCORE_ISSET_ID);
  }

  /** Returns true if field minScore is set (has been assigned a value) and false otherwise */
  public boolean isSetMinScore() {
    return __isset_bit_vector.get(__MINSCORE_ISSET_ID);
  }

  public void setMinScoreIsSet(boolean value) {
    __isset_bit_vector.set(__MINSCORE_ISSET_ID, value);
  }

  /**
   * The maximum number of moves to consider. Moves are sorted by score and the
   * top N scoring moves are explored. This effectively truncates the search
   * tree and hopefully prunes branches without much promise.
   */
  public int getBranchingFactorLimit() {
    return this.branchingFactorLimit;
  }

  /**
   * The maximum number of moves to consider. Moves are sorted by score and the
   * top N scoring moves are explored. This effectively truncates the search
   * tree and hopefully prunes branches without much promise.
   */
  public FixedDepthParams setBranchingFactorLimit(int branchingFactorLimit) {
    this.branchingFactorLimit = branchingFactorLimit;
    setBranchingFactorLimitIsSet(true);
    return this;
  }

  public void unsetBranchingFactorLimit() {
    __isset_bit_vector.clear(__BRANCHINGFACTORLIMIT_ISSET_ID);
  }

  /** Returns true if field branchingFactorLimit is set (has been assigned a value) and false otherwise */
  public boolean isSetBranchingFactorLimit() {
    return __isset_bit_vector.get(__BRANCHINGFACTORLIMIT_ISSET_ID);
  }

  public void setBranchingFactorLimitIsSet(boolean value) {
    __isset_bit_vector.set(__BRANCHINGFACTORLIMIT_ISSET_ID, value);
  }

  /**
   * The maximum depth to explore
   */
  public int getMaxDepth() {
    return this.maxDepth;
  }

  /**
   * The maximum depth to explore
   */
  public FixedDepthParams setMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
    setMaxDepthIsSet(true);
    return this;
  }

  public void unsetMaxDepth() {
    __isset_bit_vector.clear(__MAXDEPTH_ISSET_ID);
  }

  /** Returns true if field maxDepth is set (has been assigned a value) and false otherwise */
  public boolean isSetMaxDepth() {
    return __isset_bit_vector.get(__MAXDEPTH_ISSET_ID);
  }

  public void setMaxDepthIsSet(boolean value) {
    __isset_bit_vector.set(__MAXDEPTH_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case MIN_SCORE:
      if (value == null) {
        unsetMinScore();
      } else {
        setMinScore((Integer)value);
      }
      break;

    case BRANCHING_FACTOR_LIMIT:
      if (value == null) {
        unsetBranchingFactorLimit();
      } else {
        setBranchingFactorLimit((Integer)value);
      }
      break;

    case MAX_DEPTH:
      if (value == null) {
        unsetMaxDepth();
      } else {
        setMaxDepth((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case MIN_SCORE:
      return Integer.valueOf(getMinScore());

    case BRANCHING_FACTOR_LIMIT:
      return Integer.valueOf(getBranchingFactorLimit());

    case MAX_DEPTH:
      return Integer.valueOf(getMaxDepth());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case MIN_SCORE:
      return isSetMinScore();
    case BRANCHING_FACTOR_LIMIT:
      return isSetBranchingFactorLimit();
    case MAX_DEPTH:
      return isSetMaxDepth();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof FixedDepthParams)
      return this.equals((FixedDepthParams)that);
    return false;
  }

  public boolean equals(FixedDepthParams that) {
    if (that == null)
      return false;

    boolean this_present_minScore = true && this.isSetMinScore();
    boolean that_present_minScore = true && that.isSetMinScore();
    if (this_present_minScore || that_present_minScore) {
      if (!(this_present_minScore && that_present_minScore))
        return false;
      if (this.minScore != that.minScore)
        return false;
    }

    boolean this_present_branchingFactorLimit = true && this.isSetBranchingFactorLimit();
    boolean that_present_branchingFactorLimit = true && that.isSetBranchingFactorLimit();
    if (this_present_branchingFactorLimit || that_present_branchingFactorLimit) {
      if (!(this_present_branchingFactorLimit && that_present_branchingFactorLimit))
        return false;
      if (this.branchingFactorLimit != that.branchingFactorLimit)
        return false;
    }

    boolean this_present_maxDepth = true && this.isSetMaxDepth();
    boolean that_present_maxDepth = true && that.isSetMaxDepth();
    if (this_present_maxDepth || that_present_maxDepth) {
      if (!(this_present_maxDepth && that_present_maxDepth))
        return false;
      if (this.maxDepth != that.maxDepth)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(FixedDepthParams other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    FixedDepthParams typedOther = (FixedDepthParams)other;

    lastComparison = Boolean.valueOf(isSetMinScore()).compareTo(typedOther.isSetMinScore());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMinScore()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.minScore, typedOther.minScore);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetBranchingFactorLimit()).compareTo(typedOther.isSetBranchingFactorLimit());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBranchingFactorLimit()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.branchingFactorLimit, typedOther.branchingFactorLimit);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMaxDepth()).compareTo(typedOther.isSetMaxDepth());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMaxDepth()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.maxDepth, typedOther.maxDepth);
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
    StringBuilder sb = new StringBuilder("FixedDepthParams(");
    boolean first = true;

    if (isSetMinScore()) {
      sb.append("minScore:");
      sb.append(this.minScore);
      first = false;
    }
    if (isSetBranchingFactorLimit()) {
      if (!first) sb.append(", ");
      sb.append("branchingFactorLimit:");
      sb.append(this.branchingFactorLimit);
      first = false;
    }
    if (isSetMaxDepth()) {
      if (!first) sb.append(", ");
      sb.append("maxDepth:");
      sb.append(this.maxDepth);
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

  private static class FixedDepthParamsStandardSchemeFactory implements SchemeFactory {
    public FixedDepthParamsStandardScheme getScheme() {
      return new FixedDepthParamsStandardScheme();
    }
  }

  private static class FixedDepthParamsStandardScheme extends StandardScheme<FixedDepthParams> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, FixedDepthParams struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // MIN_SCORE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.minScore = iprot.readI32();
              struct.setMinScoreIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // BRANCHING_FACTOR_LIMIT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.branchingFactorLimit = iprot.readI32();
              struct.setBranchingFactorLimitIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // MAX_DEPTH
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.maxDepth = iprot.readI32();
              struct.setMaxDepthIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, FixedDepthParams struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.isSetMinScore()) {
        oprot.writeFieldBegin(MIN_SCORE_FIELD_DESC);
        oprot.writeI32(struct.minScore);
        oprot.writeFieldEnd();
      }
      if (struct.isSetBranchingFactorLimit()) {
        oprot.writeFieldBegin(BRANCHING_FACTOR_LIMIT_FIELD_DESC);
        oprot.writeI32(struct.branchingFactorLimit);
        oprot.writeFieldEnd();
      }
      if (struct.isSetMaxDepth()) {
        oprot.writeFieldBegin(MAX_DEPTH_FIELD_DESC);
        oprot.writeI32(struct.maxDepth);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class FixedDepthParamsTupleSchemeFactory implements SchemeFactory {
    public FixedDepthParamsTupleScheme getScheme() {
      return new FixedDepthParamsTupleScheme();
    }
  }

  private static class FixedDepthParamsTupleScheme extends TupleScheme<FixedDepthParams> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, FixedDepthParams struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetMinScore()) {
        optionals.set(0);
      }
      if (struct.isSetBranchingFactorLimit()) {
        optionals.set(1);
      }
      if (struct.isSetMaxDepth()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetMinScore()) {
        oprot.writeI32(struct.minScore);
      }
      if (struct.isSetBranchingFactorLimit()) {
        oprot.writeI32(struct.branchingFactorLimit);
      }
      if (struct.isSetMaxDepth()) {
        oprot.writeI32(struct.maxDepth);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, FixedDepthParams struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.minScore = iprot.readI32();
        struct.setMinScoreIsSet(true);
      }
      if (incoming.get(1)) {
        struct.branchingFactorLimit = iprot.readI32();
        struct.setBranchingFactorLimitIsSet(true);
      }
      if (incoming.get(2)) {
        struct.maxDepth = iprot.readI32();
        struct.setMaxDepthIsSet(true);
      }
    }
  }

}

