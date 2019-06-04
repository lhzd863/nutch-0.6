/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

import net.nutch.util.LogFormatter;

import net.nutch.protocol.ProtocolFactory;
import net.nutch.protocol.Protocol;
import net.nutch.protocol.Content;

import java.util.logging.Logger;

/**
 * Parser checker, useful for testing parser.
 * 
 * @author John Xing
 */

public class ParserChecker {

  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.parse.ParserChecker");

  public ParserChecker() {}

  public static void main(String[] args) throws Exception {
    boolean dumpText = false;
    boolean force = false;
    String contentType = null;
    String url = null;

    String usage = "Usage: ParserChecker [-dumpText] [-forceAs mimeType] url";

    if (args.length == 0) {
      System.err.println(usage);
      System.exit(-1);
    }

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-forceAs")) {
        force = true;
        contentType = args[++i];
      } else if (args[i].equals("-dumpText")) {
        dumpText = true;
      } else if (i != args.length-1) {
        System.err.println(usage);
        System.exit(-1);
      } else {
        url = args[i];
      }
    }

    LOG.info("fetching: "+url);

    Protocol protocol = ProtocolFactory.getProtocol(url);
    Content content = protocol.getContent(url);

    if (force) {
      content.setContentType(contentType);
    } else {
      contentType = content.getContentType();
    }

    if (contentType == null) {
      System.err.println("");
      System.exit(-1);
    }

    LOG.info("parsing: "+url);
    LOG.info("contentType: "+contentType);

    Parser parser = ParserFactory.getParser(contentType, url);
    Parse parse = parser.getParse(content);

    System.out.print("---------\nParseData\n---------\n");
    System.out.print(parse.getData().toString());
    if (dumpText) {
      System.out.print("---------\nParseText\n---------\n");
      System.out.print(parse.getText());
    }

    System.exit(0);
  }
}
