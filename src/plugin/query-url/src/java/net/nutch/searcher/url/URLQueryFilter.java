/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher.url;

import net.nutch.searcher.FieldQueryFilter;

/** Handles "url:" query clauses, causing them to search the field indexed by
 * BasicIndexingFilter. */
public class URLQueryFilter extends FieldQueryFilter {
  public URLQueryFilter() {
    super("url");
  }
}
