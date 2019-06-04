/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer.basic;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import net.nutch.parse.Parse;

import net.nutch.indexer.IndexingFilter;
import net.nutch.indexer.IndexingException;

import net.nutch.fetcher.FetcherOutput;
import net.nutch.pagedb.FetchListEntry;

import java.util.logging.Logger;
import net.nutch.util.LogFormatter;
import net.nutch.util.NutchConf;

/** Adds basic searchable fields to a document. */
public class BasicIndexingFilter implements IndexingFilter {
  public static final Logger LOG
    = LogFormatter.getLogger(BasicIndexingFilter.class.getName());

  private static final int MAX_TITLE_LENGTH =
    NutchConf.getInt("indexer.max.title.length", 100);

  public Document filter(Document doc, Parse parse, FetcherOutput fo)
    throws IndexingException {
    
    String url = fo.getUrl().toString();

    // url is both stored and indexed, so it's both searchable and returned
    doc.add(Field.Text("url", url));
    
    // content is indexed, so that it's searchable, but not stored in index
    doc.add(Field.UnStored("content", parse.getText()));
    
    // anchors are indexed, so they're searchable, but not stored in index
    String[] anchors = fo.getAnchors();
    for (int i = 0; i < anchors.length; i++) {
      doc.add(Field.UnStored("anchor", anchors[i]));
    }

    // title
    String title = parse.getData().getTitle();
    if (title.length() > MAX_TITLE_LENGTH) {      // truncate title if needed
      title = title.substring(0, MAX_TITLE_LENGTH);
    }
    // add title as anchor so it is searchable.  doesn't warrant its own field.
    doc.add(Field.UnStored("anchor", title));
    // add title unindexed, so that it can be displayed
    doc.add(Field.UnIndexed("title", title));

    return doc;
  }

}
