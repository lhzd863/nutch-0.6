/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;
import java.io.File;

import net.nutch.fs.NutchFileSystem;

/** An {@link InputFormat.Split} implementation for sections of files. */
public class FileSplit implements InputFormat.Split {
  private NutchFileSystem fs;
  private File file;
  private long start;
  private long length;
  
  /** Constructs a split.
   *
   * @param fs the {@link NutchFileSystem} the file lives in
   * @param file the file name
   * @param start the position of the first byte in the file to process
   * @param length the number of bytes in the file to process
   */
  public FileSplit(NutchFileSystem fs, File file, long start, long length) {
    this.fs = fs;
    this.file = file;
    this.start = start;
    this.length = length;
  }
  
  /** The file system containing this split's data. */
  public NutchFileSystem getFileSystem() { return fs; }
  
  /** The file containing this split's data. */
  public File getFile() { return file; }
  
  /** The position of the first byte in the file to process. */
  public long getStart() { return start; }
  
  /** The number of bytes in the file to process. */
  public long getLength() { return length; }
}
