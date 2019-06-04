/* Copyright (c) 2003-2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.nio.channels.*;
import org.apache.lucene.util.PriorityQueue;
import net.nutch.fs.*;
import net.nutch.util.*;

/** Support for flat files of binary key/value pairs. */
public class SequenceFile {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.io.SequenceFile");

  private SequenceFile() {}                         // no public ctor

  private static byte[] VERSION = new byte[] {
    (byte)'S', (byte)'E', (byte)'Q', 1
  };

  /** Write key/value pairs to a sequence-format file. */
  public static class Writer {
    private NFSDataOutputStream out;
    private DataOutputBuffer buffer = new DataOutputBuffer();
    private NutchFileSystem nfs = null;
    private File target = null;

    private Class keyClass;
    private Class valClass;

    /** Create the named file. */
    public Writer(NutchFileSystem nfs, String name,
                  Class keyClass, Class valClass)
      throws IOException {
      this.nfs = nfs;
      this.target = new File(name);
      if (nfs.exists(target)) {
        throw new IOException("already exists: " + target);
      }
      init(new NFSDataOutputStream(nfs.create(target)),
           keyClass, valClass);
    }
    
    /** Write to an arbitrary stream using a specified buffer size. */
    private Writer(NFSDataOutputStream out,
                   Class keyClass, Class valClass) throws IOException {
      init(out, keyClass, valClass);
    }
    
    private void init(NFSDataOutputStream out,
                      Class keyClass, Class valClass) throws IOException {
      this.out = out;
      this.out.write(VERSION);

      this.keyClass = keyClass;
      this.valClass = valClass;

      new UTF8(WritableName.getName(keyClass)).write(this.out);
      new UTF8(WritableName.getName(valClass)).write(this.out);

      this.out.flush();                           // flush header
    }
    

    /** Returns the class of keys in this file. */
    public Class getKeyClass() { return keyClass; }

    /** Returns the class of values in this file. */
    public Class getValueClass() { return valClass; }


    /** Close the file. */
    public void close() throws IOException {
      if (out != null) {
        out.close();
        out = null;
      }
    }

    /** Append a key/value pair. */
    public void append(Writable key, Writable val) throws IOException {
      if (key.getClass() != keyClass)
        throw new IOException("wrong key class: "+key+" is not "+keyClass);
      if (val.getClass() != valClass)
        throw new IOException("wrong value class: "+val+" is not "+valClass);

      buffer.reset();

      key.write(buffer);
      int keyLength = buffer.getLength();
      if (keyLength == 0)
        throw new IOException("zero length keys not allowed: " + key);

      val.write(buffer);
      append(buffer.getData(), 0, buffer.getLength(), keyLength);
    }

    /** Append a key/value pair. */
    public void append(byte[] data, int start, int length, int keyLength)
      throws IOException {
      if (keyLength == 0)
        throw new IOException("zero length keys not allowed");

      out.writeInt(length);                       // total record length
      out.writeInt(keyLength);                    // key portion length
      out.write(data, start, length);             // data
    }

    /** Returns the current length of the output file. */
    public long getLength() throws IOException {
      return out.getPos();
    }

  }

  /** Writes key/value pairs from a sequence-format file. */
  public static class Reader {
    private String file;
    private NFSDataInputStream in;
    private DataOutputBuffer outBuf = new DataOutputBuffer();
    private DataInputBuffer inBuf = new DataInputBuffer();
    private NutchFileSystem nfs = null;

    private Class keyClass;
    private Class valClass;

    private long end;
    private int keyLength;

    /** Open the named file. */
    public Reader(NutchFileSystem nfs, String file) throws IOException {
      this(nfs, file, NutchConf.getInt("io.file.buffer.size", 4096));
    }

    private Reader(NutchFileSystem nfs, String name, int bufferSize) throws IOException {
      this.nfs = nfs;
      this.file = name;
      File file = new File(name);
      this.in = new NFSDataInputStream(nfs.open(file), bufferSize);
      this.end = nfs.getLength(file);
      init();
    }
    
    private Reader(NutchFileSystem nfs, String file, int bufferSize, long start, long length)
      throws IOException {
      this.nfs = nfs;
      this.file = file;
      this.in = new NFSDataInputStream(nfs.open(new File(file)), bufferSize);
      seek(start);
      init();

      this.end = in.getPos() + length;
    }
    
    private void init() throws IOException {
      byte[] version = new byte[VERSION.length];
      in.readFully(version);
      if (!Arrays.equals(version, VERSION)) {
        throw new VersionMismatchException(VERSION[3], version[3]);
      }

      UTF8 className = new UTF8();
      
      className.readFields(in);                   // read key class name
      this.keyClass = WritableName.getClass(className.toString());
      
      className.readFields(in);                   // read val class name
      this.valClass = WritableName.getClass(className.toString());
    }
    
    /** Close the file. */
    public synchronized void close() throws IOException {
      in.close();
    }

    /** Returns the class of keys in this file. */
    public Class getKeyClass() { return keyClass; }

    /** Returns the class of values in this file. */
    public Class getValueClass() { return valClass; }

    /** Read the next key in the file into <code>key</code>, skipping its
     * value.  True if another entry exists, and false at end of file. */
    public synchronized boolean next(Writable key) throws IOException {
      if (key.getClass() != keyClass)
        throw new IOException("wrong key class: "+key+" is not "+keyClass);

      outBuf.reset();

      keyLength = next(outBuf);
      if (keyLength < 0)
        return false;

      inBuf.reset(outBuf.getData(), outBuf.getLength());

      key.readFields(inBuf);
      if (inBuf.getPosition() != keyLength)
        throw new IOException(key + " read " + inBuf.getPosition()
                              + " bytes, should read " + keyLength);

      return true;
    }

    /** Read the next key/value pair in the file into <code>key</code> and
     * <code>val</code>.  Returns true if such a pair exists and false when at
     * end of file */
    public synchronized boolean next(Writable key, Writable val)
      throws IOException {
      if (val.getClass() != valClass)
        throw new IOException("wrong value class: "+val+" is not "+valClass);

      boolean more = next(key);

      if (more) {
        val.readFields(inBuf);
        if (inBuf.getPosition() != outBuf.getLength())
          throw new IOException(val+" read "+(inBuf.getPosition()-keyLength)
                                + " bytes, should read " +
                                (outBuf.getLength()-keyLength));
      }

      return more;
    }

    /** Read the next key/value pair in the file into <code>buffer</code>.
     * Returns the length of the key read, or -1 if at end of file.  The length
     * of the value may be computed by calling buffer.getLength() before and
     * after calls to this method. */
    public synchronized int next(DataOutputBuffer buffer) throws IOException {
      if (in.getPos() >= end)
        return -1;

      int length = in.readInt();
      int keyLength = in.readInt();
      buffer.write(in, length);
      return keyLength;
    }

    /** Set the current byte position in the input file. */
    public synchronized void seek(long position) throws IOException {
      in.seek(position);
    }

    /** Return the current byte position in the input file. */
    public synchronized long getPosition() throws IOException {
      return in.getPos();
    }

    /** Returns the name of the file. */
    public String toString() {
      return file;
    }

  }

  /** Sorts key/value pairs in a sequence-format file.
   *
   * <p>For best performance, applications should make sure that the {@link
   * Writable#readFields(DataInput)} implementation of their keys is
   * very efficient.  In particular, it should avoid allocating memory.
   */
  public static class Sorter {
    private static final int FACTOR = NutchConf.getInt("io.sort.factor", 100); 
    private static final int MEGABYTES = NutchConf.getInt("io.sort.mb", 100); 

    private WritableComparator comparator;

    private String inFile;                        // when sorting
    private String[] inFiles;                     // when merging

    private String outFile;

    private int memory = MEGABYTES * 1024*1024;   // bytes
    private int factor = FACTOR;                  // merged per pass

    private NutchFileSystem nfs = null;

    private Class keyClass;
    private Class valClass;

    /** Sort and merge files containing the named classes. */
    public Sorter(NutchFileSystem nfs, Class keyClass, Class valClass)  {
      this(nfs, new WritableComparator(keyClass), valClass);
    }

    /** Sort and merge using an arbitrary {@link WritableComparator}. */
    public Sorter(NutchFileSystem nfs, WritableComparator comparator, Class valClass) {
      this.nfs = nfs;
      this.comparator = comparator;
      this.keyClass = comparator.getKeyClass();
      this.valClass = valClass;
    }

    /** Set the number of streams to merge at once.*/
    public void setFactor(int factor) { this.factor = factor; }

    /** Get the number of streams to merge at once.*/
    public int getFactor() { return factor; }

    /** Set the total amount of buffer memory, in bytes.*/
    public void setMemory(int memory) { this.memory = memory; }

    /** Get the total amount of buffer memory, in bytes.*/
    public int getMemory() { return memory; }

    /** Perform a file sort.*/
    public void sort(String inFile, String outFile) throws IOException {
      if (nfs.exists(new File(outFile))) {
        throw new IOException("already exists: " + outFile);
      }

      this.inFile = inFile;
      this.outFile = outFile;

      int segments = sortPass();
      int pass = 1;
      while (segments > 1) {
        segments = mergePass(pass, segments <= factor);
        pass++;
      }
    }

    private int sortPass() throws IOException {
      LOG.fine("running sort pass");
      SortPass sortPass = new SortPass();         // make the SortPass
      try {
        return sortPass.run();                    // run it
      } finally {
        sortPass.close();                         // close it
      }
    }

    private class SortPass {
      private int limit = memory/4;
      private DataOutputBuffer buffer = new DataOutputBuffer();
      private byte[] rawBuffer;

      private int[] starts = new int[1024];
      private int[] pointers = new int[starts.length];
      private int[] pointersCopy = new int[starts.length];
      private int[] keyLengths = new int[starts.length];
      private int[] lengths = new int[starts.length];
      
      private Reader in;
      private NFSDataOutputStream out;
        private String outName;

      public SortPass() throws IOException {
        in = new Reader(nfs, inFile);
      }
      
      public int run() throws IOException {
        int segments = 0;
        boolean atEof = false;
        while (!atEof) {
          int count = 0;
          buffer.reset();
          while (!atEof && buffer.getLength() < limit) {

            int start = buffer.getLength();       // read an entry into buffer
            int keyLength = in.next(buffer);
            int length = buffer.getLength() - start;

            if (keyLength == -1) {
              atEof = true;
              break;
            }

            if (count == starts.length)
              grow();

            starts[count] = start;                // update pointers
            pointers[count] = count;
            lengths[count] = length;
            keyLengths[count] = keyLength;

            count++;
          }

          // buffer is full -- sort & flush it
          LOG.finer("flushing segment " + segments);
          rawBuffer = buffer.getData();
          sort(count);
          flush(count, segments==0 && atEof);
          segments++;
        }
        return segments;
      }

      public void close() throws IOException {
        in.close();

        if (out != null) {
          out.close();
        }
      }

      private void grow() {
        int newLength = starts.length * 3 / 2;
        starts = grow(starts, newLength);
        pointers = grow(pointers, newLength);
        pointersCopy = new int[newLength];
        keyLengths = grow(keyLengths, newLength);
        lengths = grow(lengths, newLength);
      }

      private int[] grow(int[] old, int newLength) {
        int[] result = new int[newLength];
        System.arraycopy(old, 0, result, 0, old.length);
        return result;
      }

      private void flush(int count, boolean done) throws IOException {
        if (out == null) {
          outName = done ? outFile : outFile+".0";
          out = new NFSDataOutputStream(nfs.create(new File(outName)));
        }

        if (!done) {                              // an intermediate file
          long length = buffer.getLength() + count*8;
          out.writeLong(length);                  // write size
        }

        Writer writer = new Writer(out, keyClass, valClass);

        for (int i = 0; i < count; i++) {         // write in sorted order
          int p = pointers[i];
          writer.append(rawBuffer, starts[p], lengths[p], keyLengths[p]);
        }
      }

      private void sort(int count) {
        System.arraycopy(pointers, 0, pointersCopy, 0, count);
        mergeSort(pointersCopy, pointers, 0, count);
      }

      private int compare(int i, int j) {
        return comparator.compare(rawBuffer, starts[i], keyLengths[i],
                                  rawBuffer, starts[j], keyLengths[j]);
      }

      private void mergeSort(int src[], int dest[], int low, int high) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7) {
          for (int i=low; i<high; i++)
            for (int j=i; j>low && compare(dest[j-1], dest[j])>0; j--)
              swap(dest, j, j-1);
          return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high) >> 1;
        mergeSort(dest, src, low, mid);
        mergeSort(dest, src, mid, high);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (compare(src[mid-1], src[mid]) <= 0) {
          System.arraycopy(src, low, dest, low, length);
          return;
        }

        // Merge sorted halves (now in src) into dest
        for(int i = low, p = low, q = mid; i < high; i++) {
          if (q>=high || p<mid && compare(src[p], src[q]) <= 0)
            dest[i] = src[p++];
          else
            dest[i] = src[q++];
        }
      }

      private void swap(int x[], int a, int b) {
	int t = x[a];
	x[a] = x[b];
	x[b] = t;
      }
    }

    private int mergePass(int pass, boolean last) throws IOException {
      LOG.fine("running merge pass=" + pass);
      MergePass mergePass = new MergePass(pass, last);
      try {                                       // make a merge pass
        return mergePass.run();                  // run it
      } finally {
        mergePass.close();                       // close it
      }
    }

    private class MergePass {
      private int pass;
      private boolean last;

      private MergeQueue queue;
      private NFSDataInputStream in;
      private String inName;

      public MergePass(int pass, boolean last) throws IOException {
        this.pass = pass;
        this.last = last;

        this.queue = new MergeQueue(factor, last ? outFile : outFile+"."+pass);

        this.inName = outFile+"."+(pass-1);
        this.in = new NFSDataInputStream(nfs.open(new File(inName)));
      }

      public void close() throws IOException {
        in.close();                               // close and delete input
        nfs.delete(new File(inName));

        queue.close();                            // close queue
      }

      public int run() throws IOException {
        int segments = 0;
        long end = nfs.getLength(new File(inName));

        while (in.getPos() < end) {
          LOG.finer("merging segment " + segments);
          long totalLength = 0;
          while (in.getPos() < end && queue.size() < factor) {
            long length = in.readLong();
            totalLength += length;
            Reader reader = new Reader(nfs, inName, memory/(factor+1),
                                       in.getPos(), length);
            MergeStream ms = new MergeStream(reader); // add segment to queue
            if (ms.next()) {
              queue.put(ms);
            }
            in.seek(reader.end);
          }

          if (!last)                              // intermediate file
            queue.out.writeLong(totalLength);     // write sizes

          queue.merge();                          // do a merge

          segments++;
        }

        return segments;
      }
    }

    /** Merge the provided files.*/
    public void merge(String[] inFiles, String outFile) throws IOException {
      this.inFiles = inFiles;
      this.outFile = outFile;
      this.factor = inFiles.length;

      if (new File(outFile).exists()) {
        throw new IOException("already exists: " + outFile);
      }

      MergeFiles mergeFiles = new MergeFiles();
      try {                                       // make a merge pass
        mergeFiles.run();                         // run it
      } finally {
        mergeFiles.close();                       // close it
      }

    }

    private class MergeFiles {
      private MergeQueue queue;

      public MergeFiles() throws IOException {
        this.queue = new MergeQueue(factor, outFile);
      }

      public void close() throws IOException {
        queue.close();
      }

      public void run() throws IOException {
        LOG.finer("merging files=" + inFiles.length);
        for (int i = 0; i < inFiles.length; i++) {
          String inFile = inFiles[i];
          MergeStream ms =
            new MergeStream(new Reader(nfs, inFile, memory/(factor+1)));
          if (ms.next())
            queue.put(ms);
        }

        queue.merge();
      }
    }

    private class MergeStream {
      private Reader in;

      private DataOutputBuffer buffer = new DataOutputBuffer();
      private int keyLength;
      
      public MergeStream(Reader reader) throws IOException {
        if (reader.keyClass != keyClass)
          throw new IOException("wrong key class: " + reader.getKeyClass() +
                                " is not " + keyClass);
        if (reader.valClass != valClass)
          throw new IOException("wrong value class: "+reader.getValueClass()+
                                " is not " + valClass);
        this.in = reader;
      }

      public boolean next() throws IOException {
        buffer.reset();
        keyLength = in.next(buffer);
        return keyLength >= 0;
      }
    }

    private class MergeQueue extends PriorityQueue {
      private NFSDataOutputStream out;

      public MergeQueue(int size, String outName) throws IOException {
        initialize(size);
        this.out =
          new NFSDataOutputStream(nfs.create(new File(outName)),
                                  memory/(factor+1));
      }

      protected boolean lessThan(Object a, Object b) {
        MergeStream msa = (MergeStream)a;
        MergeStream msb = (MergeStream)b;
        return comparator.compare(msa.buffer.getData(), 0, msa.keyLength,
                                  msb.buffer.getData(), 0, msb.keyLength) < 0;
      }

      public void merge() throws IOException {
        Writer writer = new Writer(out, keyClass, valClass);

        while (size() != 0) {
          MergeStream ms = (MergeStream)top();
          DataOutputBuffer buffer = ms.buffer;    // write top entry
          writer.append(buffer.getData(), 0, buffer.getLength(), ms.keyLength);
          
          if (ms.next()) {                        // has another entry
            adjustTop();
          } else {
            pop();                                // done with this file
            ms.in.close();
          }
        }
      }

      public void close() throws IOException {
        MergeStream ms;                           // close inputs
        while ((ms = (MergeStream)pop()) != null) {
          ms.in.close();
        }
        out.close();                              // close output
      }
    }
  }

}
