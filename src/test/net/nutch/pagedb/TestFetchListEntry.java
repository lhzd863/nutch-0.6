/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.pagedb;

import java.io.*;
import net.nutch.io.*;
import net.nutch.pagedb.*;
import junit.framework.TestCase;

/** Unit tests for FetchListEntry. */
public class TestFetchListEntry extends TestCase {
  public TestFetchListEntry(String name) { super(name); }

  public void testFetchListEntry() throws Exception {

    String[] anchors = new String[] {"foo", "bar"};

    FetchListEntry e1 = new FetchListEntry(true, TestPage.getTestPage(),
                                           anchors);
    TestWritable.testWritable(e1);

    FetchListEntry e2 = new FetchListEntry(false, TestPage.getTestPage(),
                                           anchors);
    TestWritable.testWritable(e2);

  }
	
}
