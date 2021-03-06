/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol.file;

import javax.activation.MimetypesFileTypeMap;
// 20040528, xing, disabled for now
//import xing.net.nutch.util.magicfile.*;

import net.nutch.net.protocols.HttpDateFormat;

import net.nutch.util.LogFormatter;
import net.nutch.util.NutchConf;

import net.nutch.protocol.Content;
import net.nutch.protocol.Protocol;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.URL;

import java.io.InputStream;
// 20040528, xing, disabled for now
//import java.io.Reader;
import java.io.IOException;

/************************************
 * File.java deals with file: scheme.
 *
 * Configurable parameters are defined under "FILE properties" section
 * in ./conf/nutch-default.xml or similar.
 *
 * @author John Xing
 ***********************************/
public class File implements Protocol {

  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.protocol.file.File");

  static final int MAX_REDIRECTS = 5;

  static int maxContentLength = NutchConf.getInt("file.content.limit",64*1024);

  // 20040412, xing
  // the following three: HttpDateFormat, MimetypesFileTypeMap, MagicFile
  // are placed in each thread before we check out if they're thread-safe.

  // http date format
  HttpDateFormat httpDateFormat = null;

  // file name extension to mime-type map
  static MimetypesFileTypeMap TYPE_MAP = null;

  static {
    try {
      // read mime types from config file
      InputStream is =
        NutchConf.getConfResourceAsInputStream
        (NutchConf.get("mime.types.file"));
      if (is == null) {
        LOG.warning
          ("no mime.types.file: won't use url extension for content-type.");
        TYPE_MAP = null;
      } else {
        TYPE_MAP = new MimetypesFileTypeMap(is);
      }
      
      if (is != null)
        is.close();
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Unexpected error", e);
    }
  }

// 20040528, xing, disabled for now
//  // file magic for determining content type
//  static MagicFile MAGIC = null;
//
//  static {
//    try {
//      // read file magic from config file
//      Reader reader =
//        NutchConf.getConfResourceAsReader
//          (NutchConf.get("mime.magic.file"));
//      if (reader == null) {
//        LOG.warning
//          ("no mime.magic.file: won't use file magic for content-type.");
//        MAGIC = null;
//      } else {
//        MAGIC = MagicFile.getInstance(reader);
//      }
//
//      if (reader != null)
//        reader.close();
//    } catch (IOException e) {
//      LOG.log(Level.SEVERE, "Unexpected error", e);
//    }
//  }

  // constructor
  public File() {
    this.httpDateFormat = new HttpDateFormat();
  }

  /** Set the point at which content is truncated. */
  public void setMaxContentLength(int length) {this.maxContentLength = length;}

  public Content getContent(String urlString) throws FileException {
    try {
      URL url = new URL(urlString);
  
      int redirects = 0;
  
      while (true) {
        FileResponse response;
        response = new FileResponse(urlString, url, this);   // make a request
  
        int code = response.getCode();
  
        if (code == 200) {                          // got a good response
          return response.toContent();              // return it
  
        } else if (code >= 300 && code < 400) {     // handle redirect
          if (redirects == MAX_REDIRECTS)
            throw new FileException("Too many redirects: " + url);
          url = new URL(response.getHeader("Location"));
          redirects++;                
          if (LOG.isLoggable(Level.FINE))
            LOG.fine("redirect to " + url); 
  
        } else {                                    // convert to exception
          throw new FileError(code);
        }
      } 
    } catch (IOException e) {
      throw new FileException(e);
    }
  }

//  protected void finalize () {
//    // nothing here
//  }

  /** For debugging. */
  public static void main(String[] args) throws Exception {
    int maxContentLength = Integer.MIN_VALUE;
    String logLevel = "info";
    boolean dumpContent = false;
    String urlString = null;

    String usage = "Usage: File [-logLevel level] [-maxContentLength L] [-dumpContent] url";

    if (args.length == 0) {
      System.err.println(usage);
      System.exit(-1);
    }
      
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-logLevel")) {
        logLevel = args[++i];
      } else if (args[i].equals("-maxContentLength")) {
        maxContentLength = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-dumpContent")) {
        dumpContent = true;
      } else if (i != args.length-1) {
        System.err.println(usage);
        System.exit(-1);
      } else
        urlString = args[i];
    }

    File file = new File();

    if (maxContentLength != Integer.MIN_VALUE) // set maxContentLength
      file.setMaxContentLength(maxContentLength);

    // set log level
    LOG.setLevel(Level.parse((new String(logLevel)).toUpperCase()));

    Content content = file.getContent(urlString);

    System.err.println("Content-Type: " + content.getContentType());
    System.err.println("Content-Length: " + content.get("Content-Length"));
    System.err.println("Last-Modified: " + content.get("Last-Modified"));
    if (dumpContent) {
      System.out.print(new String(content.getContent()));
    }

    file = null;
  }

}
