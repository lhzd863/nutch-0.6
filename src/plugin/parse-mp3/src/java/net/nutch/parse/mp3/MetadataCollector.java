package net.nutch.parse.mp3;

import net.nutch.parse.Outlink;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * This class allows meta data to be collected and manipulated
 * @author Andy Hedges 
 */
public class MetadataCollector {

  private Properties metadata = new Properties();
  private String title = null;
  private String artist = null;
  private String album = null;
  private ArrayList links = new ArrayList();
  private String text = "";

  public void notifyProperty(String name, String value) throws MalformedURLException {
    if (name.equals("TIT2-Text"))
      setTitle(value);
    if (name.equals("TALB-Text"))
      setAlbum(value);
    if (name.equals("TPE1-Text"))
      setArtist(value);

    if (name.indexOf("URL Link") > -1) {
      links.add(new Outlink(value, ""));
    } else if (name.indexOf("Text") > -1) {
      text += value + "\n";
    }

    metadata.setProperty(name, value);
  }

  public void putAll(Properties properties) {
    metadata.putAll(properties);
  }

  public Properties getData() {
    return metadata;
  }

  public Outlink[] getOutlinks() {
    return (Outlink[]) links.toArray(new Outlink[links.size()]);
  }

  public String getTitle() {
    String text = "";
    if (title != null) {
      text = title;
    }
    if (album != null) {
      if (!text.equals("")) {
        text += " - " + album;
      } else {
        text = title;
      }
    }
    if (artist != null) {
      if (!text.equals("")) {
        text += " - " + artist;
      } else {
        text = artist;
      }
    }
    return text;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public void setAlbum(String album) {
    this.album = album;
  }

  public String getText() {
    return text;
  }

}