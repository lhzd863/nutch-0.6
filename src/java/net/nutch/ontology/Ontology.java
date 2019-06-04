/* Copyright (c) 2004 michael j pan.  All rights reserved.   */
/* Use subject to the same conditions as Nutch. */
/* see http://www.nutch.org/LICENSE.txt. */

package net.nutch.ontology;

import java.util.Iterator;

public interface Ontology {
  /** The name of the extension point. */
  public final static String X_POINT_ID = Ontology.class.getName();

  public void load(String[] urls);

  // not yet implemented
  //public void merge(Ontology o);

  public Iterator subclasses(String entitySearchTerm);

  public Iterator synonyms(String queryKeyPhrase);
}
