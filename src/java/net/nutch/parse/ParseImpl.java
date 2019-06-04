/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

/** The result of parsing a page's raw content.
 * @see Parser#getParse(Content)
 */
public class ParseImpl implements Parse {
  private String text;
  private ParseData data;

  public ParseImpl(String text, ParseData data) {
    this.text = text;
    this.data = data;
  }

  public String getText() { return text; }

  public ParseData getData() { return data; }
}
