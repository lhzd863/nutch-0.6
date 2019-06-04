/* Copyright (c) 2003 The Nutch Organization. All rights reserved. */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

import java.util.HashMap;

import org.apache.lucene.document.Document;

import net.nutch.plugin.*;
import net.nutch.parse.Parse;
import net.nutch.fetcher.FetcherOutput;

/** Creates and caches {@link IndexingFilter} implementing plugins.*/
public class IndexingFilters {

  private static final IndexingFilter[] CACHE;
  static {
    try {
      ExtensionPoint point = PluginRepository.getInstance()
        .getExtensionPoint(IndexingFilter.X_POINT_ID);
      if (point == null)
        throw new RuntimeException(IndexingFilter.X_POINT_ID+" not found.");
      Extension[] extensions = point.getExtentens();
      HashMap filterMap = new HashMap();
      for (int i = 0; i < extensions.length; i++) {
        Extension extension = extensions[i];
        IndexingFilter filter = (IndexingFilter)extension.getExtensionInstance();
        if (!filterMap.containsKey(filter.getClass().getName())) {
        	filterMap.put(filter.getClass().getName(), filter);
        }
      }
      CACHE = (IndexingFilter[])filterMap.values().toArray(new IndexingFilter[0]);
    } catch (PluginRuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  private  IndexingFilters() {}                  // no public ctor

  /** Run all defined filters. */
  public static Document filter(Document doc, Parse parse, FetcherOutput fo)
    throws IndexingException {

    for (int i = 0; i < CACHE.length; i++) {
      doc = CACHE[i].filter(doc, parse, fo);
    }

    return doc;
  }
}
