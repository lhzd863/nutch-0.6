/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net;

import java.net.URL;
import junit.framework.TestCase;

/** Unit tests for BasicUrlNormalizer. */
public class TestBasicUrlNormalizer extends TestCase {
  public TestBasicUrlNormalizer(String name) { super(name); }

  public void testNormalizer() throws Exception {
    // check that leading and trailing spaces are removed
    normalizeTest(" http://foo.com/ ", "http://foo.com/");

    // check that protocol is lower cased
    normalizeTest("HTTP://foo.com/", "http://foo.com/");

    // check that host is lower cased
    normalizeTest("http://Foo.Com/index.html", "http://foo.com/index.html");
    normalizeTest("http://Foo.Com/index.html", "http://foo.com/index.html");

    // check that port number is normalized
    normalizeTest("http://foo.com:80/index.html", "http://foo.com/index.html");
    normalizeTest("http://foo.com:81/", "http://foo.com:81/");

    // check that null path is normalized
    normalizeTest("http://foo.com", "http://foo.com/");

    // check that references are removed
    normalizeTest("http://foo.com/foo.html#ref", "http://foo.com/foo.html");

    //     // check that encoding is normalized
    //     normalizeTest("http://foo.com/%66oo.html", "http://foo.com/foo.html");

    // check that unnecessary "../" are removed
    normalizeTest("http://foo.com/aa/../",
                  "http://foo.com/" );
    normalizeTest("http://foo.com/aa/bb/../",
                  "http://foo.com/aa/");
    normalizeTest("http://foo.com/aa/..",
                  "http://foo.com/aa/..");
    normalizeTest("http://foo.com/aa/bb/cc/../../foo.html",
                  "http://foo.com/aa/foo.html");
    normalizeTest("http://foo.com/aa/bb/../cc/dd/../ee/foo.html",
                  "http://foo.com/aa/cc/ee/foo.html");
    normalizeTest("http://foo.com/../foo.html",
                  "http://foo.com/foo.html" );
    normalizeTest("http://foo.com/../../foo.html",
                  "http://foo.com/foo.html" );
    normalizeTest("http://foo.com/../aa/../foo.html",
                  "http://foo.com/foo.html" );
    normalizeTest("http://foo.com/aa/../../foo.html",
                  "http://foo.com/foo.html" );
    normalizeTest("http://foo.com/aa/../bb/../foo.html/../../",
                  "http://foo.com/" );
    normalizeTest("http://foo.com/../aa/foo.html",
                  "http://foo.com/aa/foo.html" );
    normalizeTest("http://foo.com/../aa/../foo.html",
                  "http://foo.com/foo.html" );
    normalizeTest("http://foo.com/a..a/foo.html",
                  "http://foo.com/a..a/foo.html" );
    normalizeTest("http://foo.com/a..a/../foo.html",
                  "http://foo.com/foo.html" );
    normalizeTest("http://foo.com/foo.foo/../foo.html",
                  "http://foo.com/foo.html" );
  }

  private void normalizeTest(String weird, String normal) throws Exception {
    assertEquals(normal, UrlNormalizerFactory.getNormalizer().normalize(weird));
  }
	
  public static void main(String[] args) throws Exception {
    new TestBasicUrlNormalizer("test").testNormalizer();
  }



}
