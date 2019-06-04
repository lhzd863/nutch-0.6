/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;

import net.nutch.fs.NutchFileSystem;
import net.nutch.fs.NFSDataInputStream;

import net.nutch.io.Writable;
import net.nutch.io.LongWritable;
import net.nutch.io.UTF8;

/** An {@link InputFormat} for plain text files.  Files are broken into lines.
 * Either linefeed or carriage-return are used to signal end of line.  Keys are
 * the position in the file, and values are the line of text.. */
public class TextInputFormat implements InputFormat {

  private static final double SPLIT_SLOP = 0.1;          // 10% slop

  public Split[] getSplits(NutchFileSystem fs, File[] files, int numSplits)
    throws IOException {

    long totalSize = 0;
    for (int i = 0; i < files.length; i++) {
      totalSize += fs.getLength(files[i]);
    }

    long bytesPerSplit = totalSize / numSplits;
    long maxPerSplit = bytesPerSplit + (long)(bytesPerSplit*SPLIT_SLOP);

    ArrayList splits = new ArrayList(numSplits);
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      long length = fs.getLength(file);

      long bytesRemaining = length;
      while (bytesRemaining >= maxPerSplit) {
        splits.add(new FileSplit(fs, file, length-bytesRemaining,
                                 bytesPerSplit));
        bytesRemaining -= bytesPerSplit;
      }

      if (bytesRemaining != 0) {
        splits.add(new FileSplit(fs, file, length-bytesRemaining,
                                 bytesRemaining));
      }
    }
    return (Split[])splits.toArray();
  }

  public RecordReader getRecordReader(Split s) throws IOException {
    final FileSplit split = (FileSplit)s;

    final long start = split.getStart();
    final long end = start + split.getLength();

    // open the file and seek to the start of the split
    final NFSDataInputStream in =
      new NFSDataInputStream(split.getFileSystem().open(split.getFile()));
    in.seek(start);
    
    if (start != 0) {
      while (in.getPos() < end) {    // scan to the next newline in the file
        char c = (char)in.read();    // bug: this assumes eight-bit characters.
        if (c == '\r' || c == '\n') {
          break;
        }
      }
    }

    return new RecordReader() {

        /** Keys are longs. */
        public Writable createKey() { return new LongWritable(); }

        /** Values are lines. */
        public Writable createValue() { return new UTF8(); }

        /** Read a line. */
        public boolean next(Writable key, Writable value) throws IOException {
          long pos = in.getPos();
          if (pos >= end)
            return false;

          ((LongWritable)key).set(pos);           // key is position
          ((UTF8)value).set(readLine(in));        // value is line
          return true;
        }
        
      };
  }

  private static String readLine(NFSDataInputStream in) throws IOException {
    StringBuffer buffer = new StringBuffer();
    while (true) {

      int b = in.read();
      if (b == -1)
        break;

      char c = (char)b;              // bug: this assumes eight-bit characters.
      if (c == '\r' || c == '\n')
        break;

      buffer.append(c);
    }
    
    return buffer.toString();
  }

}

