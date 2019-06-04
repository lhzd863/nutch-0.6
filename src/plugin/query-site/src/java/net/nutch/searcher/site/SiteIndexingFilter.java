/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher.site;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import net.nutch.parse.Parse;

import net.nutch.indexer.IndexingFilter;
import net.nutch.indexer.IndexingException;

import net.nutch.fetcher.FetcherOutput;
import net.nutch.pagedb.FetchListEntry;

import java.util.logging.Logger;
import net.nutch.util.LogFormatter;

import java.net.URL;
import java.net.MalformedURLException;

/** Adds the host name to a "site" field, so that it can be searched by
 * SiteQueryFilter. */
public class SiteIndexingFilter implements IndexingFilter {
  public static final Logger LOG
    = LogFormatter.getLogger(SiteIndexingFilter.class.getName());

  public Document filter(Document doc, Parse parse, FetcherOutput fo)
    throws IndexingException {
    
    // parse the url to get the host name
    URL url;                                      
    try {
      url = new URL(fo.getUrl().toString());
    } catch (MalformedURLException e) {
      throw new IndexingException(e);
    }

    // add host as un-stored, indexed and un-tokenized
    doc.add(new Field("site", url.getHost(), false, true, false));

    // return the modified document
    return doc;
  }

}
