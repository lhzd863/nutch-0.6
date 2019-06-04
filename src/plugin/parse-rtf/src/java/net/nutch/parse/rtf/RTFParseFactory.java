/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.rtf;

import net.nutch.parse.*;
import net.nutch.parse.ParseException;
import net.nutch.protocol.Content;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.etranslate.tm.processing.rtf.RTFParser;

/**
 * A parser for RTF documents
 * @author Andy Hedges
 */
public class RTFParseFactory implements Parser {

  public Parse getParse(Content content) throws ParseException {
    byte[] raw = content.getContent();
    Reader reader = new InputStreamReader(new ByteArrayInputStream(raw));
    RTFParserDelegateImpl delegate = new RTFParserDelegateImpl();
    RTFParser rtfParser = null;
    rtfParser = RTFParser.createParser(reader);
    rtfParser.setNewLine("\n");
    rtfParser.setDelegate(delegate);

    try {
      rtfParser.parse();
    } catch (com.etranslate.tm.processing.rtf.ParseException e) {
      throw new ParseException("Exception parsing RTF document", e);
    }

    Properties metadata = new Properties();
    metadata.putAll(content.getMetadata());
    metadata.putAll(delegate.getMetaData());
    String title = metadata.getProperty("title");

    if(title != null){
      metadata.remove(title);
    } else {
      title = "";
    }

    ParseData parseData = new ParseData(title, new Outlink[0], metadata);

    return new ParseImpl(delegate.getText(), parseData);
  }


}
