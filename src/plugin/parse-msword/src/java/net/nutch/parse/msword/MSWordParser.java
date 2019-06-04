/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.msword;

import net.nutch.protocol.Content;
import net.nutch.util.LogFormatter;
import net.nutch.parse.Parser;
import net.nutch.parse.Parse;
import net.nutch.parse.ParseData;
import net.nutch.parse.ParseImpl;
import net.nutch.parse.Outlink;
import net.nutch.parse.ParseException;

import java.util.Properties;
//import java.util.logging.Logger;

import java.io.ByteArrayInputStream;

/**
 * parser for mime type application/msword.
 * It is based on org.apache.poi.*. We have to see how well it performs.
 *
 * @author John Xing
 *
 * Note on 20040614 by Xing:
 * Some codes are stacked here for convenience (see inline comments).
 * They may be moved to more appropriate places when new codebase
 * stabilizes, especially after code for indexing is written.
 *
 * @author Andy Hedges
 * code to extract all msword properties.
 *
 */

public class MSWordParser implements Parser {
//  public static final Logger LOG =
//    LogFormatter.getLogger("net.nutch.parse.msword");

  public MSWordParser () {}

  public Parse getParse(Content content) throws ParseException {

    // check that contentType is one we can handle
    String contentType = content.getContentType();
    if (contentType != null && !contentType.startsWith("application/msword"))
      throw new ParseException(
        "Content-Type not application/msword: "+contentType);

    String text = null;
    String title = null;
    Properties properties = null;

    try {

      byte[] raw = content.getContent();

      String contentLength = content.get("Content-Length");
      if (contentLength != null
            && raw.length != Integer.parseInt(contentLength)) {
          throw new ParseException("Content truncated at "+raw.length
            +" bytes. Parser can't handle incomplete msword file.");
      }

      WordExtractor extractor = new WordExtractor();

      // collect text
      text = extractor.extractText(new ByteArrayInputStream(raw));

      // collect meta info
      properties = extractor.extractProperties(new ByteArrayInputStream(raw));

      extractor = null;

    } catch (ParseException e) {
      throw e;
    } catch (FastSavedException e) {
      throw new ParseException(e);
    } catch (PasswordProtectedException e) {
      throw new ParseException(e);
    } catch (Exception e) { // run time exception
      throw new ParseException("Can't be handled as msword document. "+e);
    } finally {
      // nothing so far
    }

    // collect meta data
    Properties metadata = new Properties();
    metadata.putAll(content.getMetadata()); // copy through

    if(properties != null) {
      title = properties.getProperty("Title");
      properties.remove("Title");
      metadata.putAll(properties);
    }

    if (text == null)
      text = "";

    if (title == null)
      title = "";

    // collect outlink
    Outlink[] outlinks = new Outlink[0];

    ParseData parseData = new ParseData(title, outlinks, metadata);
    return new ParseImpl(text, parseData);
    // any filter?
    //return HtmlParseFilters.filter(content, parse, root);
  }

}
