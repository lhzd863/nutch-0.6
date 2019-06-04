/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.pdf;

import net.nutch.protocol.ProtocolFactory;
import net.nutch.protocol.Protocol;
import net.nutch.protocol.Content;
import net.nutch.protocol.ProtocolException;

import net.nutch.parse.ParserFactory;
import net.nutch.parse.Parser;
import net.nutch.parse.Parse;
import net.nutch.parse.ParseException;

import junit.framework.TestCase;

/** 
 * Unit tests for PdfParser.
 *
 * @author John Xing
 */
public class TestPdfParser extends TestCase {

  private String fileSeparator = System.getProperty("file.separator");
  // This system property is defined in ./src/plugin/build-plugin.xml
  private String sampleDir = System.getProperty("test.data",".");
  // Make sure sample files are copied to "test.data" as specified in
  // ./src/plugin/parse-pdf/build.xml during plugin compilation.
  // Check ./src/plugin/parse-pdf/sample/README.txt for what they are.
  private String[] sampleFiles = {"pdftest.pdf"};

  private String expectedText = "A VERY SMALL PDF FILE";

  public TestPdfParser(String name) { 
    super(name); 
  }

  protected void setUp() {}

  protected void tearDown() {}

  public void testIt() throws ProtocolException, ParseException {
    String urlString;
    Protocol protocol;
    Content content;
    Parser parser;
    Parse parse;

    for (int i=0; i<sampleFiles.length; i++) {
      urlString = "file:" + sampleDir + fileSeparator + sampleFiles[i];

      protocol = ProtocolFactory.getProtocol(urlString);
      content = protocol.getContent(urlString);

      parser = ParserFactory.getParser(content.getContentType(), urlString);
      parse = parser.getParse(content);

      int index = parse.getText().indexOf(expectedText);
      assertTrue(index > 0);
    }
  }

}
