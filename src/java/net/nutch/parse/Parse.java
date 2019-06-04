/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

/** The result of parsing a page's raw content.
 * @see Parser#getParse(FetcherOutput,Content)
 */
public interface Parse {
  /** The textual content of the page. This is indexed, searched, and used when
   * generating snippets.*/ 
  String getText();

  /** Other data extracted from the page. */
  ParseData getData();
}
