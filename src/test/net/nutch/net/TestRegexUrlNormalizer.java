/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net;

import java.net.URL;
import junit.framework.TestCase;
import net.nutch.net.RegexUrlNormalizer;

/** Unit tests for RegexUrlNormalizer. */
public class TestRegexUrlNormalizer extends TestBasicUrlNormalizer {
  public TestRegexUrlNormalizer(String name) { super(name); }

  public void testNormalizer() throws Exception {
    normalizeTest("http://foo.com/foo.php?f=2&PHPSESSID=cdc993a493e899bed04f4d0c8a462a03",
      "http://foo.com/foo.php?f=2");
    normalizeTest("http://foo.com/foo.php?f=2&PHPSESSID=cdc993a493e899bed04f4d0c8a462a03&q=3",
      "http://foo.com/foo.php?f=2&q=3");
    normalizeTest("http://foo.com/foo.php?PHPSESSID=cdc993a493e899bed04f4d0c8a462a03&f=2",
      "http://foo.com/foo.php?f=2");
    normalizeTest("http://foo.com/foo.php?PHPSESSID=cdc993a493e899bed04f4d0c8a462a03",
      "http://foo.com/foo.php");
  }

  private void normalizeTest(String weird, String normal) throws Exception {
    String testSrcDir = System.getProperty("test.src.dir");
    String path = testSrcDir + "/net/nutch/net/test-regex-normalize.xml";
    RegexUrlNormalizer normalizer = new RegexUrlNormalizer(path);
    assertEquals(normal, normalizer.normalize(weird));
  }
	
  public static void main(String[] args) throws Exception {
    new TestRegexUrlNormalizer("test").testNormalizer();
    new TestBasicUrlNormalizer("test").testNormalizer(); // need to make sure it passes this test too
  }



}
