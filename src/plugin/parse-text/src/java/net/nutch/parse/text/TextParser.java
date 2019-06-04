/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.text;

import java.util.Properties;

import net.nutch.protocol.Content;
import net.nutch.parse.*;
import net.nutch.util.*;

public class TextParser implements Parser {
  public Parse getParse(Content content) throws ParseException {
    // copy content meta data through
    Properties metadata = new Properties();
    metadata.putAll(content.getMetadata());

    ParseData parseData = new ParseData("", new Outlink[0], metadata);

    String encoding =
      StringUtil.parseCharacterEncoding(content.getContentType());
    String text;
    if (encoding != null) {                       // found an encoding header
      try {                                       // try to use named encoding
        text = new String(content.getContent(), encoding);
      } catch (java.io.UnsupportedEncodingException e) {
        throw new ParseException(e);
      }
    } else {
      // FIXME: implement charset detector. This code causes problem when 
      //        character set isn't specified in HTTP header.
      text = new String(content.getContent());    // use default encoding
    }

    return new ParseImpl(text, parseData);
  }
}
