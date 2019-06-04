/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;

import net.nutch.io.Writable;
import net.nutch.io.WritableComparable;

/** The default {@link Mapper}.  Implements the identity function, mapping
 * inputs directly to outputs. */
public class DefaultMapper implements Mapper {

  /** The identify function.  Input key/value pair is written directly to
   * output.*/
  public void map(WritableComparable key, Writable val,
                  OutputCollector results) throws IOException {
    results.collect(key, val);
  }

}
