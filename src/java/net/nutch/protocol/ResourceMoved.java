/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol;

import java.io.IOException;
import java.net.URL;

/** Thrown by {@link Protocol#getContent(String)} when a {@link URL} no longer
 * exists.*/
public class ResourceMoved extends IOException {
  private URL oldUrl;
  private URL newUrl;

  public ResourceMoved(URL oldUrl, URL newUrl, String message) {
    super(message);
    this.newUrl = newUrl;
    this.oldUrl = oldUrl;
  }

  public URL getNewUrl() { return newUrl; }
  public URL getOldUrl() { return oldUrl; }
}
