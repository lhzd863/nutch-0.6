/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.segment;

import java.io.File;
import java.io.FileFilter;
import java.util.Vector;
import java.util.logging.Logger;

import net.nutch.fs.*;
import net.nutch.fetcher.FetcherOutput;
import net.nutch.parse.ParseData;
import net.nutch.parse.ParseText;
import net.nutch.protocol.Content;
import net.nutch.util.LogFormatter;

/**
 * This class reads data from one or more input segments, and outputs it to one
 * or more output segments, optionally deleting the input segments when it's
 * finished.
 * 
 * <p>Data is read sequentially from input segments, and appended to output
 * segment until it reaches the target count of entries, at which point the next
 * output segment is created, and so on.</p>
 * <p>NOTE 1: this tool does NOT de-duplicate data - use SegmentMergeTool for that.</p>
 * <p>NOTE 2: this tool does NOT copy indexes. It is currently impossible to slice
 * Lucene indexes. The proper procedure is first to create slices, and then to index
 * them.</p>
 * <p>NOTE 3: if one or more input segments are in non-parsed format, the output
 * segments will also use non-parsed format. This means that any parseData and parseText
 * data from input segments will NOT be copied to the output segments.</p>
 * @author Andrzej Bialecki &lt;ab@getopt.org&gt;
 */
public class SegmentSlicer implements Runnable {
  public static final Logger LOG = LogFormatter.getLogger("net.nutch.segment.SegmentSlicer");
  public static int LOG_STEP = 20000;
  
  private NutchFileSystem nfs = null;
  private File[] input = null;
  private File output = null;
  private boolean withContent = true;
  private boolean withParseData = true;
  private boolean withParseText = true;
  private boolean autoFix = false;
  private long maxCount = Long.MAX_VALUE;
  
  /**
   * Create new SegmentSlicer.
   * @param nfs filesystem
   * @param input list of input segments
   * @param output output directory, created if not exists. Output segments
   * will be created inside this directory
   * @param withContent if true, read content, otherwise ignore it
   * @param withParseText if true, read parse_text, otherwise ignore it
   * @param withParseData if true, read parse_data, otherwise ignore it
   * @param autoFix if true, attempt to fix corrupt segments
   * @param maxCount if greater than 0, determines the maximum number of entries
   * per output segment. New multiple output segments will be created as needed.
   */
  public SegmentSlicer(NutchFileSystem nfs, File[] input, File output,
          boolean withContent, boolean withParseText, boolean withParseData,
          boolean autoFix, long maxCount) {
    this.nfs = nfs;
    this.input = input;
    this.output = output;
    this.withContent = withContent;
    this.withParseData = withParseData;
    this.withParseText = withParseText;
    this.autoFix = autoFix;
    if (maxCount > 0) this.maxCount = maxCount;
  }

  /** Run the slicer. */
  public void run() {
    long start = System.currentTimeMillis();
    Vector readers = new Vector();
    long total = 0L;
    boolean parsed = true;
    for (int i = 0; i < input.length; i++) {
      SegmentReader sr = null;
      try {
        sr = new SegmentReader(nfs, input[i], withContent, withParseText, withParseData, autoFix);
      } catch (Exception e) {
        LOG.warning(e.getMessage());
        continue;
      }
      total += sr.size;
      parsed = parsed && sr.isParsed;
      readers.add(sr);
    }
    LOG.info("Input: " + total + " entries in " + readers.size() + " segments.");
    if (!parsed)
      LOG.warning(" - some input segments are non-parsed, forcing non-parsed output!");
    FetcherOutput fo = new FetcherOutput();
    Content co = new Content();
    ParseData pd = new ParseData();
    ParseText pt = new ParseText();
    long outputCnt = 0L;
    int segCnt = 1;
    File outDir = new File(output, SegmentWriter.getNewSegmentName());
    LOG.info("Writing output in " + output);
    try {
      LOG.info(" - starting first output segment in " + outDir.getName());
      SegmentWriter sw = new SegmentWriter(nfs,
            outDir, true, parsed, withContent, withParseText, withParseData);
      long delta = System.currentTimeMillis();
      for (int i = 0; i < readers.size(); i++) {
        SegmentReader sr = (SegmentReader)readers.get(i);
        for (long k = 0L; k < sr.size; k++) {
          try {
            if (!sr.next(fo, co, pt, pd)) break;
          } catch (Throwable t) {
            LOG.warning(" - error reading entry #" + k + " from " + sr.segmentDir.getName());
            break;
          }
          sw.append(fo, co, pt, pd);
          outputCnt++;
          if (outputCnt % LOG_STEP == 0) {
            LOG.info(" Processed " + outputCnt + " entries (" +
                    (float)LOG_STEP / (float)(System.currentTimeMillis() - delta) * 1000.0f + " rec/s)");
            delta = System.currentTimeMillis();
          }
          if (outputCnt % maxCount == 0) {
            sw.close();
            outDir = new File(output, SegmentWriter.getNewSegmentName());
            segCnt++;
            LOG.info(" - starting next output segment in " + outDir.getName());
            sw = new SegmentWriter(nfs, outDir,
                    true, parsed, withContent, withParseText, withParseData);
          }
        }
        sr.close();
      }
      sw.close();
      delta = System.currentTimeMillis() - start;
      float eps = (float) outputCnt / (float) (delta / 1000);
      LOG.info("DONE segment slicing, INPUT: " + total + " -> OUTPUT: " + outputCnt + " entries in "
              + segCnt + " segment(s), " + ((float) delta / 1000f) + " s (" + eps + " entries/sec).");
    } catch (Throwable t) {
      t.printStackTrace();
      LOG.info("Unexpected error " + t.getMessage() + ", aborting at " + outputCnt + " output entries.");
    }
  }
  
  /** Command-line wrapper. Run without arguments to see usage help. */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      usage();
      return;
    }
    String segDir = null;
    String outDir = null;
    Vector dirs = new Vector();
    boolean fix = false;
    long maxCount = Long.MAX_VALUE;
    boolean withParseText = true;
    boolean withParseData = true;
    boolean withContent = true;
    NutchFileSystem nfs = NutchFileSystem.parseArgs(args, 0);
    for (int i = 0; i < args.length; i++) {
      if (args[i] != null) {
        if (args[i].equals("-noparsetext")) withParseText = false;
        else if (args[i].equals("-noparsedata")) withParseData = false;
        else if (args[i].equals("-nocontent")) withContent = false;
        else if (args[i].equals("-fix")) fix = true;
        else if (args[i].equals("-dir")) segDir = args[++i];
        else if (args[i].equals("-o")) outDir = args[++i];
        else if (args[i].equals("-max")) {
          String cnt = args[++i];
          try {
            maxCount = Long.parseLong(cnt);
          } catch (Exception e) {
            LOG.warning("Invalid count '" + cnt + "', setting to Long.MAX_VALUE.");
          }
        } else dirs.add(new File(args[i]));
      }
    }
    if (outDir == null) {
      LOG.severe("Missing output path.");
      usage();
      return;
    }
    if (segDir != null) {
      File sDir = new File(segDir);
      if (!sDir.exists() || !sDir.isDirectory()) {
        LOG.warning("Invalid path: " + sDir);
      } else {
        File[] files = sDir.listFiles(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory();
          }
        });
        if (files != null && files.length > 0) {
          for (int i = 0; i < files.length; i++) dirs.add(files[i]);
        }
      }
    }
    if (dirs.size() == 0) {
      LOG.severe("No input segment dirs.");
      usage();
      return;
    }
    File[] input = (File[])dirs.toArray(new File[0]);
    File output = new File(outDir);
    SegmentSlicer slicer = new SegmentSlicer(nfs, input, output,
            withContent, withParseText, withParseData, fix, maxCount);
    slicer.run();
  }

  private static void usage() {
    System.err.println("SegmentSlicer (-local | -ndfs <namenode:port>) -o outputDir [-max count] [-fix] [-nocontent] [-noparsedata] [-noparsetext] (-dir segments | seg1 seg2 ...)");
    System.err.println("\tNOTE: at least one segment dir name is required, or '-dir' option.");
    System.err.println("\t      outputDir is always required.");
    System.err.println("\t-o outputDir\toutput directory for segments");
    System.err.println("\t-max count\t(optional) output multiple segments, each with maximum 'count' entries");
    System.err.println("\t-fix\t\t(optional) automatically fix corrupted segments");
    System.err.println("\t-nocontent\t(optional) ignore content data");
    System.err.println("\t-noparsedata\t(optional) ignore parse_data data");
    System.err.println("\t-nocontent\t(optional) ignore parse_text data");
    System.err.println("\t-dir segments\tdirectory containing multiple segments");
    System.err.println("\tseg1 seg2 ...\tsegment directories\n");
  }
}
