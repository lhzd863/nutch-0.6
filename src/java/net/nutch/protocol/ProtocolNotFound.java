/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol;

public class ProtocolNotFound extends ProtocolException {
  private String url;

  public ProtocolNotFound(String url) {
    this(url, "protocol not found for url="+url);
  }

  public ProtocolNotFound(String url, String message) {
    super(message);
    this.url = url;
  }

  public String getUrl() { return url; }
}
