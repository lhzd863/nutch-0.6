/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol;

import java.io.IOException;
import java.net.URL;

/** Thrown by {@link Protocol#getContent(String)} when a {@link URL} is invalid.*/
public class ResourceGone extends ProtocolException {
  private URL url;

  public ResourceGone(URL url, String message) {
    super(message);
    this.url = url;
  }

  public URL getUrl() { return url; }
}
