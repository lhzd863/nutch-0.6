/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net;

import java.net.MalformedURLException;

/** Interface used to convert URLs to normal form and optionally do regex substitutions */
public interface UrlNormalizer {
  
  /* Interface for URL normalization */
  public String normalize(String urlString) throws MalformedURLException;

}
