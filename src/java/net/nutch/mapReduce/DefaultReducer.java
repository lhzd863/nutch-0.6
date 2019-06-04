/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;

import java.util.Iterator;

import net.nutch.io.Writable;
import net.nutch.io.WritableComparable;

/** The default {@link Reducer}.  Performs no reduction, writing all input
 * values directly to the output. */
public class DefaultReducer implements Reducer {

  /** Writes all values directly to results. */
  public void reduce (WritableComparable key, Iterator values,
                      OutputCollector results) throws IOException {
    while (values.hasNext()) {
      results.collect(key, (Writable)values.next());
    }
  }

}
