/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package org.creativecommons.nutch;

import net.nutch.parse.*;
import net.nutch.protocol.Content;

import java.util.Properties;
import java.io.*;
import java.net.URL;

import junit.framework.TestCase;

public class TestCCParseFilter extends TestCase {

  private static final File testDir =
    new File(System.getProperty("test.input"));

  public void testPages() throws Exception {
    pageTest(new File(testDir, "anchor.html"), "http://foo.com/",
             "http://creativecommons.org/licenses/by-nc-sa/1.0", "a", null);
    pageTest(new File(testDir, "rel.html"), "http://foo.com/",
             "http://creativecommons.org/licenses/by-nc/2.0", "rel", null);
    pageTest(new File(testDir, "rdf.html"), "http://foo.com/",
             "http://creativecommons.org/licenses/by-nc/1.0", "rdf", "text");
  }

  public void pageTest(File file, String url,
                       String license, String location, String type)
    throws Exception {

    String contentType = "text/html";
    InputStream in = new FileInputStream(file);
    ByteArrayOutputStream out = new ByteArrayOutputStream((int)file.length());
    byte[] buffer = new byte[1024];
    int i;
    while ((i = in.read(buffer)) != -1) {
      out.write(buffer, 0, i);
    }
    in.close();
    byte[] bytes = out.toByteArray();

    Parser parser = ParserFactory.getParser(contentType, url);
    Content content =
      new Content(url, url, bytes, contentType, new Properties());
    Parse parse = parser.getParse(content);

    Properties metadata = parse.getData().getMetadata();
    assertEquals(license, metadata.get("License-Url"));
    assertEquals(location, metadata.get("License-Location"));
    assertEquals(type, metadata.get("Work-Type"));
  }
}
