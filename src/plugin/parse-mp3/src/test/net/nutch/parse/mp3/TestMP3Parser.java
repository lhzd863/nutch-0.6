package net.nutch.parse.mp3;

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
 * Unit tests for TestMP3Parser.  (Adapted from John Xing msword unit tests).
 *
 * @author Andy Hedges
 */
public class TestMP3Parser extends TestCase {

  private String fileSeparator = System.getProperty("file.separator");
  // This system property is defined in ./src/plugin/build-plugin.xml
  private String sampleDir = System.getProperty("test.data", ".");
  // Make sure sample files are copied to "test.data" as specified in
  // ./src/plugin/parse-mp3/build.xml during plugin compilation.
  // Check ./src/plugin/parse-mp3/sample/README.txt for what they are.
  private String id3v1 = "postgresql-id3v1.mp3";
  private String id3v2 = "postgresql-id3v2.mp3";
  private String none = "postgresql-none.mp3";

  public TestMP3Parser(String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testId3v2() throws ProtocolException, ParseException {

    String urlString;
    Protocol protocol;
    Content content;
    Parser parser;
    Parse parse;

    urlString = "file:" + sampleDir + fileSeparator + id3v2;
    protocol = ProtocolFactory.getProtocol(urlString);
    content = protocol.getContent(urlString);

    parser = ParserFactory.getParser(content.getContentType(), urlString);
    parse = parser.getParse(content);
    Properties metadata = parse.getData().getMetadata();
    assertEquals("postgresql comment id3v2", metadata.getProperty("COMM-Text"));
    assertEquals("postgresql composer id3v2", metadata.getProperty("TCOM-Text"));
    assertEquals("02", metadata.getProperty("TRCK-Text"));
    assertEquals("http://localhost/", metadata.getProperty("WCOP-URL Link"));
    assertEquals("postgresql artist id3v2", metadata.getProperty("TPE1-Text"));
    assertEquals("(28)", metadata.getProperty("TCON-Text"));
    assertEquals("2004", metadata.getProperty("TYER-Text"));
    assertEquals("postgresql title id3v2", metadata.getProperty("TIT2-Text"));
    assertEquals("postgresql album id3v2", metadata.getProperty("TALB-Text"));
    assertEquals("postgresql encoded by id3v2", metadata.getProperty("TENC-Text"));

    assertEquals("postgresql title id3v2 - "
        + "postgresql album id3v2 - "
        + "postgresql artist id3v2", parse.getData().getTitle());
    assertEquals("http://localhost/", parse.getData().getOutlinks()[0].getToUrl());

  }

  public void testId3v1() throws ProtocolException, ParseException {

    String urlString;
    Protocol protocol;
    Content content;
    Parser parser;
    Parse parse;

    urlString = "file:" + sampleDir + fileSeparator + id3v1;
    protocol = ProtocolFactory.getProtocol(urlString);
    content = protocol.getContent(urlString);
    parser = ParserFactory.getParser(content.getContentType(), urlString);
    parse = parser.getParse(content);

    Properties metadata = parse.getData().getMetadata();
    assertEquals("postgresql comment id3v1", metadata.getProperty("COMM-Text"));
    assertEquals("postgresql artist id3v1", metadata.getProperty("TPE1-Text"));
    assertEquals("(28)", metadata.getProperty("TCON-Text"));
    assertEquals("2004", metadata.getProperty("TYER-Text"));
    assertEquals("postgresql title id3v1", metadata.getProperty("TIT2-Text"));
    assertEquals("postgresql album id3v1", metadata.getProperty("TALB-Text"));

    assertEquals("postgresql title id3v1 - "
        + "postgresql album id3v1 - "
        + "postgresql artist id3v1", parse.getData().getTitle());

  }

  public void testNone() throws ProtocolException, ParseException {
    String urlString;
    Protocol protocol;
    Content content;
    Parser parser;
    Parse parse;

    urlString = "file:" + sampleDir + fileSeparator + none;
    protocol = ProtocolFactory.getProtocol(urlString);
    content = protocol.getContent(urlString);
    parser = ParserFactory.getParser(content.getContentType(), urlString);
    try {
      parse = parser.getParse(content);
      Properties metadata = parse.getData().getMetadata();
    } catch (ParseException e) {
      return;
    }
    fail("Expected ParseException");

  }

}
