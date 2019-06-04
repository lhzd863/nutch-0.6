/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import net.nutch.io.*;
import net.nutch.db.*;
import net.nutch.fs.*;
import net.nutch.util.*;
import net.nutch.pagedb.*;
import net.nutch.parse.*;
import net.nutch.protocol.*;

import java.io.*;
import java.util.logging.Level;
import junit.framework.TestCase;


public class TestFetcher extends TestCase {

  public TestFetcher(String name) { 
      super(name); 
  }
	

  public void testFetcher() throws Exception {
    NutchFileSystem nfs = new LocalFileSystem();
    try {
        String directory = System.getProperty("test.build.data",".");
    
        String fetchListFilename = directory + "/" + FetchListEntry.DIR_NAME;
		
        ArrayFile.Writer testFetchList =
            new ArrayFile.Writer(nfs, fetchListFilename, FetchListEntry.class);

        MD5Hash id1 = new MD5Hash(new byte[]{0,0,0,0, 0,0,0,0, 0,0,0,0, 1,2,3,4});
        MD5Hash id2 = new MD5Hash(new byte[]{0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0});
	
        String url1 = "http://sourceforge.net/projects/nutch/";
        String url2 = "http://www.yahoo.com/";
        String url3 = "http://jakarta.apache.org/lucene/";
        String url4 = "http://www.nutch.org/docs/index.html";
        String url5 = "ftp://ftp.redhat.com/";
    
        Page page1 = new Page(url1, id1);
        Page page2 = new Page(url2, id2);
        Page page3 = new Page(url3, id2);
        Page page4 = new Page(url4, id2);
        Page page5 = new Page(url5, id2);
    
        String[] anchors = new String[] {"foo", "bar"};

        FetchListEntry fe1 = new FetchListEntry(true, page1, anchors);
        FetchListEntry fe2 = new FetchListEntry(true, page2, anchors);
        FetchListEntry fe3 = new FetchListEntry(true, page3, anchors);
        FetchListEntry fe4 = new FetchListEntry(true, page4, anchors);
        FetchListEntry fe5 = new FetchListEntry(false, page5, anchors);
    
        testFetchList.append(fe1);
        testFetchList.append(fe2);
        testFetchList.append(fe3);
        testFetchList.append(fe4);
        testFetchList.append(fe5);
        testFetchList.close();
		
        Fetcher fetcher = new Fetcher(nfs, directory, true);
        fetcher.setLogLevel(Level.FINE);
        //fetcher.getHttp().setMaxContentLength(4096);
        //fetcher.getHttp().setAgentString("NutchCVS");

        fetcher.run();

        ArrayFile.Reader fetcher_stripped;
        String stripped = directory + "/" + ParseText.DIR_NAME;
        ParseText s = new ParseText();
        fetcher_stripped = new ArrayFile.Reader(nfs, stripped);

        boolean yahoo = false;
        boolean nutch = false;

        while (fetcher_stripped.next(s) != null) {

            if (s.toString().indexOf("Yahoo!") >= 0)
                yahoo = true;

            if (s.toString().indexOf("Nutch") >= 0 )
                nutch = true;
        }
        fetcher_stripped.close();
        assertTrue(yahoo);
        assertTrue(nutch);
    } finally {
        nfs.close();
    }
  }
}





