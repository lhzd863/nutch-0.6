/* Copyright (c) 2004 The Nutch Organization. All rights reserved. */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.analysis.lang;

import java.util.Properties;

import junit.framework.TestCase;
import net.nutch.parse.Parse;
import net.nutch.parse.Parser;
import net.nutch.parse.ParserFactory;
import net.nutch.protocol.Content;

public class TestHTMLLanguageParser extends TestCase {

  private static String URL = "http://foo.bar/";

  private static String BASE = "http://foo.bar/";

  String docs[] = {
      "<html lang=\"fi\"><head>document 1 title</head><body>jotain suomeksi</body></html>",
      "<html><head><meta http-equiv=\"content-language\" content=\"en\"><title>document 2 title</head><body>this is english</body></html>",
      "<html><head><meta name=\"dc.language\" content=\"en\"><title>document 3 title</head><body>this is english</body></html>" };

  String metalanguages[] = { "fi", "en", "en" };

  /**
   * Test parsing of language identifiers from html 
   **/
  public void testMetaHTMLParsing() {

    try {

      /* loop through the test documents and validate result */
      for (int t = 0; t < docs.length; t++) {

        Content content = getContent(docs[t]);
        Parser parser = ParserFactory.getParser("text/html", URL);
        Parse parse = parser.getParse(content);

        assertEquals(metalanguages[t], (String) parse.getData().get(
            HTMLLanguageParser.META_LANG_NAME));

      }
    } catch (Exception e) {
      e.printStackTrace(System.out);
      fail(e.toString());
    }

  }

  private Content getContent(String text) {
    Properties p = new Properties();
    p.put("Content-Type", "text/html");

    Content content = new Content(URL, BASE, text.getBytes(), "text/html", p);
    return content;
  }
}