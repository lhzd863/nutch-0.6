/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher.basic;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;

import net.nutch.analysis.NutchDocumentAnalyzer;
import net.nutch.analysis.CommonGrams;

import net.nutch.searcher.QueryFilter;
import net.nutch.searcher.Query;
import net.nutch.searcher.Query.*;

import java.io.IOException;
import java.util.HashSet;

/** The default query filter.  Query terms in the default query field are
 * expanded to search the url, anchor and content document fields.*/
public class BasicQueryFilter implements QueryFilter {

  private static float URL_BOOST = 4.0f;
  private static float ANCHOR_BOOST = 2.0f;

  private static int SLOP = Integer.MAX_VALUE;
  private static float PHRASE_BOOST = 1.0f;

  private static final String[] FIELDS = {"url", "anchor", "content"};
  private static final float[] FIELD_BOOSTS = {URL_BOOST, ANCHOR_BOOST, 1.0f};

  /** Set the boost factor for url matches, relative to content and anchor
   * matches */
  public static void setUrlBoost(float boost) { URL_BOOST = boost; }

  /** Set the boost factor for title/anchor matches, relative to url and
   * content matches. */
  public static void setAnchorBoost(float boost) { ANCHOR_BOOST = boost; }

  /** Set the boost factor for sloppy phrase matches relative to unordered term
   * matches. */
  public static void setPhraseBoost(float boost) { PHRASE_BOOST = boost; }

  /** Set the maximum number of terms permitted between matching terms in a
   * sloppy phrase match. */
  public static void setSlop(int slop) { SLOP = slop; }

  public BooleanQuery filter(Query input, BooleanQuery output) {
    addTerms(input, output);
    addSloppyPhrases(input, output);
    return output;
  }

  private static void addTerms(Query input, BooleanQuery output) {
    Clause[] clauses = input.getClauses();
    for (int i = 0; i < clauses.length; i++) {
      Clause c = clauses[i];

      if (!c.getField().equals(Clause.DEFAULT_FIELD))
        continue;                                 // skip non-default fields

      BooleanQuery out = new BooleanQuery();
      for (int f = 0; f < FIELDS.length; f++) {

        Clause o = c;
        if (c.isPhrase()) {                         // optimize phrase clauses
          String[] opt = CommonGrams.optimizePhrase(c.getPhrase(), FIELDS[f]);
          if (opt.length==1) {
            o = new Clause(new Term(opt[0]), c.isRequired(), c.isProhibited());
          } else {
            o = new Clause(new Phrase(opt), c.isRequired(), c.isProhibited());
          }
        }

        out.add(o.isPhrase()
                ? exactPhrase(o.getPhrase(), FIELDS[f], FIELD_BOOSTS[f])
                : termQuery(FIELDS[f], o.getTerm(), FIELD_BOOSTS[f]),
                false, false);
      }
      output.add(out, c.isRequired(), c.isProhibited());
    }
  }

  private static void addSloppyPhrases(Query input, BooleanQuery output) {
    Clause[] clauses = input.getClauses();
    for (int f = 0; f < FIELDS.length; f++) {

      PhraseQuery sloppyPhrase = new PhraseQuery();
      sloppyPhrase.setBoost(FIELD_BOOSTS[f] * PHRASE_BOOST);
      sloppyPhrase.setSlop("anchor".equals(FIELDS[f])
                           ? NutchDocumentAnalyzer.INTER_ANCHOR_GAP
                           : SLOP);
      int sloppyTerms = 0;

      for (int i = 0; i < clauses.length; i++) {
        Clause c = clauses[i];
        
        if (!c.getField().equals(Clause.DEFAULT_FIELD))
          continue;                               // skip non-default fields
        
        if (c.isPhrase())                         // skip exact phrases
          continue;

        if (c.isProhibited())                     // skip prohibited terms
          continue;
        
        sloppyPhrase.add(luceneTerm(FIELDS[f], c.getTerm()));
        sloppyTerms++;
      }

      if (sloppyTerms > 1)
        output.add(sloppyPhrase, false, false);
    }
  }


  private static org.apache.lucene.search.Query
        termQuery(String field, Term term, float boost) {
    TermQuery result = new TermQuery(luceneTerm(field, term));
    result.setBoost(boost);
    return result;
  }

  /** Utility to construct a Lucene exact phrase query for a Nutch phrase. */
  private static org.apache.lucene.search.Query
       exactPhrase(Phrase nutchPhrase,
                   String field, float boost) {
    Term[] terms = nutchPhrase.getTerms();
    PhraseQuery exactPhrase = new PhraseQuery();
    for (int i = 0; i < terms.length; i++) {
      exactPhrase.add(luceneTerm(field, terms[i]));
    }
    exactPhrase.setBoost(boost);
    return exactPhrase;
  }

  /** Utility to construct a Lucene Term given a Nutch query term and field. */
  private static org.apache.lucene.index.Term luceneTerm(String field,
                                                         Term term) {
    return new org.apache.lucene.index.Term(field, term.toString());
  }
}
