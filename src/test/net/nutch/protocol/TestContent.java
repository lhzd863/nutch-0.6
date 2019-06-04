/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol;

import java.io.*;
import java.util.Properties;
import net.nutch.io.*;
import net.nutch.pagedb.*;
import junit.framework.TestCase;

/** Unit tests for Content. */

public class TestContent extends TestCase {
  public TestContent(String name) { super(name); }

  public void testContent() throws Exception {

    String page = "<HTML><BODY><H1>Hello World</H1><P>The Quick Brown Fox Jumped Over the Lazy Fox.</BODY></HTML>";

    String url = "http://www.foo.com/";

    Properties metaData = new Properties();
    metaData.put("Host", "www.foo.com");
    metaData.put("Content-Type", "text/html");

    Content r = new Content(url, url, page.getBytes("UTF8"), "text/html",
                            metaData);
                        
    TestWritable.testWritable(r);
  }
	
}
