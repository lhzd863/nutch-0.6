/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;

import net.nutch.io.Writable;
import net.nutch.io.WritableComparable;

/** Maps input key/value pairs to a set of intermediate key/value pairs.  All
 * intermediate values associated with a given output key are subsequently
 * grouped by the map/reduce system, and passed to a {@link Reducer} to
 * determine the final output.. */
public interface Mapper {
  /** Maps a single input key/value pair into intermediate key/value pairs.
   * Output pairs need not be of the same types as input pairs.  A given input
   * pair may map to zero or many output pairs.  Output pairs are collected
   * with calls to {@link
   * OutputCollector#collect(WritableComparable,Writable)}.
   *
   * @param key the key
   * @param value the values
   * @param output collects mapped keys and values
   */
  void map(WritableComparable key, Writable value, OutputCollector output)
    throws IOException;
}
