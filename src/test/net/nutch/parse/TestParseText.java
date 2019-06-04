/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

import java.io.*;
import net.nutch.io.*;
import net.nutch.pagedb.*;
import junit.framework.TestCase;

/** Unit tests for ParseText. */

public class TestParseText extends TestCase {
  public TestParseText(String name) { super(name); }

  public void testParseText() throws Exception {

    String page = "Hello World The Quick Brown Fox Jumped Over the Lazy Fox";

    ParseText s = new ParseText(page);
                        
    TestWritable.testWritable(s);
  }
	
}
