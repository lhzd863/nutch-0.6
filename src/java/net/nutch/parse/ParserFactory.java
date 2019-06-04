/* Copyright (c) 2003 The Nutch Organization. All rights reserved. */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

import java.util.Hashtable;

import net.nutch.plugin.*;

import java.util.logging.Logger;
import net.nutch.util.LogFormatter;

/** Creates and caches {@link Parser} plugins.*/
public class ParserFactory {

  public static final Logger LOG = LogFormatter
    .getLogger(ParserFactory.class.getName());

  private static final ExtensionPoint X_POINT = PluginRepository.getInstance()
      .getExtensionPoint(Parser.X_POINT_ID);

  static {
    if (X_POINT == null) {
      throw new RuntimeException("x point "+Parser.X_POINT_ID+" not found.");
    }
  }

  private static final Hashtable CACHE = new Hashtable();

  private ParserFactory() {}                      // no public ctor

  /** Returns the appropriate {@link Parser} implementation given a content
   * type and url.
   *
   * <p>Parser extensions should define the attributes"contentType" and/or
   * "pathSuffix".  Content type has priority: the first plugin found whose
   * "contentType" attribute matches the beginning of the content's type is
   * used.  If none match, then the first whose "pathSuffix" attribute matches
   * the end of the url's path is used.  If neither of these match, then the
   * first plugin whose "pathSuffix" is the empty string is used.
   */
  public static Parser getParser(String contentType, String url)
    throws ParserNotFound {

    try {
      Extension extension = getExtension(contentType, getSuffix(url));
      if (extension == null)
        throw new ParserNotFound(url, contentType);

      return (Parser)extension.getExtensionInstance();

    } catch (PluginRuntimeException e) {
      throw new ParserNotFound(url, contentType, e.toString());
    }
  }

  private static String getSuffix(String url) {
    int i = url.lastIndexOf('.');
    int j = url.lastIndexOf('/');
    if (i == -1 || i == url.length()-1 || i < j)
      return null;
    return url.substring(i+1);
  }


  private static Extension getExtension(String contentType, String suffix)
    throws PluginRuntimeException {

    //LOG.fine("getExtension: contentType="+contentType+" suffix="+suffix);

    String key = contentType + "+" + suffix;

    if (CACHE.containsKey(key))
      return (Extension)CACHE.get(key);
    
    Extension extension = findExtension(contentType, suffix);
    
    CACHE.put(key, extension);
    
    return extension;
  }

  private static Extension findExtension(String contentType, String suffix)
    throws PluginRuntimeException{

    //LOG.fine("findExtension: contentType="+contentType+" suffix="+suffix);

    Extension[] extensions = X_POINT.getExtentens();

    // first look for a content-type match
    if (contentType != null) {
      for (int i = 0; i < extensions.length; i++) {
        Extension extension = extensions[i];
        if (contentType.startsWith(extension.getAttribute("contentType")))
          return extension;                       // found a match
      }
    }

    // next look for a url path suffix match
    if (suffix != null) {
      for (int i = 0; i < extensions.length; i++) {
        Extension extension = extensions[i];
        if (suffix.equals(extension.getAttribute("pathSuffix")))
          return extension;                       // found a match
      }
    }

    // finally, look for an extension that accepts anything
    for (int i = 0; i < extensions.length; i++) {
      Extension extension = extensions[i];
      if ("".equals(extension.getAttribute("pathSuffix"))) // matches all
        return extension;
    }

    return null;
  }
}
