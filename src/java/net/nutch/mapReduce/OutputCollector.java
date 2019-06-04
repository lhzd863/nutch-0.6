/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;
import java.io.DataInput;

import net.nutch.io.Writable;
import net.nutch.io.WritableComparable;


/** Passed to {@link Mapper} and {@link Reducer} implementations to collect
 * output data. */
public interface OutputCollector {
  /** Adds a key/value pair to the output.
   *
   * @param key the key to add
   * @param value to value to add
   */
  void collect(WritableComparable key, Writable value) throws IOException;
}
