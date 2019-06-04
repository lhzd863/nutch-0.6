/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;

import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.FieldCache;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import net.nutch.io.*;
import net.nutch.util.*;
import net.nutch.db.*;
import net.nutch.fetcher.*;
import net.nutch.linkdb.*;
import net.nutch.indexer.*;
import net.nutch.analysis.NutchDocumentAnalyzer;

/** Implements {@link Searcher} and {@link HitDetailer} for either a single
 * merged index, or for a set of individual segment indexes. */
public class IndexSearcher implements Searcher, HitDetailer {

  private org.apache.lucene.search.Searcher luceneSearcher;

  private String[] sites;
  
  private LuceneQueryOptimizer optimizer = new LuceneQueryOptimizer
    (NutchConf.getInt("searcher.filter.cache.size", 16),
     NutchConf.getFloat("searcher.filter.cache.threshold", 0.05f));

  /** Construct given a number of indexed segments. */
  public IndexSearcher(File[] segmentDirs) throws IOException {
    IndexReader[] readers = new IndexReader[segmentDirs.length];
    for (int i = 0; i < segmentDirs.length; i++) {
      readers[i] = IndexReader.open(new File(segmentDirs[i], "index"));
    }
    init(new MultiReader(readers));
  }

  /** Construct given a directory containing fetched segments, and a separate
   * directory naming their merged index. */
  public IndexSearcher(String index)
    throws IOException {
    init(IndexReader.open(index));
  }

  private void init(IndexReader reader) throws IOException {
    this.sites = FieldCache.DEFAULT.getStrings(reader, "site");
    this.luceneSearcher = new org.apache.lucene.search.IndexSearcher(reader);
    this.luceneSearcher.setSimilarity(new NutchSimilarity());
  }

  public Hits search(Query query, int numHits) throws IOException {

    org.apache.lucene.search.BooleanQuery luceneQuery =
      QueryFilters.filter(query);
    
    return translateHits
      (optimizer.optimize(luceneQuery, luceneSearcher, numHits));
  }

  public String getExplanation(Query query, Hit hit) throws IOException {
    return luceneSearcher.explain(QueryFilters.filter(query),
                                  hit.getIndexDocNo()).toHtml();
  }

  public HitDetails getDetails(Hit hit) throws IOException {
    ArrayList fields = new ArrayList();
    ArrayList values = new ArrayList();

    Document doc = luceneSearcher.doc(hit.getIndexDocNo());

    Enumeration e = doc.fields();
    while (e.hasMoreElements()) {
      Field field = (Field)e.nextElement();
      fields.add(field.name());
      values.add(field.stringValue());
    }

    return new HitDetails((String[])fields.toArray(new String[fields.size()]),
                          (String[])values.toArray(new String[values.size()]));
  }

  public HitDetails[] getDetails(Hit[] hits) throws IOException {
    HitDetails[] results = new HitDetails[hits.length];
    for (int i = 0; i < hits.length; i++)
      results[i] = getDetails(hits[i]);
    return results;
  }

  private Hits translateHits(TopDocs topDocs) throws IOException {
    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
    int length = scoreDocs.length;
    Hit[] hits = new Hit[length];
    for (int i = 0; i < length; i++) {
      int doc = scoreDocs[i].doc;
      hits[i] = new Hit(doc, scoreDocs[i].score, sites[doc]);
    }
    return new Hits(topDocs.totalHits, hits);
  }

}
