/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse.mp3;


import net.nutch.parse.*;
import net.nutch.protocol.Content;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.object.AbstractMP3Object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

/**
 * A parser for MP3 audio files
 * @author Andy Hedges
 */

public class MP3Parser implements Parser {

  private MetadataCollector metadataCollector = new MetadataCollector();

  public Parse getParse(Content content) throws ParseException {
    Parse parse = null;
    metadataCollector.putAll(content.getMetadata());

    byte[] raw = content.getContent();

    File tmp = null;
    try {
      tmp = File.createTempFile("nutch", ".mp3");
      FileOutputStream fos = new FileOutputStream(tmp);
      fos.write(raw);
      fos.close();
      MP3File mp3 = new MP3File(tmp);

      if (mp3.hasID3v2Tag()) {
        parse = getID3v2Parse(mp3);
      } else if (mp3.hasID3v1Tag()) {
        parse = getID3v1Parse(mp3);
      } else {
        throw new ParseException("No textual content available");
      }


    } catch (IOException e) {
      throw new ParseException("Couldn't create temporary file", e);
    } catch (TagException e) {
      throw new ParseException("ID3 Tags could not be parsed", e);
    } finally{
      tmp.delete();
    }
    return parse;
  }

  private Parse getID3v1Parse(MP3File mp3) throws MalformedURLException {
    ID3v1 tag = mp3.getID3v1Tag();
    metadataCollector.notifyProperty("TALB-Text", tag.getAlbum());
    metadataCollector.notifyProperty("TPE1-Text", tag.getArtist());
    metadataCollector.notifyProperty("COMM-Text", tag.getComment());
    metadataCollector.notifyProperty("TCON-Text", "(" + tag.getGenre() + ")");
    metadataCollector.notifyProperty("TIT2-Text", tag.getTitle());
    metadataCollector.notifyProperty("TYER-Text", tag.getYear());
    ParseData parseData = new ParseData(metadataCollector.getTitle(),
        metadataCollector.getOutlinks(),
        metadataCollector.getData());
    return new ParseImpl(metadataCollector.getText(), parseData);
  }

  public Parse getID3v2Parse(MP3File mp3) throws IOException {
    AbstractID3v2 tag = mp3.getID3v2Tag();
    Iterator it = tag.iterator();
    while (it.hasNext()) {
      AbstractID3v2Frame frame = (AbstractID3v2Frame) it.next();
      String name = frame.getIdentifier().trim();
      if (!name.equals("APIC")) {
        Iterator itBody = frame.getBody().iterator();
        while (itBody.hasNext()) {
          AbstractMP3Object mp3Obj = (AbstractMP3Object) itBody.next();
          String bodyName = mp3Obj.getIdentifier();
          if (!bodyName.equals("Picture data")) {
            String bodyValue = mp3Obj.getValue().toString();
            metadataCollector.notifyProperty(name + "-" + bodyName, bodyValue);
          }
        }
      }
    }
    ParseData parseData = new ParseData(metadataCollector.getTitle(),
        metadataCollector.getOutlinks(),
        metadataCollector.getData());
    return new ParseImpl(metadataCollector.getText(), parseData);
  }


}
