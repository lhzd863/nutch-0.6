/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;
import java.io.DataOutput;

import net.nutch.io.Writable;

/** Writes key/value pairs to an output file.  Implemented by {@link
 * OutputFormat} implementations. */
public interface RecordWriter {
  /** Writes a key/value pair.
   *
   * @param key the key to write
   * @param value the value to write
   *
   * @see Writable#write(DataOutput)
   */      
  public boolean next(Writable key, Writable value) throws IOException;
}
