/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

import java.io.*;
import java.util.Properties;
import net.nutch.io.*;
import net.nutch.pagedb.*;
import junit.framework.TestCase;

/** Unit tests for ParseData. */

public class TestParseData extends TestCase {
  public TestParseData(String name) { super(name); }

  public void testParseData() throws Exception {

    String title = "The Foo Page";

    Outlink[] outlinks = new Outlink[] {
      new Outlink("http://foo.com/", "Foo"),
      new Outlink("http://bar.com/", "Bar")
    };

    Properties metaData = new Properties();
    metaData.put("Language", "en/us");
    metaData.put("Charset", "UTF-8");

    ParseData r = new ParseData(title, outlinks, metaData);
                        
    TestWritable.testWritable(r);
  }
	
}
