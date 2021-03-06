/* Copyright (c) 2003-2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import net.nutch.fs.*;
import net.nutch.util.*;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriter;

/*************************************************************************
 * IndexMerger creates an index for the output corresponding to a 
 * single fetcher run.
 * 
 * @author Doug Cutting
 * @author Mike Cafarella
 *************************************************************************/
public class IndexMerger {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.indexer.IndexMerger");

  public static final String DONE_NAME = "merge.done";

  private NutchFileSystem nfs;
  private File outputIndex;
  private File localWorkingDir;
  private File[] segments;

  /**
   * Merge all of the segments given
   */
  public IndexMerger(NutchFileSystem nfs, File[] segments, File outputIndex, File localWorkingDir) throws IOException {
      this.nfs = nfs;
      this.segments = segments;
      this.outputIndex = outputIndex;
      this.localWorkingDir = localWorkingDir;
  }

  /**
   * Load all input segment indices, then add to the single output index
   */
  private void merge() throws IOException {
    //
    // Open local copies of NFS indices
    //
    Directory[] dirs = new Directory[segments.length];
    File[] localSegments = new File[segments.length];
    for (int i = 0; i < segments.length; i++) {
        File tmpFile = new File(localWorkingDir, "indexmerge-" + new SimpleDateFormat("yyyMMddHHmmss").format(new Date(System.currentTimeMillis())));
        localSegments[i] = nfs.startLocalInput(new File(segments[i], "index"), tmpFile);
        dirs[i] = FSDirectory.getDirectory(localSegments[i], false);
    }

    //
    // Get local output target
    //
    File tmpLocalOutput = new File(localWorkingDir, "merge-output");
    File localOutput = nfs.startLocalOutput(outputIndex, tmpLocalOutput);

    //
    // Merge indices
    //
    IndexWriter writer = new IndexWriter(localOutput, null, true);
    writer.mergeFactor = 50;
    writer.infoStream = LogFormatter.getLogStream(LOG, Level.INFO);
    writer.setUseCompoundFile(false);
    writer.setSimilarity(new NutchSimilarity());
    writer.addIndexes(dirs);
    writer.close();

    //
    // Put target back
    //
    nfs.completeLocalOutput(outputIndex, tmpLocalOutput);

    //
    // Delete all local inputs, if necessary
    //
    for (int i = 0; i < localSegments.length; i++) {
        nfs.completeLocalInput(localSegments[i]);
    }
    localWorkingDir.delete();
  }

  /** 
   * Create an index for the input files in the named directory. 
   */
  public static void main(String[] args) throws Exception {
    String usage = "IndexMerger (-local | -ndfs <nameserver:port>) [-workingdir <workingdir>] outputIndex segments...";
    if (args.length < 2) {
      System.err.println("Usage: " + usage);
      return;
    }

    //
    // Parse args, read all segment directories to be processed
    //
    NutchFileSystem nfs = NutchFileSystem.parseArgs(args, 0);
    try {
        File workingDir = new File(new File("").getCanonicalPath());
        Vector segments = new Vector();

        int i = 0;
        if ("-workingdir".equals(args[i])) {
            i++;
            workingDir = new File(new File(args[i++]).getCanonicalPath());
        }
        File outputIndex = new File(args[i++]);

        for (; i < args.length; i++) {
            if (args[i] != null) {
                segments.add(new File(args[i]));
            }
        }
        workingDir = new File(workingDir, "indexmerger-workingdir");

        //
        // Merge the indices
        //
        File[] segmentFiles = (File[]) segments.toArray(new File[segments.size()]);
        LOG.info("merging segment indexes to: " + outputIndex);

        if (workingDir.exists()) {
            FileUtil.fullyDelete(workingDir);
        }
        workingDir.mkdirs();
        IndexMerger merger = new IndexMerger(nfs, segmentFiles, outputIndex, workingDir);
        merger.merge();
        LOG.info("done merging");
        FileUtil.fullyDelete(workingDir);
    } finally {
        nfs.close();
    }
  }
}
