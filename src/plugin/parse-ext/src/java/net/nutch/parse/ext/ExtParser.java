/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.ext;

import net.nutch.protocol.Content;
import net.nutch.parse.Parser;
import net.nutch.parse.Parse;
import net.nutch.parse.ParseData;
import net.nutch.parse.ParseImpl;
import net.nutch.parse.Outlink;
import net.nutch.parse.ParseException;

import net.nutch.util.LogFormatter;
import net.nutch.util.NutchConf;
import net.nutch.util.CommandRunner;

import net.nutch.plugin.Extension;
import net.nutch.plugin.PluginRepository;

import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * A wrapper that invokes external command to do real parsing job.
 * 
 * @author John Xing
 */

public class ExtParser implements Parser {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.parse.ext");

  static final int BUFFER_SIZE = 4096;

  static final int TIMEOUT_DEFAULT = 30; // in seconds

  // handy map from String contentType to String[] {command, timeoutString}
  static Hashtable TYPE_PARAMS_MAP = new Hashtable();

  // set TYPE_PARAMS_MAP using plugin.xml of this plugin
  static {
    Extension[] extensions = PluginRepository.getInstance()
      .getExtensionPoint("net.nutch.parse.Parser").getExtentens();

    String contentType, command, timeoutString;

    for (int i = 0; i < extensions.length; i++) {
      Extension extension = extensions[i];

      // only look for extensions defined by plugin parse-ext
      if (!extension.getDiscriptor().getPluginId().equals("parse-ext"))
        continue;

      contentType = extension.getAttribute("contentType");
      if (contentType == null || contentType.equals(""))
        continue;

      command = extension.getAttribute("command");
      if (command == null || command.equals(""))
        continue;

      timeoutString = extension.getAttribute("timeout");
      if (timeoutString == null || timeoutString.equals(""))
        timeoutString = "" + TIMEOUT_DEFAULT;

      TYPE_PARAMS_MAP.put(contentType, new String[]{command, timeoutString});
    }
  }

  public ExtParser () {}

  public Parse getParse(Content content) throws ParseException {

    String contentType = content.getContentType();

    String[] params = (String[]) TYPE_PARAMS_MAP.get(contentType);
    if (params == null)
      throw new ParseException(
        "No external command defined for contentType: " + contentType);

    String command = params[0];
    int timeout = Integer.parseInt(params[1]);

    if (LOG.isLoggable(Level.FINE))
      LOG.fine("Use "+command+ " with timeout="+timeout+"secs");

    String text = null;
    String title = null;

    try {

      byte[] raw = content.getContent();

      String contentLength =
        (String)content.getMetadata().get("Content-Length");
      if (contentLength != null
            && raw.length != Integer.parseInt(contentLength)) {
          throw new ParseException("Content truncated at "+raw.length
            +" bytes. Parser can't handle incomplete "+contentType+" file.");
      }

      ByteArrayOutputStream os = new ByteArrayOutputStream(BUFFER_SIZE);
      ByteArrayOutputStream es = new ByteArrayOutputStream(BUFFER_SIZE/4);

      CommandRunner cr = new CommandRunner();

      cr.setCommand(command+ " " +contentType);
      cr.setInputStream(new ByteArrayInputStream(raw));
      cr.setStdOutputStream(os);
      cr.setStdErrorStream(es);

      cr.setTimeout(timeout);

      cr.evaluate();

      if (cr.getExitValue() != 0)
        throw new ParseException("External command "+command
          +" failed with error: "+es.toString());

      text = os.toString();

    } catch (ParseException e) {
      throw e;
    } catch (Exception e) { // run time exception
      throw new ParseException("ExtParser failed. "+e);
    }

    if (text == null)
      text = "";

    if (title == null)
      title = "";

    // collect outlink
    Outlink[] outlinks = new Outlink[0];

    // collect meta data
    Properties metaData = new Properties();
    metaData.putAll(content.getMetadata()); // copy through

    ParseData parseData = new ParseData(title, outlinks, metaData);
    return new ParseImpl(text, parseData);
  }

}
