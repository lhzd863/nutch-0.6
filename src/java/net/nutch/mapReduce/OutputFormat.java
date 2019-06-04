/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;
import java.io.File;

import net.nutch.fs.NutchFileSystem;

/** An output data format.  Output files are stored in a {@link
 * NutchFileSystem}. */
public interface OutputFormat {
  /** Construct a {@link RecordWriter}.
   *
   * @param file the file name to write
   * @return a {@link RecordWriter}
   */
  RecordWriter getRecordWriter(File file) throws IOException;
}

