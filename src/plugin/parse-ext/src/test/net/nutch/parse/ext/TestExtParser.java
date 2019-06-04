/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.ext;

import net.nutch.protocol.ProtocolFactory;
import net.nutch.protocol.Protocol;
import net.nutch.protocol.Content;
import net.nutch.protocol.ProtocolException;

import net.nutch.parse.ParserFactory;
import net.nutch.parse.Parser;
import net.nutch.parse.Parse;
import net.nutch.parse.ParseException;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** 
 * Unit tests for ExtParser.
 * First creates a temp file with fixed content, then fetch
 * and parse it using external command 'cat' and 'md5sum' alternately
 * for 10 times. Doing so also does a light stress test for class
 * CommandRunner.java (as used in ExtParser.java).
 *
 * Warning: currently only do test on linux platform.
 *
 * @author John Xing
 */
public class TestExtParser extends TestCase {
  private File tempFile = null;
  private String urlString = null;
  private Content content = null;;
  private Parser parser = null;;
  private Parse parse = null;

  private String expectedText = "nutch rocks nutch rocks nutch rocks";
  // echo -n "nutch rocks nutch rocks nutch rocks" | md5sum
  private String expectedMD5sum = "df46711a1a48caafc98b1c3b83aa1526";

  public TestExtParser(String name) { 
    super(name); 
  }

  protected void setUp() throws ProtocolException, IOException {
    // prepare a temp file with expectedText as its content
    // This system property is defined in ./src/plugin/build-plugin.xml
    String path = System.getProperty("test.data");
    if (path != null) {
      File tempDir = new File(path);
      if (!tempDir.exists())
        tempDir.mkdir();
      tempFile = File.createTempFile("nutch.test.plugin.ExtParser.","",tempDir);
    } else {
      // otherwise in java.io.tmpdir
      tempFile = File.createTempFile("nutch.test.plugin.ExtParser.","");
    }
    urlString = tempFile.toURL().toString();

    FileOutputStream fos = new FileOutputStream(tempFile);
    fos.write(expectedText.getBytes());
    fos.close();

    // get nutch content
    Protocol protocol = ProtocolFactory.getProtocol(urlString);
    content = protocol.getContent(urlString);
    protocol = null;
  }

  protected void tearDown() {
    // clean content
    content = null;

    // clean temp file
    //if (tempFile != null && tempFile.exists())
    //  tempFile.delete();
  }

  public void testIt() throws ParseException {
    String contentType;

    // now test only on linux platform
    if (!System.getProperty("os.name").equalsIgnoreCase("linux"))
      return;

    // loop alternately, total 10*2 times of invoking external command
    for (int i=0; i<10; i++) {
      // check external parser that does 'cat'
      contentType = "application/vnd.nutch.example.cat";
      content.setContentType(contentType);
      parser = ParserFactory.getParser(contentType, urlString);
      parse = parser.getParse(content);
      assertEquals(expectedText,parse.getText());

      // check external parser that does 'md5sum'
      contentType = "application/vnd.nutch.example.md5sum";
      content.setContentType(contentType);
      parser = ParserFactory.getParser(contentType, urlString);
      parse = parser.getParse(content);
      assertTrue(parse.getText().startsWith(expectedMD5sum));
    }
  }

}
