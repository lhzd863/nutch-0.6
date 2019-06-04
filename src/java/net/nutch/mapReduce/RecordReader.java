/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;
import java.io.DataInput;

import net.nutch.io.Writable;

/** Reads key/value pairs from an input file {@link InputFormat.Split}.
 * Implemented by {@link InputFormat} implementations. */
public interface RecordReader {

  /** Constructs a key suitable to pass as the first parameter to {@link
   * #next(Writable,Writable)}. */
  Writable createKey();

  /** Constructs a value suitable to pass as the second parameter to {@link
   * #next(Writable,Writable)}. */
  Writable createValue();

  /** Reads the next key/value pair.
   *
   * @param key the key to read data into
   * @param value the value to read data into
   * @return true iff a key/value was read, false if at EOF
   *
   * @see Writable#readFields(DataInput)
   */      
  boolean next(Writable key, Writable value) throws IOException;
}
