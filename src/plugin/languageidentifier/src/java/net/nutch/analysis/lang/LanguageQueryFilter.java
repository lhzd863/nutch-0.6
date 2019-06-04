/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.analysis.lang;

import net.nutch.searcher.RawFieldQueryFilter;

/** Handles "lang:" query clauses, causing them to search the "lang" field
 * indexed by LanguageIdentifier. */
public class LanguageQueryFilter extends RawFieldQueryFilter {
  public LanguageQueryFilter() {
    super("lang");
  }
}
