/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher.site;

import net.nutch.searcher.RawFieldQueryFilter;

/** Handles "site:" query clauses, causing them to search the field indexed by
 * SiteIndexingFilter. */
public class SiteQueryFilter extends RawFieldQueryFilter {
  public SiteQueryFilter() {
    super("site");
  }
}
