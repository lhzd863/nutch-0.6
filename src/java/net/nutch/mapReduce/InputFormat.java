/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;
import java.io.File;

import net.nutch.fs.NutchFileSystem;

/** An input data format.  Input files are stored in a {@link NutchFileSystem}.
 * The processing of an input file may be split across multiple machines.
 * Files are processed as sequences of records, implementing {@link
 * RecordReader}.  Files must thus be split on record boundaries. */
public interface InputFormat {

  /** A section of an input file.  Returned by {@link
   * InputFormat#getSplits(File[], int)} and passed to
   * InputFormat#getRecordReader(Split). */
  public interface Split {}

  /** Splits a set of input files.  One split is created per map task.
   *
   * @param fs the filesystem containing the files to be split
   * @param files the input files to split
   * @param numSplits the desired number of splits
   * @return the splits
   */
  Split[] getSplits(NutchFileSystem fs, File[] files, int numSplits)
    throws IOException;

  /** Construct a {@link RecordReader} for a {@link Split}.
   *
   * @param split the split
   * @return a {@link RecordReader}
   */
  RecordReader getRecordReader(Split split) throws IOException;
}

