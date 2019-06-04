/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

import org.apache.lucene.document.Document;
import net.nutch.parse.Parse;
import net.nutch.fetcher.FetcherOutput;

/** Extension point for indexing.  Permits one to add metadata to the indexed
 * fields.  All plugins found which implement this extension point are run
 * sequentially on the parse.
 */
public interface IndexingFilter {
  /** The name of the extension point. */
  final static String X_POINT_ID = IndexingFilter.class.getName();

  /** Adds fields or otherwise modifies the document that will be indexed for a
   * parse. */
  Document filter(Document doc, Parse parse, FetcherOutput fo)
    throws IndexingException;
}
