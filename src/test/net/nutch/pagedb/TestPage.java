/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.pagedb;

import java.io.*;
import java.util.Random;
import net.nutch.io.*;
import net.nutch.db.*;
import junit.framework.TestCase;

/** Unit tests for Page. */
public class TestPage extends TestCase {
  public TestPage(String name) { super(name); }

  public static Page getTestPage() throws Exception {
    return new Page("http://foo.com/", TestMD5Hash.getTestHash());
  }

  public void testPage() throws Exception {
    TestWritable.testWritable(getTestPage());
  }

  public void testPageCtors() throws Exception {
    Random random = new Random();
    String urlString = "http://foo.com/";
    MD5Hash md5 = MD5Hash.digest(urlString);
    long now = System.currentTimeMillis();
    float score = random.nextFloat();
    float nextScore = random.nextFloat();

    Page page = new Page(urlString, md5);
    assertEquals(page.getURL().toString(), urlString);
    assertEquals(page.getMD5(), md5);

    page = new Page(urlString, score);
    assertEquals(page.getURL().toString(), urlString);
    assertEquals(page.getMD5(), md5);
    assertEquals(page.getScore(), score, 0.0f);
    assertEquals(page.getNextScore(), score, 0.0f);

    page = new Page(urlString, score, now);
    assertEquals(page.getURL().toString(), urlString);
    assertEquals(page.getMD5(), md5);
    assertEquals(page.getNextFetchTime(), now);
    assertEquals(page.getScore(), score, 0.0f);
    assertEquals(page.getNextScore(), score, 0.0f);

    page = new Page(urlString, score, nextScore, now);
    assertEquals(page.getURL().toString(), urlString);
    assertEquals(page.getMD5(), md5);
    assertEquals(page.getNextFetchTime(), now);
    assertEquals(page.getScore(), score, 0.0f);
    assertEquals(page.getNextScore(), nextScore, 0.0f);
  }

}
