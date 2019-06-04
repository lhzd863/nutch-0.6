/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import java.io.*;
import net.nutch.io.*;
import net.nutch.pagedb.*;
import junit.framework.TestCase;

/** Unit tests for FetcherOutput. */
public class TestFetcherOutput extends TestCase {
  public TestFetcherOutput(String name) { super(name); }

  public void testFetcherOutput() throws Exception {

    String[] anchors = new String[] {"foo", "bar"};

    FetcherOutput o =
      new FetcherOutput(new FetchListEntry(true, TestPage.getTestPage(),
                                           anchors),
                        TestMD5Hash.getTestHash(), FetcherOutput.SUCCESS);
                        
    TestWritable.testWritable(o);

  }
	
}
