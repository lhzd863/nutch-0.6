/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

import java.io.*;
import net.nutch.io.*;
import net.nutch.fs.*;
import net.nutch.util.*;

/* The text conversion of page's content, stored using gzip compression.
 * @see Parse#getText()
 */
public final class ParseText extends VersionedWritable {
  public static final String DIR_NAME = "parse_text";

  private final static byte VERSION = 1;

  public ParseText() {}
  private String text;
    
  public ParseText(String text){
    this.text = text;
  }

  public byte getVersion() { return VERSION; }

  public void readFields(DataInput in) throws IOException {
    super.readFields(in);                         // check version
    text = WritableUtils.readCompressedString(in);
    return;
  }

  public final void write(DataOutput out) throws IOException {
    super.write(out);                             // write version
    WritableUtils.writeCompressedString(out, text);
    return;
  }

  public final static ParseText read(DataInput in) throws IOException {
    ParseText parseText = new ParseText();
    parseText.readFields(in);
    return parseText;
  }

  //
  // Accessor methods
  //
  public String getText()  { return text; }

  public boolean equals(Object o) {
    if (!(o instanceof ParseText))
      return false;
    ParseText other = (ParseText)o;
    return this.text.equals(other.text);
  }

  public String toString() {
    return text;
  }

  public static void main(String argv[]) throws Exception {
    String usage = "ParseText (-local | -ndfs <namenode:port>) recno segment";

    if (argv.length < 3) {
      System.out.println("usage:" + usage);
      return;
    }

    NutchFileSystem nfs = NutchFileSystem.parseArgs(argv, 0);
    try {
      int recno = Integer.parseInt(argv[0]);
      String segment = argv[1];
      String filename = new File(segment, ParseText.DIR_NAME).getPath();

      ParseText parseText = new ParseText();
      ArrayFile.Reader parseTexts = new ArrayFile.Reader(nfs, filename);

      parseTexts.get(recno, parseText);
      System.out.println("Retrieved " + recno + " from file " + filename);
      System.out.println(parseText);
      parseTexts.close();
    } finally {
      nfs.close();
    }
  }
}
