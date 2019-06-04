/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import net.nutch.io.Writable;
import net.nutch.io.WritableComparable;

/** The default {@link Partitioner}.  Uses {@link Object#hashCode()} to
 * partition keys. */
public class DefaultPartitioner implements Partitioner {

  /** Use {@link Object#hashCode()} to partition. */
  public int getPartition(WritableComparable key, int numReduceTasks) {
    return (key.hashCode() & Integer.MAX_VALUE) % numReduceTasks;
  }

}
