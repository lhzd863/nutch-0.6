/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer.more;

import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.MalformedPatternException;

import javax.activation.MimetypesFileTypeMap;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import net.nutch.net.protocols.HttpDateFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.nutch.parse.Parse;

import net.nutch.indexer.IndexingFilter;
import net.nutch.indexer.IndexingException;

import net.nutch.fetcher.FetcherOutput;

import net.nutch.util.NutchConf;

import net.nutch.util.LogFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import java.io.InputStream;
import java.io.IOException;

/************************************
 * Add (or reset) a few metaData properties as respective fields
 * (if they are available), * so that they can be displayed by more.jsp
 * (called by search.jsp).
 * In future, need to make some of them searchable!
 *
 * @author John Xing
 ***********************************/

public class MoreIndexingFilter implements IndexingFilter {
  public static final Logger LOG
    = LogFormatter.getLogger(MoreIndexingFilter.class.getName());

  // Filename extension to mime-type map.
  // Used by addType().
  static MimetypesFileTypeMap TYPE_MAP = null;
  static {
    try {
      // read mime types from config file
      InputStream is =
        NutchConf.getConfResourceAsInputStream
        (NutchConf.get("mime.types.file"));
      if (is == null) {
        LOG.warning
          ("no mime.types.file: content-type won't be indexed.");
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

  public Document filter(Document doc, Parse parse, FetcherOutput fo)
    throws IndexingException {

    String url = fo.getUrl().toString();

    // normalize metaData (see note in the method below).
    Properties metaData = normalizeMeta(parse.getData().getMetadata());

    addTime(doc, metaData, url);

    addLength(doc, metaData, url);

    if (TYPE_MAP != null)
      addType(doc, metaData, url);

    resetTitle(doc, metaData, url);

    return doc;
  }
    
  // Add time related meta info, now Last-Modified only
  // Others for consideration: Date, Expires
  private Document addTime(Document doc, Properties metaData, String url) {

    String lastModified = metaData.getProperty("last-modified");
    if (lastModified == null)
      return doc;

    // index/store it as long value
    DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy zzz");
    try {
      lastModified = new Long(HttpDateFormat.toLong(lastModified)).toString();
    } catch  (ParseException e) {
      // try to parse it as date in alternative format
      try {
        Date d = df.parse(lastModified);
        lastModified = new Long(d.getTime()).toString();
      } catch (Exception e1) {
        LOG.fine(url+": can't parse erroneous last-modified: "+lastModified);
        lastModified = null;
      }
    }

    if (lastModified != null)
      doc.add(Field.UnIndexed("lastModified", lastModified));

    return doc;
  }

  // Add Content-Length
  private Document addLength(Document doc, Properties metaData, String url) {
    String contentLength = metaData.getProperty("content-length");

    if (contentLength != null)
      doc.add(Field.UnIndexed("contentLength", contentLength));

    return doc;
  }

  // Add Content-Type as primaryType and subType
  private Document addType(Document doc, Properties metaData, String url) {
    String contentType = metaData.getProperty("content-type");
    if (contentType == null)
      return doc;

    MimeType mimeType;
    try {
      mimeType = new MimeType(contentType);
    } catch (MimeTypeParseException e) {
      LOG.warning(url+": can't parse erroneous content-type: "+contentType);
      return doc;
    }

    String primaryType = mimeType.getPrimaryType();
    String subType = mimeType.getSubType();
    // leave this for future improvement
    //MimeTypeParameterList parameterList = mimeType.getParameters()

    // primaryType and subType are stored
    doc.add(Field.UnIndexed("primaryType", primaryType));
    doc.add(Field.UnIndexed("subType", subType));

    return doc;
  }

  // Reset title if we see non-standard HTTP header "Content-Disposition".
  // It's a good indication that content provider wants filename therein
  // be used as the title of this url.

  // Patterns used to extract filename from possible non-standard
  // HTTP header "Content-Disposition". Typically it looks like:
  // Content-Disposition: inline; filename="foo.ppt"
  private PatternMatcher matcher = new Perl5Matcher();
  static Perl5Pattern patterns[] = {null, null};
  static {
    Perl5Compiler compiler = new Perl5Compiler();
    try {
      // order here is important
      patterns[0] =
        (Perl5Pattern) compiler.compile("\\bfilename=['\"](.+)['\"]");
      patterns[1] =
        (Perl5Pattern) compiler.compile("\\bfilename=(\\S+)\\b");
    } catch (MalformedPatternException e) {
      // just ignore
    }
  }

  private Document resetTitle(Document doc, Properties metaData, String url) {
    String contentDisposition = metaData.getProperty("content-disposition");
    if (contentDisposition == null)
      return doc;

    MatchResult result;
    for (int i=0; i<patterns.length; i++) {
      if (matcher.contains(contentDisposition,patterns[i])) {
        result = matcher.getMatch();
        doc.add(Field.UnIndexed("title", result.group(1)));
        break;
      }
    }

    return doc;
  }

  // Meta info in nutch metaData are saved in raw form, i.e.,
  // whatever the fetcher sees. To facilitate further processing,
  // a "normalization" is necessary.
  // This includes fixing http server oddities, such as:
  // (*) non-uniform casing of header names
  // (*) empty header value
  // Note: the original metaData should be kept intact,
  // because there is a benefit to preserve whatever comes from server.
  private Properties normalizeMeta(Properties old) {
    Properties normalized = new Properties();

    for (Enumeration e = old.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      String value = old.getProperty(key).trim();
      // some http server sends out header with empty value! if so, skip it
      if (value == null || value.equals(""))
        continue;
      // convert key (but, not value) to lower-case
      normalized.setProperty(key.toLowerCase(),value);
    }

    return normalized;
  }

}
