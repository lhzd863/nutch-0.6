/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import org.apache.lucene.search.BooleanQuery;

/** Extension point for query translation.  Permits one to add metadata to a
 * query.  All plugins found which implement this extension point are run
 * sequentially on the query.
 */
public interface QueryFilter {
  /** The name of the extension point. */
  final static String X_POINT_ID = QueryFilter.class.getName();

  /** Adds clauses or otherwise modifies the BooleanQuery that will be
   * searched. */
  BooleanQuery filter(Query input, BooleanQuery translation)
    throws QueryException;
}
