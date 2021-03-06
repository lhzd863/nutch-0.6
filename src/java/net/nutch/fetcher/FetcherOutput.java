/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

import net.nutch.io.*;
import net.nutch.fs.*;
import net.nutch.util.*;
import net.nutch.pagedb.FetchListEntry;
import net.nutch.tools.UpdateDatabaseTool;
import net.nutch.parse.Outlink;

/*********************************************
 * An entry in the fetcher's output.  This includes all of the fetcher output
 * except the raw and stripped versions of the content, which are placed in
 * separate files.
 *
 * <p>
 * Note by John Xing: As of 20041022, option -noParsing is introduced
 * in Fetcher.java. This changes fetcher behavior. Accordingly
 * there are necessary modifications in this class.
 * Check Fetcher.java and ParseSegment.java for details.
 *
 * @author Doug Cutting
 *********************************************/
public final class FetcherOutput implements Writable {
  public static final String DIR_NAME = "fetcher";
  // 20041024, xing, 
  // When fetcher is run with option -noParsing, DIR_NAME_NP is created
  // instead of DIR_NAME. In separate pass, ParseSegment.java looks for
  // DIR_NAME_NP and generates DIR_NAME. Check ParseSegment.java for more info.
  public static final String DIR_NAME_NP = DIR_NAME+"_output";
  public static final String DONE_NAME = "fetcher.done";
  public static final String ERROR_NAME = "fetcher.error";

  private final static byte VERSION = 4;

  public final static byte RETRY = 0;
  public final static byte SUCCESS = 1;
  public final static byte NOT_FOUND = 2;
  public final static byte CANT_PARSE = 4; // fetched, but can't be parsed

  private FetchListEntry fetchListEntry;
  private MD5Hash md5Hash;
  private int status;
  private long fetchDate;

  public FetcherOutput() {}

  public FetcherOutput(FetchListEntry fetchListEntry,
                       MD5Hash md5Hash, int status) {
    this.fetchListEntry = fetchListEntry;
    this.md5Hash = md5Hash;
    this.status = status;
    this.fetchDate = System.currentTimeMillis();
  }

  public byte getVersion() { return VERSION; }

  public final void readFields(DataInput in) throws IOException {
    byte version = in.readByte();                 // read version
    fetchListEntry = FetchListEntry.read(in);
    md5Hash = MD5Hash.read(in);
    status = in.readByte();

    if (version < 4) {
      UTF8.readString(in);                        // read & ignore title
      int totalOutlinks = in.readInt();           // read & ignore outlinks
      for (int i = 0; i < totalOutlinks; i++) {
        Outlink.skip(in);
      }
    }

    fetchDate = (version > 1) ? in.readLong() : 0; // added in version=2
  }

  public final void write(DataOutput out) throws IOException {
    out.writeByte(VERSION);                       // store current version
    fetchListEntry.write(out);
    md5Hash.write(out);
    out.writeByte(status);
    out.writeLong(fetchDate);
  }

  public static FetcherOutput read(DataInput in) throws IOException {
    FetcherOutput fetcherOutput = new FetcherOutput();
    fetcherOutput.readFields(in);
    return fetcherOutput;
  }

  //
  // Accessor methods
  //
  public FetchListEntry getFetchListEntry() { return fetchListEntry; }
  public MD5Hash getMD5Hash() { return md5Hash; }
  public int getStatus() { return status; }
  public void setStatus(int status) { this.status = status; }
  public long getFetchDate() { return fetchDate; }
  public void setFetchDate(long fetchDate) { this.fetchDate = fetchDate; }

  // convenience methods
  public UTF8 getUrl() { return getFetchListEntry().getUrl(); }
  public String[] getAnchors() { return getFetchListEntry().getAnchors(); }

  public boolean equals(Object o) {
    if (!(o instanceof FetcherOutput))
      return false;
    FetcherOutput other = (FetcherOutput)o;
    return
      this.fetchListEntry.equals(other.fetchListEntry) &&
      this.md5Hash.equals(other.md5Hash) &&
      (this.status == other.status);
  }


  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("FetchListEntry: " + fetchListEntry + "Fetch Result:\n" );
    buffer.append("MD5Hash: " + md5Hash + "\n" );
    buffer.append("Status: " + status + "\n" );
    buffer.append("FetchDate: " + new Date(fetchDate) + "\n" );
    return buffer.toString();
  }

  public static void main(String argv[]) throws Exception {
    String usage = "FetcherOutput (-local <path> | -ndfs <path> <namenode:port>) (-recno <recno> | -dumpall) [-filename <filename>]";
    if (argv.length == 0 || argv.length > 4) {
      System.out.println("usage:" + usage);
      return;
    }

    // Process the args
    String filename = FetcherOutput.DIR_NAME;
    boolean dumpall = false;
    int recno = -1;
    int i = 0;
    NutchFileSystem nfs = NutchFileSystem.parseArgs(argv, i);
    for (; i < argv.length; i++) {
        if ("-recno".equals(argv[i])) {
            recno = Integer.parseInt(argv[i+1]);
            i++;
        } else if ("-dumpall".equals(argv[i])) {
            dumpall = true;
        } else if ("-filename".equals(argv[i])) {
            filename = argv[i+1];
            i++;
        }
    }

    // Now carry out the command
    ArrayFile.Reader fetcher = new ArrayFile.Reader(nfs, filename);
    try {
      FetcherOutput fo = new FetcherOutput();

      if (dumpall) {
        while ((fo = (FetcherOutput) fetcher.next(fo)) != null) {
          recno++;
          System.out.println("Retrieved " + recno + " from file " + filename);
          System.out.println(fo);
        }
      } else if (recno >= 0) {
        fetcher.get(recno, fo);
        System.out.println("Retrieved " + recno + " from file " + filename);
        System.out.println(fo);
      }
    } finally {
      fetcher.close();
    }
  }
}
