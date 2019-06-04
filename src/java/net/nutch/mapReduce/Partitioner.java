/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import net.nutch.io.Writable;
import net.nutch.io.WritableComparable;

/** Partitions the key space.  A partition is created for each reduce task. */
public interface Partitioner {
  /** Returns the paritition number for a given key given the total number of
   * partitions.  Typically a hash function on a all or a subset of the key.
   *
   * @param key the key
   * @param numPartitions the number of partitions
   * @return the partition number
   */
  int getPartition(WritableComparable key, int numPartitions);
}
