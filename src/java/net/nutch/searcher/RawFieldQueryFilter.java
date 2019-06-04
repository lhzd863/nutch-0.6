/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.Term;

import net.nutch.searcher.Query.Clause;

/** Translate raw query fields to search the same-named field, as indexed by an
 * IndexingFilter. */
public abstract class RawFieldQueryFilter implements QueryFilter {
  private String field;
  private boolean lowerCase;
  private float boost;

  /** Construct for the named field, lowercasing query values.*/
  protected RawFieldQueryFilter(String field) {
    this(field, true);
  }

  /** Construct for the named field, lowercasing query values.*/
  protected RawFieldQueryFilter(String field, float boost) {
    this(field, true, boost);
  }

  /** Construct for the named field, potentially lowercasing query values.*/
  protected RawFieldQueryFilter(String field, boolean lowerCase) {
    this(field, lowerCase, 0.0f);
  }

  /** Construct for the named field, potentially lowercasing query values.*/
  protected RawFieldQueryFilter(String field, boolean lowerCase, float boost) {
    this.field = field;
    this.lowerCase = lowerCase;
    this.boost = boost;
  }

  public BooleanQuery filter(Query input, BooleanQuery output)
    throws QueryException {
    
    // examine each clause in the Nutch query
    Clause[] clauses = input.getClauses();
    for (int i = 0; i < clauses.length; i++) {
      Clause c = clauses[i];

      // skip non-matching clauses
      if (!c.getField().equals(field))
        continue;

      // get the field value from the clause
      // raw fields are guaranteed to be Terms, not Phrases
      String value = c.getTerm().toString();
      if (lowerCase)
        value = value.toLowerCase();

      // add a Lucene TermQuery for this clause
      TermQuery clause = new TermQuery(new Term(field, value));
      // set boost
      clause.setBoost(boost);
      // add it as specified in query
      output.add(clause, c.isRequired(), c.isProhibited());
    }
    
    // return the modified Lucene query
    return output;
  }
}
