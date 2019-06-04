package net.nutch.parse.rtf;

import junit.framework.TestCase;
import net.nutch.parse.Parse;
import net.nutch.parse.ParseException;
import net.nutch.parse.Parser;
import net.nutch.parse.ParserFactory;
import net.nutch.protocol.Content;
import net.nutch.protocol.Protocol;
import net.nutch.protocol.ProtocolException;
import net.nutch.protocol.ProtocolFactory;

import java.util.Properties;

/**
 * Unit tests for TestRTFParser.  (Adapted from John Xing msword unit tests).
 *
 * @author Andy Hedges
 */
public class TestRTFParser extends TestCase {

  private String fileSeparator = System.getProperty("file.separator");
  // This system property is defined in ./src/plugin/build-plugin.xml
  private String sampleDir = System.getProperty("test.data", ".");
  // Make sure sample files are copied to "test.data" as specified in
  // ./src/plugin/parse-rtf/build.xml during plugin compilation.
  // Check ./src/plugin/parse-rtf/sample/README.txt for what they are.
  private String rtfFile = "test.rtf";

  public TestRTFParser(String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testIt() throws ProtocolException, ParseException {

    String urlString;
    Protocol protocol;
    Content content;
    Parser parser;
    Parse parse;

    urlString = "file:" + sampleDir + fileSeparator + rtfFile;
    protocol = ProtocolFactory.getProtocol(urlString);
    content = protocol.getContent(urlString);

    parser = ParserFactory.getParser(content.getContentType(), urlString);
    parse = parser.getParse(content);
    String text = parse.getText();
    assertEquals("The quick brown fox jumps over the lazy dog", text.trim());

    String title = parse.getData().getTitle();
    Properties meta = parse.getData().getMetadata();
    assertEquals("test rft document", title);
    assertEquals("tests", meta.getProperty("subject"));



  }


}
