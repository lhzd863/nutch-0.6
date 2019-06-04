/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

import java.io.IOException;

public class ParserNotFound extends ParseException {
  private String url;
  private String contentType;

  public ParserNotFound(String url, String contentType) {
    this(url, contentType,
         "parser not found for contentType="+contentType+" url="+url);
  }

  public ParserNotFound(String url, String contentType, String message) {
    super(message);
    this.url = url;
    this.contentType = contentType;
  }

  public String getUrl() { return url; }
  public String getContentType() { return contentType; }
}
