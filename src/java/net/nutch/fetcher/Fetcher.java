/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import java.io.IOException;
import java.io.File;
import java.util.Properties;

import net.nutch.pagedb.FetchListEntry;
import net.nutch.io.*;
import net.nutch.db.*;
import net.nutch.fs.*;
import net.nutch.util.*;
import net.nutch.protocol.*;
import net.nutch.parse.*;
import net.nutch.plugin.*;

import java.util.logging.*;

/**
 * The fetcher. Most of the work is done by plugins.
 *
 * <p>
 * Note by John Xing: As of 20041022, option -noParsing is introduced.
 * Without this option, fetcher behaves the old way, i.e., it not only
 * crawls but also parses content. With option -noParsing, fetcher
 * does crawl only. Use ParseSegment.java to parse fetched contents.
 * Check FetcherOutput.java and ParseSegment.java for further description.
 */
public class Fetcher {

  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.fetcher.Fetcher");

  static {
    if (NutchConf.getBoolean("fetcher.verbose", false)) {
      setLogLevel(Level.FINE);
    }
  }

  private ArrayFile.Reader fetchList;             // the input
  private ArrayFile.Writer fetcherWriter;         // the output
  private ArrayFile.Writer contentWriter;
  private ArrayFile.Writer parseTextWriter;
  private ArrayFile.Writer parseDataWriter;

  private String name;                            // name of the segment
  private long start;                             // start time of fetcher run
  private long bytes;                             // total bytes fetched
  private int pages;                              // total pages fetched
  private int errors;                             // total pages errored

  private boolean parsing = true;                 // whether do parsing

  private int threadCount =                       // max number of threads
    NutchConf.getInt("fetcher.threads.fetch", 10);

  // All threads (FetcherThread or thread started by it) belong to
  // group "fetcher". Each FetcherThread is named as "fetcherXX",
  // where XX is the order it's started.
  private static final String THREAD_GROUP_NAME = "fetcher";

  private ThreadGroup group = new ThreadGroup(THREAD_GROUP_NAME); // our group

  // count of FetcherThreads that are through the loop and just about to return
  private int atCompletion = 0;

  /********************************************
   * Fetcher thread
   ********************************************/
  private class FetcherThread extends Thread {

    public FetcherThread(String name) { super(group, name); }

    /**
     * This thread keeps looping, grabbing an item off the list
     * of URLs to be fetched (in a thread-safe way).  It checks 
     * whether the URL is OK to download.  If so, we do it.
     */
    public void run() {

      FetchListEntry fle = new FetchListEntry();

      while (true) {
        if (LogFormatter.hasLoggedSevere())       // something bad happened
          break;                                  // exit
        
        String url = null;
        try {

          if (fetchList.next(fle) == null)
            break;

          url = fle.getPage().getURL().toString();

          if (!fle.getFetch()) {                  // should we fetch this page?
            if (LOG.isLoggable(Level.FINE))
              LOG.fine("not fetching " + url);
            handleNoFetch(fle, FetcherOutput.SUCCESS);
            continue;
          }

          LOG.info("fetching " + url);            // fetch the page

          Protocol protocol = ProtocolFactory.getProtocol(url);
          Content content = protocol.getContent(url);

          handleFetch(url, fle, content);

          synchronized (Fetcher.this) {           // update status
            pages++;
            bytes += content.getContent().length;
            if ((pages % 100) == 0) {             // show status every 100pp
              status();
            }
          }
        } catch (ResourceGone e) {                // don't retry
          logError(url, fle, e);
          handleNoFetch(fle, FetcherOutput.NOT_FOUND);

        // dealt with in handleFetch() below
        //} catch (ParseException e) {              // don't retry
        //  logError(url, fle, e);
        //  handleNoFetch(fle, FetcherOutput.CANT_PARSE);

        } catch (RetryLater e) {                  // explicit retry
          logError(url, fle, e);
          handleNoFetch(fle, FetcherOutput.RETRY);

        } catch (ProtocolException e) {           // implicit retry
          logError(url, fle, e);
          handleNoFetch(fle, FetcherOutput.RETRY);

        } catch (Throwable t) {                   // an unchecked exception
          if (fle != null) {
            logError(url, fle, t);                // retry?
            handleNoFetch(fle, FetcherOutput.RETRY);
          }
        }
      }

      // Explicitly invoke shutDown() for all possible plugins.
      // Done by the FetcherThread finished the last.
      synchronized (Fetcher.this) {
        atCompletion++;
        if (atCompletion == threadCount) {
          try {
            PluginRepository.getInstance().finalize();
          } catch (java.lang.Throwable t) {
            // do nothing
          }
        }
      }
      return;
    }

    private void logError(String url, FetchListEntry fle, Throwable t) {
      LOG.info("fetch of " + url + " failed with: " + t);
      LOG.log(Level.FINE, "stack", t);            // stack trace
      synchronized (Fetcher.this) {               // record failure
        errors++;
      }
    }

    private void handleFetch(String url, FetchListEntry fle, Content content) {
      if (!Fetcher.this.parsing) {
        outputPage(new FetcherOutput(fle, MD5Hash.digest(content.getContent()),
                                    FetcherOutput.SUCCESS),
                content, null, null);
        return;
      }

      try {
        String contentType = content.getContentType();
        Parser parser = ParserFactory.getParser(contentType, url);
        Parse parse = parser.getParse(content);
        outputPage(new FetcherOutput(fle, MD5Hash.digest(content.getContent()),
                                    FetcherOutput.SUCCESS),
                content, new ParseText(parse.getText()), parse.getData());
      } catch (ParseException e) {
        // 20041026, xing
        // If fetching succeeds, but parsing fails, content should be saved
        // so that we can try to parse again in separate pass, possibly
        // using better/alternative parser.
        LOG.info("fetch okay, but can't parse " + url + ", reason: "
          + e.getMessage());
        outputPage(new FetcherOutput(fle, MD5Hash.digest(content.getContent()),
                                    FetcherOutput.CANT_PARSE),
                content, new ParseText(""),
                new ParseData("", new Outlink[0], new Properties()));
      }
    }

    private void handleNoFetch(FetchListEntry fle, int status) {
      String url = fle.getPage().getURL().toString();
      MD5Hash hash = MD5Hash.digest(url);

      if (Fetcher.this.parsing) {
        outputPage(new FetcherOutput(fle, hash, status),
                   new Content(url, url, new byte[0], "", new Properties()),
                   new ParseText(""),
                   new ParseData("", new Outlink[0], new Properties()));
      } else {
        outputPage(new FetcherOutput(fle, hash, status),
                   new Content(url, url, new byte[0], "", new Properties()),
                   null, null);
      }
    }
      
    private void outputPage(FetcherOutput fo, Content content,
                            ParseText text, ParseData parseData) {
      try {
        synchronized (fetcherWriter) {
          fetcherWriter.append(fo);
          contentWriter.append(content);
          if (Fetcher.this.parsing) {
            parseTextWriter.append(text);
            parseDataWriter.append(parseData);
          }
        }
      } catch (Throwable t) {
        LOG.severe("error writing output:" + t.toString());
      }
    }
                                       
  }
			
  public Fetcher(NutchFileSystem nfs, String directory, boolean parsing)
    throws IOException {

    this.parsing = parsing;

    // Set up in/out streams
    fetchList = new ArrayFile.Reader
      (nfs, new File(directory, FetchListEntry.DIR_NAME).toString());
    if (this.parsing) {
      fetcherWriter = new ArrayFile.Writer
        (nfs, new File(directory, FetcherOutput.DIR_NAME).toString(),
        FetcherOutput.class);
    } else {
      fetcherWriter = new ArrayFile.Writer
        (nfs, new File(directory, FetcherOutput.DIR_NAME_NP).toString(),
        FetcherOutput.class);
    }
    contentWriter = new ArrayFile.Writer
      (nfs, new File(directory, Content.DIR_NAME).toString(), Content.class);
    if (this.parsing) {
      parseTextWriter = new ArrayFile.Writer(nfs,
        new File(directory, ParseText.DIR_NAME).toString(), ParseText.class);
      parseDataWriter = new ArrayFile.Writer(nfs,
        new File(directory, ParseData.DIR_NAME).toString(), ParseData.class);
    }
    name = new File(directory).getName();
  }

  /** Set thread count */
  public void setThreadCount(int threadCount) {
    this.threadCount=threadCount;
  }

  /** Set the logging level. */
  public static void setLogLevel(Level level) {
    LOG.setLevel(level);
    PluginRepository.LOG.setLevel(level);
    ParserFactory.LOG.setLevel(level);
    LOG.info("logging at " + level);
  }

  /** Runs the fetcher. */
  public void run() throws IOException, InterruptedException {
    start = System.currentTimeMillis();
    for (int i = 0; i < threadCount; i++) {       // spawn threads
      FetcherThread thread = new FetcherThread(THREAD_GROUP_NAME+i); 
      thread.start();
    }

    // Quit monitoring if all FetcherThreads are gone.
    // There could still be other threads, which may well be runaway threads
    // started by external libs via FetcherThreads and it is generally safe
    // to ignore them because our main FetcherThreads have finished their jobs.
    // In fact we are a little more cautious here by making sure
    // there is no more outstanding page fetches via monitoring
    // changes of pages, errors and bytes.
    int pages0 = pages; int errors0 = errors; long bytes0 = bytes;
  
    while (true) {
      Thread.sleep(1000);

      if (LogFormatter.hasLoggedSevere()) 
        throw new RuntimeException("SEVERE error logged.  Exiting fetcher.");

      int n = group.activeCount();
      Thread[] list = new Thread[n];
      group.enumerate(list);
      boolean noMoreFetcherThread = true; // assumption
      for (int i = 0; i < n; i++) {
        // this thread may have gone away in the meantime
        if (list[i] == null) continue;
        String name = list[i].getName();
        if (name.startsWith(THREAD_GROUP_NAME)) // prove it
          noMoreFetcherThread = false;
        if (LOG.isLoggable(Level.FINE))
          LOG.fine(list[i].toString());
      }
      if (noMoreFetcherThread) {
        if (LOG.isLoggable(Level.FINE))
          LOG.fine("number of active threads: "+n);
        if (pages == pages0 && errors == errors0 && bytes == bytes0)
          break;
        status();
        pages0 = pages; errors0 = errors; bytes0 = bytes;
      }
    }

    fetchList.close();                            // close databases
    fetcherWriter.close();
    contentWriter.close();
    if (this.parsing) {
      parseTextWriter.close();
      parseDataWriter.close();
    }

  }
  
  public static class FetcherStatus {
    private String name;
    private long startTime, curTime;
    private int pageCount, errorCount;
    private long byteCount;
    
    /**
     * FetcherStatus encapsulates a snapshot of the Fetcher progress status.
     * @param name short name of the segment being processed
     * @param start the time in millisec. this fetcher was started
     * @param pages number of pages fetched
     * @param errors number of fetching errors
     * @param bytes number of bytes fetched
     */
    public FetcherStatus(String name, long start, int pages, int errors, long bytes) {
      this.name = name;
      this.startTime = start;
      this.curTime = System.currentTimeMillis();
      this.pageCount = pages;
      this.errorCount = errors;
      this.byteCount = bytes;
    }
    
    public String getName() {return name;}
    public long getStartTime() {return startTime;}
    public long getCurTime() {return curTime;}
    public long getElapsedTime() {return curTime - startTime;}
    public int getPageCount() {return pageCount;}
    public int getErrorCount() {return errorCount;}
    public long getByteCount() {return byteCount;}
    
    public String toString() {
      return "status: segment " + name + ", "
        + pageCount + " pages, "
        + errorCount + " errors, "
        + byteCount + " bytes, "
        + (curTime - startTime) + " ms";
    }
  }
  
  public synchronized FetcherStatus getStatus() {
    return new FetcherStatus(name, start, pages, errors, bytes);
  }

  /** Display the status of the fetcher run. */
  public synchronized void status() {
    FetcherStatus status = getStatus();
    LOG.info(status.toString());
    LOG.info("status: "
             + (((float)status.getPageCount())/(status.getElapsedTime()/1000.0f))+" pages/s, "
             + (((float)status.getByteCount()*8/1024)/(status.getElapsedTime()/1000.0f))+" kb/s, "
             + (((float)status.getByteCount())/status.getPageCount()) + " bytes/page");
  }

  /** Run the fetcher. */
  public static void main(String[] args) throws Exception {
    int threadCount = -1;
    long delay = -1;
    String logLevel = "info";
    boolean parsing = true;
    boolean showThreadID = false;
    String directory = null;

    String usage = "Usage: Fetcher (-local | -ndfs <namenode:port>) [-logLevel level] [-noParsing] [-showThreadID] [-threads n] <dir>";

    if (args.length == 0) {
      System.err.println(usage);
      System.exit(-1);
    }
      
    int i = 0;
    NutchFileSystem nfs = NutchFileSystem.parseArgs(args, i);
    for (; i < args.length; i++) {       // parse command line
      if (args[i] == null) {
          continue;
      } else if (args[i].equals("-threads")) {    // found -threads option
        threadCount =  Integer.parseInt(args[++i]);
      } else if (args[i].equals("-logLevel")) {
        logLevel = args[++i];
      } else if (args[i].equals("-noParsing")) {
        parsing = false;
      } else if (args[i].equals("-showThreadID")) {
        showThreadID = true;
      } else                                      // root is required parameter
        directory = args[i];
    }

    Fetcher fetcher = new Fetcher(nfs, directory, parsing);// make a Fetcher
    if (threadCount != -1) {                      // set threadCount option
      fetcher.setThreadCount(threadCount);
    }

    // set log level
    fetcher.setLogLevel(Level.parse(logLevel.toUpperCase()));

    if (showThreadID) {
      LogFormatter.setShowThreadIDs(showThreadID);
    }
    
    try {
      fetcher.run();                                // run the Fetcher
    } finally {
      nfs.close();
    }

  }
}
