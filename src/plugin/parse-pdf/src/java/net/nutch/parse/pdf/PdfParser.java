/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.pdf;

import org.pdfbox.encryption.DocumentEncryption;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.util.PDFTextStripper;

import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;

import net.nutch.protocol.Content;
import net.nutch.util.LogFormatter;
import net.nutch.parse.Parser;
import net.nutch.parse.Parse;
import net.nutch.parse.ParseData;
import net.nutch.parse.ParseImpl;
import net.nutch.parse.Outlink;
import net.nutch.parse.ParseException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Properties;
import java.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/*********************************************
 * parser for mime type application/pdf.
 * It is based on org.pdfbox.*. We have to see how well it does the job.
 * 
 * @author John Xing
 *
 * Note on 20040614 by Xing:
 * Some codes are stacked here for convenience (see inline comments).
 * They may be moved to more appropriate places when new codebase
 * stabilizes, especially after code for indexing is written.
 *
 *********************************************/

public class PdfParser implements Parser {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.parse.pdf");

  public PdfParser () {
    // redirect org.apache.log4j.Logger to java's native logger, in order
    // to, at least, suppress annoying log4j warnings.
    // Note on 20040614 by Xing:
    // log4j is used by pdfbox. This snippet'd better be moved
    // to a common place shared by all parsers that use log4j.
    org.apache.log4j.Logger rootLogger =
      org.apache.log4j.Logger.getRootLogger();

    rootLogger.setLevel(org.apache.log4j.Level.INFO);

    org.apache.log4j.Appender appender = new org.apache.log4j.WriterAppender(
      new org.apache.log4j.SimpleLayout(),
      net.nutch.util.LogFormatter.getLogStream(
        this.LOG, java.util.logging.Level.INFO));

    rootLogger.addAppender(appender);
  }

  public Parse getParse(Content content) throws ParseException {

    // check that contentType is one we can handle
    String contentType = content.getContentType();
    if (contentType != null && !contentType.startsWith("application/pdf"))
      throw new ParseException(
        "Content-Type not application/pdf: "+contentType);

    // in memory representation of pdf file
    PDDocument pdf = null;

    String text = null;
    String title = null;

    try {

      byte[] raw = content.getContent();

      String contentLength = content.get("Content-Length");
      if (contentLength != null
            && raw.length != Integer.parseInt(contentLength)) {
          throw new ParseException("Content truncated at "+raw.length
            +" bytes. Parser can't handle incomplete pdf file.");
      }

      PDFParser parser = new PDFParser(
        new ByteArrayInputStream(raw));
      parser.parse();

      pdf = parser.getPDDocument();

      if (pdf.isEncrypted()) {
        DocumentEncryption decryptor = new DocumentEncryption(pdf);
        //Just try using the default password and move on
        decryptor.decryptDocument("");
      }

      // collect text
      PDFTextStripper stripper = new PDFTextStripper();
      text = stripper.getText(pdf);

      // collect title
      PDDocumentInformation info = pdf.getDocumentInformation();
      title = info.getTitle();
      // more useful info, currently not used. please keep them for future use.
      // pdf.getPageCount();
      // info.getAuthor()
      // info.getSubject()
      // info.getKeywords()
      // info.getCreator()
      // info.getProducer()
      // info.getTrapped()
      // formatDate(info.getCreationDate())
      // formatDate(info.getModificationDate())

    } catch (ParseException e) {
      throw e;
    } catch (CryptographyException e) {
      throw new ParseException("Error decrypting document. "+e);
    } catch (InvalidPasswordException e) {
      throw new ParseException("Can't decrypt document. "+e);
    } catch (Exception e) { // run time exception
      throw new ParseException("Can't be handled as pdf document. "+e);
    } finally {
      try {
        if (pdf != null)
          pdf.close();
        } catch (IOException e) {
          // nothing to do
        }
    }

    if (text == null)
      text = "";

    if (title == null)
      title = "";

    // collect outlink
    Outlink[] outlinks = new Outlink[0];

    // collect meta data
    Properties metadata = new Properties();
    metadata.putAll(content.getMetadata()); // copy through

    ParseData parseData = new ParseData(title, outlinks, metadata);
    return new ParseImpl(text, parseData);
    // any filter?
    //return HtmlParseFilters.filter(content, parse, root);
  }

  // format date
  // currently not used. please keep it for future use.
  private String formatDate(Calendar date) {
    String retval = null;
    if(date != null) {
      SimpleDateFormat formatter = new SimpleDateFormat();
      retval = formatter.format(date.getTime());
    }
    return retval;
  }

}
