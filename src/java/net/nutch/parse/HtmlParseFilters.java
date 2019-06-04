/* Copyright (c) 2003 The Nutch Organization. All rights reserved. */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

import net.nutch.protocol.Content;
import net.nutch.plugin.*;

import org.w3c.dom.DocumentFragment;

/** Creates and caches {@link HtmlParseFilter} implementing plugins.*/
public class HtmlParseFilters {

  private static final HtmlParseFilter[] CACHE;
  static {
    try {
      ExtensionPoint point = PluginRepository.getInstance()
        .getExtensionPoint(HtmlParseFilter.X_POINT_ID);
      if (point == null)
        throw new RuntimeException(HtmlParseFilter.X_POINT_ID+" not found.");
      Extension[] extensions = point.getExtentens();
      CACHE = new HtmlParseFilter[extensions.length];
      for (int i = 0; i < extensions.length; i++) {
        Extension extension = extensions[i];
        CACHE[i] = (HtmlParseFilter)extension.getExtensionInstance();
      }
    } catch (PluginRuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  private  HtmlParseFilters() {}                  // no public ctor

  /** Run all defined filters. */
  public static Parse filter(Content content,Parse parse,DocumentFragment doc)
    throws ParseException {

    for (int i = 0 ; i < CACHE.length; i++) {
      parse = CACHE[i].filter(content, parse, doc);
    }

    return parse;
  }
}
