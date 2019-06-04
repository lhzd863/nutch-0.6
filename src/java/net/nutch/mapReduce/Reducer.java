/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;

import java.util.Iterator;

import net.nutch.io.Writable;
import net.nutch.io.WritableComparable;

/** Reduces a set of intermediate values which share a key to a smaller set of
 * values.  Input values are the grouped output of a {@link Mapper}. */
public interface Reducer {
  /** Combines values for a given key.  Output values must be of the same type
   * as input values.  Input keys must not be altered.  Typically all values
   * are combined into zero or one value.  Output pairs are collected with
   * calls to {@link OutputCollector#collect(WritableComparable,Writable)}.
   *
   * @param key the key
   * @param values the values to combine
   * @param output to collect combined values
   */
  void reduce(WritableComparable key, Iterator values, OutputCollector output)
    throws IOException;
}
