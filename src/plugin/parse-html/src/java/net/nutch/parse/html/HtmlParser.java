/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.html;

import java.util.Properties;
import java.util.ArrayList;
import java.util.logging.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.*;

import org.cyberneko.html.parsers.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.apache.html.dom.*;

import net.nutch.fetcher.FetcherOutput;
import net.nutch.protocol.Content;
import net.nutch.util.*;
import net.nutch.parse.*;
import net.nutch.parse.html.RobotsMetaProcessor.*;


public class HtmlParser implements Parser {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.parse.html");

  // I used 1000 bytes at first, but  found that some documents have 
  // meta tag well past the first 1000 bytes. 
  // (e.g. http://cn.promo.yahoo.com/customcare/music.html)
  private static final int CHUNK_SIZE = 2000;
  private static Pattern metaPattern =
    Pattern.compile("<meta\\s+([^>]*http-equiv=\"?content-type\"?[^>]*)>",
                    Pattern.CASE_INSENSITIVE);
  private static Pattern charsetPattern =
    Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)",
                    Pattern.CASE_INSENSITIVE);

  /**
   * Given a <code>byte[]</code> representing an html file of an 
   * <em>unknown</em> encoding,  read out 'charset' parameter in the meta tag   
   * from the first <code>CHUNK_SIZE</code> bytes.
   * If there's no meta tag for Content-Type or no charset is specified,
   * <code>null</code> is returned.  <br />
   * FIXME: non-byte oriented character encodings (UTF-16, UTF-32)
   * can't be handled with this. 
   * We need to do something similar to what's done by mozilla
   * (http://lxr.mozilla.org/seamonkey/source/parser/htmlparser/src/nsParser.cpp#1993).
   * See also http://www.w3.org/TR/REC-xml/#sec-guessing
   * <br />
   *
   * @param content <code>byte[]</code> representation of an html file
   */

  private static String sniffCharacterEncoding(byte[] content) {
    int length = content.length < CHUNK_SIZE ? 
                 content.length : CHUNK_SIZE;

    // We don't care about non-ASCII parts so that it's sufficient
    // to just inflate each byte to a 16-bit value by padding. 
    // For instance, the sequence {0x41, 0x82, 0xb7} will be turned into 
    // {U+0041, U+0082, U+00B7}. 
    String str = new String(content, 0, 0, length); 

    Matcher metaMatcher = metaPattern.matcher(str);
    String encoding = null;
    if (metaMatcher.find()) {
      Matcher charsetMatcher = charsetPattern.matcher(metaMatcher.group(1));
      if (charsetMatcher.find()) 
        encoding = new String(charsetMatcher.group(1));
    }

    return encoding;
  }


  private static String defaultCharEncoding =
    NutchConf.get("parser.character.encoding.default", "windows-1252");

  public Parse getParse(Content content) throws ParseException {
    DOMFragmentParser parser = new DOMFragmentParser();
    
    // some plugins, e.g., creativecommons, need to examine html comments
    try {
      parser.setFeature("http://apache.org/xml/features/include-comments", 
                        true);
    } catch (SAXException e) {}

    RobotsMetaIndicator robotsMeta = new RobotsMetaIndicator();

    URL base;
    try {
      base = new URL(content.getBaseUrl());
    } catch (MalformedURLException e) {
      throw new ParseException(e);
    }

    String text = "";
    String title = "";
    Outlink[] outlinks = new Outlink[0];
    Properties metadata = new Properties();

    // check that contentType is one we can handle
    String contentType = content.getContentType();
    if (!"".equals(contentType) && !contentType.startsWith("text/html"))
      throw new ParseException("Content-Type not text/html: " + contentType);
    
    // parse the content
    HTMLDocumentImpl impl = new HTMLDocumentImpl();
    impl.setErrorChecking(false);
    DocumentFragment root = impl.createDocumentFragment();
    try {
      byte[] contentInOctets = content.getContent();
      InputSource input =
        new InputSource(new ByteArrayInputStream(contentInOctets));
      String encoding = StringUtil.parseCharacterEncoding(contentType);
      if (encoding!=null) {
        metadata.put("OriginalCharEncoding", encoding);
        if ((encoding = StringUtil.resolveEncodingAlias(encoding)) != null) {
	  input.setEncoding(encoding); 
          metadata.put("CharEncodingForConversion", encoding);
          LOG.fine(base + ": setting encoding to " + encoding);
        }
      }

      // sniff out 'charset' value from the beginning of a document
      if (encoding == null) {
        encoding = sniffCharacterEncoding(contentInOctets);
        if (encoding!=null) {
          metadata.put("OriginalCharEncoding", encoding);
          if ((encoding = StringUtil.resolveEncodingAlias(encoding)) != null) {
	    input.setEncoding(encoding); 
            metadata.put("CharEncodingForConversion", encoding);
            LOG.fine(base + ": setting encoding to " + encoding);
          }
        }
      }

      if (encoding == null) {
        // fallback encoding.
        // FIXME : In addition to the global fallback value,
        // we should make it possible to specify fallback encodings for each ccTLD.
        // (e.g. se: windows-1252, kr: x-windows-949, cn: gb18030, tw: big5
        // doesn't work for jp because euc-jp and shift_jis have about the
        // same share)
       
        metadata.put("CharEncodingForConversion", defaultCharEncoding);
        input.setEncoding(defaultCharEncoding);
        LOG.fine(base + ": falling back to " + defaultCharEncoding);
      }

      parser.parse(input, root);
    } catch (IOException e) {
      throw new ParseException(e);
    } catch (DOMException e) {
      throw new ParseException(e);
    } catch (SAXException e) {
      throw new ParseException(e);
    }
      
    // get meta directives
    RobotsMetaProcessor.getRobotsMetaDirectives(robotsMeta, root, base);
      
    // check meta directives
    if (!robotsMeta.getNoIndex()) {               // okay to index
      StringBuffer sb = new StringBuffer();
      DOMContentUtils.getText(sb, root);          // extract text
      text = sb.toString();
      sb.setLength(0);
      DOMContentUtils.getTitle(sb, root);         // extract title
      title = sb.toString().trim();
    }
      
    if (!robotsMeta.getNoFollow()) {              // okay to follow links
      ArrayList l = new ArrayList();              // extract outlinks
      URL baseTag = DOMContentUtils.getBase(root);
      DOMContentUtils.getOutlinks(baseTag!=null?baseTag:base, l, root);
      outlinks = (Outlink[])l.toArray(new Outlink[l.size()]);
      LOG.fine("found "+outlinks.length+" outlinks in "+content.getUrl());
    }
    
    if (!robotsMeta.getNoCache()) {             // okay to cache
      // ??? FIXME ???
    }
    
    // copy content metadata through
    metadata.putAll(content.getMetadata());

    ParseData parseData = new ParseData(title, outlinks, metadata);
    Parse parse = new ParseImpl(text, parseData);

    // run filters on parse
    return HtmlParseFilters.filter(content, parse, root);
  }
}
