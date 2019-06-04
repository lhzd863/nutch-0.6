/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

import net.nutch.protocol.Content;

import org.w3c.dom.DocumentFragment;

/** Extension point for DOM-based HTML parsers.  Permits one to add additional
 * metadata to HTML parses.  All plugins found which implement this extension
 * point are run sequentially on the parse.
 */
public interface HtmlParseFilter {
  /** The name of the extension point. */
  final static String X_POINT_ID = HtmlParseFilter.class.getName();

  /** Adds metadata or otherwise modifies a parse of HTML content, given
   * the DOM tree of a page. */
  Parse filter(Content content, Parse parse, DocumentFragment doc)
    throws ParseException;
}
