/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.segment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import net.nutch.fetcher.FetcherOutput;
import net.nutch.io.ArrayFile;
import net.nutch.fs.*;
import net.nutch.parse.ParseData;
import net.nutch.parse.ParseText;
import net.nutch.protocol.Content;
import net.nutch.util.LogFormatter;

/**
 * This class holds together all data writers for a new segment.
 * Some convenience methods are also provided, to append to the segment.
 * 
 * @author Andrzej Bialecki &lt;ab@getopt.org&gt;
 */
public class SegmentWriter {
  public static final Logger LOG = LogFormatter.getLogger("net.nutch.segment.SegmentWriter");
  
  public ArrayFile.Writer fetcherWriter;
  public ArrayFile.Writer contentWriter;
  public ArrayFile.Writer parseTextWriter;
  public ArrayFile.Writer parseDataWriter;

  public long size = 0L;
  
  public File segmentDir;

  public SegmentWriter(File dir, boolean force) throws Exception {
    this(new LocalFileSystem(), dir, force, true, true, true, true);
  }
  
  public SegmentWriter(NutchFileSystem nfs, File dir, boolean force) throws Exception {
    this(nfs, dir, force, true, true, true, true);
  }
  
  public SegmentWriter(File dir, boolean force, boolean isParsed) throws Exception {
    this(new LocalFileSystem(), dir, force, isParsed, true, true, true);
  }
  
  public SegmentWriter(NutchFileSystem nfs, File dir, boolean force, boolean isParsed) throws Exception {
    this(nfs, dir, force, isParsed, true, true, true);
  }
  
  /**
   * Open a segment for writing. When a segment is open, its data files are created.
   * 
   * @param nfs NutchFileSystem to use
   * @param dir directory to contain the segment data
   * @param force if true, and segment directory already exists and its content
   *        is in the way, sliently overwrite that content as needed.
   *        If false and the above condition arises, throw an Exception. Note: this
   *        doesn't result in an Exception, if force=false, and the target directory
   *        already exists, but contains other data not conflicting with the segment
   *        data.
   * @param isParsed if true, create a segment with parseData and parseText; otherwise
   * create a segment without them, and with the fetcher output located in
   * {@link FetcherOutput#DIR_NAME_NP} directory.
   * @param withContent if true, write Content, otherwise ignore it
   * @param withParseText if true, write ParseText, otherwise ignore it. NOTE: if isParsed is
   * false, this will be automaticaly set to false, too.
   * @param withParseData if true, write ParseData, otherwise ignore it. NOTE: if isParsed is
   * false, this will be automaticaly set to false, too.
   * @throws Exception
   */
  public SegmentWriter(NutchFileSystem nfs, File dir, boolean force, boolean isParsed, 
          boolean withContent, boolean withParseText, boolean withParseData) throws Exception {
    segmentDir = dir;
    if (!nfs.exists(segmentDir)) {
      nfs.mkdirs(segmentDir);
    }
    File out = null;
    if (isParsed) {
      out = new File(segmentDir, FetcherOutput.DIR_NAME);
    } else {
      out = new File(segmentDir, FetcherOutput.DIR_NAME_NP);
      withParseData = false;
      withParseText = false;
    }
    if (nfs.exists(out) && !force) {
      throw new Exception("Output directory " + out + " already exists.");
    }
    fetcherWriter = new ArrayFile.Writer(nfs, out.toString(), FetcherOutput.class);
    if (withContent) {
      out = new File(dir, Content.DIR_NAME);
      if (nfs.exists(out) && !force) {
        throw new Exception("Output directory " + out + " already exists.");
      }
      contentWriter = new ArrayFile.Writer(nfs, out.toString(), Content.class);
    }
    if (withParseText) {
      out = new File(dir, ParseText.DIR_NAME);
      if (nfs.exists(out) && !force) {
        throw new Exception("Output directory " + out + " already exists.");
      }
      parseTextWriter = new ArrayFile.Writer(nfs, out.toString(), ParseText.class);
    }
    if (withParseData) {
      out = new File(dir, ParseData.DIR_NAME);
      if (nfs.exists(out) && !force) {
        throw new Exception("Output directory " + out + " already exists.");
      }
      parseDataWriter = new ArrayFile.Writer(nfs, out.toString(), ParseData.class);
    }
  }

  /** Create a new segment name */
  public static String getNewSegmentName() {
    return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
  }

  /** Sets the index interval for all segment writers. */
  public synchronized void setIndexInterval(int interval) throws IOException {
    fetcherWriter.setIndexInterval(interval);
    if (contentWriter != null) contentWriter.setIndexInterval(interval);
    if (parseTextWriter != null) parseTextWriter.setIndexInterval(interval);
    if (parseDataWriter != null) parseDataWriter.setIndexInterval(interval);
  }

  private Content _co = new Content();
  private ParseText _pt = new ParseText();
  private ParseData _pd = new ParseData();
  
  /**
   * Append new values to the output segment.
   * <p>NOTE: if this segment writer has some data files open, but the respective
   * arguments are null, empty values will be written instead.</p>
   * @param fo fetcher output, must not be null
   * @param co content, may be null (but see the note above)
   * @param pt parseText, may be null (but see the note above)
   * @param pd parseData, may be null (but see the note above)
   * @throws IOException
   */
  public synchronized void append(FetcherOutput fo, Content co, ParseText pt, ParseData pd) throws IOException {
    fetcherWriter.append(fo);
    if (contentWriter != null) {
      if (co == null) co = _co;
      contentWriter.append(co);
    }
    if (parseTextWriter != null) {
      if (pt == null) pt = _pt;
      parseTextWriter.append(pt);
    }
    if (parseDataWriter != null) {
      if (pd == null) pd = _pd;
      parseDataWriter.append(pd);
    }
    size++;
  }
  
  /** Close all writers. */
  public void close() {
    try {
      fetcherWriter.close();
    } catch (Exception e) {
      LOG.fine("Exception closing fetcherWriter: " + e.getMessage());
    }
    if (contentWriter != null) try {
      contentWriter.close();
    } catch (Exception e) {
      LOG.fine("Exception closing contentWriter: " + e.getMessage());
    }
    if (parseTextWriter != null) try {
      parseTextWriter.close();
    } catch (Exception e) {
      LOG.fine("Exception closing parseTextWriter: " + e.getMessage());
    }
    if (parseDataWriter != null) try {
      parseDataWriter.close();
    } catch (Exception e) {
      LOG.fine("Exception closing parseDataWriter: " + e.getMessage());
    }
  }

  public static void main(String[] args) {}
}
